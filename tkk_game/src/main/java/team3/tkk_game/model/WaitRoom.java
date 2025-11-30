package team3.tkk_game.model;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WaitRoom {
  ArrayList<Player> waitRoom = new ArrayList<>();

  @Autowired
  PlayerList players;

  public WaitRoom() {
  }

  public ArrayList<Player> getWaitRoom() {
    return waitRoom;
  }

  public Boolean isInRoom(String playerName) {
    for (Player player : waitRoom) {
      if (player.getName().equals(playerName)) {
        return true;
      }
    }
    return false;
  }

  public void addPlayer(String playerName) {
    if (!isInRoom(playerName)) {
      Player player = players.getOrCreate(playerName);
      player.setStatus(PlayerStatus.WAITING);
      waitRoom.add(player);
    }
  }
  
  public void rmRoom(String playerName) {
    waitRoom.removeIf(player -> player.getName().equals(playerName));
  }

}
