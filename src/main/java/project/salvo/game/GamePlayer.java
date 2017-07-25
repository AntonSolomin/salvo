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
    private long GamePlayerId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @OneToMany(mappedBy="gamePlayer", fetch = FetchType.EAGER)
    private Set<Ship> ships = new HashSet<>();

    @OneToMany(mappedBy="gamePlayer", fetch = FetchType.EAGER)
    private Set<Salvo> locations = new HashSet<>();

    private Date gamePlayerCreationDate;

    public GamePlayer() {
        gamePlayerCreationDate = new Date();
    }
    public GamePlayer(Player inputPlayer, Game inputGame) {
        gamePlayerCreationDate = new Date();
        game = inputGame;
        player = inputPlayer;
        inputGame.addGamePlayer(this);
        inputPlayer.addGamePlayer(this);
    }

    public Date getGamePlayerCreationDate (){
        return gamePlayerCreationDate;
    }
    public void setGamePlayerCreationDate(Date gamePlayerCreationDate) {
        this.gamePlayerCreationDate = gamePlayerCreationDate;
    }

    public Game getGame() {
        return game;
    }
    public void setGame(Game game) {
        this.game = game;
    }

    public Set<Score> getScore () {
        return player.getScores();
    }

    public Set<Ship> getShips() {
        return ships;
    }
    public void setShips(Set<Ship> ships) {
        this.ships = ships;
    }

    public Set<Salvo> getSalvo() {
        return locations;
    }
    public void setSalvo(Set<Salvo> salvo) {
        this.locations = salvo;
    }

    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }

    public void addShip(Ship newShip){ships.add(newShip);}

    public void addSalvo(Salvo newSalvo){locations.add(newSalvo);}

    public long getId () {return this.GamePlayerId;}

}
