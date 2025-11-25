package team3.tkk_game.model;

import org.springframework.stereotype.Component;
import java.util.ArrayList;

@Component
public class GameRoom {
  ArrayList<Game> games = new ArrayList<>();

  public String addGame(String player1Name, String player2Name) {
    String id = String.valueOf(System.currentTimeMillis());
    games.add(new Game(id, player1Name, player2Name));
    return id;
  }

  public String inGamePlayer2(String playerName) {
    for (Game game : games) {
      if (game.getPlayer2().equals(playerName) && game.getPlayer2Status() == PlayerStatus.WAITING) {
        game.setPlayer2Status(PlayerStatus.IN_GAME);
        return game.getId();
      }
    }
    return null;
  }
}
