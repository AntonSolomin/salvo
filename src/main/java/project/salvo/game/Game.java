package project.salvo.game;


import javax.persistence.*;
import java.util.*;


/**
 * Created by Anton on 12.07.2017.
 */

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long gameId;
    private Date gameCreationDate;
    private boolean finished = false;

    @OneToMany(mappedBy="game", fetch=FetchType.LAZY)
    private Set<GamePlayer> gamePlayers = new HashSet<>();

    @OneToMany(mappedBy="game", fetch=FetchType.LAZY)
    private Set<Score> score = new HashSet<>();

    public Game () {
        gameCreationDate = new Date();
    }

    public long getGameId () {
        return this.gameId;
    }

    public Date getDate() { return this.gameCreationDate;}

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void addGamePlayer(GamePlayer inputGamePlayer){
        gamePlayers.add(inputGamePlayer);
    }

    public void addScore (Score score){this.score.add(score);}

    public Date getGameCreationDate (){
        return gameCreationDate;
    }

    public void setGameCreationDate(Date gameCreationDate) {
        this.gameCreationDate = gameCreationDate;
    }

    public void addSeconds (int seconds) {
        this.gameCreationDate = gameCreationDate.from(gameCreationDate.toInstant().plusSeconds(seconds));
    }

    public Set<Score> getScore() {
        return score;
    }

    public void setScore(Set<Score> score) {
        this.score = score;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }
}
