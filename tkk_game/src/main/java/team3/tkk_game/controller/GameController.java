package team3.tkk_game.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import team3.tkk_game.mapper.DeckMapper;
import team3.tkk_game.mapper.KomaMapper;
import team3.tkk_game.model.Ban;
import team3.tkk_game.model.Game;
import team3.tkk_game.model.GameRoom;
import team3.tkk_game.model.Koma.Koma;
import team3.tkk_game.model.Koma.KomaDB;
import team3.tkk_game.model.Koma.KomaRule;
import team3.tkk_game.model.Player;
import team3.tkk_game.model.PlayerStatus;
import team3.tkk_game.model.WaitRoom;
import team3.tkk_game.services.TurnChecker;

@Controller
@RequestMapping("/game")
public class GameController {

  @Autowired
  GameRoom gameRoom;
  @Autowired
  WaitRoom waitRoom;
  @Autowired
  TurnChecker turnChecker;
  @Autowired
  KomaMapper komaMapper;
  @Autowired
  DeckMapper deckMapper;

  private boolean isMyTurn(Game game, String playerName) {
    return game.getPlayerByName(playerName).getStatus() == PlayerStatus.GAME_THINKING;
  }

  private String returnGame(Model model, Game game, String playerName, Ban ban) {
    model.addAttribute("gameId", game.getId());
    if (ban != null) {
      model.addAttribute("ban", ban);
    } else {
      if (isMyTurn(game, playerName)) {
        model.addAttribute("ban", game.getDisplayBan());
      } else {
        Ban myban = new Ban(game.getDisplayBan());
        myban.rotate180();
        model.addAttribute("ban", myban);
      }
    }
    model.addAttribute("playerStatus", game.getPlayerByName(playerName).getStatus());
    model.addAttribute("game", game);
    return "game.html";
  }

  private String returnGame(Model model, Game game, String playerName, Ban ban, String errMessage) {
    model.addAttribute("errMessage", errMessage);
    return returnGame(model, game, playerName, ban);
  }

  @GetMapping("/start")
  public String gameStart(Principal principal, Model model) {
    String loginPlayerName = principal.getName();
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);
    if (game == null) {
      return "redirect:/match";
    }

    String p1sfen = deckMapper.selectDeckById(game.getDeckIdPlayer1()).getSfen();
    applySfenToBan(game, p1sfen, game.getPlayer1());

    game.getBan().rotate180(); // プレイヤー2側から見た盤面にする;

    String p2sfen = deckMapper.selectDeckById(game.getDeckIdPlayer2()).getSfen();
    applySfenToBan(game, p2sfen, game.getPlayer2());

    // 表示用盤面に反映
    game.getDisplayBan().applyBan(game.getBan());

    return "redirect:/game";
  }

  @GetMapping
  public String gamePage(Principal principal, Model model) {
    String loginPlayerName = principal.getName();
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);
    if (game == null) {
      return "redirect:/match";
    }
    return returnGame(model, game, loginPlayerName, null);
  }

  @GetMapping("/move")
  public String gameMove(Principal principal, Model model, @RequestParam int fromX, @RequestParam int fromY,
      @RequestParam int toX, @RequestParam int toY) {
    String loginPlayerName = principal.getName();
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);

    if (!isMyTurn(game, loginPlayerName)) {
      return returnGame(model, game, loginPlayerName, null, "自分のターンではありません");
    }

    Koma koma = game.getBan().getKomaAt(fromX, fromY);
    if (koma != null && koma.getOwner() != game.getPlayerByName(loginPlayerName)) {
      return returnGame(model, game, loginPlayerName, game.getBan(), "自分の駒ではありません");
    }

    Boolean canMove = koma.canMove(fromX, fromY, toX, toY);
    System.out.println("canMove:" + canMove);
    if (!canMove) {
      return returnGame(model, game, loginPlayerName, game.getBan(), "不正な手です");
    }

    // 駒を移動
    game.getBan().setKomaAt(toX, toY, koma);
    game.getBan().setKomaAt(fromX, fromY, null);

    Ban myban = new Ban(game.getBan());
    game.getDisplayBan().applyBan(game.getBan());
    game.switchTurn();
    return returnGame(model, game, loginPlayerName, myban);
  }

  @GetMapping("/turn")
  public SseEmitter game(Principal principal, @RequestParam String gameId) {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    turnChecker.checkTurn(emitter, gameRoom.getGameById(gameId), principal.getName());
    return emitter;
  }

  /**
   * SFEN をパースして game.getBan() に駒をセットする。
   * gameStart 側で両プレイヤー分を呼んでいるため、ここでは単に盤面に駒を配置するだけにする。
   * フォーマット:
   * - 行は '/' で区切る
   * - 数字は連続する空セル数
   * - 駒は [id] の形式（例: [12]）
   */
  private void applySfenToBan(Game game, String sfen, Player owner) {
    if (sfen == null || sfen.isBlank())
      return;

    String[] rows = sfen.split("/");
    int half = (game.getBan().getBoard().length - 1) / 2;
    int startY = half - (rows.length - 1); // rows.length に応じた開始 y
    for (int row = 0; row < rows.length; row++) {
      String token = rows[row];
      int y = startY + row;
      int x = -half; // 行内は左から右へ x を増やす。盤の左端が -half

      int i = 0;
      while (i < token.length()) {
        char ch = token.charAt(i);
        if (Character.isDigit(ch)) {
          int j = i + 1;
          while (j < token.length() && Character.isDigit(token.charAt(j)))
            j++;
          int empty = Integer.parseInt(token.substring(i, j));
          x += empty;
          i = j;
          continue;
        }

        if (ch == '[') {
          int j = token.indexOf(']', i + 1);
          if (j == -1) {
            System.out.println("Malformed SFEN: missing ']' in token: " + token);
            break;
          }
          String idStr = token.substring(i + 1, j);
          int komaId;
          try {
            komaId = Integer.parseInt(idStr);
          } catch (NumberFormatException ex) {
            System.out.println("Invalid koma id in SFEN: " + idStr);
            i = j + 1;
            continue;
          }

          KomaDB kdb = null;
          try {
            kdb = komaMapper.selectKomaById(komaId);
          } catch (Exception ex) {
            System.out.println("Failed to select Koma by id: " + komaId + " -> " + ex.getMessage());
          }

          if (kdb != null) {
            List<KomaRule> rules = null;
            try {
              rules = komaMapper.selectKomaRuleById(komaId);
            } catch (Exception ex) {
              // ルール取得失敗はログ出力してフォールバック
              System.out.println("Failed to select KomaRule for id: " + komaId + " -> " + ex.getMessage());
            }
            if (rules == null) {
              rules = java.util.Collections.emptyList();
            }

            Koma koma = new Koma(kdb, rules, owner);
            try {
              game.getBan().setKomaAt(x, y, koma);
            } catch (Exception ex) {
              System.out
                  .println("Failed to set Koma at x=" + x + " y=" + y + " id=" + komaId + " -> " + ex.getMessage());
            }
          } else {
            System.out.println("Unknown piece id in SFEN: " + komaId);
          }

          x++;
          i = j + 1;
          continue;
        }

        // その他の記号は無視
        i++;
      }
    }
  }
}
