package project.salvo.game;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by Anton on 12.07.2017.
 */
@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long gamePlayerId;

    private boolean firstGamePlayer = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="player_id")
    private Player player;

    @OneToMany(mappedBy="gamePlayer", fetch = FetchType.LAZY)
    private Set<Ship> ships = new HashSet<>();

    @OneToMany(mappedBy="gamePlayer", fetch = FetchType.LAZY)
    private Set<Salvo> salvos = new HashSet<>();

    private Date gamePlayerCreationDate;

    public GamePlayer() {
        gamePlayerCreationDate = new Date();
    }

    public GamePlayer(Player inputPlayer, Game inputGame) {
        //checking if the gp is first in the game
        if (inputGame.getGamePlayers().size() == 0) {
            firstGamePlayer = true;
        }

        gamePlayerCreationDate = new Date();
        game = inputGame;
        player = inputPlayer;
        inputGame.addGamePlayer(this);
        inputPlayer.addGamePlayer(this);
    }

    public Date getGamePlayerCreationDate(){
        return gamePlayerCreationDate;
    }

    public void setGamePlayerCreationDate(Date gamePlayerCreationDate) {
        this.gamePlayerCreationDate = gamePlayerCreationDate;
    }

    public Game getGame() {
        return game;
    }

    public GamePlayer getEnemyGamePlayer() {
        final Set<GamePlayer> gamePlayers = this.getGame().getGamePlayers();
        GamePlayer theOtherGamePlayer = null;
        for (GamePlayer gamePlayer1 : gamePlayers) {
            // if one of them is not me
            if (this.getId() != gamePlayer1.getId()) {
                // then this is the other gp
                theOtherGamePlayer = gamePlayer1;
                break;
            }
        }
        return theOtherGamePlayer;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Set<Score> getScore() {
        return player.getScores();
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public void setShips(Set<Ship> ships) {
        this.ships = ships;
    }

    public Set<Salvo> getSalvo() {
        return salvos;
    }

    public void setSalvo(Set<Salvo> salvo) {
        this.salvos = salvo;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void addShip(Ship newShip){ships.add(newShip);}

    public void addSalvo(Salvo newSalvo){
        salvos.add(newSalvo);}

    public long getId () {return this.gamePlayerId;}

    public boolean isFirstGamePlayer() {
        return firstGamePlayer;
    }

    public void setFirstGamePlayer(boolean firstGamePlayer) {
        this.firstGamePlayer = firstGamePlayer;
    }

}
