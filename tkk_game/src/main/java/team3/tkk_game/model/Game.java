package team3.tkk_game.model;

import java.util.Date;

import team3.tkk_game.model.Ban.LocalBan;
import team3.tkk_game.model.Ban.DisplayBan;

public class Game {
  // デバッグ用にpublicに変更
  public String id;
  public Date lastActivity;
  Player player1;
  Player player2;
  LocalBan player1Ban;
  LocalBan player2Ban;
  DisplayBan displayBan;

  public Game(String id, String player1Name) {
    this.id = id;
    this.player1 = new Player(player1Name, PlayerStatus.GAME_WAITING);
    this.lastActivity = new Date();
  }

  public void init_game() {
    this.displayBan = new DisplayBan();
    this.player1Ban = new LocalBan(this.player1);
    this.player2Ban = new LocalBan(this.player2);
    
    // 初期配置の駒を設定
    initKoma();
  }

  private void initKoma() {
    // player1の王を配置
    Koma player1King = new Koma("王", new KomaPattern[] {
        new KomaPattern(1, 0),
        new KomaPattern(0, 1),
        new KomaPattern(-1, 0),
        new KomaPattern(0, -1),
        new KomaPattern(1, 1),
        new KomaPattern(1, -1),
        new KomaPattern(-1, 1),
        new KomaPattern(-1, -1)
    }, player1);
    player1Ban.setKoma(0, 2, player1King);
    displayBan.setKoma(0, 2, player1King);
    
    // player2の王を配置
    Koma player2King = new Koma("玉", new KomaPattern[] {
        new KomaPattern(1, 0),
        new KomaPattern(0, 1),
        new KomaPattern(-1, 0),
        new KomaPattern(0, -1),
        new KomaPattern(1, 1),
        new KomaPattern(1, -1),
        new KomaPattern(-1, 1),
        new KomaPattern(-1, -1)
    }, player2);
    player2Ban.setKoma(0, -2, player2King);
    displayBan.setKoma(0, -2, player2King);
  }

  public String getId() {
    return id;
  }

  public Player getPlayer1() {
    return player1;
  }

  public Player getPlayer2() {
    return player2;
  }

  public void setPlayer2(String player2Name) {
    this.player2 = new Player(player2Name, PlayerStatus.GAME_THINKING);
  }

  public void clearPlayer2() {
    this.player2 = null;
  }

  public Player getPlayerByName(String playerName) {
    if (player1.getName().equals(playerName)) {
      return player1;
    } else if (player2.getName().equals(playerName)) {
      return player2;
    } else {
      return null;
    }
  }

  public LocalBan getLocalBan(String playerName) {
    if (player1Ban.getOwner().getName().equals(playerName)) {
      return player1Ban;
    } else if (player2Ban.getOwner().getName().equals(playerName)) {
      return player2Ban;
    }
    return null;
  }

  public DisplayBan getDisplayBan() {
    return displayBan;
  }

  public void switchTurn() {
    updateLastActivity();
    if (player1.getStatus() == PlayerStatus.GAME_THINKING) {
      player1.setStatus(PlayerStatus.GAME_WAITING);
      player2.setStatus(PlayerStatus.GAME_THINKING);
    } else if (player2.getStatus() == PlayerStatus.GAME_THINKING) {
      player2.setStatus(PlayerStatus.GAME_WAITING);
      player1.setStatus(PlayerStatus.GAME_THINKING);
    }
  }

  // 用修正、SSE時に更新処理として動作させるようにすることを検討中
  private void updateLastActivity() {
    this.lastActivity = new Date();
  }

  public Date getLastActivity() {
    return lastActivity;
  }
}
