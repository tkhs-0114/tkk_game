# レビューレポート: デッキ機能実装（簡易）

## レビュー日
2025-12-02

## 対象ブランチ/範囲（作業実装）
- `tkk_game` モジュール内の以下主要実装
  - コントローラ: `GameController`（画面遷移 + 一部 REST 統合）
  - サービス: `DeckService`（SFEN 生成/解析）
  - マッパー: `DeckMapper`, `KomaMapper`（MyBatis）
  - テンプレート: `deckmake.html`, `deckchoose.html`, `home.html`
  - 設定: `application.properties`, `SecurityConfig`（開発用変更あり）
  - 初期データ: `schema.sql`, `data.sql`

## 全体所見（短く）
開発目的に沿って、駒一覧取得・デッキの生成・保存・一覧表示・選択 UI が動作する形で実装されています。SFEN ロジックがサービスとして分離されており、DB 永続化も MyBatis で実装されているため、今後の拡張に向けた基盤は整っています。一方で、責務分離、セキュリティ設定、起動/初期化設定などに開発時の暫定対応が残っており、運用前に整備が必要です。

## 良い点
- Deck の SFEN 生成/解析ロジックを `DeckService` に切り出している（単体テストしやすい）。
- MyBatis による DB マッパーを利用して永続化がシンプルに実装されている。
- クライアント側で駒選択・盤面編集 UI が用意され、保存フローが一通り動作する。
- docs を更新して実装記録・仕様を残している（再現性がある）。

## 問題点・懸念（優先度付）
1. コントローラの責務混在（高）
   - `GameController` にページ遷移と REST API（/api/*）が混在している。既存の `DeckController`/`KomaController` の掃除痕があり、構造が分かりにくい。
3. DB 初期化の扱い（中）
   - `spring.sql.init.mode=always` が有効で、起動時に data.sql を再投入する。永続 DB を使う場合は上書きリスク。
4. エラーハンドリングと入力検証（中）
   - `POST /api/decks` の入力バリデーションが弱く、DB 一意制約違反などで500が返る場合がある。クライアント側でのエラー表示はあるが、API 側の安定化が必要。
5. 環境依存の文字コード問題（中）
   - 起動環境で `_JAVA_OPTIONS` により `file.encoding` が変わる事象が発生。build 側での固定化やドキュメント化を推奨。
6. テスト不足（中）
   - `DeckService` のユニットテストが未実装。生成/解析の整合性検証が必要。

## 優先度付き推奨対応（短期→中期）
優先(高)
- R1: REST 責務を分離する（推奨）
  - 実施内容: `DeckRestController` と `KomaRestController` を `@RestController` として新設し、`GameController` から削除。空の旧コントローラは削除。
  - 理由: 読みやすさ、認可設定の適用容易化、単体テストの分離。
- R2: SecurityConfig を本番想定に戻し、API 毎に明示的な許可設定を行う
  - 例: `/api/koma` を匿名許可するなら `requestMatchers(HttpMethod.GET, "/api/koma").permitAll()` を設定。
- R3: `spring.sql.init.mode` を dev プロファイルへ移動 / Flyway 導入検討

中期
- R4: DeckService のユニットテスト追加（generate/parse round-trip）
- R5: API 入力バリデーションと ControllerAdvice による一元的なエラーハンドリング
- R6: build.gradle に `bootRun` の `jvmArgs = ['-Dfile.encoding=UTF-8']` を追加またはドキュメント化

低
- R7: OpenAPI ドキュメント作成、UI の UX 改善（盤面全体表示、ドラッグ）

## 具体的変更案（実行可能なコマンド/編集）
- 編集: 新規 `src/main/java/team3/tkk_game/controller/DeckRestController.java`、`KomaRestController.java` を作成。GameController 内の該当 API を削除。既存の空クラスを削除。
- 編集: `src/main/java/team3/tkk_game/security/SecurityConfig.java` を修正し、`csrf()` を有効のままにして API 単位で `permitAll()` を設定。
- 編集: `src/main/resources/application.properties` の `spring.sql.init.mode=always` を `application-dev.properties` に移動し、`application.properties` では無効にする。
- 追加: `src/test/java/.../DeckServiceTest.java` に基本的なユニットテストを追加。

## 影響範囲/注意事項
- REST 分離時は既存のフロント（`deckmake.html` 等）が `/api/*` エンドポイントを参照しているためパス変更は不要。認証ポリシー変更するとフロントの動作確認が必要。
- DB マイグレーション方式変更（Flyway）を導入する際は `data.sql` の取り扱いを整理する必要あり。

## 結論（短く）
機能面は要件を満たしており、基盤として問題なく使えるが「コードの構成」と「セキュリティ/運用設定」に開発時の暫定対処が残っているため、優先的に REST 責務の分離と SecurityConfig の整理を実施してください。これらを適用すればリリース候補としての品質に近づきます。

---

作業指示があれば自動で編集・テスト雛形追加などを行います。希望を番号で指示してください: 1=REST分離自動実施, 2=SecurityConfig 本番化修正, 3=DeckService テスト追加, 4=まとめて 1→2→3。
