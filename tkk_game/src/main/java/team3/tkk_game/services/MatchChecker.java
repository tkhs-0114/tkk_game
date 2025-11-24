package team3.tkk_game.services;

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import team3.tkk_game.model.WaitRoom;

@Service
public class MatchChecker {
  boolean matched = false;

  @Async
  public void checkMatch(SseEmitter emitter, WaitRoom waitRoom) {
    // マッチング処理
    try {
      while (true) {
        emitter.send(waitRoom.getWaitRoom());
        TimeUnit.SECONDS.sleep(1);
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    } finally {
      emitter.complete();
    }
  }
}
