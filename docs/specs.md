# システム仕様書（全体）

## 1. 基本情報
- プロジェクト名: tkk_game
- 最終更新日: 2025-12-02
- 開発言語/実行環境: Java 21 (Gradle)、Spring Boot 3.5.x
- 目的: Web ベースの簡易対戦ゲーム基盤。認証付き画面、駒/デッキ管理、簡易マッチングを提供する開発試作。

## 2. アーキテクチャ概要
- レイヤ構成: Controller (Spring MVC) / Service / Mapper(MyBatis) / Model / Templates(Thymeleaf)
- DB: H2（開発用、schema.sql/data.sql による初期化）
- ビルド: Gradle（`./gradlew bootRun` で起動）

## 3. 主要コンポーネント
- Web: Spring MVC + Thymeleaf テンプレート
- 認証: Spring Security（フォームログイン、InMemoryUserDetailsManager、開発用ユーザ）
- 永続化: MyBatis mapper（`KomaMapper`, `DeckMapper`）、H2
- ビジネスロジック: `DeckService`（5×5 用 SFEN 生成/解析）

## 4. 実装済みエンドポイント（概要）
- 画面（HTML）
  - GET / (static/index.html) : トップページ（未ログイン向け）
  - GET /login : Spring 標準ログインフォーム
  - GET /home : ホーム画面（ログイン後）
  - GET /deckmake : デッキ作成画面（簡易UI、クライアント側で盤面編集）
  - GET /deckchoose : サーバ上のデッキ一覧から選ぶ画面
  - GET /match, /gameStart, /waitRoom : マッチング・ゲーム開始関連ページ

- REST API (JSON)
  - GET /api/koma : 駒一覧を返す（{id,name,type} リスト）
  - GET /api/decks : デッキ一覧を返す（{id,name,sfen} リスト）
  - GET /api/decks/{id} : 単一デッキを返す
  - POST /api/decks : placements を受け取り SFEN を生成して Deck を保存（戻り値に生成 id を含む Deck）

注: これらは開発向けに実装されています。認可・CSRF 設定は現状開発用の扱いがあります。

## 5. データモデル（簡易）
- テーブル: koma
  - id INT PRIMARY KEY AUTO_INCREMENT
  - name VARCHAR(50)
  - type VARCHAR(1)  // 例: P,L,N,S,G,B,R,K

- テーブル: Deck
  - id INT PRIMARY KEY AUTO_INCREMENT
  - name VARCHAR(255)
  - sfen VARCHAR(255)  // 5×5 の簡易 SFEN（例 `5/5/5/5/5`）

- サーバ内部モデル
  - team3.tkk_game.model.Placement: file:int, rank:int, type:String, owner:String
  - team3.tkk_game.model.Deck: id,Integer name,String sfen,String

## 6. 文字エンコーディング・起動注意
- 開発時に日本語が化ける原因として JVM 環境変数 `_JAVA_OPTIONS=-Dfile.encoding=SHIFT-JIS` に注意（Windows 環境）。起動時に `-Dfile.encoding=UTF-8` を指定するか環境変数を解除すること。
- `application.properties` に HTTP/Thymeleaf のエンコーディング（UTF-8）を設定している。

## 7. セキュリティ
- デフォルト: フォームログイン + InMemoryUserDetailsManager
- CSRF: デフォルトでは有効。POST API をフロントから呼ぶ場合は CSRF トークンを送る必要がある。
- 開発で一時的に CSRF/認証を緩めた履歴あり。本番移行前に必ず確認・固定化すること。

## 8. デプロイ・運用（開発向けメモ）
- 開発: `spring.sql.init.mode=always` により起動時に schema/data を再初期化している（ローカル検証用）。本番では無効化すること。
- 本番DB移行: Flyway 等でマイグレーション管理を導入すること。

## 9. テスト戦略
- 単体テスト
  - `DeckService.generateSfen` と `parseSfen` の正逆変換テストを必須。
- 結合テスト
  - REST API の認可・CSRF を含むエンドツーエンドテスト（MockMvc）を整備する。

## 10. 保守・リファクタリング提案（優先度付き）
1. REST API と画面コントローラを責務で分離（`KomaController` 等を作成）。
2. Deck バリデーションと重複チェックを追加（POST /api/decks）。
3. `sfen` 型は将来拡張のため TEXT に変更検討。
4. Flyway で DB マイグレーション導入。
5. CI でユニット・統合テストを実行するパイプライン構築。

## 11. 変更履歴（簡易）
- 2025-12-02: デッキ作成・保存・一覧・選択 UI を実装。DeckService, DeckMapper, KomaMapper を追加。application.properties に開発用エンコーディング/初期化設定を追加。

---

必要ならこの仕様書から実装チケット（JIRA / GitHub Issues 形式）を生成します。どれを作成しますか？（例: `DeckService unit tests`, `API 認可設定`）
