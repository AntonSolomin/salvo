package project.salvo.game;

import antlr.LexerSharedInputState;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalvoApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(PlayerRepository playerRepository,
                                      GameRepository gameRepository,
                                      GamePlayerRepository gamePlayerRepository,
                                      ShipRepository shipRepository,
                                      SalvoRepository salvoRepository,
                                      ScoreRepository scoreRepository) {
        return (args) -> {
            Player p1 = new Player("Jack", "Bauer", "1@gmail.com");
            Player p2 = new Player("Chloe", "O'Brian", "2@gmail.com");
            Player p3 = new Player("Kim", "Bauer", "3@gmail.com");
            Player p4 = new Player("David", "Palmer", "4@gmail.com");
            Player p5 = new Player("Michelle", "Dessler", "5@gmail.com");
            Player p6 = new Player("Dick", "Dickenson", "123@mail.com");

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

            GamePlayer gameTwoPlayerOne = new GamePlayer(p1, mySecondGame);
            GamePlayer gameTwoPlayerTwo = new GamePlayer(p2, mySecondGame);
            GamePlayer gameThreePlayerOne = new GamePlayer(p5, myThirdGame);

            gamePlayerRepository.save(gameTwoPlayerOne);
            gamePlayerRepository.save(gameTwoPlayerTwo);
            gamePlayerRepository.save(gameThreePlayerOne);

            Ship s1 = new Ship(gameTwoPlayerOne,  new ArrayList<>(Arrays.asList("A2", "B2", "C2")), Ship.ShipClass.DESTROYER);
            shipRepository.save(s1);

            Ship s2 = new Ship(gameTwoPlayerOne,  new ArrayList<>(Arrays.asList("C3", "C4", "C5")), Ship.ShipClass.SUBMARINE);
            shipRepository.save(s2);

            Ship s3 = new Ship(gameTwoPlayerTwo,  new ArrayList<>(Arrays.asList("D7", "E7", "F7")), Ship.ShipClass.SUBMARINE);
            shipRepository.save(s3);

            Ship s4 = new Ship(gameThreePlayerOne,  new ArrayList<>(Arrays.asList("A1", "A2", "A3")), Ship.ShipClass.SUBMARINE);
            shipRepository.save(s4);

            Salvo newSalvo = new Salvo(gameTwoPlayerOne, new ArrayList<>(Arrays.asList("A1", "A2", "A4")), 4);
            salvoRepository.save(newSalvo);

            Salvo secondSalvo = new Salvo(gameTwoPlayerTwo, new ArrayList<>(Arrays.asList("B1", "B2", "C3")), 8);
            Salvo thirdSalvo = new Salvo(gameTwoPlayerTwo, new ArrayList<>(Arrays.asList("B8", "B8", "C8")) ,27);
            salvoRepository.save(secondSalvo);
            salvoRepository.save(thirdSalvo);

            Score newScore = new Score(mySecondGame, p1, 1);
            Score newOtherScore = new Score(mySecondGame, p2, 0);
            scoreRepository.save(newScore);
            scoreRepository.save(newOtherScore);
        };
    }
}
