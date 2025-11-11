# システム仕様書 (最新版)

## 最終更新日
2025-11-11

## システム概要
Spring Boot を用いた最小構成の Web アプリケーション。トップページ(`/`)表示と Spring Security のデフォルトログインフォーム(`/login`)による認証確認のみを目的とする初期段階。業務機能や保護対象ページは未実装。

## 使用技術 / バージョン
- Java 21 (Gradle Toolchain)
- Spring Boot 3.5.7
- Spring Security
- Spring Web
- Thymeleaf
- Thymeleaf Extras SpringSecurity6
- MyBatis (現段階で未使用 / 今後拡張余地)
- H2 Database (現段階で未使用 / 今後拡張余地)

## ビルド/起動方法
- 起動: `./gradlew bootRun`
- ポート: デフォルト 8080

## 現在のエンドポイント一覧
| パス | メソッド | 認可 | 説明 |
|------|----------|------|------|
| `/` | GET | 認証不要 | TOPページ: Hello World + ログインリンク |
| `/login` | GET/POST | 認証不要(フォーム) | Spring Security 標準ログインフォーム |
| `/logout` | POST/GET | 認証済み時 | ログアウト後 `/` リダイレクト |

現段階では `SecurityConfig` により全て `permitAll()` のため保護対象なし。

## 認証仕様
- 認証方式: フォームログイン (Spring Security デフォルト)
- ユーザー保管: インメモリ (`InMemoryUserDetailsManager`)
- ユーザー一覧:
  - `takahashi` / パスワード `p@ss` (BCryptハッシュ保存) / ロール `ROLE_USER`
- パスワードハッシュ: `{bcrypt}$2y$10$ngxCDmuVK1TaGchiYQfJ1OAKkd64IH6skGsNw1sLabrTICOHPxC0e`

## セキュリティ設定概要 (`tkk_game/src/main/java/team3/tkk_game/security/SecurityConfig.java`)
- `formLogin().permitAll()` 有効化
- `logout().logoutUrl("/logout").logoutSuccessUrl("/")`
- 全リクエスト `anyRequest().permitAll()`
- 目的が「ログイン動作確認」のみのため認可制御は未適用

## 画面仕様
### TOPページ `tkk_game/src/main/resources/static/index.html`
- 内容: `Hello World` 見出し
- 追加要素: `/login` へのリンク、利用可能な認証情報（ユーザー名/パスワード）表示
- テンプレートエンジン未使用 (静的HTML)

## 現状の制約
- ログイン後の視覚的変化がない（ユーザー名表示なし）
- 業務用コントローラー/DB利用機能未実装
- 認証保護パス未設定

## 今後の拡張予定（案）
1. `/game` など認証必須ページ追加と `requestMatchers` による認可設定
2. Thymeleaf テンプレート導入しログインユーザー名表示 (`sec:authentication` 利用)
3. ロール追加とロール別表示制御 (`hasRole`) の適用
4. H2/MyBatis によるユーザー永続化 + 独自 `UserDetailsService` 実装
5. CSRF設定の適正化（必要時に例外パス限定）

## ディレクトリ構成要点
| パス | 役割 |
|------|------|
| `tkk_game/src/main/java/team3/tkk_game/TkkGameApplication.java` | Spring Boot 起動クラス |
| `tkk_game/src/main/java/team3/tkk_game/security/SecurityConfig.java` | セキュリティ設定クラス (最小構成) |
| `tkk_game/src/main/resources/static/index.html` | トップページ (静的) |
| `docs/tasks.md` | 現在の作業計画 (最小ログイン) |
| `docs/reports/done/done_2025-11-11_ログイン機能最小実装.md` | 完了レポート |

## 状態サマリ
最小ログイン機能が稼働し、ユーザー `takahashi` による認証確認が可能な初期状態。保護対象ページや業務ロジックは未実装のため、次フェーズで機能拡張を行う。
