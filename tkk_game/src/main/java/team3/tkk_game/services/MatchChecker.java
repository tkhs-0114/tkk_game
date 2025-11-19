package team3.tkk_game.services;

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class MatchChecker {
  boolean matched = false;

  @Async
  public void checkMatch(SseEmitter emitter) {
    // マッチング処理
    try {
      while (true) {
        emitter.send(matched);
        TimeUnit.SECONDS.sleep(1);
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
}
