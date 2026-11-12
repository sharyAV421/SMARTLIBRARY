package edu.smartlibrary.repository;

import edu.smartlibrary.model.Libro;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.QueryHint;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "10000"))
    @org.springframework.data.jpa.repository.Query("select l from Libro l where l.id = :id")
    Optional<Libro> findByIdForUpdate(@Param("id") Long id);
}
