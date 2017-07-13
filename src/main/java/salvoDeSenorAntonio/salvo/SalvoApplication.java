package salvoDeSenorAntonio.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository,
                                      GameRepository gameRepository,
                                      GamePlayerRepository gamePlayerRepository) {
		return (args) -> {
            Player p1 = new Player("Jack", "Bauer", "1@gmail.com");
            Player p2 = new Player("Chloe", "O'Brian", "2@gmail.com");
            Player p3 = new Player("Kim", "Bauer", "3@gmail.com");
            Player p4 = new Player("David", "Palmer", "4@gmail.com");
            Player p5 = new Player("Michelle", "Dessler", "5@gmail.com");
            Player p6 = new Player( "Dick", "Dickenson", "123@mail.com");

			playerRepository.save(p1);
			playerRepository.save(p2);
			playerRepository.save(p3);
			playerRepository.save(p4);
			playerRepository.save(p5);
			playerRepository.save(p6);

			gameRepository.save(new Game());

			Game mySecondGame = new Game();
            mySecondGame.addSeconds(3600);

			Game myThirdGame = new Game();
            myThirdGame.addSeconds(7200);

            gameRepository.save(mySecondGame);
            gameRepository.save(myThirdGame);


            GamePlayer myGamePlayer = new GamePlayer(p1, mySecondGame);
            gamePlayerRepository.save(myGamePlayer);

		};
	}
}
