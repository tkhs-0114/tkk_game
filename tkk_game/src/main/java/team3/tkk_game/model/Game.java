package team3.tkk_game.model;

public class Game {
  String id;
  Player player1;
  Player player2;

  public Game(String id, String player1Name, String player2Name) {
    this.id = id;
    this.player1 = new Player(player1Name, PlayerStatus.IN_GAME);
    this.player2 = new Player(player2Name, PlayerStatus.WAITING);
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
    this.player2.setStatus(status);
  }
}
