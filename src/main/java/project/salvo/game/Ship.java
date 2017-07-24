package project.salvo.game;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Anton on 18.07.2017.
 */
@Entity
public class Ship {
    public enum ShipClass { BATTLESHIP, CARRIER, SUBMARINE, DESTROYER, PATROL_BOAT }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long shipId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;



    private ShipClass shipClass;

    @ElementCollection
    @Column(name = "locations")
    private List<String> locations = new ArrayList<>();

    public Ship() {}

    public Ship(GamePlayer gamePlayer, List<String> shipLocations, ShipClass boatType) {
        this.gamePlayer = gamePlayer;
        gamePlayer.addShip(this);
        this.locations = shipLocations;
        this.shipClass = boatType;
    }

    public GamePlayer getGamePlayer() {
        return this.gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public ShipClass getShipClass() {
        return shipClass;
    }

    public void setShipClass(ShipClass shipClass) {
        this.shipClass = shipClass;
    }
}