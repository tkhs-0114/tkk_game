# 自作駒管理機能実装計画

## 計画作成日
2026年01月10日

## 概要
自作駒の一覧表示・削除・編集機能を実装する。

## 要件
- home画面から自作駒一覧へのリンク
- 自作駒一覧（削除機能付き）
- 駒作成（既存の/deck/make/komaを流用）
- 駒編集画面の新規作成
- デッキ作成画面で自分が作成した駒のみ表示

## URL階層
```
home
└── 自作駒一覧 (/koma/list)
    ├── 駒作成 (/koma/make)
    └── 駒編集 (/koma/edit/{id})
```

---

## タスク一覧

### タスク1: スキーマ変更（PlayerKomaテーブル追加）
**関連ファイル:**
- `tkk_game/src/main/resources/schema.sql`
- `tkk_game/src/main/resources/data.sql`

**作業内容:**
- PlayerKomaテーブルの作成
- 既存駒の初期所有権設定

### タスク2: PlayerKomaモデル作成
**関連ファイル:**
- `tkk_game/src/main/java/team3/tkk_game/model/PlayerKoma.java` (新規)

**作業内容:**
- PlayerDeckを参考にしたモデルクラスの作成

### タスク3: PlayerKomaMapper作成
**関連ファイル:**
- `tkk_game/src/main/java/team3/tkk_game/mapper/PlayerKomaMapper.java` (新規)

**作業内容:**
- 駒とプレイヤーの紐づけCRUD操作

### タスク4: KomaMapper拡張
**関連ファイル:**
- `tkk_game/src/main/java/team3/tkk_game/mapper/KomaMapper.java`

**作業内容:**
- プレイヤー別駒取得メソッド追加
- 駒削除・更新メソッド追加

### タスク5: KomaController作成
**関連ファイル:**
- `tkk_game/src/main/java/team3/tkk_game/controller/KomaController.java` (新規)

**作業内容:**
- 駒一覧表示 (/koma/list)
- 駒作成 (/koma/make) - DeckControllerから移動
- 駒作成保存 (/koma/make/save)
- 駒編集画面 (/koma/edit/{id})
- 駒更新 (/koma/update/{id})
- 駒削除 (/koma/delete/{id})

### タスク6: View作成
**関連ファイル:**
- `tkk_game/src/main/resources/templates/komalist.html` (新規)
- `tkk_game/src/main/resources/templates/komaedit.html` (新規)
- `tkk_game/src/main/resources/static/css/komalist.css` (新規)
- `tkk_game/src/main/resources/static/css/komaedit.css` (新規)

**作業内容:**
- deckselect.htmlを参考にした一覧画面
- komamake.htmlを参考にした編集画面
- スタイルの作成

### タスク7: home.html変更
**関連ファイル:**
- `tkk_game/src/main/resources/templates/home.html`

**作業内容:**
- 自作駒一覧へのリンク追加

### タスク8: DeckController変更
**関連ファイル:**
- `tkk_game/src/main/java/team3/tkk_game/controller/DeckController.java`

**作業内容:**
- デッキ作成画面で自分の駒のみ表示するよう変更
- 駒作成関連エンドポイントをKomaControllerへ移動後、リダイレクト設定

### タスク9: komamake.html変更
**関連ファイル:**
- `tkk_game/src/main/resources/templates/komamake.html`

**作業内容:**
- リダイレクト先の変更

---

## DoD (Definition of Done)
1. `gradle bootRun` でアプリケーションを起動
2. ブラウザで `localhost:8080/` にアクセスしてログイン
3. ホーム画面に「自作駒一覧」リンクが表示される
4. 自作駒一覧画面で自分が作成した駒のみ表示される
5. 駒の削除ができる（確認ダイアログ付き）
6. 駒作成から新規駒を作成し、一覧に表示される
7. 駒編集画面で既存の駒を編集できる
8. デッキ作成画面で自分が作成した駒のみ選択可能
