package team3.tkk_game.services;

import java.util.List;

import org.springframework.stereotype.Service;

import team3.tkk_game.model.Ban;
import team3.tkk_game.model.Koma.Koma;
import team3.tkk_game.model.Koma.KomaRule;

/**
 * 駒の移動可否を判定するサービスクラス
 */
@Service
public class MoveValidator {

  /**
   * 駒が指定された位置に移動可能かを判定する
   * 経路上に駒がある場合は移動不可
   *
   * @param ban   盤面
   * @param fromX 移動元X座標
   * @param fromY 移動元Y座標
   * @param toX   移動先X座標
   * @param toY   移動先Y座標
   * @return 移動可能な場合はtrue
   */
  public boolean canMove(Ban ban, int fromX, int fromY, int toX, int toY) {
    Koma koma = ban.getKomaAt(fromX, fromY);

    List<KomaRule> rules = koma.getRules();
    System.out.println(rules);
    int dx = toX - fromX;
    int dy = toY - fromY;
    for (KomaRule rule : rules) {
      switch (rule) {
        case UP:
          if (dx == 0 && dy == -1)
            return true;
          break;
        case DOWN:
          if (dx == 0 && dy == 1)
            return true;
          break;
        case LEFT:
          if (dx == -1 && dy == 0)
            return true;
          break;
        case RIGHT:
          if (dx == 1 && dy == 0)
            return true;
          break;
        case UP_LEFT:
          if (dx == -1 && dy == -1)
            return true;
          break;
        case UP_RIGHT:
          if (dx == 1 && dy == -1)
            return true;
          break;
        case DOWN_LEFT:
          if (dx == -1 && dy == 1)
            return true;
          break;
        case DOWN_RIGHT:
          if (dx == 1 && dy == 1)
            return true;
          break;
        case LINE_UP:
          if (dx == 0 && dy < 0) {
            boolean blocked = false;
            for (int currentY = fromY - 1; currentY > toY; currentY--) {
              if (ban.getKomaAt(fromX, currentY) != null) {
                blocked = true;
                break;
              }
            }
            if (!blocked) {
              return true;
            }
          }
          break;
        case LINE_DOWN:
          if (dx == 0 && dy > 0) {
            boolean blocked = false;
            for (int currentY = fromY + 1; currentY < toY; currentY++) {
              if (ban.getKomaAt(fromX, currentY) != null) {
                blocked = true;
                break;
              }
            }
            if (!blocked) {
              return true;
            }
          }
          break;
        case LINE_LEFT:
          if (dx < 0 && dy == 0) {
            boolean blocked = false;
            for (int currentX = fromX - 1; currentX > toX; currentX--) {
              if (ban.getKomaAt(currentX, fromY) != null) {
                blocked = true;
                break;
              }
            }
            if (!blocked) {
              return true;
            }
          }
          break;
        case LINE_RIGHT:
          if (dx > 0 && dy == 0) {
            boolean blocked = false;
            for (int currentX = fromX + 1; currentX < toX; currentX++) {
              if (ban.getKomaAt(currentX, fromY) != null) {
                blocked = true;
                break;
              }
            }
            if (!blocked) {
              return true;
            }
          }
          break;
        case LINE_UP_LEFT:
          if (dx < 0 && dy < 0 && Math.abs(dx) == Math.abs(dy)) {
            boolean blocked = false;
            for (int i = 1; i < Math.abs(dx); i++) {
              if (ban.getKomaAt(fromX - i, fromY - i) != null) {
                blocked = true;
                break;
              }
            }
            if (!blocked) {
              return true;
            }
          }
          break;
        case LINE_UP_RIGHT:
          if (dx > 0 && dy < 0 && Math.abs(dx) == Math.abs(dy)) {
            boolean blocked = false;
            for (int i = 1; i < Math.abs(dx); i++) {
              if (ban.getKomaAt(fromX + i, fromY - i) != null) {
                blocked = true;
                break;
              }
            }
            if (!blocked) {
              return true;
            }
          }
          break;
        case LINE_DOWN_LEFT:
          if (dx < 0 && dy > 0 && Math.abs(dx) == Math.abs(dy)) {
            boolean blocked = false;
            for (int i = 1; i < Math.abs(dx); i++) {
              if (ban.getKomaAt(fromX - i, fromY + i) != null) {
                blocked = true;
                break;
              }
            }
            if (!blocked) {
              return true;
            }
          }
          break;
        case LINE_DOWN_RIGHT:
          if (dx > 0 && dy > 0 && Math.abs(dx) == Math.abs(dy)) {
            boolean blocked = false;
            for (int i = 1; i < Math.abs(dx); i++) {
              if (ban.getKomaAt(fromX + i, fromY + i) != null) {
                blocked = true;
                break;
              }
            }
            if (!blocked) {
              return true;
            }
          }
          break;
      }
    }
    return false;
  }
}
