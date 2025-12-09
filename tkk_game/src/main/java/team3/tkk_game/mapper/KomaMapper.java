package team3.tkk_game.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import team3.tkk_game.model.KomaEntity;
import team3.tkk_game.model.KomaRuleRow;

/**
 * 駒関連のデータベースアクセスを行うMapperインターフェース
 */
@Mapper
public interface KomaMapper {

  /**
   * 全ての駒を取得する
   * @return 駒エンティティのリスト
   */
  @Select("SELECT id, name FROM koma")
  List<KomaEntity> selectAllKoma();

  /**
   * 駒とルールの組み合わせを全て取得する
   * KOMARULE、KOMA、RULEテーブルをJOINし、駒ID、駒名、ルールIDを取得する
   * @return 駒とルールの組み合わせリスト（1駒1ルールで1行）
   */
  @Select("SELECT KOMARULE.koma_id AS komaId, KOMA.name AS name, KOMARULE.rule_id AS ruleId " +
          "FROM KOMARULE " +
          "JOIN KOMA ON KOMARULE.koma_id = KOMA.id " +
          "JOIN RULE ON KOMARULE.rule_id = RULE.id " +
          "ORDER BY KOMARULE.koma_id")
  List<KomaRuleRow> selectAllKomaWithRules();

}
