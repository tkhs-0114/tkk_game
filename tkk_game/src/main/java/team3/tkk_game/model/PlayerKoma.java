package team3.tkk_game.model;

/**
 * プレイヤーが使用可能な駒を管理するモデル
 */
public class PlayerKoma {
  private Integer id;
  private Integer playerId;
  private Integer komaId;
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

  public Integer getKomaId() {
    return komaId;
  }

  public void setKomaId(Integer komaId) {
    this.komaId = komaId;
  }

  public Boolean getIsOwner() {
    return isOwner;
  }

  public void setIsOwner(Boolean isOwner) {
    this.isOwner = isOwner;
  }
}
