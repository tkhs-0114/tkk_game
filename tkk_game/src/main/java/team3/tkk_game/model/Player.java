package team3.tkk_game.model;

public class Player {
  String name;
  PlayerStatus status;
  String username;
  Integer selectedDeckId;

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

  public PlayerStatus getStatus() {
    return status;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Integer getSelectedDeckId() {
    return selectedDeckId;
  }

  public void setSelectedDeckId(Integer selectedDeckId) {
    this.selectedDeckId = selectedDeckId;
  }

}
