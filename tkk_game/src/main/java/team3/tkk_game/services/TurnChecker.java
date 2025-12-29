package team3.tkk_game.services;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import team3.tkk_game.model.Game;
import team3.tkk_game.model.Player;
import team3.tkk_game.model.PlayerStatus;

/**
 * ゲームターンの監視と通知を行うサービスクラス
 * イベント駆動型でターン変更を通知する
 */
@Service
public class TurnChecker {

  @Autowired
  private GameEventEmitterManager gameEventEmitterManager;

  /**
   * プレイヤーのSSE接続を登録し、初回のターン状態を送信する
   *
   * @param game       ゲームオブジェクト
   * @param playerName プレイヤー名
   * @return 登録されたSseEmitter
   */
  public SseEmitter registerTurnEmitter(Game game, String playerName) {
    // GameEventEmitterManagerにEmitterを登録
    SseEmitter emitter = gameEventEmitterManager.registerPlayerEmitter(playerName, game.getId());

    // 初回接続時に現在のターン状態を送信
    try {
      Player player = game.getPlayerByName(playerName);
      boolean isMyTurn = player.getStatus() == PlayerStatus.GAME_THINKING;
      boolean isGameEnd = game.getIsFinished();

      String response = String.format("{\"isMyTurn\":%b,\"isGameEnd\":%b}", isMyTurn, isGameEnd);
      emitter.send(response);
    } catch (IOException e) {
      emitter.complete();
    }

    return emitter;
  }
}
