package team3.tkk_game.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import team3.tkk_game.model.Game;
import team3.tkk_game.model.GameRoom;

/**
 * プレイヤーの切断を一元的に処理するサービスクラス
 * ブラウザクローズ、他サイトへの遷移、ネットワーク切断等の切断を検知・処理する
 */
@Service
public class DisconnectionHandler {

  private final Logger logger = LoggerFactory.getLogger(DisconnectionHandler.class);

  @Autowired
  private GameRoom gameRoom;

  @Autowired
  private GameEventEmitterManager gameEventEmitterManager;


  /**
   * プレイヤーが切断した時の処理（ゲーム中の切断）
   * 
   * @param playerName 切断したプレイヤー名
   * @param reason     切断理由（"INTENTIONAL", "NETWORK_ERROR", "TIMEOUT" 等）
   */
  public void handlePlayerDisconnection(String playerName, String reason) {
    logger.info("Player disconnection detected: {} (reason: {})", playerName, reason);

    // 1. ゲームの取得
    Game game = gameRoom.getGameByPlayerName(playerName);
    if (game == null) {
      logger.info("No active game found for player: {}", playerName);
      return;
    }

    /*
    ここに切断相手のペナルティを追加
    */

    // 2. 相手プレイヤーの取得
    String enemyPlayerName = game.getEnemyPlayerByName(playerName).getName();
    logger.info("Notifying opponent: {}", enemyPlayerName);

    // 3. 相手プレイヤーへの通知
    gameEventEmitterManager.notifyPlayerDisconnection(game.getId(), playerName);

    // 4. SSE接続のクリーンアップ
    gameEventEmitterManager.removePlayerEmittersByGameId(game.getId());

    // 5. ゲームステータスの更新
    game.setIsFinished();

    // 6. ゲームの削除
    gameRoom.rmGameByName(playerName);

    logger.info("Player disconnection handled successfully: {}", playerName);
  }
}
