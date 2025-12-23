package team3.tkk_game.model;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import team3.tkk_game.services.WaitRoomEventEmitterManager;

/**
 * 待機室を管理するクラス
 * 部屋の追加・削除・リクエスト管理を行い、変更時にSSEで通知する
 */
@Component
public class WaitRoom {
  ArrayList<Game> waitRoom = new ArrayList<>();

  @Autowired
  private WaitRoomEventEmitterManager waitRoomEventEmitterManager;

  public WaitRoom() {
  }

  public ArrayList<Game> getWaitRoom() {
    return waitRoom;
  }

  public Boolean isInRoom(String playerName) {
    for (Game game : waitRoom) {
      if (game.getPlayer1().getName().equals(playerName)) {
        return true;
      }
    }
    return false;
  }

  public void addWaitRoom(String playerName) {
    if (!isInRoom(playerName)) {
      String id = String.valueOf(System.currentTimeMillis());
      Game game = new Game(id, playerName);
      waitRoom.add(game);
      // 待機室リストの変更を通知
      waitRoomEventEmitterManager.notifyRoomListChange(waitRoom);
    }
  }

  public Game getRoomByName(String playerName) {
    for (Game game : waitRoom) {
      if (game.getPlayer1().getName().equals(playerName)
          || (game.getPlayer2() != null && game.getPlayer2().getName().equals(playerName))) {
        return game;
      }
    }
    return null;
  }

  public void rmRoom(String playerName) {
    waitRoom.removeIf(game -> game.getPlayer1().getName().equals(playerName));
    // 待機室リストの変更を通知
    waitRoomEventEmitterManager.notifyRoomListChange(waitRoom);
  }

  public boolean sendRequest(String Player2Name, String Player1Name) {
    Game room = getRoomByName(Player1Name);
    if (room != null && room.getPlayer2() == null) {
      room.setPlayer2(Player2Name);
      // 待機室リストの変更を通知
      waitRoomEventEmitterManager.notifyRoomListChange(waitRoom);
      return true;
    }
    return false;
  }

  public void clearRequest(String playerName) {
    Game room = getRoomByName(playerName);
    if (room != null) {
      room.clearPlayer2();
      // 待機室リストの変更を通知
      waitRoomEventEmitterManager.notifyRoomListChange(waitRoom);
    }
  }

}
