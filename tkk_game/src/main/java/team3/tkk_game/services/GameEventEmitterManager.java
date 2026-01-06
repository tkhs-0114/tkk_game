package team3.tkk_game.services;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * ゲームイベントのSSE配信を管理するクラス
 * イベント駆動型でクライアントにターン変更通知を送信する
 */
@Component
public class GameEventEmitterManager {

  private static final Logger logger = LoggerFactory.getLogger(GameEventEmitterManager.class);

  /**
   * ゲームID別のEmitter管理
   * key: ゲームID, value: 登録されたEmitterのリスト
   */
  private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> gameEmitters = new ConcurrentHashMap<>();

  /**
   * プレイヤー名とEmitterの対応
   * key: プレイヤー名, value: SseEmitter
   */
  private final ConcurrentHashMap<String, SseEmitter> playerEmitters = new ConcurrentHashMap<>();

  /**
   * プレイヤー名とゲームIDの対応
   * key: プレイヤー名, value: ゲームID
   */
  private final ConcurrentHashMap<String, String> playerToGame = new ConcurrentHashMap<>();

  /**
   * プレイヤーのEmitterを登録する
   *
   * @param playerName プレイヤー名
   * @param gameId     ゲームID
   * @return 登録されたSseEmitter
   */
  public SseEmitter registerPlayerEmitter(String playerName, String gameId) {
    // 既存のEmitterがあれば削除
    removeEmitter(playerName);

    SseEmitter emitter = new SseEmitter(30000L); // タイムアウトは30秒

    // 切断時のクリーンアップ設定
    emitter.onCompletion(() -> removeEmitter(playerName));
    emitter.onTimeout(() -> removeEmitter(playerName));
    emitter.onError(e -> removeEmitter(playerName));

    // プレイヤー名とEmitterの対応を保存
    playerEmitters.put(playerName, emitter);
    playerToGame.put(playerName, gameId);

    // ゲームID別のEmitterリストに追加
    gameEmitters.computeIfAbsent(gameId, k -> new CopyOnWriteArrayList<>()).add(emitter);

    return emitter;
  }

  /**
   * ターン変更を該当ゲームの全プレイヤーに通知する
   *
   * @param gameId                ゲームID
   * @param currentTurnPlayerName 現在のターンのプレイヤー名
   */
  public void notifyTurnChange(String gameId, String currentTurnPlayerName) {
    CopyOnWriteArrayList<SseEmitter> emitters = gameEmitters.get(gameId);
    if (emitters == null) {
      return;
    }

    // 削除対象のプレイヤー名を収集するリスト
    java.util.ArrayList<String> deadPlayers = new java.util.ArrayList<>();

    for (SseEmitter emitter : emitters) {
      try {
        // このEmitterに対応するプレイヤー名を取得
        String playerName = getPlayerNameByEmitter(emitter);
        if (playerName != null) {
          // 自分のターンかどうかを通知
          boolean isMyTurn = playerName.equals(currentTurnPlayerName);
          emitter.send(isMyTurn);
        }
      } catch (IOException | IllegalStateException e) {
        // 送信失敗時（接続切断やEmitter完了済み）は削除対象に追加
        String playerName = getPlayerNameByEmitter(emitter);
        if (playerName != null) {
          deadPlayers.add(playerName);
        }
      }
    }

    // 送信失敗したEmitterを削除
    for (String playerName : deadPlayers) {
      removeEmitter(playerName);
    }
  }

  /**
   * プレイヤーのEmitterを削除する
   *
   * @param playerName プレイヤー名
   */
  public void removeEmitter(String playerName) {
    SseEmitter emitter = playerEmitters.remove(playerName);
    String gameId = playerToGame.remove(playerName);

    if (emitter != null && gameId != null) {
      CopyOnWriteArrayList<SseEmitter> emitters = gameEmitters.get(gameId);
      if (emitters != null) {
        emitters.remove(emitter);
        // リストが空になったらゲームIDのエントリを削除
        if (emitters.isEmpty()) {
          gameEmitters.remove(gameId);
        }
      }
    }
  }

  /**
   * 指定されたゲームIDに関連するすべてのEmitterを削除する
   * ゲーム終了時にリソースを解放するために使用する
   *
   * @param gameId ゲームID
   */
  public void removePlayerEmittersByGameId(String gameId) {
    // 削除対象のプレイヤー名を収集
    java.util.ArrayList<String> playersToRemove = new java.util.ArrayList<>();
    for (var entry : playerToGame.entrySet()) {
      if (gameId.equals(entry.getValue())) {
        playersToRemove.add(entry.getKey());
      }
    }

    // 各プレイヤーのEmitterを既存のメソッドで削除
    for (String playerName : playersToRemove) {
      removeEmitter(playerName);
    }
  }

  /**
   * プレイヤーの切断を該当ゲームの全プレイヤーに通知する
   *
   * @param gameId                ゲームID
   * @param disconnectedPlayerName 切断したプレイヤー名
   */
  public void notifyPlayerDisconnection(String gameId, String disconnectedPlayerName) {
    CopyOnWriteArrayList<SseEmitter> emitters = gameEmitters.get(gameId);
    if (emitters == null) {
      return;
    }

    // 削除対象のプレイヤー名を収集するリスト
    java.util.ArrayList<String> deadPlayers = new java.util.ArrayList<>();

    for (SseEmitter emitter : emitters) {
      try {
        // このEmitterに対応するプレイヤー名を取得
        String playerName = getPlayerNameByEmitter(emitter);
        if (playerName != null && !playerName.equals(disconnectedPlayerName)) {
          // 切断したプレイヤー以外に通知
          emitter.send(SseEmitter.event()
              .name("disconnect")
              .data(disconnectedPlayerName));
        }
      } catch (IOException | IllegalStateException e) {
        // 送信失敗時（接続切断やEmitter完了済み）は削除対象に追加
        String playerName = getPlayerNameByEmitter(emitter);
        if (playerName != null) {
          deadPlayers.add(playerName);
        }
      }
    }

    // 送信失敗したEmitterを削除
    for (String playerName : deadPlayers) {
      removeEmitter(playerName);
    }
  }

  /**
   * Emitterに対応するプレイヤー名を取得する
   *
   * @param emitter SseEmitter
   * @return プレイヤー名（見つからない場合はnull）
   */
  private String getPlayerNameByEmitter(SseEmitter emitter) {
    for (var entry : playerEmitters.entrySet()) {
      if (entry.getValue() == emitter) {
        return entry.getKey();
      }
    }
    return null;
  }

  /**
   * 定期的にheartbeatを送信して接続を維持する
   * 5秒ごとに全てのアクティブなEmitterにheartbeatを送信
   */
  @Scheduled(fixedRate = 5000)
  public void sendHeartbeat() {
    // 削除対象のプレイヤー名を収集するリスト
    java.util.ArrayList<String> deadPlayers = new java.util.ArrayList<>();

    for (var entry : playerEmitters.entrySet()) {
      String playerName = entry.getKey();
      SseEmitter emitter = entry.getValue();
      try {
        // heartbeatイベントを送信（comment形式で送ることでイベントとして処理されない）
        emitter.send(SseEmitter.event()
            .name("heartbeat")
            .data("ping"));
      } catch (IOException | IllegalStateException e) {
        // 送信失敗時（接続切断やEmitter完了済み）は削除対象に追加
        logger.debug("Heartbeat failed for player: {}, error: {}", playerName, e.getMessage());
        deadPlayers.add(playerName);
      }
    }

    // 送信失敗したEmitterを削除
    for (String playerName : deadPlayers) {
      removeEmitter(playerName);
    }
  }
}
