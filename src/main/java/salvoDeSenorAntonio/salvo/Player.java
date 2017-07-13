package salvoDeSenorAntonio.salvo;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
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

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    private Set<GamePlayer> gamePlayers = new HashSet<>();


    public Player() {

    }

    public Player(String inputFirstName, String inputLastname, String inputUserName) {
        firstName = inputFirstName;
        lastName = inputLastname;
        userName = inputUserName;
    }

    public long getUserId() {
        return userId;
    }

    public void addGamePlayer(GamePlayer inputGamePlayer){
        gamePlayers.add(inputGamePlayer);

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
