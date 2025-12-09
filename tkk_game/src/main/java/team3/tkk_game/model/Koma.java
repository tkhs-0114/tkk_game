package team3.tkk_game.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ゲーム用駒クラス
 * 駒の名前とルールIDのリストを保持する
 */
public class Koma {
  /** 駒名 */
  String name;

  /** 駒ID */
  Integer id;

  /** ルールIDのリスト */
  List<Integer> rules = new ArrayList<>();

  /**
   * デフォルトコンストラクタ
   */
  public Koma() {
  }

  /**
   * コンストラクタ
   * @param name 駒名
   * @param rules ルールIDのリスト
   */
  public Koma(String name, List<Integer> rules) {
    this.name = name;
    this.rules = rules;
  }

  /**
   * 駒が指定された位置に移動可能かどうかを判定する
   * @param fromX 移動元X座標
   * @param fromY 移動元Y座標
   * @param toX 移動先X座標
   * @param toY 移動先Y座標
   * @return 移動可能な場合はtrue
   */
  // この処理を要修正
  public Boolean canMove(int fromX, int fromY, int toX, int toY) {
    return true;
  }

  /**
   * 駒IDを取得する
   * @return 駒ID
   */
  public Integer getId() {
    return id;
  }

  /**
   * 駒名を取得する
   * @return 駒名
   */
  public String getName() {
    return name;
  }

  /**
   * 駒IDを設定する
   * @param id 駒ID
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * 駒名を設定する
   * @param name 駒名
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * ルールIDのリストを取得する
   * @return ルールIDのリスト
   */
  public List<Integer> getRules() {
    return rules;
  }

  /**
   * ルールIDのリストを設定する
   * @param rules ルールIDのリスト
   */
  public void setRules(List<Integer> rules) {
    this.rules = rules;
  }
}
