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
    /*
    this.player2 = new Player(player2Name, PlayerStatus.MATCHED);
    this.player1Ban = new LocalBan(this.player1);
    this.player2Ban = new LocalBan(this.player2);
    this.displayBan = new DisplayBan();
    this.ban = new Ban();
    */
    this.lastActivity = new Date();
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

  public DisplayBan getBan() {
    return displayBan;
  }

  public void moveKomaByPlayer(String playerName, int fromX, int fromY, int toX, int toY) {
    Player loginPlayer = getPlayerByName(playerName);
    if (loginPlayer == null) {
      throw new IllegalArgumentException("その名前のプレイヤーは存在しません");
    }
    if (loginPlayer.getStatus() != PlayerStatus.GAME_THINKING) {
      throw new IllegalStateException("現在のターンではありません");
    }
    boolean isSuccess;
    if (player1.getName().equals(playerName)) {
      isSuccess = player1Ban.moveKoma(fromX, fromY, toX, toY);
    } else {
      isSuccess = player2Ban.moveKoma(fromX, fromY, toX, toY);
    }
    if (!isSuccess) {
      throw new IllegalStateException("移動に失敗しました");
    }
    // 表示盤面も更新
    switchTurn();
  }

  private void switchTurn() {
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
