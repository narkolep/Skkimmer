package com.narkolep.skkimmer.keyboard

import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
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
import com.narkolep.skkimmer.data.EmojiManager
import com.narkolep.skkimmer.data.SkkDictionaryManager
import com.narkolep.skkimmer.keyboard.ui.KeyboardLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SkkService : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val uiStateFlow = MutableStateFlow(SkkUIState())
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store
    private var emojiCategories: List<EmojiManager.Category> = emptyList()
    private lateinit var dictionaryManager: SkkDictionaryManager
    private lateinit var composingManager: ComposingManager
    private lateinit var keyProcessor: KeyProcessor
    private lateinit var actionProcessor: ActionProcessor
    private var currentEditorInfo: EditorInfo? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        dictionaryManager = SkkDictionaryManager(this)

        composingManager = ComposingManager(
            stateFlow = uiStateFlow,
            inputCommitter = InputCommitter {
                currentInputConnection
            },
            dictionaryManager
        )

        keyProcessor = KeyProcessor(
            stateFlow = uiStateFlow,
            dictionaryManager = dictionaryManager,
            connectionProvider = {
                currentInputConnection
            }
        )

        actionProcessor = ActionProcessor(
            stateFlow = uiStateFlow,
            composingManager = composingManager,
            editorInfo = currentEditorInfo,
            inputCommitter = InputCommitter {
                currentInputConnection
            },
            keyProcessor = keyProcessor,
            dictionaryManager = dictionaryManager
        )

        lifecycleScope.launch {
            val parsedList = withContext(Dispatchers.IO) {
                val jsonString =
                    assets.open("all-emoji.json").bufferedReader().use { it.readText() }
                EmojiManager(this@SkkService).loadEmojis(jsonString)
            }
            emojiCategories = parsedList

            uiStateFlow.collect {
                composingManager.update()
            }
        }
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this)

        val decorView = window?.window?.decorView
        if (decorView != null) {
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }

        composeView.apply {
            setViewTreeLifecycleOwner(this@SkkService)
            setViewTreeViewModelStoreOwner(this@SkkService)
            setViewTreeSavedStateRegistryOwner(this@SkkService)

            setContent {
                val uiState by uiStateFlow.collectAsState()

                KeyboardLayout(
                    uiState = uiState,
                    categories = emojiCategories,
                    onKeyClick = { keyId -> keyProcessor.handle(keyId) },
                    onActionClick = { action -> actionProcessor.handle(action) }
                )
            }
        }

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        return composeView
    }


    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        currentEditorInfo = attribute
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)

        // 自動判定したモードを取得
        val autoMode = determineInputMode(editorInfo)

        // 入力モードを更新
        uiStateFlow.update { current ->
            current.copy(
                inputMode = autoMode,
                skkState = SkkState.NORMAL,
                composingText = "",
                isFlick = (autoMode == InputMode.HIRAGANA)
            )
        }
    }

    /**
     * EditorInfoから適切なInputModeを決定する関数
     */
    private fun determineInputMode(editorInfo: EditorInfo?): InputMode {
        if (editorInfo == null) return InputMode.HIRAGANA

        val inputType = editorInfo.inputType
        val classType = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION

        return when (classType) {
            // --- 数字のみの入力欄（テンキーを出すべき状態） ---
            InputType.TYPE_CLASS_NUMBER,
            InputType.TYPE_CLASS_PHONE,
            InputType.TYPE_CLASS_DATETIME -> {
                InputMode.NUMERIC // テンキーモード
            }

            // --- テキストの入力欄 ---
            InputType.TYPE_CLASS_TEXT -> {
                when (variation) {
                    // パスワード、メールアドレス、URLなどは最初から半角英数にする
                    InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                    InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
                    InputType.TYPE_TEXT_VARIATION_URI -> {
                        InputMode.HALF_ASCII
                    }
                    // それ以外の通常のテキスト欄はひらがな（日本語入力）
                    else -> {
                        InputMode.HIRAGANA
                    }
                }
            }

            // --- （デフォルト） ---
            else -> InputMode.HIRAGANA
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)

        composingManager.commit()
        uiStateFlow.update {
            it.copy(
                tourokuText = "",
                tourokuFlag = ""
            )
        }
        uiStateFlow.update { it.clear() }
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStore.clear()
        super.onDestroy()
    }
}