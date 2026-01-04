package team3.tkk_game.model;

import java.util.Date;
import java.util.ArrayList;
import team3.tkk_game.model.Koma.Koma;

public class Game {
  // デバッグ用にpublicに変更
  String id;
  Date lastActivity;
  Player player1;
  Player player2;
  Ban ban;
  Ban displayBan;
  ArrayList<Koma> haveKoma1;
  ArrayList<Koma> haveKoma2;

  public Game(String id, String player1Name) {
    this.id = id;
    this.player1 = new Player(player1Name, PlayerStatus.WAITING);
    this.lastActivity = new Date();
    this.ban = new Ban();
    this.displayBan = new Ban();
    this.haveKoma1 = new ArrayList<Koma>();
    this.haveKoma2 = new ArrayList<Koma>();
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
    this.player2 = new Player(player2Name, PlayerStatus.GAME_STARTING);
  }

  public void clearPlayer2() {
    this.player2 = null;
  }

  public Ban getBan() {
    return ban;
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

  public ArrayList<Koma> getHaveKomaByName(String playerName) {
    if (player1.getName().equals(playerName)) {
      return haveKoma1;
    } else if (player2.getName().equals(playerName)) {
      return haveKoma2;
    } else {
      return null;
    }
  }

  public ArrayList<Koma> getEHaveKomaByName(String playerName) {
    if (player1.getName().equals(playerName)) {
      return haveKoma2;
    } else if (player2.getName().equals(playerName)) {
      return haveKoma1;
    } else {
      return null;
    }
  }

  public void addHaveKomaByName(String playerName, Koma koma) {
    if (player1.getName().equals(playerName)) {
      haveKoma1.add(getHaveKomaIndex(haveKoma1, koma), koma);
    } else if (player2.getName().equals(playerName)) {
      haveKoma2.add(getHaveKomaIndex(haveKoma2, koma), koma);
    }
  }

  private int getHaveKomaIndex(ArrayList<Koma> haveKoma, Koma koma) {
    if (haveKoma.isEmpty())
      return 0;
    for (int i = 0; i < haveKoma.size(); i++) {
      if (haveKoma.get(i).getId() > koma.getId())
        return i;
    }
    return haveKoma.size();
  }

  public void switchTurn() {
    updateLastActivity();
    // 盤面を相手視点に回転
    this.displayBan.rotate180();
    this.ban.rotate180();
    // プレイヤーステータスを入れ替え
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
