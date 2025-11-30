package team3.tkk_game.model;

import java.util.ArrayList;
import org.springframework.stereotype.Component;

/**
 * プレイヤーオブジェクトを一元管理するリポジトリ
 * 同じ名前のプレイヤーに対して常に同じインスタンスを返す
 */
@Component
public class PlayerList {
  
  private ArrayList<Player> players = new ArrayList<>();

  public Player getPlayerbyName(String playerName) {
    for (Player player : players) {
      if (player.getName().equals(playerName)) {
        return player;
      }
    }
    return null;
  }

  public Player getOrCreate(String playerName) {
    Player player = getPlayerbyName(playerName);
    if (player == null) {
      player = new Player(playerName, PlayerStatus.OFFLINE);
      players.add(player);
    }
    return player;
  }

  public boolean setStatus(String playerName, PlayerStatus status) {
    Player player = getPlayerbyName(playerName);
    if (player != null) {
      player.setStatus(status);
      return true;
    }
    return false;
  }
}
