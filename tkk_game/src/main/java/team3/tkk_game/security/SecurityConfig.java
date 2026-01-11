package team3.tkk_game.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Securityの設定クラス
 * InMemoryUserDetailsManagerを使用し、動的にユーザーを追加可能
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /**
   * 認可設定: formLoginと登録画面への許可
   * ログアウトは /logout で行い成功時 / に戻る
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .formLogin(login -> login
            .permitAll().defaultSuccessUrl("/home", true))
        .logout(logout -> logout
            .logoutUrl("/logout").logoutSuccessUrl("/"))
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/h2-console/**", "/game/disconnect"))
        .headers(headers -> headers.frameOptions(frame -> frame.disable()))
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/h2-console/**", "/", "/index.html", "/register", "/auth/**", "/css/**", "/js/**").permitAll()
            .anyRequest().authenticated());
    return http.build();
  }

  /**
   * パスワードエンコーダー（BCrypt）
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * InMemoryUserDetailsManager（初期ユーザーなし、動的追加用）
   */
  @Bean
  public InMemoryUserDetailsManager userDetailsService() {
    UserDetails admin = User.withUsername("admin")
        .password("$2y$10$ngxCDmuVK1TaGchiYQfJ1OAKkd64IH6skGsNw1sLabrTICOHPxC0e")
        .roles("ADMIN")
        .build();
    return new InMemoryUserDetailsManager(admin);
  }
}
