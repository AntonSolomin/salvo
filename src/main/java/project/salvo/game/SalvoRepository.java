package project.salvo.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by Anton on 20.07.2017.
 */
@RepositoryRestResource
public interface SalvoRepository extends JpaRepository<Salvo, Long> {}
