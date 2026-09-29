package io.github.dobrosavljevicnenad.dressinventory.dress;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DressRepository extends JpaRepository<Dress, UUID> {

    /** Loads the whole catalog with stock in a single query (no N+1 selects). */
    @EntityGraph(attributePaths = "stock")
    List<Dress> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "stock")
    Optional<Dress> findWithStockById(UUID id);
}
