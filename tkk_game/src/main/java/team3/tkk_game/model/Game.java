package team3.tkk_game.model;

import java.util.Date;

public class Game {
  // デバッグ用にpublicに変更
  public String id;
  public Date lastActivity;
  Player player1;
  Player player2;

  public Game(String id, String player1Name, String player2Name) {
    this.id = id;
    this.player1 = new Player(player1Name, PlayerStatus.IN_GAME);
    this.player2 = new Player(player2Name, PlayerStatus.WAITING);
    this.lastActivity = new Date();
  }

  public String getId() {
    return id;
  }

  public String getPlayer1() {
    return player1.getName();
  }

  public String getPlayer2() {
    return player2.getName();
  }

  public void setPlayer2Status(PlayerStatus status) {
    this.updateLastActivity();
    this.player2.setStatus(status);
  }

  public PlayerStatus getPlayer2Status() {
    return player2.getStatus();
  }

  public void updateLastActivity() {
    this.lastActivity = new Date();
  }

  public Date getLastActivity() {
    return lastActivity;
  }
}
