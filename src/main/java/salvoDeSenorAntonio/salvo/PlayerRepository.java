package salvoDeSenorAntonio.salvo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
/**
 * Created by Anton on 11.07.2017.
 */
@RepositoryRestResource
public interface PlayerRepository extends JpaRepository<Player, Long> {
    //List<Player> findByLastName(String lastName);
}
