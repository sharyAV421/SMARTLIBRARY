package edu.smartlibrary.controller;

import edu.smartlibrary.dto.PrestamoRequest;
import edu.smartlibrary.dto.PrestamoResponse;
import edu.smartlibrary.service.PrestamoService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/prestamos")
@Tag(name = "Préstamos", description = "Registro y consulta de préstamos bibliotecarios")
public class PrestamoController {
    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) { this.prestamoService = prestamoService; }

    @PostMapping
    @Operation(summary = "Registrar un préstamo", description = "Registra un préstamo para un estudiante y un libro disponible.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Préstamo registrado"),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos"),
            @ApiResponse(responseCode = "404", description = "Estudiante o libro inexistente"),
            @ApiResponse(responseCode = "409", description = "Libro no disponible")
    })
    public ResponseEntity<PrestamoResponse> registrar(@Valid @RequestBody PrestamoRequest solicitud) {
        PrestamoResponse prestamo = prestamoService.registrar(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(prestamo.id()).toUri();
        return ResponseEntity.created(ubicacion).body(prestamo);
    }

    @GetMapping
    @Operation(summary = "Consultar préstamos", description = "Devuelve todos los préstamos registrados, ordenados por ID.")
    @ApiResponse(responseCode = "200", description = "Lista de préstamos")
    public List<PrestamoResponse> listar() { return prestamoService.listar(); }
}
