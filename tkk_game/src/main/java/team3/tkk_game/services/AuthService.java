package team3.tkk_game.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import team3.tkk_game.mapper.PlayerDeckMapper;
import team3.tkk_game.mapper.PlayerKomaMapper;
import team3.tkk_game.model.PlayerDeck;
import team3.tkk_game.model.PlayerKoma;

/**
 * 認証関連のサービスクラス
 * ユーザー登録、初期データ作成を担当
 */
@Service
public class AuthService {

  @Autowired
  private InMemoryUserDetailsManager userDetailsManager;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private PlayerDeckMapper playerDeckMapper;

  @Autowired
  private PlayerKomaMapper playerKomaMapper;

  /** 共通デッキのID（王のみ） */
  private static final int COMMON_DECK_KING_ONLY = 1;

  /** 共通駒の最大ID */
  private static final int COMMON_KOMA_MAX_ID = 15;

  /**
   * ユーザーを登録する
   * 
   * @param username ユーザー名
   * @param password パスワード（平文）
   * @throws IllegalArgumentException ユーザー名が既に存在する場合
   */
  @Transactional
  public void registerUser(String username, String password) {
    // ユーザー名の重複チェック
    if (userDetailsManager.userExists(username)) {
      throw new IllegalArgumentException("このユーザー名は既に使用されています");
    }

    // DBにユーザー名が存在するかチェック
    Integer existingPlayerId = playerDeckMapper.selectPlayerIdByUsername(username);
    if (existingPlayerId != null) {
      throw new IllegalArgumentException("このユーザー名は既に使用されています");
    }

    // 1. InMemoryにユーザーを追加（パスワードをハッシュ化）
    UserDetails newUser = User.withUsername(username)
        .password(passwordEncoder.encode(password))
        .roles("USER")
        .build();
    userDetailsManager.createUser(newUser);

    // 2. DBにPlayerレコードを作成
    playerDeckMapper.insertPlayer(username);
    Integer playerId = playerDeckMapper.selectPlayerIdByUsername(username);

    // 3. 初期デッキを作成（王のみのデッキ）
    // Deck initialDeck = new Deck();
    // initialDeck.setName(username + "の初期デッキ");
    // initialDeck.setSfen("5/2[0]2");  // 王のみ
    // initialDeck.setCost(8);  // 王のコスト
    // deckMapper.insertDeck(initialDeck);

    // 4. 初期デッキをプレイヤーに紐付け（所有者として）
    // PlayerDeck playerDeck = new PlayerDeck();
    // playerDeck.setPlayerId(playerId);
    // playerDeck.setDeckId(initialDeck.getId());
    // playerDeck.setIsOwner(true);
    // playerDeckMapper.insertPlayerDeck(playerDeck);

    // 5. 共通デッキをプレイヤーに紐付け
    PlayerDeck commonDeck1 = new PlayerDeck();
    commonDeck1.setPlayerId(playerId);
    commonDeck1.setDeckId(COMMON_DECK_KING_ONLY);
    commonDeck1.setIsOwner(false);
    playerDeckMapper.insertPlayerDeck(commonDeck1);

    // 6. 共通駒をプレイヤーに紐付け
    for (int komaId = 0; komaId <= COMMON_KOMA_MAX_ID; komaId++) {
      PlayerKoma playerKoma = new PlayerKoma();
      playerKoma.setPlayerId(playerId);
      playerKoma.setKomaId(komaId);
      playerKoma.setIsOwner(false);
      playerKomaMapper.insertPlayerKoma(playerKoma);
    }

    // 7. 選択中のデッキを共通デッキに設定
    playerDeckMapper.updateSelectedDeckId(playerId, COMMON_DECK_KING_ONLY);
  }

  /**
   * ユーザー名が使用可能かチェック
   * 
   * @param username ユーザー名
   * @return 使用可能ならtrue
   */
  public boolean isUsernameAvailable(String username) {
    if (userDetailsManager.userExists(username)) {
      return false;
    }
    Integer existingPlayerId = playerDeckMapper.selectPlayerIdByUsername(username);
    return existingPlayerId == null;
  }
}
