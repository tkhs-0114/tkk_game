package team3.tkk_game.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import team3.tkk_game.mapper.KomaMapper;
import team3.tkk_game.model.Koma;
import team3.tkk_game.model.KomaRuleRow;

/**
 * 駒関連のビジネスロジックを提供するサービスクラス
 */
@Service
public class KomaService {

  @Autowired
  KomaMapper komaMapper;

  /**
   * 全ての駒をルール付きで取得する
   * JOINクエリの結果を駒ごとに集約し、各駒のrulesリストにルールIDを格納する
   * 
   * @return 駒のリスト（各駒にルールIDのリストが設定されている）
   */
  public List<Koma> getAllKomaWithRules() {
    // JOINクエリで全データを取得
    List<KomaRuleRow> rows = komaMapper.selectAllKomaWithRules();

    // komaIdをキーにしてKomaオブジェクトを集約
    Map<Integer, Koma> komaMap = new LinkedHashMap<>();

    for (KomaRuleRow row : rows) {
      Integer komaId = row.getKomaId();

      if (!komaMap.containsKey(komaId)) {
        // 新しい駒を作成
        List<Integer> rules = new ArrayList<>();
        rules.add(row.getRuleId());
        Koma koma = new Koma(row.getName(), rules);
        koma.setId(komaId);
        komaMap.put(komaId, koma);
      } else {
        // 既存の駒にルールを追加
        komaMap.get(komaId).getRules().add(row.getRuleId());
      }
    }

    return new ArrayList<>(komaMap.values());
  }
}
