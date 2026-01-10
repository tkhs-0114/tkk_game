package team3.tkk_game.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import team3.tkk_game.mapper.DeckMapper;
import team3.tkk_game.mapper.KomaMapper;

import team3.tkk_game.model.GameRoom;
import team3.tkk_game.model.Player;
import team3.tkk_game.model.PlayerStatus;
import team3.tkk_game.model.Ban;
import team3.tkk_game.model.Game;
import team3.tkk_game.model.WaitRoom;
import team3.tkk_game.services.DisconnectionHandler;
import team3.tkk_game.services.GameEventEmitterManager;
import team3.tkk_game.services.MoveValidator;
import team3.tkk_game.services.TurnChecker;

import team3.tkk_game.model.Koma.Koma;
import team3.tkk_game.model.Koma.KomaDB;
import team3.tkk_game.model.Koma.KomaRule;

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
  @Autowired
  MoveValidator moveValidator;
  @Autowired
  GameEventEmitterManager gameEventEmitterManager;
  @Autowired
  DisconnectionHandler disconnectionHandler;

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

    model.addAttribute("haveKoma", game.getHaveKomaByName(playerName));
    model.addAttribute("enemyHaveKoma", game.getEnemyHaveKomaByName(playerName));
    // デバッグ用
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
    // ゲームが見つからない場合はマッチング画面に戻る
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

    // P2に通知
    game.getPlayer1().setStatus(PlayerStatus.GAME_WAITING);
    game.getPlayer2().setStatus(PlayerStatus.GAME_THINKING);
    String currentTurnPlayerName = getCurrentTurnPlayerName(game);
    gameEventEmitterManager.notifyTurnChange(game.getId(), currentTurnPlayerName);

    return "redirect:/game";
  }

  @GetMapping
  public String gamePage(Principal principal, Model model) {
    String loginPlayerName = principal.getName();
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);
    // ゲームが見つからない場合はマッチング画面に戻る
    if (game == null) {
      return "redirect:/match";
    }
    // ゲームが終了している場合は結果画面へ
    if (game.getIsFinished()) {
      return "redirect:/game/result";
    }
    return returnGame(model, game, loginPlayerName, null);
  }

  @GetMapping("/move")
  public String gameMove(Principal principal, Model model, @RequestParam int fromX, @RequestParam int fromY,
      @RequestParam int toX, @RequestParam int toY, @RequestParam boolean isUpdate) {
    String loginPlayerName = principal.getName();
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);

    // 自分のターンか確認
    if (!isMyTurn(game, loginPlayerName)) {
      return returnGame(model, game, loginPlayerName, null, "自分のターンではありません");
    }

    // 自分の駒か確認
    Koma koma = game.getBan().getKomaAt(fromX, fromY);
    if (koma != null && koma.getOwner() != game.getPlayerByName(loginPlayerName)) {
      return returnGame(model, game, loginPlayerName, game.getBan(), "自分の駒ではありません");
    }

    // 移動ルールを確認
    boolean canMove = moveValidator.canMove(game.getBan(), fromX, fromY, toX, toY);
    if (!canMove) {
      return returnGame(model, game, loginPlayerName, game.getBan(), "不正な手です");
    }

    // 移動先に駒がある時の処理
    Koma targetKoma = game.getBan().getKomaAt(toX, toY);
    if (targetKoma != null) {
      targetKoma.setOwner(game.getPlayerByName(loginPlayerName));
      if (targetKoma.getOriginalKoma() != -1) {
        KomaDB originalKomaDB = komaMapper.selectKomaById(targetKoma.getOriginalKoma());
        List<KomaRule> originalKomaRules = komaMapper.selectKomaRuleById(targetKoma.getOriginalKoma());
        Koma originalKoma = new Koma(originalKomaDB, originalKomaRules, targetKoma.getOwner(),
            targetKoma.getOriginalKoma());
        targetKoma = originalKoma;
      }
      game.getBan().setKomaAt(toX, toY, null);
      game.addHaveKomaByName(loginPlayerName, targetKoma);
    }

    // 駒の成り処理
    if (isUpdate) {
      if (fromY <= -1 * (Ban.BAN_LENGTH / 2) || toY <= -1 * (Ban.BAN_LENGTH / 2)) {
        KomaDB updatedKomaDB = komaMapper.selectKomaById(koma.getUpdateKoma());
        List<KomaRule> updatedKomaRules = komaMapper.selectKomaRuleById(koma.getUpdateKoma());
        Koma updatedKoma = new Koma(updatedKomaDB, updatedKomaRules, koma.getOwner(), koma.getId());
        koma = updatedKoma;
      } else {
        return returnGame(model, game, loginPlayerName, game.getBan(), "成ることができません");
      }
    }

    Ban myban = null;

    switch (koma.getSkill()) {
      default:

        // 駒を移動
        game.getBan().setKomaAt(toX, toY, koma);
        game.getBan().setKomaAt(fromX, fromY, null);

        // 自分視点の盤面を保存
        myban = new Ban(game.getBan());

        // 相手視点の盤面を保存
        game.getDisplayBan().applyBan(game.getBan());

        break;
      case STEALTH:
        System.out.println("STEALTHスキル発動");

        // 相手視点の盤面を保存
        game.getDisplayBan().applyBan(game.getBan());

        // 駒を移動
        game.getBan().setKomaAt(toX, toY, koma);
        game.getBan().setKomaAt(fromX, fromY, null);

        // 自分視点の盤面を保存
        myban = new Ban(game.getBan());

        break;
    }

    // 勝利判定
    if (!game.getBan().isHaveKing(game.getEnemyPlayerByName(loginPlayerName))) {
      game.getPlayerByName(loginPlayerName).setStatus(PlayerStatus.GAME_WIN);
      game.setIsFinished();

      // 勝者を結果画面にリダイレクト
      return "redirect:/game/result";
    }

    // ターンを交代
    game.switchTurn();
    // ターン変更をSSEで通知
    String currentTurnPlayerName = getCurrentTurnPlayerName(game);
    gameEventEmitterManager.notifyTurnChange(game.getId(), currentTurnPlayerName);

    return returnGame(model, game, loginPlayerName, myban);
  }

  @GetMapping("/putKoma")
  public String gamePutKoma(Principal principal, Model model, @RequestParam int index, @RequestParam int toX,
      @RequestParam int toY) {
    String loginPlayerName = principal.getName();
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);

    // 自分のターンか確認
    if (!isMyTurn(game, loginPlayerName)) {
      return returnGame(model, game, loginPlayerName, null, "自分のターンではありません");
    }

    // 持ち駒のインデックス確認
    List<Koma> haveKoma = game.getHaveKomaByName(loginPlayerName);
    if (index < 0 || index >= haveKoma.size()) {
      return returnGame(model, game, loginPlayerName, game.getBan(), "その持ち駒は存在しません");
    }

    // 移動先に駒がないか確認
    if (game.getBan().getKomaAt(toX, toY) != null) {
      return returnGame(model, game, loginPlayerName, game.getBan(), "その位置には駒が存在します");
    }

    // 駒を盤面に置く
    Koma koma = haveKoma.get(index);
    game.getBan().setKomaAt(toX, toY, koma);
    haveKoma.remove(index);

    // 自分視点の盤面を保存
    Ban myban = new Ban(game.getBan());

    // 相手視点の盤面を保存
    game.getDisplayBan().applyBan(game.getBan());

    // 勝利判定
    if (!game.getBan().isHaveKing(game.getEnemyPlayerByName(loginPlayerName))) {
      game.getPlayerByName(loginPlayerName).setStatus(PlayerStatus.GAME_WIN);
      game.setIsFinished();

      // 勝者を結果画面にリダイレクト
      return "redirect:/game/result";
    }

    // ターンを交代
    game.switchTurn();

    // ターン変更をSSEで通知
    String currentTurnPlayerName = getCurrentTurnPlayerName(game);
    gameEventEmitterManager.notifyTurnChange(game.getId(), currentTurnPlayerName);

    return returnGame(model, game, loginPlayerName, myban);
  }

  @GetMapping("/turn")
  public SseEmitter game(Principal principal, @RequestParam String gameId) {
    Game game = gameRoom.getGameById(gameId);
    return turnChecker.registerTurnEmitter(game, principal.getName());
  }

  @GetMapping("/result")
  public String gameEnd(Principal principal, Model model) {
    String loginPlayerName = principal.getName();
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);
    boolean isWinner = false;

    // ゲームが見つからない場合はホームへ
    if (game == null) {
      return "redirect:/home";
    }

    // ゲームが終了していない場合はゲーム画面へ
    if (!game.getIsFinished()) {
      return "redirect:/game";
    }

    Player player = game.getPlayerByName(loginPlayerName);
    Player enemyPlayer = game.getEnemyPlayerByName(loginPlayerName);
    Ban myBan = new Ban(game.getBan());

    // ステータスをGAME_ENDに変更（得点調整等の処理もここで行う）
    if (player.getStatus() == PlayerStatus.GAME_WIN) {

      /*
       * ここに勝者の得点調整等の処理を追加
       */
      isWinner = true;

      // ターンを交代して相手に最後の盤面を見せる
      player.setStatus(PlayerStatus.GAME_THINKING);
      game.switchTurn();
      String currentTurnPlayerName = getCurrentTurnPlayerName(game);
      gameEventEmitterManager.notifyTurnChange(game.getId(), currentTurnPlayerName);

      player.setStatus(PlayerStatus.GAME_END);
    } else if (player.getStatus() != PlayerStatus.GAME_END) {

      if (enemyPlayer.getStatus() == PlayerStatus.GAME_WIN) {
        // 勝者の処理が完了してないので、ゲーム画面に戻して待機させる
        return "redirect:/game";
      }

      /*
       * ここに敗者の得点調整等の処理を追加
       */

      player.setStatus(PlayerStatus.GAME_END);

      gameEventEmitterManager.removePlayerEmittersByGameId(game.getId());
      gameRoom.rmGameByName(loginPlayerName);
    }

    // モデルに最終盤面情報を追加
    model.addAttribute("GAME_END", true);
    model.addAttribute("isWinner", isWinner);
    return returnGame(model, game, loginPlayerName, myBan);
  }

  // 指定した駒の移動可能なマスを取得するAPI
  @GetMapping("/movable")
  @ResponseBody
  public List<int[]> getMovableCells(Principal principal, @RequestParam int x, @RequestParam int y) {
    String loginPlayerName = principal.getName();
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);

    if (game == null || !isMyTurn(game, loginPlayerName)) {
      return List.of();
    }

    return moveValidator.getMovableCells(game.getDisplayBan(), x, y);
  }

  /**
   * 現在のターンのプレイヤー名を取得する
   *
   * @param game ゲームオブジェクト
   * @return 現在のターン（GAME_THINKING）のプレイヤー名
   */
  private String getCurrentTurnPlayerName(Game game) {
    if (game.getPlayer1().getStatus() == PlayerStatus.GAME_THINKING) {
      return game.getPlayer1().getName();
    } else {
      return game.getPlayer2().getName();
    }
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

  /**
   * プレイヤーの切断を通知するエンドポイント
   * ブラウザクローズや他サイトへの遷移時に呼ばれる
   *
   * @param principal ログイン中のプレイヤー情報
   * @return 切断通知の結果
   */
  @PostMapping("/disconnect")
  @ResponseBody
  public Map<String, String> notifyDisconnection(Principal principal) {
    String playerName = principal.getName();
    disconnectionHandler.handlePlayerDisconnection(playerName, "INTENTIONAL");
    return Map.of("message", "disconnection notified");
  }
}
