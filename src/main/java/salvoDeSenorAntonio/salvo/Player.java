package salvoDeSenorAntonio.salvo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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


    public Player() {
    }

    public Player(String inputFirstName, String inputLastname, String inputUserName) {
        firstName = inputFirstName;
        lastName = inputLastname;
        userName = inputUserName;
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
}
