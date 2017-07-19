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

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    private Set<GamePlayer> gamePlayers = new HashSet<>();

    public Game () {
        gameCreationDate = new Date();
    }
    public long getGameId () {
        return this.gameId;
    }
    public Date getDate() { return this.gameCreationDate;}
    public void addGamePlayer(GamePlayer inputGamePlayer){
        gamePlayers.add(inputGamePlayer);
    }
    public Date getGameCreationDate (){
        return gameCreationDate;
    }
    public void setGameCreationDate(Date gameCreationDate) {
        this.gameCreationDate = gameCreationDate;
    }
    public void addSeconds (int seconds) {
        this.gameCreationDate = gameCreationDate.from(gameCreationDate.toInstant().plusSeconds(seconds));
    }
    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }
    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }
}
