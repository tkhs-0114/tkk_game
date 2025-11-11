# ログイン機能最小実装計画

## 計画作成日
2025年11月11日

## 目的
TOPページ（`/`）からSpring Securityのデフォルトログインフォーム（`/login`）へ遷移し、ユーザー名「takahashi」、パスワード「p@ss」、ロール「USER」でログインできる最小限の仕組みのみを提供する。

## 前提条件
- `build.gradle`にSpring Security関連依存関係が追加済み
- 現在のTOPページ: `tkk_game/src/main/resources/static/index.html`
- 追加のコントローラーやテンプレートは不要（デフォルトログインフォームを使用）

## スコープ（含む）
- セキュリティ設定クラスの作成
- TOPページにログインフォームへのリンク追加

## スコープ（含まない）
- 業務用コントローラーの作成
- 独自ログイン画面の作成
- ロール別画面表示
- ゲーム画面等の機能

---
### タスク1: セキュリティ設定クラスの作成
**関連ファイル**: 新規作成 `tkk_game/src/main/java/team3/tkk_game/security/SecurityConfig.java`

**作業内容**:
1. `security`パッケージを作成
2. `SecurityConfig`クラスを作成し`@Configuration` `@EnableWebSecurity`を付与
3. `SecurityFilterChain` Bean を定義
   - `formLogin().permitAll()` を設定
   - `logout()` を設定（`/logout` → 成功時 `/` リダイレクト）
   - ひとまず全パス `permitAll()`（ログイン導線確認が目的のため）
4. `InMemoryUserDetailsManager` Bean を定義
   - ユーザー: `takahashi`
   - パスワード (BCrypt): `{bcrypt}$2y$10$ngxCDmuVK1TaGchiYQfJ1OAKkd64IH6skGsNw1sLabrTICOHPxC0e`
   - ロール: `USER`

**確認手順**:
- `./gradlew bootRun` で起動
- ブラウザで `http://localhost:8080/login` にアクセスしログインフォーム表示を確認
- `takahashi / p@ss` でログイン → エラーなしで認証成功（直前アクセスが`/login`のみの場合 TOP `/`へ遷移）

**期待結果**:
- デフォルトログインフォームが表示され、指定ユーザーでログイン成功する

---
### タスク2: TOPページにログイン導線追加
**関連ファイル**: 編集 `tkk_game/src/main/resources/static/index.html`

**作業内容**:
1. `/login` へのリンクを追加: 例 `<a href="/login">ログイン</a>`
2. 簡単な説明文を追加（「ユーザー: takahashi / パスワード: p@ss」）

**確認手順**:
- ブラウザで `http://localhost:8080/` を表示
- 追加したリンクをクリック → `/login` に遷移しフォーム表示
- 認証成功後 `/` に戻る（もしくはデフォルトの遷移）

**期待結果**:
- TOPページからログインフォームへ遷移でき、ログイン後再びTOPページが閲覧可能

---
## Definition of Done (DoD)
1. `./gradlew bootRun` でアプリが正常起動する
2. `http://localhost:8080/` にアクセスしログインリンクが表示される
3. リンククリックでデフォルトログインフォーム表示
4. `takahashi / p@ss` でログイン成功し画面遷移が完了する
5. `/logout` 実行後再度 `/login` からログイン可能

## 注意事項
- 追加の業務機能や画面を実装しないこと
- ログイン成功のみを最小確認対象とする
- 今後拡張する場合は別途計画を再作成すること
