package edu.smartlibrary.repository;

import edu.smartlibrary.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> { }
