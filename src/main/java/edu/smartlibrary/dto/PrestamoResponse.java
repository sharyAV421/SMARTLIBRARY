package edu.smartlibrary.dto;

import edu.smartlibrary.model.Prestamo;
import java.time.LocalDate;

public record PrestamoResponse(Long id, LocalDate fechaPrestamo, LocalDate fechaDevolucion,
                               Long estudianteId, String estudianteNombre, Long libroId, String libroTitulo) {
    public static PrestamoResponse desde(Prestamo prestamo) {
        return new PrestamoResponse(prestamo.getId(), prestamo.getFechaPrestamo(), prestamo.getFechaDevolucion(),
                prestamo.getEstudiante().getId(), prestamo.getEstudiante().getNombre(),
                prestamo.getLibro().getId(), prestamo.getLibro().getTitulo());
    }
}
