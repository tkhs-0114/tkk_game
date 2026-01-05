# コスト機能拡張実装計画

## 計画作成日
2026年01月05日（2026年01月05日更新）

## 目的
コストシステムを拡張し、以下の機能を追加する：
1. デッキ作成画面のSelectで駒のコストを一覧表示
2. デッキにコストを保存し、コスト上限でバリデーション（上限は定数）
3. 駒作成画面で選択中のルールの合計コストをリアルタイム表示

## 背景・要件

### 基本要件
1. **セレクトボックスのコスト表示**: 「コスト: 駒名」形式で表示（例：「1: 歩兵」「8: 王将」）
2. **デッキコスト管理**:
   - データベースに `cost` カラムを追加（デッキの合計コストを保存）
   - コスト上限は `DeckController` に定数としてハードコーディング（例：50）
   - デッキ保存時にSFENから駒のコストを計算し、DB に保存
   - 上限超過時にフロントエンドで警告表示
   - サーバー側でもバリデーション実施
3. **駒作成画面のコスト表示**: チェックボックスで選択中の移動ルールの合計コストをリアルタイム表示

### 実装方針
- データベーススキーマを変更（`Deck` テーブルに `cost` カラム追加）
- SFEN から駒IDを抽出し、コストを計算する機能を実装
- コスト上限は `DeckController` に `private static final int COST_LIMIT = 50;` として定義
- フラッシュメッセージでエラー・成功通知
- JavaScript でリアルタイムバリデーション

## スコープ（含む）
- データベーススキーマの変更（`cost` カラム追加）
- `Deck` モデルクラスの修正（`cost` フィールド追加）
- `DeckMapper` の SQL 修正
- デッキ保存時のコスト計算とバリデーション
- デッキ作成画面の UI 改善（警告表示）
- 駒作成画面のコスト表示機能

## スコープ（含まない）
- コスト上限の動的変更機能
- コスト上限値の管理画面
- ユーザーごとのコスト上限設定
- 既存デッキのコスト一括計算・更新

---

## タスク一覧

### タスク1: schema.sql に cost カラムを追加

**優先度**: 1（最優先）

**関連ファイル**:
- 修正: `tkk_game/src/main/resources/schema.sql`
- 修正: `tkk_game/src/main/resources/data.sql`

**作業内容**:
1. `schema.sql` の `Deck` テーブル定義に `cost INT DEFAULT 0` カラムを追加
2. `data.sql` の既存サンプルデッキに `cost` 値を追加（計算済みコスト値を設定）

**動作確認手順**:
- アプリケーションを起動し、H2コンソールでテーブル構造を確認
- `SELECT * FROM Deck` でサンプルデッキに `cost` が設定されていることを確認

**入力**: なし（スキーマ定義）
**出力**: `cost` カラムを持つ Deck テーブル

---

### タスク2: Deck クラスに cost フィールドを追加

**優先度**: 2

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/model/Deck.java`

**作業内容**:
1. `Integer cost` フィールドを追加
2. `getCost()` getter メソッドを追加
3. `setCost(Integer cost)` setter メソッドを追加

**動作確認手順**:
- アプリケーションが正常にコンパイルできることを確認
- `gradle build` コマンドが成功することを確認

**入力**: なし（クラス定義の修正）
**出力**: `cost` フィールドを持つ Deck クラス

---

### タスク3: DeckMapper に cost カラムを追加

**優先度**: 3

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/mapper/DeckMapper.java`

**作業内容**:
1. `insertDeck` メソッドの SQL に `cost` カラムを追加
   - `INSERT INTO Deck(name, sfen, cost) VALUES(#{name}, #{sfen}, #{cost})`
2. `selectAllDecks` メソッドの SQL に `cost` を追加
3. `selectDeckById` メソッドの SQL に `cost` を追加

**動作確認手順**:
- アプリケーションが正常にコンパイルできることを確認
- `gradle build` コマンドが成功することを確認

**入力**: なし（マッパーインターフェースの修正）
**出力**: `cost` を扱える DeckMapper

---

### タスク4: DeckController にコスト計算とバリデーション機能を追加

**優先度**: 4

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/controller/DeckController.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/mapper/KomaMapper.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/Koma/KomaDB.java`

**作業内容**:
1. クラス定数を追加: `private static final int COST_LIMIT = 50;`
2. `import java.util.regex.Pattern;` と `import java.util.regex.Matcher;` を追加
3. `import java.util.ArrayList;` を追加
4. `import org.springframework.web.servlet.mvc.support.RedirectAttributes;` を追加
5. `saveDeck()` メソッドに `RedirectAttributes redirectAttributes` パラメータを追加
6. SFEN から駒IDリストを抽出するヘルパーメソッド `extractKomaIdsFromSfen(String sfen)` を追加
7. `saveDeck()` メソッド内でデッキの合計コストを計算するロジックを追加
8. コスト上限チェックを追加（`COST_LIMIT` 超過時はエラーメッセージを設定してリダイレクト）
9. `deck.setCost(totalCost)` を追加（計算したコストをデッキに設定）
10. `deckmake()` メソッドで `COST_LIMIT` をモデルに追加: `model.addAttribute("costLimit", COST_LIMIT);`

**動作確認手順**:
- アプリケーションが正常にコンパイルできることを確認
- `gradle build` コマンドが成功することを確認

**入力**: デッキ名、SFEN
**出力**: 計算されたコスト、バリデーション結果（成功 or エラー）

---

### タスク5: deckmake.html にコスト上限機能を追加

**優先度**: 5

**関連ファイル**:
- 修正: `tkk_game/src/main/resources/templates/deckmake.html`

**作業内容**:
1. セレクトボックスの `th:text` 属性を変更してコスト表示
   - `th:text="${komaCosts[k.id]} + ': ' + ${k.name}"`
2. コスト上限の表示エリアを追加
   - `<div>コスト上限: <span id="cost-limit-display" th:text="${costLimit}">50</span></div>`
3. コスト上限超過警告の表示エリアを追加
   - `<div id="cost-warning" style="display:none; color:red;"></div>`
4. エラー・成功メッセージの表示エリアを追加（Thymeleaf）
   - `<div th:if="${error}" th:text="${error}" style="color:red;"></div>`
   - `<div th:if="${success}" th:text="${success}" style="color:green;"></div>`
5. JavaScript で `checkCostLimit()` 関数を追加（Thymeleafの `${costLimit}` を使用）
6. `updateTotalCost()` 関数内で `checkCostLimit()` を呼び出し
7. フォーム送信時のバリデーションに `checkCostLimit()` を追加

**動作確認手順**:
- `gradle bootRun` でアプリケーションを起動
- ブラウザで `http://localhost:80/` にアクセスし、`user1`/`p@ss` でログイン
- デッキ作成画面でセレクトボックスに「コスト: 駒名」形式で表示されることを確認
- コスト上限（50）が表示されることを確認
- 上限を超える駒を配置すると警告が表示されることを確認
- 上限を超えた状態で保存しようとするとエラーメッセージが表示されることを確認

**入力**: ユーザーの駒選択・配置操作
**出力**: コスト表示、警告表示、バリデーション結果

---

### タスク6: komamake.html にコスト表示機能を追加

**優先度**: 6

**関連ファイル**:
- 修正: `tkk_game/src/main/resources/templates/komamake.html`

**作業内容**:
1. コスト表示エリアを追加
   - `<div><strong>選択中のルールのコスト：</strong><span id="rule-cost-display">0</span></div>`
2. JavaScript で各ルールのコストを定義した `ruleCosts` オブジェクトを作成
3. `updateRuleCost()` 関数を追加（チェック済みのルールのコストを合計）
4. 全てのチェックボックスに `change` イベントリスナーを追加

**動作確認手順**:
- `gradle bootRun` でアプリケーションを起動
- ブラウザで `http://localhost:80/` にアクセスし、`user1`/`p@ss` でログイン
- ホーム画面から「デッキ作成」→「駒を作る」をクリック
- 移動ルールのチェックボックスをON/OFFすると、コストがリアルタイムに更新されることを確認
- 例: UP(1) + DOWN(1) + LINE_UP(3) = 5 と表示される

**入力**: チェックボックスの選択状態
**出力**: 選択中のルールの合計コスト

---

## Definition of Done (DoD)

すべてのタスクが正常に完了した後、以下の手順で動作確認を実施します。

### 確認手順

#### 1. デッキ作成画面のコスト表示
1. `gradle build` を実行し、コンパイルエラーがないことを確認
2. `gradle bootRun` でアプリケーションを起動
3. ブラウザで `http://localhost:80/` にアクセス
4. `user1` / `p@ss` でログイン
5. ホーム画面から「デッキ作成」をクリック
6. 駒選択セレクトボックスで「1: 歩兵」「8: 王将」「12: 飛車」のように表示されることを確認
7. 「コスト上限: 50」と表示されることを確認

#### 2. デッキコスト計算と上限バリデーション機能
1. デッキ作成画面でコスト8の王将とコスト12の飛車を配置（合計20）
2. デッキコストが「20」と表示され、警告が出ないことを確認
3. さらに駒を配置してコストを51以上にする
4. 警告が表示されることを確認（「警告: デッキコストが上限を超えています (51/50)」）
5. 保存ボタンをクリックすると、エラーメッセージが表示されることを確認
6. 駒を削除してコストを50以下にする
7. 保存が成功し、成功メッセージが表示されることを確認
8. H2コンソールで `SELECT * FROM Deck` を実行し、保存されたデッキの `cost` カラムに計算されたコストが保存されていることを確認

#### 3. 駒作成画面のコスト表示
1. ホーム画面から「デッキ作成」→「駒を作る」をクリック
2. 移動ルールのチェックボックスで「UP」をチェック
3. 「選択中のルールのコスト: 1」と表示されることを確認
4. さらに「LINE_UP」をチェック
5. 「選択中のルールのコスト: 4」（1+3）と表示されることを確認
6. チェックを外すとコストが減ることを確認

### 期待される動作
- セレクトボックスに「コスト: 駒名」形式で表示される
- コスト上限（50）が画面に表示される
- デッキのコスト合計が上限を超過時に警告が表示される
- サーバー側でもバリデーションが行われ、上限を超えるデッキは保存されない
- 保存されたデッキのコストがデータベースに正しく保存される
- 駒作成画面で選択中のルールのコストがリアルタイムに表示される
- コンパイルエラーや実行時エラーが発生しない
   - 登録したEmitterを返却
3. 既存の `while(true)` ループと `TimeUnit.SECONDS.sleep(1)` を削除

**動作確認手順**:
- ユーザーが最後に確認を実施するため、現段階では不要、次のステップに移ってください

**入力**: なし
**出力**: 登録されたSseEmitter（部屋リスト変更時に通知を受信）

---

### タスク8: 不要コードのクリーンアップ

**優先度**: 8

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/services/TurnChecker.java`
- 修正: `tkk_game/src/main/java/team3/tkk_game/services/MatchChecker.java`

**作業内容**:
1. `TurnChecker` から不要になったimport文を削除
2. `MatchChecker` から不要になったimport文を削除
3. 各ファイルのJavaDocコメントを更新（イベント駆動型であることを明記）

**入力**: なし
**出力**: クリーンアップされたソースコード

---

## 依存関係

```
タスク1 ──┬──▶ タスク2 ──┬──▶ タスク4
          │              │
          └──▶ タスク3 ──┘

タスク5 ──┬──▶ タスク6 ──┬──▶ タスク7
          │              │
          └──────────────┘

タスク4 ──┬──▶ タスク8
タスク7 ──┘
```
