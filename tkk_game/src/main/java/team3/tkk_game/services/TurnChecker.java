package team3.tkk_game.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import team3.tkk_game.model.Game;
import team3.tkk_game.model.Player;
import team3.tkk_game.model.PlayerStatus;

@Service
public class TurnChecker {

  @Async
  public void checkTurn(SseEmitter emitter, Game game, String playerName) {

    try {
      while (true) {
        Player player = game.getPlayerByName(playerName);
        emitter.send(player.getStatus() == PlayerStatus.GAME_THINKING ? true : false);
        Thread.sleep(1000);
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    } finally {
      emitter.complete();
    }
  }
}
