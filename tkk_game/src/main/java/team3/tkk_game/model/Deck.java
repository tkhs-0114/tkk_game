package team3.tkk_game.model;

public class Deck {
  int id;
  String name;
  String sfen;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSfen() {
    return sfen;
  }

  public void setSfen(String sfen) {
    this.sfen = sfen;
  }
}
