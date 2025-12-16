# システム仕様書 (最新版)

## 最終更新日
2025-12-16

## システム概要
Spring Boot を用いた Web アプリケーション。将棋風のボードゲームを実装しており、ユーザー認証、マッチング機能、リアルタイムゲーム機能を提供する。SSE (Server-Sent Events) を活用したリアルタイム通信により、プレイヤー間の対戦マッチングとターン制ゲームプレイを実現している。デッキ作成機能とH2データベースによる駒・デッキデータの永続化機能も実装されている。

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
| `/game/start` | GET | 認証必須 | ゲーム開始 (マッチング成立時) |
| `/game` | GET | 認証必須 | ゲーム画面表示 |
| `/game/move` | GET | 認証必須 | 駒の移動処理 (fromX, fromY, toX, toY パラメータ) |
| `/game/turn` | GET (SSE) | 認証必須 | ゲームターン情報をリアルタイム配信 |
| `/deck/make` | GET | 認証必須 | デッキ作成画面 |
| `/deck/save` | POST | 認証必須 | デッキ保存 (deckName, sfen パラメータ) |
| `/deck/select` | GET | 認証必須 | デッキ選択画面 |
| `/deck/choose` | POST | 認証必須 | デッキ選択確定 |
| `/deck/load/{id}` | GET | 認証必須 | デッキ読み込み (セッションに保存) |
| `/deck/delete/{id}` | GET | 認証必須 | デッキ削除 |

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
- 駒一覧の表示
- デッキ名とSFEN形式での盤面設定保存機能

### デッキ選択画面 (`tkk_game/src/main/resources/templates/deckselect.html`)
- Thymeleaf テンプレート
- 保存済みデッキ一覧の表示
- デッキの読み込み・削除機能

### ゲーム画面 (`tkk_game/src/main/resources/templates/game.html`)
- Thymeleaf テンプレート
- 5×5の盤面を表示
- 駒の選択と移動が可能
- SSE によりターン情報をリアルタイム受信
- 自分のターンでない場合は待機状態

## ゲームロジック仕様

### 盤面 (`Ban` クラス)
- 5×5 のマス目で構成
- 座標系: 中心が (0, 0)、左下が (-2, -2)
- 配列インデックスへの変換を内部で実施 (`b2a` メソッド)
- `getBoardR180()` で盤面を180度回転 (相手視点用)

### 駒 (`Koma` パッケージ)

#### `Koma` クラス
- 駒ID、駒名、移動ルール (`KomaRule` リスト)、所有者 (`Player`)、成り先駒ID を保持
- `KomaDB` と `KomaRule` リストから生成
- `canMove()` メソッドで移動可否を判定 (現在は常に `true` を返す)

#### `KomaDB` クラス
- データベースから取得した駒情報を格納
- ID、名前、成り先駒ID (`updateKoma`) を保持

#### `KomaRule` 列挙型
- 駒の移動ルールを定義
- 単マス移動: `UP`, `DOWN`, `LEFT`, `RIGHT`, `UP_LEFT`, `UP_RIGHT`, `DOWN_LEFT`, `DOWN_RIGHT`
- 直線移動: `LINE_UP`, `LINE_DOWN`, `LINE_LEFT`, `LINE_RIGHT`, `LINE_UP_LEFT`, `LINE_UP_RIGHT`, `LINE_DOWN_LEFT`, `LINE_DOWN_RIGHT`

### デッキ (`Deck` クラス)
- デッキID、デッキ名、SFEN形式の盤面配置を保持
- データベースに永続化

### プレイヤー (`Player` クラス)
- プレイヤー名 (`name`) とステータス (`status`) を保持
- ステータス: `MATCHING`, `MATCHED`, `GAME_THINKING`, `GAME_WAITING`, `OFFLINE`

### ゲーム (`Game` クラス)
- ゲーム ID、2人のプレイヤー、盤面 (`Ban`)、表示用盤面 (`displayBan`)、最終アクティビティ時刻を管理
- `switchTurn()` でターンを切り替え
- `displayBan` は相手視点用の盤面表示に使用
- 最終アクティビティ時刻を記録し、非アクティブなゲームの削除に使用

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

#### `TurnChecker` サービス
- `@Async` による非同期処理
- SSE でプレイヤーのターン状態をリアルタイム配信
- 1秒ごとにターン状態をチェックし送信

#### `MatchChecker` サービス
- `@Async` による非同期処理
- SSE で待機室のプレイヤーリストをリアルタイム配信
- 1秒ごとに待機室の状態を送信

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
- `/game/start`: ゲーム開始処理、駒の初期配置
- `/game`: ゲーム画面表示
- `/game/move`: 駒の移動処理、移動の妥当性チェック、ターン切り替え、盤面180度回転
- `/game/turn`: SSE エンドポイント、ターン情報を配信

### `DeckController`
- `/deck/make`: デッキ作成画面表示、駒一覧取得
- `/deck/save`: デッキ保存 (名前とSFEN)
- `/deck/select`: デッキ選択画面表示
- `/deck/choose`: デッキ選択確定
- `/deck/load/{id}`: デッキ読み込み (セッション保存)
- `/deck/delete/{id}`: デッキ削除

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

### 初期データ (駒マスタ)
| ID | 名前 | 成り先ID |
|----|------|---------|
| 1 | 歩兵 | 5 (金将) |
| 2 | 香車 | 5 (金将) |
| 3 | 桂馬 | 5 (金将) |
| 4 | 銀将 | 5 (金将) |
| 5 | 金将 | 5 (成りなし) |
| 6 | 角行 | 5 (金将) |
| 7 | 飛車 | 5 (金将) |
| 8 | 王将 | -1 (成りなし) |

## Mapper インターフェース

### `KomaMapper`
- `selectAllKoma()`: 全駒を取得
- `selectKomaById(Integer komaId)`: IDで駒を取得
- `selectKomaRuleById(Integer komaId)`: 駒IDで移動ルールを取得

### `DeckMapper`
- `insertDeck(Deck deck)`: デッキを挿入
- `selectAllDecks()`: 全デッキを取得
- `selectDeckById(int id)`: IDでデッキを取得
- `deleteDeckById(int id)`: IDでデッキを削除

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
| `tkk_game/src/main/java/team3/tkk_game/model/Koma/KomaRule.java` | 駒移動ルール列挙型 |
| `tkk_game/src/main/java/team3/tkk_game/model/Deck.java` | デッキモデル |
| `tkk_game/src/main/java/team3/tkk_game/model/Player.java` | プレイヤーモデル |
| `tkk_game/src/main/java/team3/tkk_game/model/PlayerStatus.java` | プレイヤーステータス列挙型 |
| `tkk_game/src/main/java/team3/tkk_game/model/GameRoom.java` | ゲーム管理モデル |
| `tkk_game/src/main/java/team3/tkk_game/model/WaitRoom.java` | 待機室管理モデル |
| `tkk_game/src/main/java/team3/tkk_game/mapper/KomaMapper.java` | 駒マッパー (MyBatis) |
| `tkk_game/src/main/java/team3/tkk_game/mapper/DeckMapper.java` | デッキマッパー (MyBatis) |
| `tkk_game/src/main/java/team3/tkk_game/services/TurnChecker.java` | ターンチェックサービス (SSE) |
| `tkk_game/src/main/java/team3/tkk_game/services/MatchChecker.java` | マッチングチェックサービス (SSE) |
| `tkk_game/src/main/resources/static/index.html` | トップページ (静的) |
| `tkk_game/src/main/resources/templates/home.html` | ホーム画面テンプレート |
| `tkk_game/src/main/resources/templates/match.html` | マッチング画面テンプレート |
| `tkk_game/src/main/resources/templates/waiting.html` | 待機画面テンプレート |
| `tkk_game/src/main/resources/templates/game.html` | ゲーム画面テンプレート |
| `tkk_game/src/main/resources/templates/deckmake.html` | デッキ作成画面テンプレート |
| `tkk_game/src/main/resources/templates/deckselect.html` | デッキ選択画面テンプレート |
| `tkk_game/src/main/resources/templates/debug.html` | デバッグ用画面テンプレート |
| `tkk_game/src/main/resources/schema.sql` | データベーススキーマ定義 |
| `tkk_game/src/main/resources/data.sql` | 初期データ投入 |
| `tkk_game/src/main/resources/application.properties` | アプリケーション設定 (ポート: 80, H2設定) |
| `tkk_game/build.gradle` | Gradle ビルド設定 |
| `docs/tasks.md` | 現在の作業計画 |
| `docs/reports/done/2025-11-11_ログイン機能最小実装.md` | 完了レポート (ログイン機能) |
| `docs/reports/investigate/2025-11-11_ログイン機能実装方法調査.md` | 調査レポート (ログイン機能) |

## 現状の実装状態
- ✅ ユーザー認証機能 (4ユーザー登録済み)
- ✅ マッチング機能 (SSE によるリアルタイム待機室)
- ✅ 部屋作成・対戦リクエスト送受信機能
- ✅ ゲーム開始機能 (2人マッチング)
- ✅ 盤面表示機能 (5×5)
- ✅ 駒の移動機能 (盤面180度回転による相手視点対応)
- ✅ ターン制御機能 (SSE によるリアルタイム更新)
- ✅ 非アクティブゲームの自動削除 (10分)
- ✅ H2データベースによる駒・デッキデータの永続化
- ✅ MyBatisによるデータアクセス層
- ✅ デッキ作成・保存機能
- ✅ デッキ選択・読み込み・削除機能
- ⚠️ 駒の移動ルール判定 (未完成: 常に `true` を返す状態)
- ⚠️ 駒を取る処理 (未実装)
