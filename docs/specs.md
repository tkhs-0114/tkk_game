# システム仕様書 (最新版)

## 最終更新日
2025-12-09

## システム概要
Spring Boot を用いた Web アプリケーション。将棋風のボードゲームを実装しており、ユーザー認証、マッチング機能、リアルタイムゲーム機能を提供する。SSE (Server-Sent Events) を活用したリアルタイム通信により、プレイヤー間の対戦マッチングとターン制ゲームプレイを実現している。

## 使用技術 / バージョン
- Java 21 (Gradle Toolchain)
- Spring Boot 3.5.7
- Spring Security
- Spring Web
- Thymeleaf
- Thymeleaf Extras SpringSecurity6
- Spring Boot DevTools
- MyBatis 3.0.5 (依存関係設定済み、現段階では未使用)
- H2 Database (依存関係設定済み、現段階では未使用)
- Server-Sent Events (SSE) によるリアルタイム通信
- Spring Async / Scheduling

## サーバー設定
- ポート番号: 80 (`application.properties` で設定)

## 現在のエンドポイント一覧
| パス | メソッド | 認可 | 説明 |
|------|----------|------|------|
| `/` | GET | 認証不要 | TOPページ (静的HTML) |
| `/login` | GET/POST | 認証不要(フォーム) | Spring Security 標準ログインフォーム |
| `/logout` | POST/GET | 認証済み時 | ログアウト後 `/` リダイレクト |
| `/home` | GET | 認証必須 | ホーム画面 (マッチング開始へのリンク) |
| `/match` | GET | 認証必須 | マッチング画面 (待機中プレイヤー一覧表示) |
| `/waitRoom` | GET (SSE) | 認証必須 | 待機室のプレイヤーリストをリアルタイム配信 |
| `/game/start` | GET | 認証必須 | ゲーム開始 (マッチング成立時) |
| `/game` | GET | 認証必須 | ゲーム画面表示 |
| `/game/move` | GET | 認証必須 | 駒の移動処理 (fromX, fromY, toX, toY パラメータ) |
| `/game/turn` | GET (SSE) | 認証必須 | ゲームターン情報をリアルタイム配信 |

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
- ホーム画面にアクセス時、待機室から自動的に削除される

### マッチング画面 (`tkk_game/src/main/resources/templates/match.html`)
- Thymeleaf テンプレート
- SSE により待機中のプレイヤー一覧をリアルタイム表示
- 他プレイヤーへの対戦リクエスト機能
- 対戦リクエストを受けた場合、自動的にゲーム画面へ遷移
- 自分自身は一覧から除外表示

### ゲーム画面 (`tkk_game/src/main/resources/templates/game.html`)
- Thymeleaf テンプレート
- 5×5の盤面を表示
- 駒の選択と移動が可能
- SSE によりターン情報をリアルタイム受信
- 自分のターンでない場合は待機状態

## ゲームロジック仕様

### 盤面 (`Ban` クラス)
- 5×5 のマス目で構成
- 座標系: 左下が (0, 0)、配列インデックスとの変換を内部で実施
- 初期配置:
  - 王 (0, -2): 全方向1マス移動可能
  - 飛 (2, 2): 縦横に最大4マス移動可能

### 駒 (`Koma` クラス)
- 駒名 (`name`) と移動パターン (`movePatterns`) を保持
- `canMove()` メソッドで移動可否を判定
- `KomaPattern` のセットで移動可能な相対座標を定義

### 駒の移動パターン (`KomaPattern` クラス)
- dx, dy で相対座標を表現
- `equals()` と `hashCode()` をオーバーライドし、Set で使用可能

### プレイヤー (`Player` クラス)
- プレイヤー名 (`name`) とステータス (`status`) を保持
- ステータス: `MATCHING`, `MATCHED`, `GAME_THINKING`, `GAME_WAITING`, `OFFLINE`

### ゲーム (`Game` クラス)
- ゲーム ID、2人のプレイヤー、盤面 (`Ban`)、最終アクティビティ時刻を管理
- `switchTurn()` でターンを切り替え
- 最終アクティビティ時刻を記録し、非アクティブなゲームの削除に使用

### ゲームルーム (`GameRoom` クラス)
- すべてのゲームを管理 (`ArrayList<Game>`)
- ゲームの追加、検索 (ID / プレイヤー名)
- 10分間アクティビティがないゲームを定期的に削除 (`@Scheduled`)

### 待機室 (`WaitRoom` クラス)
- マッチング待機中のプレイヤー名を管理 (`ArrayList<String>`)
- プレイヤーの追加・削除機能

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
- `/home`: ホーム画面表示、待機室から削除
- `/match`: マッチング画面表示、待機室に追加
- `/waitRoom`: SSE エンドポイント、待機室情報を配信

### `GameController`
- `/game/start`: ゲーム開始処理、マッチング成立時に呼び出される
- `/game`: ゲーム画面表示
- `/game/move`: 駒の移動処理、移動の妥当性チェック、ターン切り替え
- `/game/turn`: SSE エンドポイント、ターン情報を配信

## 非同期・スケジューリング設定
- `@EnableAsync`: 非同期処理を有効化
- `@EnableScheduling`: スケジュールタスクを有効化
- 定期実行タスク:
  - `GameRoom.rmGameNoActive()`: 10分ごとに非アクティブなゲームを削除

## ディレクトリ構成
| パス | 役割 |
|------|------|
| `tkk_game/src/main/java/team3/tkk_game/TkkGameApplication.java` | Spring Boot 起動クラス (`@EnableAsync`, `@EnableScheduling`) |
| `tkk_game/src/main/java/team3/tkk_game/security/SecurityConfig.java` | セキュリティ設定クラス |
| `tkk_game/src/main/java/team3/tkk_game/controller/MainController.java` | メインコントローラー (ホーム、マッチング) |
| `tkk_game/src/main/java/team3/tkk_game/controller/GameController.java` | ゲームコントローラー (ゲーム開始、移動) |
| `tkk_game/src/main/java/team3/tkk_game/model/Game.java` | ゲームモデル |
| `tkk_game/src/main/java/team3/tkk_game/model/Ban.java` | 盤面モデル |
| `tkk_game/src/main/java/team3/tkk_game/model/Koma.java` | 駒モデル |
| `tkk_game/src/main/java/team3/tkk_game/model/KomaPattern.java` | 駒の移動パターンモデル |
| `tkk_game/src/main/java/team3/tkk_game/model/Player.java` | プレイヤーモデル |
| `tkk_game/src/main/java/team3/tkk_game/model/PlayerStatus.java` | プレイヤーステータス列挙型 |
| `tkk_game/src/main/java/team3/tkk_game/model/GameRoom.java` | ゲーム管理モデル |
| `tkk_game/src/main/java/team3/tkk_game/model/WaitRoom.java` | 待機室管理モデル |
| `tkk_game/src/main/java/team3/tkk_game/services/TurnChecker.java` | ターンチェックサービス (SSE) |
| `tkk_game/src/main/java/team3/tkk_game/services/MatchChecker.java` | マッチングチェックサービス (SSE) |
| `tkk_game/src/main/resources/static/index.html` | トップページ (静的) |
| `tkk_game/src/main/resources/templates/home.html` | ホーム画面テンプレート |
| `tkk_game/src/main/resources/templates/match.html` | マッチング画面テンプレート |
| `tkk_game/src/main/resources/templates/game.html` | ゲーム画面テンプレート |
| `tkk_game/src/main/resources/templates/debug.html` | デバッグ用画面テンプレート |
| `tkk_game/src/main/resources/application.properties` | アプリケーション設定 (ポート: 80) |
| `tkk_game/build.gradle` | Gradle ビルド設定 |
| `docs/tasks.md` | 現在の作業計画 |
| `docs/reports/done/2025-11-11_ログイン機能最小実装.md` | 完了レポート (ログイン機能) |
| `docs/reports/investigate/2025-11-11_ログイン機能実装方法調査.md` | 調査レポート (ログイン機能) |

## 現状の実装状態
- ✅ ユーザー認証機能 (4ユーザー登録済み)
- ✅ マッチング機能 (SSE によるリアルタイム待機室)
- ✅ ゲーム開始機能 (2人マッチング)
- ✅ 盤面表示機能 (5×5)
- ✅ 駒の移動機能 (移動パターンチェック付き)
- ✅ ターン制御機能 (SSE によるリアルタイム更新)
- ✅ 非アクティブゲームの自動削除 (10分)
