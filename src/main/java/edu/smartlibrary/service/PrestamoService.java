package edu.smartlibrary.service;

import edu.smartlibrary.dto.PrestamoRequest;
import edu.smartlibrary.dto.PrestamoResponse;
import edu.smartlibrary.exception.LibroNoDisponibleException;
import edu.smartlibrary.exception.RecursoNoEncontradoException;
import edu.smartlibrary.model.Estudiante;
import edu.smartlibrary.model.Libro;
import edu.smartlibrary.model.Prestamo;
import edu.smartlibrary.repository.EstudianteRepository;
import edu.smartlibrary.repository.LibroRepository;
import edu.smartlibrary.repository.PrestamoRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrestamoService {
    private final EstudianteRepository estudianteRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;

    public PrestamoService(EstudianteRepository estudianteRepository, LibroRepository libroRepository,
                           PrestamoRepository prestamoRepository) {
        this.estudianteRepository = estudianteRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
    }

    @Transactional
    public PrestamoResponse registrar(PrestamoRequest solicitud) {
        Estudiante estudiante = estudianteRepository.findById(solicitud.estudianteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estudiante " + solicitud.estudianteId()));
        Libro libro = libroRepository.findByIdForUpdate(solicitud.libroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el libro " + solicitud.libroId()));

        if (!libro.isDisponible() || prestamoRepository.existsByLibroIdAndFechaDevolucionIsNull(libro.getId())) {
            throw new LibroNoDisponibleException(libro.getId());
        }

        libro.marcarComoPrestado();
        Prestamo prestamo = new Prestamo(LocalDate.now(), estudiante, libro);
        return PrestamoResponse.desde(prestamoRepository.save(prestamo));
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> listar() {
        return prestamoRepository.findAllByOrderByIdAsc().stream().map(PrestamoResponse::desde).toList();
    }
}
