package com.narkolep.skkimmer.keyboard

import android.R.color.black
import android.content.ClipboardManager
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.narkolep.skkimmer.data.DictionaryManager
import com.narkolep.skkimmer.data.EmojiManager
import com.narkolep.skkimmer.keyboard.ui.KeyboardLayout
import com.narkolep.skkimmer.keyboard.ui.theme.KeyboardTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue

class KeyboardService : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    override val viewModelStore: ViewModelStore get() = store
    override val lifecycle: Lifecycle
        field = LifecycleRegistry(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val store = ViewModelStore()
    private val stateFlow = MutableStateFlow(KeyboardState())
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private var emojiCategories: List<EmojiManager.Category> = emptyList()
    private var currentEditorInfo: EditorInfo? = null
    private val clipboardManager by lazy {
        getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    }
    private lateinit var dictionaryManager: DictionaryManager
    private lateinit var outputManager: OutputManager
    private lateinit var keyProcessor: KeyProcessor
    private lateinit var actionProcessor: ActionProcessor

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        dictionaryManager = DictionaryManager(this)

        outputManager = OutputManager(
            stateFlow = stateFlow,
            inputCommitter = InputCommitter { currentInputConnection },
            dictionaryManager
        )

        lifecycleScope.launch {
            val parsedList = withContext(Dispatchers.IO) {
                val jsonString = assets.open("all-emoji.json").bufferedReader().use { it.readText() }
                EmojiManager(this@KeyboardService).loadEmojis(jsonString)
            }
            emojiCategories = parsedList

            stateFlow
                .map { it.toBufferSnapshot() }
                .distinctUntilChanged()
                .collect {
                    /* bufferにあたるフィールドが変化したときのみ表示を更新 */
                    outputManager.update()
                }
        }
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this)

        val win = window?.window
        if (win != null) {
            WindowCompat.setDecorFitsSystemWindows(win, false)
            win.isNavigationBarContrastEnforced = false
            win.navigationBarColor = color@black
        }

        val decorView = win?.decorView
        if (decorView != null) {
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }

        composeView.apply {
            setViewTreeLifecycleOwner(this@KeyboardService)
            setViewTreeViewModelStoreOwner(this@KeyboardService)
            setViewTreeSavedStateRegistryOwner(this@KeyboardService)

            setContent {
                val uiState by stateFlow.collectAsState()

                KeyboardTheme {
                    KeyboardLayout(
                        uiState = uiState,
                        categories = emojiCategories,
                        onKeyClick = { keyId -> keyProcessor.handle(keyId) },
                        onActionClick = { action -> actionProcessor.handle(action) }
                    )
                }
            }
        }

        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        return composeView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        /* 入力欄が切り替わるたびにキーボードタイプを決定する */
        val autoType = determineInputMode(info)

        stateFlow.update {
            it.copy(
                skkState = SkkState.NORMAL,
                keyboardType = autoType ?: KeyboardType.NORMAL,
                inputMode = if (autoType == null) InputMode.HIRAGANA else InputMode.HALF_ASCII,
                composingText = ""
            )
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        currentEditorInfo = attribute

        keyProcessor = KeyProcessor(
            stateFlow = stateFlow,
            keyboardService = this,
            dictionaryManager = dictionaryManager,
            connectionProvider = { currentInputConnection }
        )

        actionProcessor = ActionProcessor(
            stateFlow = stateFlow,
            outputManager = outputManager,
            editorInfo = currentEditorInfo,
            inputCommitter = InputCommitter { currentInputConnection },
            keyProcessor = keyProcessor,
            dictionaryManager = dictionaryManager
        )
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)

        stateFlow.update { it.tourokuClear() }
        stateFlow.update { it.clear() }
    }

    override fun onDestroy() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStore.clear()
        super.onDestroy()
    }

    /**
     * EditorInfoから適切なInputMode,KeyboardTypeを決定する関数
     */
    private fun determineInputMode(editorInfo: EditorInfo?): KeyboardType? {
        if (editorInfo == null) return null

        val inputType = editorInfo.inputType
        val classType = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION

        return when (classType) {
            /* 数字のみの入力欄 */
            InputType.TYPE_CLASS_NUMBER,
            InputType.TYPE_CLASS_PHONE,
            InputType.TYPE_CLASS_DATETIME -> {
                KeyboardType.NUMERIC
            }

            /* テキストの入力欄 */
            InputType.TYPE_CLASS_TEXT -> {
                when (variation) {
                    InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                    InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS -> {
                        KeyboardType.NORMAL
                    }
                    else -> null
                }
            }

            else -> null
        }
    }

    // bufferに該当するフィールドだけを抜き出した比較用スナップショット
    private data class BufferSnapshot(
        val candidates: List<String>,
        val selectedIndex: Int,
        val composingText: String,
        val midashiText: String,
        val okuriganaText: String,
        val okuriganaTrigger: String,
        val tourokuFlag: String,
        val oldMidashiText: String,
        val oldOkuriganaText: String,
        val oldOkuriganaTrigger: String,
    )

    private fun KeyboardState.toBufferSnapshot() = BufferSnapshot(
        candidates = candidates,
        selectedIndex = selectedIndex,
        composingText = composingText,
        midashiText = midashiText,
        okuriganaText = okuriganaText,
        okuriganaTrigger = okuriganaTrigger,
        tourokuFlag = tourokuFlag,
        oldMidashiText = oldMidashiText,
        oldOkuriganaText = oldOkuriganaText,
        oldOkuriganaTrigger = oldOkuriganaTrigger,
    )

    fun getClipboardText(): String? {
        if (!clipboardManager.hasPrimaryClip()) {
            return null
        }

        return clipboardManager.primaryClip
            ?.getItemAt(0)
            ?.coerceToText(this)
            ?.toString()
    }
}