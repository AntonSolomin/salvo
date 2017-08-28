package project.salvo.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
            final Player p1 = new Player("Jack", "Bauer", "1@gmail.com", "123");
            final Player p2 = new Player("Chloe", "O'Brian", "2@gmail.com", "qwe");
            final Player p3 = new Player("Kim", "Bauer", "3@gmail.com", "asd");
            final Player p4 = new Player("David", "Palmer", "4@gmail.com", "zxc");
            final Player p5 = new Player("Michelle", "Dessler", "5@gmail.com", "234");
            final Player p6 = new Player("Dick", "Dickenson", "123@mail.com", "345");

            playerRepository.save(p1);
            playerRepository.save(p2);
            playerRepository.save(p3);
            playerRepository.save(p4);
            playerRepository.save(p5);
            playerRepository.save(p6);

            //gameRepository.save(new Game());

            final Game mySecondGame = new Game();
            mySecondGame.addSeconds(3600);

            final Game myThirdGame = new Game();
            myThirdGame.addSeconds(7200);

            gameRepository.save(mySecondGame);
            gameRepository.save(myThirdGame);

            final GamePlayer gameTwoPlayerOne = new GamePlayer(p1, mySecondGame);
            final GamePlayer gameTwoPlayerTwo = new GamePlayer(p2, mySecondGame);
            final GamePlayer gameThreePlayerOne = new GamePlayer(p5, myThirdGame);

            gamePlayerRepository.save(gameTwoPlayerOne);
            gamePlayerRepository.save(gameTwoPlayerTwo);
            gamePlayerRepository.save(gameThreePlayerOne);

            final Ship s1 = new Ship(gameTwoPlayerOne,  new ArrayList<>(Arrays.asList("A2", "B2", "C2")), Ship.ShipClass.DESTROYER);
            shipRepository.save(s1);

            final Ship s2 = new Ship(gameTwoPlayerOne,  new ArrayList<>(Arrays.asList("C3", "C4", "C5")), Ship.ShipClass.SUBMARINE);
            shipRepository.save(s2);

            final Ship s3 = new Ship(gameTwoPlayerTwo,  new ArrayList<>(Arrays.asList("D7", "E7", "F7")), Ship.ShipClass.SUBMARINE);
            shipRepository.save(s3);

            final Ship s4 = new Ship(gameThreePlayerOne,  new ArrayList<>(Arrays.asList("A1", "A2", "A3")), Ship.ShipClass.SUBMARINE);
            shipRepository.save(s4);

            final Salvo newSalvo = new Salvo(gameTwoPlayerOne, new ArrayList<>(Arrays.asList("A1", "A2", "A4")), 4);
            salvoRepository.save(newSalvo);

            final Salvo secondSalvo = new Salvo(gameTwoPlayerTwo, new ArrayList<>(Arrays.asList("B1", "B2", "C3")), 8);
            final Salvo thirdSalvo = new Salvo(gameTwoPlayerTwo, new ArrayList<>(Arrays.asList("B8", "B8", "C8")) ,27);
            salvoRepository.save(secondSalvo);
            salvoRepository.save(thirdSalvo);

            final Score newScore = new Score(mySecondGame, p1, 1);
            final Score newOtherScore = new Score(mySecondGame, p2, 0);
            scoreRepository.save(newScore);
            scoreRepository.save(newOtherScore);
        };
    }
}


@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    PlayerRepository playerRepository;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
    }

    @Bean
    UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                Player player = playerRepository.findByUserName(username);
                if (player != null) {
                    return new User(player.getUserName(), player.getPassword(),
                            AuthorityUtils.createAuthorityList("USER"));
                } else {
                    throw new UsernameNotFoundException("Unknown user: " + username);
                }
            }
        };
    }
}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/api/game_view/**").hasAuthority("USER")
                .antMatchers("/api/games").permitAll()
                .and()
                .formLogin();

        http.formLogin()
                .usernameParameter("userName")
                .passwordParameter("password")
                .loginPage("/api/login");

        http.logout().logoutUrl("/api/logout");

        // turn off checking for CSRF tokens
        http.csrf().disable();

        // if user is not authenticated, just send an authentication failure response
        http.exceptionHandling()
                .authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if login is successful, clear the flags asking for authentication
        http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

        // if login fails, just send an authentication failure response
        http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if logout is successful, just send a success response
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}