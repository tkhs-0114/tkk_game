package team3.tkk_game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TkkGameApplication {

  public static void main(String[] args) {
    SpringApplication.run(TkkGameApplication.class, args);
  }

}
