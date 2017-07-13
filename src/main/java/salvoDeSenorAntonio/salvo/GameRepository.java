package salvoDeSenorAntonio.salvo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
/**
 * Created by Anton on 12.07.2017.
 */
@RepositoryRestResource
public interface  GameRepository extends JpaRepository<Game, Long> {
}
