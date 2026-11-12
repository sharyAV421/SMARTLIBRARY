package edu.smartlibrary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.smartlibrary.dto.PrestamoRequest;
import edu.smartlibrary.model.Libro;
import edu.smartlibrary.repository.LibroRepository;
import edu.smartlibrary.repository.PrestamoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
@AutoConfigureMockMvc
class PrestamoControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private LibroRepository libroRepository;
    @Autowired private PrestamoRepository prestamoRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void limpiarPrestamos() {
        prestamoRepository.deleteAll();
        jdbcTemplate.update("UPDATE libros SET disponible = TRUE WHERE id IN (1, 2)");
    }

    @Test
    void registraPrestamoDisponibleYActualizaDisponibilidad() throws Exception {
        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PrestamoRequest(1L, 1L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estudianteId").value(1))
                .andExpect(jsonPath("$.libroId").value(1))
                .andExpect(jsonPath("$.fechaPrestamo").exists());

        assertThat(libroRepository.findById(1L).orElseThrow().isDisponible()).isFalse();
        assertThat(prestamoRepository.count()).isEqualTo(1);
    }

    @Test
    void rechazaLibroInicialmenteNoDisponibleSinCrearPrestamo() throws Exception {
        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PrestamoRequest(1L, 3L))))
                .andExpect(status().isConflict());
        assertThat(prestamoRepository.count()).isZero();
    }

    @Test
    void rechazaEstudianteInexistenteSinCrearPrestamo() throws Exception {
        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PrestamoRequest(999L, 1L))))
                .andExpect(status().isNotFound());
        assertThat(prestamoRepository.count()).isZero();
        assertThat(libroRepository.findById(1L).orElseThrow().isDisponible()).isTrue();
    }

    @Test
    void rechazaLibroInexistenteSinCrearPrestamo() throws Exception {
        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PrestamoRequest(1L, 999L))))
                .andExpect(status().isNotFound());
        assertThat(prestamoRepository.count()).isZero();
    }

    @Test
    void consultaPrestamosRegistrados() throws Exception {
        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PrestamoRequest(1L, 2L)))).andExpect(status().isCreated());

        mockMvc.perform(get("/api/prestamos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estudianteId").value(1))
                .andExpect(jsonPath("$[0].libroId").value(2));
    }

    @Test
    void publicaSwaggerUiYDocumentaLosEndpointsExistentes() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        String documento = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var paths = objectMapper.readTree(documento).path("paths").path("/api/prestamos");

        assertThat(paths.has("post")).isTrue();
        assertThat(paths.path("post").path("responses").has("201")).isTrue();
        assertThat(paths.path("post").path("responses").has("400")).isTrue();
        assertThat(paths.path("post").path("responses").has("404")).isTrue();
        assertThat(paths.path("post").path("responses").has("409")).isTrue();
        assertThat(paths.has("get")).isTrue();
        assertThat(paths.path("get").path("responses").has("200")).isTrue();
    }

    @Test
    void validaCuerpoYNoCreaPrestamoSiFaltanIds() throws Exception {
        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        assertThat(prestamoRepository.count()).isZero();
    }

    @Test
    void impideDosPrestamosActivosDelMismoLibro() throws Exception {
        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PrestamoRequest(1L, 1L)))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PrestamoRequest(2L, 1L)))).andExpect(status().isConflict());
        assertThat(prestamoRepository.count()).isEqualTo(1);
    }

    @Test
    void solicitudesConcurrentesSoloRegistranUnPrestamo() {
        var primera = CompletableFuture.supplyAsync(() -> enviarPrestamo(1L, 2L));
        var segunda = CompletableFuture.supplyAsync(() -> enviarPrestamo(2L, 2L));

        CompletableFuture.allOf(primera, segunda).join();
        assertThat(java.util.List.of(primera.join(), segunda.join())).containsExactlyInAnyOrder(201, 409);
        assertThat(prestamoRepository.count()).isEqualTo(1);
        assertThat(libroRepository.findById(2L).orElseThrow().isDisponible()).isFalse();
    }

    private int enviarPrestamo(Long estudianteId, Long libroId) {
        try {
            return mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new PrestamoRequest(estudianteId, libroId))))
                    .andReturn().getResponse().getStatus();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
