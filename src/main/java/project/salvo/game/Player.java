package project.salvo.game;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Anton on 10.07.2017.
 */
@Entity
public class Player {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long userId;
    private String firstName;
    private String lastName;
    private String userName;

    private String password;

    @OneToMany(mappedBy="player", fetch=FetchType.LAZY)
    private Set<GamePlayer> gamePlayers = new HashSet<>();

    @OneToMany(mappedBy="player", fetch=FetchType.LAZY)
    private Set<Score> scores = new HashSet<>();

    public Player() {}

    public Player(String firstName, String inputLastname, String inputUserName, String password) {
        this.firstName = firstName;
        this.password = password;
        lastName = inputLastname;
        userName = inputUserName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Score> getScores() {
        return scores;
    }

    public void setScores(Set<Score> score) {
        this.scores = score;
    }

    public long getUserId() {
        return userId;
    }

    public void addGamePlayer(GamePlayer inputGamePlayer){
        gamePlayers.add(inputGamePlayer);
    }

    public void addScore (Score score) {
        this.scores.add(score);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }
}
