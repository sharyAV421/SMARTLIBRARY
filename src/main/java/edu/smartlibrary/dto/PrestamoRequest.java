package edu.smartlibrary.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PrestamoRequest(
        @NotNull(message = "estudianteId es obligatorio") @Positive(message = "estudianteId debe ser positivo") Long estudianteId,
        @NotNull(message = "libroId es obligatorio") @Positive(message = "libroId debe ser positivo") Long libroId) { }
