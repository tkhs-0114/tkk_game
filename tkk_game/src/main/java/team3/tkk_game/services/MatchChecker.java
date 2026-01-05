package team3.tkk_game.services;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import team3.tkk_game.model.WaitRoom;

/**
 * マッチング状態の監視と通知を行うサービスクラス
 * イベント駆動型で待機室の変更を通知する
 */
@Service
public class MatchChecker {

  @Autowired
  private WaitRoomEventEmitterManager waitRoomEventEmitterManager;

  /**
   * 待機室監視用のSSE接続を登録し、初回の待機室リストを送信する
   *
   * @param waitRoom 待機室オブジェクト
   * @return 登録されたSseEmitter
   */
  public SseEmitter registerMatchEmitter(WaitRoom waitRoom) {
    // WaitRoomEventEmitterManagerにEmitterを登録
    SseEmitter emitter = waitRoomEventEmitterManager.registerEmitter();

    // 初回接続時に現在の待機室リストを送信
    try {
      emitter.send(waitRoom.getWaitRoom());
    } catch (IOException e) {
      emitter.complete();
    }

    return emitter;
  }
}
