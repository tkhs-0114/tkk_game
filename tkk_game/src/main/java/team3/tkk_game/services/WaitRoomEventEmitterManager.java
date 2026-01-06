package team3.tkk_game.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import team3.tkk_game.model.Game;

/**
 * 待機室イベントのSSE配信を管理するクラス
 * イベント駆動型でクライアントに待機室の変更を通知する
 */
@Component
public class WaitRoomEventEmitterManager {

  /**
   * 待機室監視用Emitter一覧
   */
  private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  /**
   * 待機室監視用のEmitterを登録する
   *
   * @return 登録されたSseEmitter
   */
  public SseEmitter registerEmitter() {
    SseEmitter emitter = new SseEmitter(30000L); // タイムアウトは30秒

    // 切断時のクリーンアップ設定
    emitter.onCompletion(() -> removeEmitter(emitter));
    emitter.onTimeout(() -> removeEmitter(emitter));
    emitter.onError(e -> removeEmitter(emitter));

    emitters.add(emitter);

    return emitter;
  }

  /**
   * 待機室リストの変更を全クライアントに通知する
   *
   * @param waitRoom 待機室のゲームリスト
   */
  public void notifyRoomListChange(ArrayList<Game> waitRoom) {
    // 削除対象のEmitterを収集するリスト
    ArrayList<SseEmitter> deadEmitters = new ArrayList<>();

    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(waitRoom);
      } catch (IOException | IllegalStateException e) {
        // 送信失敗時（接続切断やEmitter完了済み）は削除対象に追加
        deadEmitters.add(emitter);
      }
    }

    // 送信失敗したEmitterを削除
    for (SseEmitter deadEmitter : deadEmitters) {
      emitters.remove(deadEmitter);
    }
  }

  /**
   * Emitterを削除する
   *
   * @param emitter 削除するSseEmitter
   */
  public void removeEmitter(SseEmitter emitter) {
    emitters.remove(emitter);
  }
}
