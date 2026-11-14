## 概要
趣味で作成したSKK風のキーボードアプリです。

(四角い)車輪の再発明。

## 使用したサードパーティ製の素材
- アイコン : [composablehorizons/compose-icons](https://github.com/composablehorizons/compose-icons) (Licensed under MIT License)
- 絵文字のリスト : [Fantantonio/Emoji-List-Unicode](https://github.com/Fantantonio/Emoji-List-Unicode)
- - -

## ショートカットキー一覧
### CTRL 押下時
- k : キーボードの切り替え
- j : 日本語モード
- e : 絵文字入力画面
- n : テンキー
- a : Select All
- x : Cut
- c : Copy
- v : Paste

### 日本語モード中
- q : カタカナ ⇔ ひらがな
- CTRL + q : 半角カタカナ ⇔ ひらがな
- Shift + q : 見出しモード
- l : 半角英数
- L : 全角英数
- / : abbreviationモード

### 変換中
- Space : 次の変換候補
- x : 前の変換候補
- - -

## EmacsLispや数値変換への対応
[emasaka/mlisj](https://github.com/emasaka/mlisj) を参考に、
SKK-JISYO.LとSKK-JISYO.lispで使われている最低限のLisp式に対応

## 辞書
設定画面から、SKKの辞書ファイルを読み込んで使用できます。

エンコード : EUC-JPもしくはUTF-8 (それ以外だった場合はエラーになります。)
- - -
## 今後、実装したい機能
#### 日本語入力中
- X : エントリの削除
#### abbrev
- CTRL + q : 全角英数に変換
#### その他、全体
- 見出し語の補完
- 閉じ括弧の自動入力
- ユーザー辞書のエクスポート(.txt)
- 絵文字の使用履歴
- 絵文字検索
