package team3.tkk_game.model;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.ArrayList;

@Component
public class GameRoom {
  ArrayList<Game> games = new ArrayList<>();

  public Game addGame(String player1Name, String player2Name) {
    String id = String.valueOf(System.currentTimeMillis());
    Game newGame = new Game(id, player1Name, player2Name);
    games.add(newGame);
    return newGame;
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

  public Game inGamePlayer2(String playerName) {
    for (Game game : games) {
      Player player2 = game.getPlayer2();
      if (player2.getName().equals(playerName) && player2.getStatus() == PlayerStatus.MATCHED) {
        player2.setStatus(PlayerStatus.GAME_WAITING);
        game.getPlayer1().setStatus(PlayerStatus.GAME_THINKING);
        return game;
      }
    }
    return null;
  }

  @Scheduled(fixedRate = 600000)
  public void rmGameNoActive() {
    System.out.println("rmGameNoActive executed");
    long now = System.currentTimeMillis();
    // 10分 = 600000ミリ秒
    games.removeIf(game -> now - game.getLastActivity().getTime() > 600000);
  }
}
