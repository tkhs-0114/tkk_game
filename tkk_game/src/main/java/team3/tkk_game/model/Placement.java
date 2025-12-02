package team3.tkk_game.model;

public class Placement {
  private int file; // 1-5
  private int rank; // 1-5
  private String type; // P,L,N,S,G,...
  private String owner; // B or W

  public int getFile() { return file; }
  public void setFile(int file) { this.file = file; }
  public int getRank() { return rank; }
  public void setRank(int rank) { this.rank = rank; }
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public String getOwner() { return owner; }
  public void setOwner(String owner) { this.owner = owner; }
}
