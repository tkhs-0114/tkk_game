package team3.tkk_game.model;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.ArrayList;

@Component
public class GameRoom {
  ArrayList<Game> games = new ArrayList<>();

  public Game addGame(Game wait, String player2Name) {
    wait.setPlayer2(player2Name);
    games.add(wait);
    return wait;
  }

  public Game getGameById(String id) {
    for (Game game : games) {
      if (game.getId().equals(id)) {
        return game;
      }
    }
    return null;
  }

  public Game getGameByPlayerName(String playerName) {
    for (Game game : games) {
      String player1Name = game.getPlayer1().getName();
      String player2Name = game.getPlayer2().getName();
      if (player1Name.equals(playerName) || player2Name.equals(playerName)) {
        return game;
      }
    }
    return null;
  }

  public ArrayList<Game> getGames() {
    return games;
  }

  public boolean rmGameByName(String playerName) {
    for (Game game : games) {
      String player1Name = game.getPlayer1().getName();
      String player2Name = game.getPlayer2().getName();
      if (player1Name.equals(playerName) || player2Name.equals(playerName)) {
        if (game.getPlayerByName(playerName).getStatus() == PlayerStatus.GAME_THINKING || game.getPlayerByName(playerName).getStatus() == PlayerStatus.GAME_WAITING) {
          /*
            ここにペナルティ処理を書く
          */
        }
        games.remove(game);
        return true;
      }
    }
    return false;
  }

  @Scheduled(fixedRate = 600000)
  public void rmGameNoActive() {
    System.out.println("rmGameNoActive executed");
    long now = System.currentTimeMillis();
    // 10分 = 600000ミリ秒
    games.removeIf(game -> now - game.getLastActivity().getTime() > 600000);
  }
}
