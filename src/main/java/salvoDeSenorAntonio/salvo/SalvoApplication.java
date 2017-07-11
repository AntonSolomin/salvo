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
	public CommandLineRunner initData(PlayerRepository repository) {
		return (args) -> {
			// new sample players
			repository.save(new Player("Jack", "Bauer", "1@gmail.com"));
			repository.save(new Player("Chloe", "O'Brian", "2@gmail.com"));
			repository.save(new Player("Kim", "Bauer", "3@gmail.com"));
			repository.save(new Player("David", "Palmer", "4@gmail.com"));
			repository.save(new Player("Michelle", "Dessler", "5@gmail.com"));
		};
	}
}
