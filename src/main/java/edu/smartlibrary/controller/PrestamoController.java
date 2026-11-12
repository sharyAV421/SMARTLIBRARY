package edu.smartlibrary.controller;

import edu.smartlibrary.dto.PrestamoRequest;
import edu.smartlibrary.dto.PrestamoResponse;
import edu.smartlibrary.service.PrestamoService;
import jakarta.validation.Valid;
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
public class PrestamoController {
    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) { this.prestamoService = prestamoService; }

    @PostMapping
    public ResponseEntity<PrestamoResponse> registrar(@Valid @RequestBody PrestamoRequest solicitud) {
        PrestamoResponse prestamo = prestamoService.registrar(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(prestamo.id()).toUri();
        return ResponseEntity.created(ubicacion).body(prestamo);
    }

    @GetMapping
    public List<PrestamoResponse> listar() { return prestamoService.listar(); }
}
