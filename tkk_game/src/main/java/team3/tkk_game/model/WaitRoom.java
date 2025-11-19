package team3.tkk_game.model;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

@Component
public class WaitRoom {
  ArrayList<String> players = new ArrayList<>();

  public WaitRoom() {
  }

  public ArrayList<String> getPlayers() {
    return players;
  }

  public void addPlayer(String player) {
    players.add(player);
  }

  public void rmPlayer(String player) {
    players.remove(player);
  }

}
