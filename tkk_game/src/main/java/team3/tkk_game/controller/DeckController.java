package team3.tkk_game.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import team3.tkk_game.mapper.DeckMapper;
import team3.tkk_game.mapper.KomaMapper;
import team3.tkk_game.mapper.PlayerDeckMapper;
import team3.tkk_game.mapper.PlayerMapper;
import team3.tkk_game.model.Deck;
import team3.tkk_game.model.PlayerDeck;
import team3.tkk_game.model.Koma.KomaDB;
import team3.tkk_game.model.Koma.KomaRule;

@Controller
@RequestMapping("/deck")
public class DeckController {

  private static final int COST_LIMIT = 50;

  @Autowired
  KomaMapper komaMapper;

  @Autowired
  DeckMapper deckMapper;

  @Autowired
  PlayerMapper playerMapper;

  @Autowired
  PlayerDeckMapper playerDeckMapper;

  @GetMapping("/make")
  public String deckmake(Principal principal, Model model) {
    model.addAttribute("playerName", principal.getName());
    List<KomaDB> komas = komaMapper.selectKomasByPlayerUsername(principal.getName());
    List<KomaDB> cantUpdateKomas = komas.stream().filter(k -> k.getUpdateKoma() != -1).toList();
    model.addAttribute("komas", cantUpdateKomas);

    // 各駒のコストを計算してモデルに追加
    Map<Integer, Integer> komaCosts = new HashMap<>();
    for (KomaDB koma : cantUpdateKomas) {
      List<KomaRule> rules = komaMapper.selectKomaRuleById(koma.getId());
      int cost = koma.calculateCost(rules);
      komaCosts.put(koma.getId(), cost);
    }
    model.addAttribute("komaCosts", komaCosts);

    // コスト上限をモデルに追加
    model.addAttribute("costLimit", COST_LIMIT);

    return "deckmake.html";
  }

  @PostMapping("/save")
  public String saveDeck(@RequestParam String deckName, @RequestParam String sfen, Principal principal,
      RedirectAttributes redirectAttributes) {
    // SFENから駒IDを抽出
    List<Integer> komaIds = extractKomaIdsFromSfen(sfen);

    // 各駒のコストを取得して合計を計算
    int totalCost = 0;
    for (Integer komaId : komaIds) {
      KomaDB koma = komaMapper.selectKomaById(komaId);
      if (koma != null) {
        List<KomaRule> rules = komaMapper.selectKomaRuleById(komaId);
        totalCost += koma.calculateCost(rules);
      }
    }

    // コスト上限チェック
    if (totalCost > COST_LIMIT) {
      redirectAttributes.addFlashAttribute("error",
          "デッキのコストが上限を超えています (" + totalCost + "/" + COST_LIMIT + ")");
      return "redirect:/deck/make";
    }

    // デッキを保存
    Deck deck = new Deck();
    deck.setName(deckName);
    deck.setSfen(sfen);
    deck.setCost(totalCost);
    deckMapper.insertDeck(deck);

    // 作成者にデッキの使用権限を付与（所有者として）
    Integer playerId = playerDeckMapper.selectPlayerIdByUsername(principal.getName());
    if (playerId != null) {
      PlayerDeck playerDeck = new PlayerDeck();
      playerDeck.setPlayerId(playerId);
      playerDeck.setDeckId(deck.getId());
      playerDeck.setIsOwner(true); // 作成者は所有者
      playerDeckMapper.insertPlayerDeck(playerDeck);
    }

    redirectAttributes.addFlashAttribute("success", "デッキを保存しました (コスト: " + totalCost + ")");
    return "redirect:/deck/make";
  }

  /**
   * SFENから駒IDを抽出するヘルパーメソッド
   *
   * @param sfen SFEN形式の文字列
   * @return 駒IDのリスト
   */
  private List<Integer> extractKomaIdsFromSfen(String sfen) {
    List<Integer> komaIds = new ArrayList<>();
    Pattern pattern = Pattern.compile("\\[(\\d+)\\]");
    Matcher matcher = pattern.matcher(sfen);
    while (matcher.find()) {
      komaIds.add(Integer.parseInt(matcher.group(1)));
    }
    return komaIds;
  }

  @GetMapping("/select")
  public String selectDeck(Principal principal, Model model) {
    model.addAttribute("playerName", principal.getName());
    // プレイヤーが使用可能なデッキを取得（PlayerDeckテーブル経由）
    List<Deck> decks = deckMapper.selectDecksByPlayerUsername(principal.getName());
    model.addAttribute("decks", decks);
    return "deckselect.html";
  }

  @PostMapping("/choose")
  public String chooseDeck(@RequestParam int deckId, Principal principal) {
    playerMapper.updateSelectedDeckId(principal.getName(), deckId);
    return "redirect:/home";
  }

  @GetMapping("/load/{id}")
  public String loadDeck(@PathVariable("id") int deckId, Principal principal) {
    playerMapper.updateSelectedDeckId(principal.getName(), deckId);
    return "redirect:/home";
  }

  @GetMapping("/delete/{id}")
  public String deleteDeck(@PathVariable("id") int deckId, Principal principal, RedirectAttributes redirectAttributes) {
    // プレイヤーIDを取得
    Integer playerId = playerDeckMapper.selectPlayerIdByUsername(principal.getName());
    if (playerId == null) {
      redirectAttributes.addFlashAttribute("error", "プレイヤー情報が見つかりません");
      return "redirect:/deck/select";
    }

    // 所有者かどうかをチェック
    Boolean isOwner = playerDeckMapper.isOwner(playerId, deckId);
    if (isOwner == null || !isOwner) {
      redirectAttributes.addFlashAttribute("error", "このデッキは削除できません（所有者のみ削除可能）");
      return "redirect:/deck/select";
    }

    // 選択中のデッキをクリア
    playerMapper.clearSelectedDeckId(deckId);

    // PlayerDeckテーブルから紐づけを削除
    playerDeckMapper.deletePlayerDecksByDeckId(deckId);

    // デッキを削除
    deckMapper.deleteDeckById(deckId);
    return "redirect:/deck/select";
  }

  /**
   * 駒作成画面へのリダイレクト（後方互換性のため）
   */
  @GetMapping("make/koma")
  public String komaMakeRedirect() {
    return "redirect:/koma/make";
  }

  /**
   * 駒作成保存へのリダイレクト（後方互換性のため）
   */
  @PostMapping("make/koma/save")
  public String saveKomaDataRedirect() {
    return "redirect:/koma/make";
  }
}
