package team3.tkk_game.model;

public class Player {
  String name;
  PlayerStatus status;

  public Player(String name, PlayerStatus status) {
    this.name = name;
    this.status = status;
  }

  public String getName() {
    return name;
  }

  public void setStatus(PlayerStatus status) {
    this.status = status;
  }
}
