package project.salvo.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by Anton on 24.07.2017.
 */
@RepositoryRestResource
public interface ScoreRepository extends JpaRepository<Score, Long> {
}
