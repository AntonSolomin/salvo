package salvoDeSenorAntonio.salvo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;



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

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
    public long getId () {return this.GamePlayerId;}
}
