package de.angr2301.genericllmadapter.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository für User-Entitäten.
 * Spring erkennt dieses Interface über @EnableJpaRepositories automatisch.
 */
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
}
