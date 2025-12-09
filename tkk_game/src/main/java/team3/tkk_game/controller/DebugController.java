package team3.tkk_game.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import team3.tkk_game.model.Koma;
import team3.tkk_game.service.KomaService;

/**
 * デバッグ用コントローラー
 * 開発・動作確認用のエンドポイントを提供する
 */
@Controller
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    KomaService komaService;

    /**
     * 駒とルールの一覧を表示するデバッグ画面
     * @param model Thymeleafに渡すモデル
     * @return debug.html
     */
    @GetMapping("/koma")
    public String showKomaWithRules(ModelMap model) {
        List<Koma> komaList = komaService.getAllKomaWithRules();
        model.addAttribute("komaList", komaList);
        return "debug.html";
    }
}
