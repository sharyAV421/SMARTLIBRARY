package edu.smartlibrary.repository;

import edu.smartlibrary.model.Prestamo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    boolean existsByLibroIdAndFechaDevolucionIsNull(Long libroId);
    List<Prestamo> findAllByOrderByIdAsc();
}
