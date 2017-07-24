package project.salvo.game;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton on 20.07.2017.
 */
@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long salvoId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    private int turnNumber;

    @ElementCollection
    @Column(name = "shotLocations")
    private List<String> shotLocations = new ArrayList<>();

    public Salvo() {}
    public Salvo(GamePlayer gamePlayer, List<String> shotLocations, int turn) {
        this.gamePlayer = gamePlayer;
        gamePlayer.addSalvo(this);
        this.shotLocations = shotLocations;
        this.turnNumber = turn;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public List<String> getShotLocations() {
        return shotLocations;
    }

    public void setShotLocations(List<String> shotLocations) {
        this.shotLocations = shotLocations;
    }

    public long getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }
}
