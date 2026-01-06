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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import team3.tkk_game.mapper.DeckMapper;
import team3.tkk_game.mapper.KomaMapper;
import team3.tkk_game.mapper.PlayerMapper;
import team3.tkk_game.model.Deck;
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

  @GetMapping("/make")
  public String deckmake(Principal principal, Model model) {
    model.addAttribute("playerName", principal.getName());
    List<KomaDB> komas = komaMapper.selectAllKoma();
    model.addAttribute("komas", komas);

    // 各駒のコストを計算してモデルに追加
    Map<Integer, Integer> komaCosts = new HashMap<>();
    for (KomaDB koma : komas) {
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
    List<Deck> decks = deckMapper.selectAllDecks();
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
  public String deleteDeck(@PathVariable("id") int deckId) {
    playerMapper.clearSelectedDeckId(deckId);
    deckMapper.deleteDeckById(deckId);
    return "redirect:/deck/select";
  }

  @GetMapping("make/koma")
  public String komaMake(Principal principal, Model model) {
    List<KomaDB> komas = komaMapper.selectAllKoma();
    List<KomaDB> canUpdateKomas = komas.stream().filter(k -> k.getUpdateKoma() == -1).toList();
    model.addAttribute("canUpdateKomas", canUpdateKomas);

    // 利用可能な移動ルールを追加
    model.addAttribute("availableRules", KomaRule.values());

    // 利用可能な特殊スキルを追加
    model.addAttribute("availableSkills", team3.tkk_game.model.Koma.KomaSkill.values());

    return "komamake.html";
  }

  @PostMapping("make/koma/save")
  @Transactional
  public String saveKomaData(
      @RequestParam String name,
      @RequestParam(required = false) List<String> rules,
      @RequestParam(required = false) String skill,
      @RequestParam(required = false) Integer updateKomaId,
      Principal principal) {

    try {
      // 1. KomaDBオブジェクトを作成
      KomaDB newKoma = new KomaDB(name, skill, updateKomaId);

      // 2. komaテーブルにInsert（IDが自動採番される）
      komaMapper.insertKoma(newKoma);

      // 3. komaruleテーブルへの挿入
      if (rules != null && !rules.isEmpty()) {
        for (String ruleName : rules) {
          KomaRule komaRule = KomaRule.valueOf(ruleName);
          komaMapper.insertKomaRule(newKoma.getId(), komaRule);
        }
      }

    } catch (RuntimeException e) {
      // エラーログ出力（将来的にはログフレームワークを使用）
      System.err.println("駒の保存に失敗しました: " + e.getMessage());
      e.printStackTrace();
      // トランザクションがロールバックされる
    }

    return "redirect:/deck/make";
  }
}
