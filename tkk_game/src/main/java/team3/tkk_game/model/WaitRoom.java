package team3.tkk_game.model;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

@Component
public class WaitRoom {
  ArrayList<String> waitRoom = new ArrayList<>();

  public WaitRoom() {
  }

  public ArrayList<String> getWaitRoom() {
    return waitRoom;
  }

  public Boolean isInRoom(String playerName) {
    for (String name : waitRoom) {
      if (name.equals(playerName)) {
        return true;
      }
    }
    return false;
  }

  public void addPlayer(String playerName) {
    if (!isInRoom(playerName)) {
      waitRoom.add(playerName);
    }
  }

  public void rmPlayer(String playerName) {
    waitRoom.removeIf(name -> name.equals(playerName));
  }

}
