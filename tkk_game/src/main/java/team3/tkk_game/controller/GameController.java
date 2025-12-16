package team3.tkk_game.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import team3.tkk_game.model.GameRoom;
import team3.tkk_game.model.PlayerStatus;
import team3.tkk_game.model.Ban;
import team3.tkk_game.model.Game;
import team3.tkk_game.model.WaitRoom;
import team3.tkk_game.services.TurnChecker;

import team3.tkk_game.mapper.KomaMapper;
import team3.tkk_game.model.Koma.Koma;
import team3.tkk_game.model.Koma.KomaDB;
import team3.tkk_game.model.Koma.KomaRule;

import org.springframework.ui.Model;

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
  KomaMapper KomaMapper;

  private boolean isMyTurn(Game game, String playerName) {
    return game.getPlayerByName(playerName).getStatus() == PlayerStatus.GAME_THINKING;
  }

  private String returnGame(Model model, Game game, String playerName, Ban ban) {
    model.addAttribute("gameId", game.getId());
    if (ban != null) {
      model.addAttribute("ban", ban);
    } else {
      model.addAttribute("ban", game.getDisplayBan());
    }
    model.addAttribute("playerStatus", game.getPlayerByName(playerName).getStatus());
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

    /*
     * ここにデッキ設定等のゲームの初期設定を記入
     */
    KomaDB koma1 = KomaMapper.selectKomaById(1); // 例: 駒ID1を選択
    List<KomaRule> koma1Rules = KomaMapper.selectKomaRuleById(1);
    Koma koma1Koma = new Koma(koma1, koma1Rules, game.getPlayer1());

    KomaDB koma2 = KomaMapper.selectKomaById(2); // 例: 駒ID2を選択
    List<KomaRule> koma2Rules = KomaMapper.selectKomaRuleById(2);
    Koma koma2Koma = new Koma(koma2, koma2Rules, game.getPlayer2());

    // 応急処置
    game.getBan().setKomaAt(0, -2, koma1Koma);
    game.getBan().setKomaAt(0, 2, koma2Koma);

    game.getDisplayBan().setBoard(game.getBan().getBoard());
    return returnGame(model, game, loginPlayerName, null);
  }

  @GetMapping
  public String gamePage(Principal principal, Model model) {
    String loginPlayerName = principal.getName();
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);
    // ゲームが見つからない場合はマッチング画面に戻る
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

    // 自分のターンか確認
    if (!isMyTurn(game, loginPlayerName)) {
      return returnGame(model, game, loginPlayerName, null, "自分のターンではありません");
    }

    // 自分の駒か確認（LocalBanには自分の駒しかない）
    Koma koma = game.getBan().getKomaAt(fromX, fromY);
    if (koma != null && koma.getOwner() != game.getPlayerByName(loginPlayerName)) {
      return returnGame(model, game, loginPlayerName, game.getBan(), "自分の駒ではありません");
    }

    // 移動ルールを確認
    Boolean canMove = koma.canMove(fromX, fromY, toX, toY);
    System.out.println("canMove:" + canMove);
    if (!canMove) {
      return returnGame(model, game, loginPlayerName, game.getBan(), "不正な手です");
    }

    // 相手の駒を取る場合の処理
    // 未実装

    // 駒を移動
    game.getBan().setKomaAt(toX, toY, koma);
    game.getBan().setKomaAt(fromX, fromY, null);

    Ban myban = new Ban();
    myban.setBoard(game.getBan().getBoard());

    game.getDisplayBan().setBoard(game.getBan().getBoardR180());

    // ターンを交代
    game.getBan().setBoard(game.getBan().getBoardR180());
    game.switchTurn();

    return returnGame(model, game, loginPlayerName, myban);
  }

  @GetMapping("/turn")
  public SseEmitter game(Principal principal, @RequestParam String gameId) {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    turnChecker.checkTurn(emitter, gameRoom.getGameById(gameId), principal.getName());
    return emitter;
  }

}
