package team3.tkk_game.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import team3.tkk_game.mapper.KomaMapper;
import team3.tkk_game.mapper.PlayerKomaMapper;
import team3.tkk_game.model.PlayerKoma;
import team3.tkk_game.model.Koma.KomaDB;
import team3.tkk_game.model.Koma.KomaRule;
import team3.tkk_game.model.Koma.KomaSkill;

/**
 * 駒管理機能のコントローラ
 */
@Controller
@RequestMapping("/koma")
public class KomaController {

  @Autowired
  KomaMapper komaMapper;

  @Autowired
  PlayerKomaMapper playerKomaMapper;

  /**
   * 自作駒一覧画面を表示
   * 
   * @param principal 認証情報
   * @param model モデル
   * @return 駒一覧画面
   */
  @GetMapping("/list")
  public String komaList(Principal principal, Model model) {
    model.addAttribute("playerName", principal.getName());
    
    // プレイヤーが所有する駒のみ取得
    List<KomaDB> komas = komaMapper.selectOwnedKomasByPlayerUsername(principal.getName());
    model.addAttribute("komas", komas);

    // 各駒のコストを計算してモデルに追加
    Map<Integer, Integer> komaCosts = new HashMap<>();
    for (KomaDB koma : komas) {
      List<KomaRule> rules = komaMapper.selectKomaRuleById(koma.getId());
      int cost = koma.calculateCost(rules);
      komaCosts.put(koma.getId(), cost);
    }
    model.addAttribute("komaCosts", komaCosts);

    return "komalist.html";
  }

  /**
   * 駒作成画面を表示
   * 
   * @param principal 認証情報
   * @param model モデル
   * @return 駒作成画面
   */
  @GetMapping("/make")
  public String komaMake(Principal principal, Model model) {
    // 自分が使用可能な駒のみ成り先として表示
    List<KomaDB> komas = komaMapper.selectKomasByPlayerUsername(principal.getName());
    List<KomaDB> canUpdateKomas = komas.stream().filter(k -> k.getUpdateKoma() == -1).toList();
    model.addAttribute("canUpdateKomas", canUpdateKomas);

    // 利用可能な移動ルールを追加
    model.addAttribute("availableRules", KomaRule.values());

    // 利用可能な特殊スキルを追加
    model.addAttribute("availableSkills", KomaSkill.values());

    return "komamake.html";
  }

  /**
   * 駒を保存
   * 
   * @param name 駒の名前
   * @param rules 移動ルールのリスト
   * @param skill 特殊スキル
   * @param updateKomaId 成り先駒ID
   * @param principal 認証情報
   * @param redirectAttributes リダイレクト属性
   * @return リダイレクト先
   */
  @PostMapping("/make/save")
  @Transactional
  public String saveKomaData(
      @RequestParam String name,
      @RequestParam(required = false) List<String> rules,
      @RequestParam(required = false) String skill,
      @RequestParam(required = false) Integer updateKomaId,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    try {
      // スキルがNULLの場合はnullに変換
      String skillValue = (skill != null && skill.equals("NULL")) ? null : skill;
      
      // 1. 成り先が指定されていない場合は-1を設定（後で自身のIDに更新）
      Integer tempUpdateKomaId = (updateKomaId == null || updateKomaId == -1) ? -1 : updateKomaId;
      
      // 2. KomaDBオブジェクトを作成
      KomaDB newKoma = new KomaDB(name, skillValue, tempUpdateKomaId);

      // 3. komaテーブルにInsert（IDが自動採番される）
      komaMapper.insertKoma(newKoma);

      // 4. 成り先が指定されていなかった場合、自身のIDで更新
      if (tempUpdateKomaId == -1) {
        newKoma.setUpdateKoma(newKoma.getId());
        komaMapper.updateKoma(newKoma);
      }

      // 5. komaruleテーブルへの挿入
      if (rules != null && !rules.isEmpty()) {
        for (String ruleName : rules) {
          KomaRule komaRule = KomaRule.valueOf(ruleName);
          komaMapper.insertKomaRule(newKoma.getId(), komaRule);
        }
      }

      // 6. 作成者に駒の使用権限を付与（所有者として）
      Integer playerId = playerKomaMapper.selectPlayerIdByUsername(principal.getName());
      if (playerId != null) {
        PlayerKoma playerKoma = new PlayerKoma();
        playerKoma.setPlayerId(playerId);
        playerKoma.setKomaId(newKoma.getId());
        playerKoma.setIsOwner(true);  // 作成者は所有者
        playerKomaMapper.insertPlayerKoma(playerKoma);
      }

      redirectAttributes.addFlashAttribute("success", "駒を作成しました");

    } catch (RuntimeException e) {
      // エラーログ出力
      System.err.println("駒の保存に失敗しました: " + e.getMessage());
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("error", "駒の保存に失敗しました");
    }

    return "redirect:/koma/list";
  }

  /**
   * 駒編集画面を表示
   * 
   * @param komaId 駒ID
   * @param principal 認証情報
   * @param model モデル
   * @param redirectAttributes リダイレクト属性
   * @return 駒編集画面
   */
  @GetMapping("/edit/{id}")
  public String komaEdit(@PathVariable("id") int komaId, Principal principal, Model model,
      RedirectAttributes redirectAttributes) {
    // プレイヤーIDを取得
    Integer playerId = playerKomaMapper.selectPlayerIdByUsername(principal.getName());
    if (playerId == null) {
      redirectAttributes.addFlashAttribute("error", "プレイヤー情報が見つかりません");
      return "redirect:/koma/list";
    }

    // 所有者かどうかをチェック
    Boolean isOwner = playerKomaMapper.isOwner(playerId, komaId);
    if (isOwner == null || !isOwner) {
      redirectAttributes.addFlashAttribute("error", "この駒は編集できません（所有者のみ編集可能）");
      return "redirect:/koma/list";
    }

    // 駒情報を取得
    KomaDB koma = komaMapper.selectKomaById(komaId);
    if (koma == null) {
      redirectAttributes.addFlashAttribute("error", "駒が見つかりません");
      return "redirect:/koma/list";
    }

    // 現在の移動ルールを取得
    List<KomaRule> currentRules = komaMapper.selectKomaRuleById(komaId);

    model.addAttribute("koma", koma);
    model.addAttribute("currentRules", currentRules);

    // 自分が使用可能な駒のみ成り先として表示
    List<KomaDB> komas = komaMapper.selectKomasByPlayerUsername(principal.getName());
    List<KomaDB> canUpdateKomas = komas.stream()
        .filter(k -> k.getUpdateKoma() == -1 && !k.getId().equals(komaId))
        .toList();
    model.addAttribute("canUpdateKomas", canUpdateKomas);

    // 利用可能な移動ルールを追加
    model.addAttribute("availableRules", KomaRule.values());

    // 利用可能な特殊スキルを追加
    model.addAttribute("availableSkills", KomaSkill.values());

    return "komaedit.html";
  }

  /**
   * 駒を更新
   * 
   * @param komaId 駒ID
   * @param name 駒の名前
   * @param rules 移動ルールのリスト
   * @param skill 特殊スキル
   * @param updateKomaId 成り先駒ID
   * @param principal 認証情報
   * @param redirectAttributes リダイレクト属性
   * @return リダイレクト先
   */
  @PostMapping("/update/{id}")
  @Transactional
  public String updateKomaData(
      @PathVariable("id") int komaId,
      @RequestParam String name,
      @RequestParam(required = false) List<String> rules,
      @RequestParam(required = false) String skill,
      @RequestParam(required = false) Integer updateKomaId,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    // プレイヤーIDを取得
    Integer playerId = playerKomaMapper.selectPlayerIdByUsername(principal.getName());
    if (playerId == null) {
      redirectAttributes.addFlashAttribute("error", "プレイヤー情報が見つかりません");
      return "redirect:/koma/list";
    }

    // 所有者かどうかをチェック
    Boolean isOwner = playerKomaMapper.isOwner(playerId, komaId);
    if (isOwner == null || !isOwner) {
      redirectAttributes.addFlashAttribute("error", "この駒は編集できません（所有者のみ編集可能）");
      return "redirect:/koma/list";
    }

    try {
      // スキルがNULLの場合はnullに変換
      String skillValue = (skill != null && skill.equals("NULL")) ? null : skill;
      
      // 1. komaテーブルを更新
      KomaDB koma = new KomaDB(name, skillValue, updateKomaId);
      koma.setId(komaId);
      komaMapper.updateKoma(koma);

      // 2. 既存のルールを削除
      komaMapper.deleteKomaRulesByKomaId(komaId);

      // 3. 新しいルールを挿入
      if (rules != null && !rules.isEmpty()) {
        for (String ruleName : rules) {
          KomaRule komaRule = KomaRule.valueOf(ruleName);
          komaMapper.insertKomaRule(komaId, komaRule);
        }
      }

      redirectAttributes.addFlashAttribute("success", "駒を更新しました");

    } catch (RuntimeException e) {
      // エラーログ出力
      System.err.println("駒の更新に失敗しました: " + e.getMessage());
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("error", "駒の更新に失敗しました");
    }

    return "redirect:/koma/list";
  }

  /**
   * 駒を削除
   * 
   * @param komaId 駒ID
   * @param principal 認証情報
   * @param redirectAttributes リダイレクト属性
   * @return リダイレクト先
   */
  @GetMapping("/delete/{id}")
  @Transactional
  public String deleteKoma(@PathVariable("id") int komaId, Principal principal,
      RedirectAttributes redirectAttributes) {
    // プレイヤーIDを取得
    Integer playerId = playerKomaMapper.selectPlayerIdByUsername(principal.getName());
    if (playerId == null) {
      redirectAttributes.addFlashAttribute("error", "プレイヤー情報が見つかりません");
      return "redirect:/koma/list";
    }

    // 所有者かどうかをチェック
    Boolean isOwner = playerKomaMapper.isOwner(playerId, komaId);
    if (isOwner == null || !isOwner) {
      redirectAttributes.addFlashAttribute("error", "この駒は削除できません（所有者のみ削除可能）");
      return "redirect:/koma/list";
    }

    try {
      // 1. PlayerKomaテーブルから紐づけを削除
      playerKomaMapper.deletePlayerKomasByKomaId(komaId);

      // 2. KomaRuleテーブルからルールを削除
      komaMapper.deleteKomaRulesByKomaId(komaId);

      // 3. komaテーブルから駒を削除
      komaMapper.deleteKomaById(komaId);

      redirectAttributes.addFlashAttribute("success", "駒を削除しました");

    } catch (RuntimeException e) {
      // エラーログ出力
      System.err.println("駒の削除に失敗しました: " + e.getMessage());
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("error", "駒の削除に失敗しました");
    }

    return "redirect:/koma/list";
  }
}
