package team3.tkk_game.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Securityの最小設定クラス
 * TOPページからデフォルトログインフォームに遷移しユーザ takahashi/p@ss でログインできることのみを目的とする
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /**
   * 認可設定: 全パス許可し formLogin の導線のみ確認
   * ログアウトは /logout で行い成功時 / に戻る
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .formLogin(login -> login.permitAll())
        .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/"))
        .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
    return http.build();
  }

  /**
   * インメモリユーザ定義
   * ユーザ: takahashi / p@ss (BCryptハッシュ) ロール: USER
   */
  @Bean
  public InMemoryUserDetailsManager userDetailsService() {
    UserDetails takahashi = User.withUsername("takahashi")
        .password("{bcrypt}$2y$10$ngxCDmuVK1TaGchiYQfJ1OAKkd64IH6skGsNw1sLabrTICOHPxC0e")
        .roles("USER")
        .build();
    return new InMemoryUserDetailsManager(takahashi);
  }
}
