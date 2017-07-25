package project.salvo.game;

import javax.persistence.*;

/**
 * Created by Anton on 24.07.2017.
 */
@Entity
public class Score {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long scoreId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    private double points;

    public Score(){}

    public Score(Game game, Player player, double points) {
        this.points = points;
        this.game = game;
        this.player = player;
        game.addScore(this);
        player.addScore(this);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Game getGame() {

        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public long getScoreId() {
        return scoreId;
    }

    public void setScoreId(long scoreId) {
        this.scoreId = scoreId;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }
}
