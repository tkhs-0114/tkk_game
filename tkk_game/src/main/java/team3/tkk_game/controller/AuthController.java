package team3.tkk_game.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import team3.tkk_game.services.AuthService;

/**
 * 認証関連のコントローラ
 * ユーザー登録画面と登録処理を担当
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

  @Autowired
  private AuthService authService;

  /**
   * ユーザー登録画面を表示
   * 
   * @param model モデル
   * @return 登録画面
   */
  @GetMapping("/register")
  public String showRegisterForm(Model model) {
    return "register.html";
  }

  /**
   * ユーザー登録処理
   * 
   * @param username ユーザー名
   * @param password パスワード
   * @param confirmPassword パスワード確認
   * @param redirectAttributes リダイレクト属性
   * @return リダイレクト先
   */
  @PostMapping("/register")
  public String registerUser(
      @RequestParam String username,
      @RequestParam String password,
      @RequestParam String confirmPassword,
      RedirectAttributes redirectAttributes) {

    // バリデーション
    if (username == null || username.trim().isEmpty()) {
      redirectAttributes.addFlashAttribute("error", "ユーザー名を入力してください");
      return "redirect:/auth/register";
    }

    if (password == null || password.isEmpty()) {
      redirectAttributes.addFlashAttribute("error", "パスワードを入力してください");
      return "redirect:/auth/register";
    }

    if (!password.equals(confirmPassword)) {
      redirectAttributes.addFlashAttribute("error", "パスワードが一致しません");
      return "redirect:/auth/register";
    }

    if (username.length() < 3 || username.length() > 20) {
      redirectAttributes.addFlashAttribute("error", "ユーザー名は3〜20文字で入力してください");
      return "redirect:/auth/register";
    }

    if (password.length() < 4) {
      redirectAttributes.addFlashAttribute("error", "パスワードは4文字以上で入力してください");
      return "redirect:/auth/register";
    }

    try {
      // ユーザー登録
      authService.registerUser(username.trim(), password);
      redirectAttributes.addFlashAttribute("success", "アカウントを作成しました。ログインしてください。");
      return "redirect:/auth/register";
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/auth/register";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "登録に失敗しました: " + e.getMessage());
      return "redirect:/auth/register";
    }
  }
}
