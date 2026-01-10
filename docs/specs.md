# システム仕様書 (最新版)

## 最終更新日
2026-01-10

## システム概要
Spring Boot を用いた Web アプリケーション。将棋風のボードゲームを実装しており、ユーザー認証、マッチング機能、リアルタイムゲーム機能を提供する。SSE (Server-Sent Events) を活用したリアルタイム通信により、プレイヤー間の対戦マッチングとターン制ゲームプレイを実現している。デッキ作成機能とH2データベースによる駒・デッキデータの永続化機能、駒のコストシステム、駒作成・編集機能も実装されている。駒の移動ルール判定は `MoveValidator` サービスに分離されており、各駒の移動ルール（単マス移動・直線移動・ジャンプ移動）を正確に判定する。また、駒にはスキル（STEALTH等）を設定可能で、特殊な動作を実装できる。プレイヤーは自分で作成した駒を一覧表示・編集・削除でき、デッキ作成時には自分が使用可能な駒のみが表示される。

## 使用技術 / バージョン
- Java 21 (Gradle Toolchain)
- Spring Boot 3.5.7
- Spring Security
- Spring Web
- Thymeleaf
- Thymeleaf Extras SpringSecurity6
- Spring Boot DevTools
- MyBatis 3.0.5 (駒・デッキのデータアクセスに使用)
- H2 Database (駒マスタ、駒ルール、デッキ保存に使用)
- Server-Sent Events (SSE) によるリアルタイム通信
- Spring Async / Scheduling

## サーバー設定
- ポート番号: 80 (`application.properties` で設定)
- H2 コンソール: 有効 (`/h2-console`)
- H2 データベース URL: `jdbc:h2:mem:tkk`
- H2 データベース ユーザー名: `sa` / パスワード: `tkk`

## 現在のエンドポイント一覧
| パス | メソッド | 認可 | 説明 |
|------|----------|------|------|
| `/` | GET | 認証不要 | TOPページ (静的HTML) |
| `/login` | GET/POST | 認証不要(フォーム) | Spring Security 標準ログインフォーム |
| `/logout` | POST/GET | 認証済み時 | ログアウト後 `/` リダイレクト |
| `/home` | GET | 認証必須 | ホーム画面 (マッチング・デッキ管理へのリンク) |
| `/match` | GET | 認証必須 | マッチング画面 (待機中プレイヤー一覧表示) |
| `/match/waitRoom` | GET (SSE) | 認証必須 | 待機室のプレイヤーリストをリアルタイム配信 |
| `/match/makeRoom` | GET | 認証必須 | 部屋作成、待機画面へ遷移 |
| `/match/sendRequest` | POST | 認証必須 | 対戦リクエスト送信 |
| `/match/accept` | POST | 認証必須 | 対戦リクエスト承認 |
| `/match/reject` | POST | 認証必須 | 対戦リクエスト拒否 |
| `/game/start` | GET | 認証必須 | ゲーム開始 (マッチング成立時、選択されたデッキを盤面に配置) |
| `/game` | GET | 認証必須 | ゲーム画面表示 |
| `/game/move` | GET | 認証必須 | 駒の移動処理 (fromX, fromY, toX, toY, isUpdate パラメータ) |
| `/game/putKoma` | GET | 認証必須 | 持ち駒を盤面に置く処理 (index, toX, toY パラメータ) |
| `/game/turn` | GET (SSE) | 認証必須 | ゲームターン情報をリアルタイム配信 |
| `/game/result` | GET | 認証必須 | ゲーム結果画面表示 |
| `/game/movable` | GET | 認証必須 | 指定した駒の移動可能なマスを取得するAPI (x, y パラメータ) |
| `/deck/make` | GET | 認証必須 | デッキ作成画面 (自分が使用可能な駒一覧とコスト表示) |
| `/deck/save` | POST | 認証必須 | デッキ保存 (deckName, sfen パラメータ、コスト上限チェック) |
| `/deck/select` | GET | 認証必須 | デッキ選択画面 (デッキ名とコスト表示) |
| `/deck/choose` | POST | 認証必須 | デッキ選択確定 (deckId パラメータ) |
| `/deck/load/{id}` | GET | 認証必須 | デッキ読み込み (セッションに保存) |
| `/deck/delete/{id}` | GET | 認証必須 | デッキ削除 |
| `/deck/make/koma` | GET | 認証必須 | 駒作成画面へリダイレクト (後方互換性) |
| `/deck/make/koma/save` | POST | 認証必須 | 駒作成保存へリダイレクト (後方互換性) |
| `/koma/list` | GET | 認証必須 | 自作駒一覧画面 |
| `/koma/make` | GET | 認証必須 | 駒作成画面 |
| `/koma/make/save` | POST | 認証必須 | 駒作成保存 (name, rules, skill, updateKomaId パラメータ) |
| `/koma/edit/{id}` | GET | 認証必須 | 駒編集画面 (所有者のみ) |
| `/koma/update/{id}` | POST | 認証必須 | 駒更新 (所有者のみ) |
| `/koma/delete/{id}` | GET | 認証必須 | 駒削除 (所有者のみ) |

## 認証仕様
- 認証方式: フォームログイン (Spring Security)
- ログイン成功後: `/home` へリダイレクト
- ユーザー保管: インメモリ (`InMemoryUserDetailsManager`)
- ユーザー一覧:
  - `user1` / パスワード `p@ss` / ロール `ROLE_USER`
  - `user2` / パスワード `p@ss` / ロール `ROLE_USER`
  - `user3` / パスワード `p@ss` / ロール `ROLE_USER`
  - `user4` / パスワード `p@ss` / ロール `ROLE_USER`
- パスワードハッシュ: `{bcrypt}$2y$10$ngxCDmuVK1TaGchiYQfJ1OAKkd64IH6skGsNw1sLabrTICOHPxC0e`

## セキュリティ設定概要 (`tkk_game/src/main/java/team3/tkk_game/security/SecurityConfig.java`)
- `formLogin()` 有効化、ログイン成功後は `/home` へリダイレクト
- `logout()` 設定: ログアウト後 `/` へリダイレクト
- 認可設定:
  - `/`, `/index.html` は認証不要 (`permitAll()`)
  - その他すべてのリクエストは認証必須 (`authenticated()`)

## 画面仕様

### TOPページ (`tkk_game/src/main/resources/static/index.html`)
- 静的HTML
- ログインへのリンクを提供

### ホーム画面 (`tkk_game/src/main/resources/templates/home.html`)
- ログイン後の最初の画面
- マッチング画面へのリンクを提供
- デッキ作成・選択画面へのリンクを提供
- 自作駒一覧画面へのリンクを提供
- ホーム画面にアクセス時、待機室から自動的に削除され、既存ゲームも削除される

### マッチング画面 (`tkk_game/src/main/resources/templates/match.html`)
- Thymeleaf テンプレート
- SSE により待機中のプレイヤー一覧をリアルタイム表示
- 他プレイヤーへの対戦リクエスト送信機能

### 待機画面 (`tkk_game/src/main/resources/templates/waiting.html`)
- Thymeleaf テンプレート
- 部屋作成後の待機画面
- 対戦リクエストの受信・承認・拒否機能

### デッキ作成画面 (`tkk_game/src/main/resources/templates/deckmake.html`)
- Thymeleaf テンプレート
- 自分が使用可能な駒一覧の表示（駒ごとのコスト表示付き）
- デッキ名とSFEN形式での盤面設定保存機能
- デッキの合計コストをリアルタイム表示
- コスト上限（50）を超えると保存時にエラー
- 駒作成画面へのリンク

### 自作駒一覧画面 (`tkk_game/src/main/resources/templates/komalist.html`)
- Thymeleaf テンプレート
- 自分が使用可能な駒一覧を表示（駒名、コスト、スキル）
- 所有者のみ編集・削除が可能
- 共通駒は「共通駒」ラベルを表示
- 駒作成画面へのリンク

### 駒作成画面 (`tkk_game/src/main/resources/templates/komamake.html`)
- Thymeleaf テンプレート
- 駒名の入力フォーム
- 移動ルールの選択（チェックボックス）
- 特殊スキルの選択（ラジオボタン）
- 成り先駒の選択（成らない駒のみ選択可能）
- 駒をデータベースに保存する機能
- 作成者に所有権を付与

### 駒編集画面 (`tkk_game/src/main/resources/templates/komaedit.html`)
- Thymeleaf テンプレート
- 既存の駒情報を編集するフォーム
- 駒名、移動ルール、特殊スキル、成り先を編集可能
- 所有者のみアクセス可能
- 更新・キャンセルボタン

### デッキ選択画面 (`tkk_game/src/main/resources/templates/deckselect.html`)
- Thymeleaf テンプレート
- 保存済みデッキ一覧の表示（デッキ名とコストを表示）
- デッキの読み込み・削除機能

### ゲーム画面 (`tkk_game/src/main/resources/templates/game.html`)
- Thymeleaf テンプレート
- 5×5の盤面を表示
- 駒の選択と移動が可能
- 駒選択時に移動可能なマスをハイライト表示
- SSE によりターン情報をリアルタイム受信
- 自分のターンでない場合は待機状態
- 駒の成り機能（成るボタン）
- 持ち駒の表示と配置機能

### 結果画面 (`/game/result`)
- ゲーム終了時に表示される画面
- 勝敗結果を表示
- 最終盤面を表示
- ホームへ戻るリンクを提供

## ゲームロジック仕様

### 盤面 (`Ban` クラス)
- 5×5 のマス目で構成
- 座標系: 中心が (0, 0)、左下が (-2, 2)
- 配列インデックスへの変換を内部で実施 (`b2a` メソッド)
- `getBoardR180()` で盤面を180度回転 (相手視点用)

### 駒 (`Koma` パッケージ)

#### `Koma` クラス
- 駒ID、駒名、移動ルール (`KomaRule` リスト)、所有者 (`Player`)、成り先駒ID を保持
- `KomaDB` と `KomaRule` リストから生成
- `canMove()` メソッドで移動可否を判定 (現在は常に `true` を返す)

#### `KomaDB` クラス
- データベースから取得した駒情報を格納
- ID、名前、スキル名、成り先駒ID (`updateKoma`) を保持
- `calculateCost(List<KomaRule> rules)` メソッドで駒のコストを計算
- デフォルトコンストラクタと引数コンストラクタを提供（MyBatisとController双方で使用）

#### `KomaRule` 列挙型
- 駒の移動ルールを定義
- 各ルールにコスト値を保持 (`getCost()` メソッド)
- 単マス移動（コスト1）: `UP`, `DOWN`, `LEFT`, `RIGHT`, `UP_LEFT`, `UP_RIGHT`, `DOWN_LEFT`, `DOWN_RIGHT`
- 直線移動（コスト3）: `LINE_UP`, `LINE_DOWN`, `LINE_LEFT`, `LINE_RIGHT`, `LINE_UP_LEFT`, `LINE_UP_RIGHT`, `LINE_DOWN_LEFT`, `LINE_DOWN_RIGHT`
- ジャンプ移動（コスト2）: `JUMP_UP_LEFT`, `JUMP_UP_RIGHT`, `JUMP_DOWN_LEFT`, `JUMP_DOWN_RIGHT`, `JUMP_LEFT_UP`, `JUMP_LEFT_DOWN`, `JUMP_RIGHT_UP`, `JUMP_RIGHT_DOWN`

#### `KomaSkill` 列挙型
- 駒のスキルを定義
- 各スキルにコスト値を保持 (`getCost()` メソッド)
- `NULL`（コスト0）: スキルなし
- `STEALTH`（コスト5）: ステルススキル（移動後に相手の盤面に駒が表示されない）

### デッキ (`Deck` クラス)
- デッキID、デッキ名、SFEN形式の盤面配置、コストを保持
- データベースに永続化

### プレイヤー (`Player` クラス)
- プレイヤー名 (`name`) とステータス (`status`) を保持
- ステータス: `MATCHING`, `WAITING`, `MATCHED`, `GAME_STARTING`, `GAME_THINKING`, `GAME_WAITING`, `GAME_WIN`, `GAME_END`, `OFFLINE`

### ゲーム (`Game` クラス)
- ゲーム ID、2人のプレイヤー、盤面 (`Ban`)、表示用盤面 (`displayBan`)、最終アクティビティ時刻を管理
- プレイヤーごとの選択デッキID (`deckIdPlayer1`, `deckIdPlayer2`) を保持
- `switchTurn()` でターンを切り替え（盤面回転も含む）
- `displayBan` は相手視点用の盤面表示に使用
- 最終アクティビティ時刻を記録し、非アクティブなゲームの削除に使用
- 持ち駒管理: `haveKoma1`, `haveKoma2` でプレイヤーごとの持ち駒を管理
- `getHaveKomaByName()` で自分の持ち駒を取得
- `getEHaveKomaByName()` で相手の持ち駒を取得
- `addHaveKomaByName()` で持ち駒を追加（駒ID順にソート挿入）
- `isFinished` フラグでゲーム終了状態を管理

### ゲームルーム (`GameRoom` クラス)
- すべてのゲームを管理 (`ArrayList<Game>`)
- ゲームの追加、検索 (ID / プレイヤー名)、削除
- 10分間アクティビティがないゲームを定期的に削除 (`@Scheduled`)

### 待機室 (`WaitRoom` クラス)
- 待機中のゲーム (部屋) を管理 (`ArrayList<Game>`)
- 部屋の作成 (`addWaitRoom`)、検索、削除
- 対戦リクエストの送信 (`sendRequest`)、クリア (`clearRequest`)
- Player1が部屋のオーナー、Player2がリクエスト送信者

### サービス層

#### `MoveValidator` サービス
- 駒の移動可否を判定するサービスクラス
- `canMove(Ban ban, int fromX, int fromY, int toX, int toY)` メソッドで判定
- `getMovableCells(Ban ban, int fromX, int fromY)` メソッドで移動可能なマス一覧を取得
- 移動先に自分の駒がある場合は移動不可
- 駒の移動ルール (`KomaRule`) に基づいて移動可否を判定
- 直線移動の場合、経路上に駒がある場合は移動不可（飛び越え不可）
- 対応ルール:
  - 単マス移動: `UP`, `DOWN`, `LEFT`, `RIGHT`, `UP_LEFT`, `UP_RIGHT`, `DOWN_LEFT`, `DOWN_RIGHT`
  - 直線移動: `LINE_UP`, `LINE_DOWN`, `LINE_LEFT`, `LINE_RIGHT`, `LINE_UP_LEFT`, `LINE_UP_RIGHT`, `LINE_DOWN_LEFT`, `LINE_DOWN_RIGHT`
  - ジャンプ移動: `JUMP_UP_LEFT`, `JUMP_UP_RIGHT`, `JUMP_DOWN_LEFT`, `JUMP_DOWN_RIGHT`, `JUMP_LEFT_UP`, `JUMP_LEFT_DOWN`, `JUMP_RIGHT_UP`, `JUMP_RIGHT_DOWN`（桂馬用）

#### `GameEventEmitterManager` サービス
- ゲームイベントのSSE配信を管理するクラス
- イベント駆動型でクライアントにターン変更通知を送信
- `registerPlayerEmitter(String playerName, String gameId)`: プレイヤーのEmitterを登録
- `notifyTurnChange(String gameId, String currentTurnPlayerName)`: ターン変更を該当ゲームの全プレイヤーに通知
- `removeEmitter(String playerName)`: プレイヤーのEmitterを削除
- `removePlayerEmittersByGameId(String gameId)`: 指定されたゲームIDに関連するすべてのEmitterを削除（ゲーム終了時に使用）

#### `TurnChecker` サービス
- `@Async` による非同期処理（従来方式、現在は `GameEventEmitterManager` に移行）
- SSE でプレイヤーのターン状態をリアルタイム配信
- 1秒ごとにターン状態をチェックし送信

#### `MatchChecker` サービス
- `@Async` による非同期処理
- SSE で待機室のプレイヤーリストをリアルタイム配信
- 1秒ごとに待機室の状態を送信

#### `WaitRoomEventEmitterManager` サービス
- 待機室イベントのSSE配信を管理するクラス
- イベント駆動型でクライアントにマッチング状態を通知

## コントローラー仕様

### `MainController`
- `/home`: ホーム画面表示、待機室・ゲームから削除

### `MatchController`
- `/match`: マッチング画面表示
- `/match/waitRoom`: SSE エンドポイント、待機室情報を配信
- `/match/makeRoom`: 部屋作成、待機画面表示
- `/match/sendRequest`: 対戦リクエスト送信
- `/match/accept`: 対戦リクエスト承認、ゲーム開始
- `/match/reject`: 対戦リクエスト拒否

### `GameController`
- `/game/start`: ゲーム開始処理、選択されたデッキから駒の初期配置を盤面に反映
- `/game`: ゲーム画面表示
- `/game/move`: 駒の移動処理、`MoveValidator` による移動の妥当性チェック、駒を取る処理、駒の成り処理、スキル処理（STEALTH等）、勝利判定、ターン切り替え、盤面180度回転
- `/game/putKoma`: 持ち駒を盤面に置く処理、持ち駒リストからの削除、勝利判定、ターン切り替え
- `/game/turn`: SSE エンドポイント、ターン情報を配信（`TurnChecker` 経由で `GameEventEmitterManager` を使用）
- `/game/result`: ゲーム結果画面表示、勝者・敗者の得点調整（コメントで記載、未実装）
- `/game/movable`: 指定した駒の移動可能なマスを取得するAPI（JSONレスポンス）

### `DeckController`
- `/deck/make`: デッキ作成画面表示、駒一覧取得、駒ごとのコスト計算、コスト上限設定
- `/deck/save`: デッキ保存 (名前とSFEN)、コスト上限チェック、コスト値をDBに保存
- `/deck/select`: デッキ選択画面表示（デッキ名とコストを表示）
- `/deck/choose`: デッキ選択確定
- `/deck/load/{id}`: デッキ読み込み (セッション保存)
- `/deck/delete/{id}`: デッキ削除
- `/deck/make/koma`: 駒作成画面表示、成り先候補駒一覧取得
- `/deck/make/koma/save`: 駒作成保存、トランザクション管理

## 非同期・スケジューリング設定
- `@EnableAsync`: 非同期処理を有効化
- `@EnableScheduling`: スケジュールタスクを有効化
- 定期実行タスク:
  - `GameRoom.rmGameNoActive()`: 10分ごとに非アクティブなゲームを削除

## データベース設定

### テーブル構成

#### koma テーブル
| カラム名 | 型 | 説明 |
|----------|-----|------|
| id | INT (PK, AUTO_INCREMENT) | 駒ID |
| name | VARCHAR(50) | 駒名 |
| skill | VARCHAR(50) | スキル名（NULL、STEALTH等） |
| update_koma | INT | 成り先駒ID (-1 は成りなし) |

#### KomaRule テーブル
| カラム名 | 型 | 説明 |
|----------|-----|------|
| id | INT (PK, AUTO_INCREMENT) | ルールID |
| koma_id | INT (FK) | 駒ID |
| rule | VARCHAR(50) | 移動ルール名 |

#### Deck テーブル
| カラム名 | 型 | 説明 |
|----------|-----|------|
| id | INT (PK, AUTO_INCREMENT) | デッキID |
| name | VARCHAR(255) | デッキ名 |
| sfen | VARCHAR(255) | SFEN形式の盤面配置 |
| cost | INT | デッキの合計コスト（デフォルト: 0） |

#### Player テーブル
| カラム名 | 型 | 説明 |
|----------|-----|------|
| id | INT (PK, AUTO_INCREMENT) | プレイヤーID |
| username | VARCHAR(255) | ユーザー名（ユニーク制約） |
| selected_deck_id | INT (FK, NULL可) | 選択中のデッキID（Deckテーブルへの外部キー） |

### 初期データ (駒マスタ)
| ID | 名前 | スキル | 成り先ID |
|----|------|--------|---------|
| 0 | 王将 | NULL | -1 (成りなし) |
| 1 | 歩兵 | NULL | 8 (と金) |
| 2 | 香車 | NULL | 9 (成香) |
| 3 | 桂馬 | NULL | 10 (成桂) |
| 4 | 銀将 | NULL | 11 (成銀) |
| 5 | 金将 | NULL | -1 (成りなし) |
| 6 | 角行 | NULL | 12 (馬) |
| 7 | 飛車 | NULL | 13 (龍) |
| 8 | と金 | NULL | -1 (成りなし) |
| 9 | 成香 | NULL | -1 (成りなし) |
| 10 | 成桂 | NULL | -1 (成りなし) |
| 11 | 成銀 | NULL | -1 (成りなし) |
| 12 | 馬 | NULL | -1 (成りなし) |
| 13 | 龍 | NULL | -1 (成りなし) |
| 14 | 忍び | STEALTH | -1 (成りなし) |

### 初期データ (デッキ)
| ID | 名前 | SFEN | コスト |
|----|------|------|--------|
| 1 | sample | 5/2[0]2 | 8 |

### 初期データ (プレイヤー)
| ユーザー名 | 選択中のデッキID |
|-----------|----------------|
| user1 | 1 |
| user2 | 1 |
| user3 | 1 |
| user4 | null (デッキ未選択) |

### 初期データ (駒移動ルール)
| 駒ID | 駒名 | ルール |
|------|------|--------|
| 0 | 王将 | UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT |
| 1 | 歩兵 | UP |
| 2 | 香車 | LINE_UP |
| 3 | 桂馬 | JUMP_UP_LEFT, JUMP_UP_RIGHT |
| 4 | 銀将 | UP, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT |
| 5 | 金将 | UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT |
| 6 | 角行 | LINE_UP_LEFT, LINE_UP_RIGHT, LINE_DOWN_LEFT, LINE_DOWN_RIGHT |
| 7 | 飛車 | LINE_UP, LINE_DOWN, LINE_LEFT, LINE_RIGHT |
| 8 | と金 | UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT |
| 9 | 成香 | UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT |
| 10 | 成桂 | UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT |
| 11 | 成銀 | UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT |
| 12 | 馬 | UP, DOWN, LEFT, RIGHT, LINE_UP_LEFT, LINE_UP_RIGHT, LINE_DOWN_LEFT, LINE_DOWN_RIGHT |
| 13 | 龍 | UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT, LINE_UP, LINE_DOWN, LINE_LEFT, LINE_RIGHT |
| 14 | 忍び | UP, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT |

## Mapper インターフェース

### `KomaMapper`
- `selectAllKoma()`: 全駒を取得
- `selectKomaById(Integer komaId)`: IDで駒を取得
- `selectKomaRuleById(Integer komaId)`: 駒IDで移動ルールを取得
- `insertKoma(KomaDB koma)`: 駒をデータベースに挿入（IDは自動採番）
- `insertKomaRule(int komaId, KomaRule ruleName)`: 駒の移動ルールを挿入

### `DeckMapper`
- `insertDeck(Deck deck)`: デッキを挿入
- `selectAllDecks()`: 全デッキを取得
- `selectDeckById(int id)`: IDでデッキを取得
- `deleteDeckById(int id)`: IDでデッキを削除

### `PlayerMapper`
- `getSelectedDeckIdByName(String username)`: ユーザー名から選択中のデッキIDを取得
- `updateSelectedDeckId(String username, Integer deckId)`: 選択中のデッキIDを更新
- `clearSelectedDeckId(Integer deckId)`: 指定したデッキIDを選択しているプレイヤーの選択を解除

## ディレクトリ構成
| パス | 役割 |
|------|------|
| `tkk_game/src/main/java/team3/tkk_game/TkkGameApplication.java` | Spring Boot 起動クラス (`@EnableAsync`, `@EnableScheduling`) |
| `tkk_game/src/main/java/team3/tkk_game/security/SecurityConfig.java` | セキュリティ設定クラス |
| `tkk_game/src/main/java/team3/tkk_game/controller/MainController.java` | メインコントローラー (ホーム) |
| `tkk_game/src/main/java/team3/tkk_game/controller/MatchController.java` | マッチングコントローラー (マッチング、待機、対戦リクエスト) |
| `tkk_game/src/main/java/team3/tkk_game/controller/GameController.java` | ゲームコントローラー (ゲーム開始、移動) |
| `tkk_game/src/main/java/team3/tkk_game/controller/DeckController.java` | デッキコントローラー (デッキ作成、選択、削除) |
| `tkk_game/src/main/java/team3/tkk_game/model/Game.java` | ゲームモデル |
| `tkk_game/src/main/java/team3/tkk_game/model/Ban.java` | 盤面モデル |
| `tkk_game/src/main/java/team3/tkk_game/model/Koma/Koma.java` | 駒モデル |
| `tkk_game/src/main/java/team3/tkk_game/model/Koma/KomaDB.java` | 駒DBモデル |
| `tkk_game/src/main/java/team3/tkk_game/model/Koma/KomaRule.java` | 駒移動ルール列挙型（コスト値含む） |
| `tkk_game/src/main/java/team3/tkk_game/model/Koma/KomaSkill.java` | 駒スキル列挙型（コスト値含む） |
| `tkk_game/src/main/java/team3/tkk_game/model/Deck.java` | デッキモデル |
| `tkk_game/src/main/java/team3/tkk_game/model/Player.java` | プレイヤーモデル |
| `tkk_game/src/main/java/team3/tkk_game/model/PlayerStatus.java` | プレイヤーステータス列挙型 |
| `tkk_game/src/main/java/team3/tkk_game/model/GameRoom.java` | ゲーム管理モデル |
| `tkk_game/src/main/java/team3/tkk_game/model/WaitRoom.java` | 待機室管理モデル |
| `tkk_game/src/main/java/team3/tkk_game/mapper/KomaMapper.java` | 駒マッパー (MyBatis) |
| `tkk_game/src/main/java/team3/tkk_game/mapper/DeckMapper.java` | デッキマッパー (MyBatis) |
| `tkk_game/src/main/java/team3/tkk_game/mapper/PlayerMapper.java` | プレイヤーマッパー (MyBatis) |
| `tkk_game/src/main/java/team3/tkk_game/services/TurnChecker.java` | ターンチェックサービス (SSE、従来方式) |
| `tkk_game/src/main/java/team3/tkk_game/services/MatchChecker.java` | マッチングチェックサービス (SSE) |
| `tkk_game/src/main/java/team3/tkk_game/services/MoveValidator.java` | 駒移動可否判定サービス |
| `tkk_game/src/main/java/team3/tkk_game/services/GameEventEmitterManager.java` | ゲームイベントSSE配信管理サービス（イベント駆動型） |
| `tkk_game/src/main/java/team3/tkk_game/services/WaitRoomEventEmitterManager.java` | 待機室イベントSSE配信管理サービス |
| `tkk_game/src/main/resources/static/index.html` | トップページ (静的) |
| `tkk_game/src/main/resources/templates/home.html` | ホーム画面テンプレート |
| `tkk_game/src/main/resources/templates/match.html` | マッチング画面テンプレート |
| `tkk_game/src/main/resources/templates/waiting.html` | 待機画面テンプレート |
| `tkk_game/src/main/resources/templates/game.html` | ゲーム画面テンプレート |
| `tkk_game/src/main/resources/templates/deckmake.html` | デッキ作成画面テンプレート |
| `tkk_game/src/main/resources/templates/deckselect.html` | デッキ選択画面テンプレート |
| `tkk_game/src/main/resources/templates/komamake.html` | 駒作成画面テンプレート |
| `tkk_game/src/main/resources/templates/debug.html` | デバッグ用画面テンプレート |
| `tkk_game/src/main/resources/schema.sql` | データベーススキーマ定義 |
| `tkk_game/src/main/resources/data.sql` | 初期データ投入 |
| `tkk_game/src/main/resources/application.properties` | アプリケーション設定 (ポート: 80, H2設定) |
| `tkk_game/build.gradle` | Gradle ビルド設定 |
| `docs/tasks.md` | 現在の作業計画 |
| `docs/reports/done/2025-11-11_ログイン機能最小実装.md` | 完了レポート (ログイン機能) |
| `docs/reports/done/done_2026-01-05_コストシステム実装.md` | 完了レポート (コストシステム) |
| `docs/reports/investigate/2025-11-11_ログイン機能実装方法調査.md` | 調査レポート (ログイン機能) |
| `docs/reports/investigate/2025-12-23_canMoveサービス分離調査.md` | 調査レポート (canMoveサービス分離) |
| `docs/reports/investigate/2026-01-04_駒作成フォームのDB保存実装方法調査.md` | 調査レポート (駒作成フォームのDB保存) |
| `docs/reports/investigate/2026-01-05_コストシステム実装方法調査.md` | 調査レポート (コストシステム) |
| `docs/reports/investigate/2026-01-05_コスト機能拡張実装方法調査.md` | 調査レポート (コスト機能拡張) |
| `docs/reports/review/2025-11-11_最小ログイン機能.md` | レビューレポート (ログイン機能) |
| `docs/reports/review/review_2025-12-23_canMoveサービス分離.md` | レビューレポート (canMoveサービス分離) |
| `docs/reports/review/review_2025-12-23_SSEイベント駆動化.md` | レビューレポート (SSEイベント駆動化) |
| `docs/reports/review/review_2026-01-04_駒作成機能実装.md` | レビューレポート (駒作成機能) |
| `docs/reports/review/plan_2026-01-04_駒作成機能実装計画.md` | 実装計画 (駒作成機能) |

## 現状の実装状態
- ✅ ユーザー認証機能 (4ユーザー登録済み)
- ✅ マッチング機能 (SSE によるリアルタイム待機室)
- ✅ 部屋作成・対戦リクエスト送受信機能
- ✅ ゲーム開始機能 (2人マッチング)
- ✅ 盤面表示機能 (5×5)
- ✅ 駒の移動機能 (盤面180度回転による相手視点対応)
- ✅ 駒の移動ルール判定 (`MoveValidator` サービスで実装)
  - ✅ 単マス移動: UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT
  - ✅ 直線移動: LINE_UP, LINE_DOWN, LINE_LEFT, LINE_RIGHT, LINE_UP_LEFT, LINE_UP_RIGHT, LINE_DOWN_LEFT, LINE_DOWN_RIGHT
  - ✅ 経路ブロック判定 (直線移動時に途中に駒がある場合は移動不可)
  - ✅ ジャンプ移動: JUMP_UP_LEFT, JUMP_UP_RIGHT, JUMP_DOWN_LEFT, JUMP_DOWN_RIGHT, JUMP_LEFT_UP, JUMP_LEFT_DOWN, JUMP_RIGHT_UP, JUMP_RIGHT_DOWN (桂馬用)
- ✅ 移動可能マス表示機能 (`/game/movable` APIでマス一覧を取得、フロントエンドでハイライト表示)
- ✅ 駒を取る処理 (相手の駒を取って持ち駒に追加)
- ✅ 持ち駒機能 (取った駒を保持、盤面に置く)
- ✅ 駒の成り処理 (敵陣で成ることが可能、成り先駒IDに基づいて駒を変更)
- ✅ 駒のスキル機能 (`KomaSkill` 列挙型で定義、STEALTH スキル実装済み)
  - ✅ STEALTH スキル: 移動後に相手の盤面に駒が表示されない
- ✅ ターン制御機能 (SSE によるリアルタイム更新、イベント駆動型に移行)
- ✅ 勝利判定機能 (相手の王将を取ったら勝利)
- ✅ ゲーム結果画面 (勝敗表示、最終盤面表示)
- ✅ 非アクティブゲームの自動削除 (10分)
- ✅ H2データベースによる駒・デッキデータの永続化
- ✅ MyBatisによるデータアクセス層
- ✅ デッキ作成・保存機能（コストシステム対応）
- ✅ デッキ選択・読み込み・削除機能（コスト表示対応）
- ✅ 駒作成機能（駒名、移動ルール、成り先の設定が可能）
  - ✅ 駒名の入力
  - ✅ 移動ルールの選択（チェックボックス）
  - ✅ 成り先駒の選択（成らない駒のみ選択可能）
  - ✅ データベースへの保存（トランザクション管理）
- ✅ コストシステム実装
  - ✅ 駒の移動ルールとスキルにコスト値を設定
  - ✅ デッキ作成画面でコストをリアルタイム表示
  - ✅ デッキ保存時にコスト上限（50）をチェック
  - ✅ デッキのコスト値をデータベースに保存
- ✅ SSEイベント駆動化 (`GameEventEmitterManager`, `WaitRoomEventEmitterManager`)
  - ✅ ポーリング方式から直接通知方式に移行
  - ✅ ゲーム終了時のリソース解放機能
- ✅ 駒作成機能 (`/deck/make/koma`, `/deck/make/koma/save`)
  - ✅ 駒のデータベースへの保存
  - ✅ 移動ルールの保存
  - ✅ 成り先駒の設定
- ✅ ゲーム開始時のデッキ配置機能（選択されたデッキを盤面に配置）

## 未実装タスク一覧

### 必須タスク（優先度: 最高）
- **切断処理の実装**
  - SSE切断時の適切なクリーンアップ
  - 切断時の対戦相手への通知
  - ゲーム中断時の処理

- **駒のスキル関連**
  - 新たにスキルを追加
  - 駒作成画面でスキルを選べるように
  - デッキ作成でスキルを選べるように

- **ゲーム画面のUI改善**
  - 配置のデザイン
  - サウンドエフェクトの追加（駒を動かす音、取る音など）←これは優先度中

- **駒作成画面のUI改善**
  - 選択中のルールの合計コストをリアルタイム表示
  - スキル選択機能の追加
  - 作成済み駒の一覧表示

- **戦績機能**
  - ユーザーテーブルの拡張（現在は基本情報のみ）
  - 勝敗・レーティングの記録
  - 戦績表示画面の作成

### 優先度: 高（実装検討中）

- **マッチング・待機画面のUI改善**
  - より見やすいレイアウト
  - プレイヤー情報の充実

- **デッキ選択画面のUI改善**
  - デッキプレビュー機能（盤面の視覚化）
  - デッキの編集機能（現在は削除のみ）

- **ランキング機能**
  - レーティングシステム
  - ランキング表示画面

- **駒の編集・削除機能**
  - 自作駒の編集機能
  - 自作駒の削除機能（現在insertのみ実装済み）
  - Mapper に `updateKoma()`, `deleteKoma()` の追加が必要

### 優先度: 中（余裕があれば実装）
- **エラーハンドリング強化**
  - 異常系のテストとエラー処理
  - タイムアウト処理

- **観戦機能**
  - 対局中のゲームを観戦できる機能

- **チャット機能**
  - 対局中のコミュニケーション機能


## コストシステム仕様

### コスト計算
駒のコストは、移動ルールのコスト合計とスキルのコストの合計で計算される。

#### 移動ルールのコスト
- **単マス移動**（コスト1）: UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT
- **直線移動**（コスト3）: LINE_UP, LINE_DOWN, LINE_LEFT, LINE_RIGHT, LINE_UP_LEFT, LINE_UP_RIGHT, LINE_DOWN_LEFT, LINE_DOWN_RIGHT
- **ジャンプ移動**（コスト2）: JUMP_UP_LEFT, JUMP_UP_RIGHT, JUMP_DOWN_LEFT, JUMP_DOWN_RIGHT, JUMP_LEFT_UP, JUMP_LEFT_DOWN, JUMP_RIGHT_UP, JUMP_RIGHT_DOWN

#### スキルのコスト
- **NULL**（コスト0）: スキルなし
- **STEALTH**（コスト5）: ステルススキル

### 駒ごとのコスト例
| 駒名 | 移動ルール | スキル | コスト計算 | 合計コスト |
|------|----------|--------|-----------|-----------|
| 王将 | 8個の単マス移動 | NULL | 8×1 + 0 | 8 |
| 歩兵 | 1個の単マス移動 | NULL | 1×1 + 0 | 1 |
| 香車 | 1個の直線移動 | NULL | 1×3 + 0 | 3 |
| 桂馬 | 2個のジャンプ移動 | NULL | 2×2 + 0 | 4 |
| 銀将 | 5個の単マス移動 | NULL | 5×1 + 0 | 5 |
| 金将 | 6個の単マス移動 | NULL | 6×1 + 0 | 6 |
| 角行 | 4個の直線移動 | NULL | 4×3 + 0 | 12 |
| 飛車 | 4個の直線移動 | NULL | 4×3 + 0 | 12 |
| と金 | 6個の単マス移動 | NULL | 6×1 + 0 | 6 |
| 成香 | 6個の単マス移動 | NULL | 6×1 + 0 | 6 |
| 成桂 | 6個の単マス移動 | NULL | 6×1 + 0 | 6 |
| 成銀 | 6個の単マス移動 | NULL | 6×1 + 0 | 6 |
| 馬 | 4個の直線移動 + 4個の単マス移動 | NULL | 4×3 + 4×1 + 0 | 16 |
| 龍 | 4個の直線移動 + 4個の単マス移動 | NULL | 4×3 + 4×1 + 0 | 16 |
| 忍び | 5個の単マス移動 | STEALTH | 5×1 + 5 | 10 |

### デッキコスト上限
- コスト上限: **50**
- デッキ作成画面でリアルタイムに合計コストを表示
- デッキ保存時にコスト上限を超えている場合はエラーメッセージを表示し保存不可

### コスト実装箇所
- `KomaRule` 列挙型: 各移動ルールのコスト値を保持
- `KomaSkill` 列挙型: 各スキルのコスト値を保持
- `Koma` クラス: `getCost()` メソッドでコスト計算
- `KomaDB` クラス: `calculateCost(List<KomaRule> rules)` メソッドでコスト計算
- `DeckController`: デッキ作成画面で駒ごとのコストを計算し、モデルに追加
- `DeckController`: デッキ保存時にコスト上限チェックとコスト値のDB保存
- `deckmake.html`: 駒選択時とデッキ編集時にコストをリアルタイム表示
- `Deck` テーブル: cost カラムでデッキのコストを保存
