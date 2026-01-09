package team3.tkk_game.model;

/**
 * プレイヤーが使用可能なデッキを管理するモデル
 */
public class PlayerDeck {
  private Integer id;
  private Integer playerId;
  private Integer deckId;
  private Boolean isOwner;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getPlayerId() {
    return playerId;
  }

  public void setPlayerId(Integer playerId) {
    this.playerId = playerId;
  }

  public Integer getDeckId() {
    return deckId;
  }

  public void setDeckId(Integer deckId) {
    this.deckId = deckId;
  }

  public Boolean getIsOwner() {
    return isOwner;
  }

  public void setIsOwner(Boolean isOwner) {
    this.isOwner = isOwner;
  }
}
