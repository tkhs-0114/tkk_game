package team3.tkk_game.model;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

@Component
public class WaitRoom {
  ArrayList<Game> waitRoom = new ArrayList<>();
  
  public WaitRoom() {
  }

  public ArrayList<Game> getWaitRoom() {
    return waitRoom;
  }

  public Boolean isInRoom(String playerName) {
    for (Game game : waitRoom) {
      if (game.getPlayer1().getName().equals(playerName)) {
        return true;
      }
    }
    return false;
  }

  public void addWaitRoom(String playerName) {
    if (!isInRoom(playerName)) {
      String id = String.valueOf(System.currentTimeMillis());
      Game game = new Game(id, playerName);
      waitRoom.add(game);
    }
  }

  public Game getRoomByName(String playerName) {
    for (Game game : waitRoom) {
      if (game.getPlayer1().getName().equals(playerName)) {
        return game;
      }
    }
    return null;
  }

  public void rmRoom(String playerName) {
    waitRoom.removeIf(game -> game.getPlayer1().getName().equals(playerName));
  }

}
