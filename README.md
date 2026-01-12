# 創棋(しょうぎ)

## ユーザーマニュアル

こちらからご覧になれます

## セットアップ

最低条件:Java21のインストール<br>
想定環境:UNIXベースのコマンドライン

### 利用者向け
1. [こちら](https://github.com/tkhs-0114/tkk_game/releases)から`tkk_game-【version番号】.jar`をダウンロードします
2. ダウンロードしたフォルダ内で`java -jar tkk_game-【version番号】.jar`というコマンドを実行します
3. ポート80番で開かれるのであとは接続するだけです

### 開発者向け
1. `git clone https://github.com/tkhs-0114/tkk_game.git`もしくは<br>`git clone git@github.com:tkhs-0114/tkk_game.git`でプロジェクトをダウンロードします
2. プロジェクト内の`/tkk_game/tkk_game`内で`gradle bootrun`もしくは`bash ./gradlew bootrun`を実行します
3. ポート80番で開かれるのであとは接続するだけです

## 使用技術 / バージョン
- Java 21 (Gradle Toolchain)
- Spring Boot 3.5.7
- Spring Security
- Spring Web
- Thymeleaf
- Thymeleaf Extras SpringSecurity6
- Spring Boot DevTools
- MyBatis 3.0.5
- H2 Database
- Server-Sent Events
- Spring Async / Scheduling

詳しい仕様は[こちら](https://github.com/tkhs-0114/tkk_game/blob/main/docs/specs.md)から