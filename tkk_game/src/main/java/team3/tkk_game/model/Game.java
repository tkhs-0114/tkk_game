package team3.tkk_game.model;

import java.util.Date;

public class Game {
  // デバッグ用にpublicに変更
  public String id;
  public Date lastActivity;
  Player player1;
  Player player2;
  Ban Ban;
  Ban displayBan;

  public Game(String id, String player1Name) {
    this.id = id;
    this.player1 = new Player(player1Name, PlayerStatus.GAME_WAITING);
    this.lastActivity = new Date();
    this.Ban = new Ban();
    this.displayBan = new Ban();
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

  public Ban getBan() {
    return Ban;
  }

  public Ban getDisplayBan() {
    return displayBan;
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
