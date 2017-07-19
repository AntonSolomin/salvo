package project.salvo.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
/**
 * Created by Anton on 11.07.2017.
 */
@RepositoryRestResource
public interface PlayerRepository extends JpaRepository<Player, Long> {}
