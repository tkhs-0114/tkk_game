package team3.tkk_game.model;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

@Component
public class WaitRoom {
  ArrayList<String> players;

  public WaitRoom() {
  }

  public ArrayList<String> getPlayers() {
    return players;
  }

  public void setPlayers(ArrayList<String> players) {
    this.players = players;
  }

  public void addPlayer(String player) {
    if (players == null) {
      players = new ArrayList<>();
    }
    players.add(player);
  }

  public void rmPlayer(String player) {
    if (players != null) {
      players.remove(player);
    }
  }

}
