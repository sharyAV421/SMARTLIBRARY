# SmartLibrary

API REST académica para administrar estudiantes, libros y préstamos bibliotecarios. El caso de uso permite registrar un préstamo y consultar el historial. La fecha se genera en el servidor y cada libro puede tener como máximo un préstamo activo.


## Arquitectura

El flujo respeta `Controller → Service → Repository`. El controlador traduce HTTP y valida el DTO; el servicio concentra las reglas y la transacción; los repositorios encapsulan persistencia. Las entidades del dominio son `Estudiante`, `Libro` y `Prestamo`, sin composición ni agregación.

### Paquetes

```text
edu.smartlibrary
├── controller   PrestamoController
├── service      PrestamoService
├── repository   EstudianteRepository, LibroRepository, PrestamoRepository
├── model        Estudiante, Libro, Prestamo
├── dto          PrestamoRequest, PrestamoResponse
└── exception    excepciones de negocio y GlobalExceptionHandler
```

Los diagramas y justificación de decisiones están en `docs/`.



La aplicación usa `PORT` si está definido y, de lo contrario, el puerto `8080`. Para comprobar otro puerto en PowerShell:

```powershell
$env:PORT = "18080"
.\mvnw.cmd spring-boot:run
```

H2 inicia con dos estudiantes (IDs 1 y 2) y tres libros (IDs 1, 2 y 3); el libro 3 empieza no disponible. La consola H2 local está en `http://localhost:8080/h2-console`, con JDBC URL `jdbc:h2:mem:smartlibrary`, usuario `sa` y contraseña vacía.

## API

La interfaz Swagger UI está disponible en `http://localhost:8080/swagger-ui/index.html` y el documento OpenAPI JSON en `http://localhost:8080/v3/api-docs`.

### Registrar préstamo

`POST /api/prestamos`

```json
{
  "estudianteId": 1,
  "libroId": 1
}
```

Respuestas: `201 Created`, `400 Bad Request` por datos inválidos, `404 Not Found` por estudiante/libro inexistente y `409 Conflict` si el libro no está disponible. El `201` incluye la cabecera `Location` y un cuerpo con fecha, IDs y nombres/títulos.

```bash
curl -i -X POST http://localhost:8080/api/prestamos \
  -H 'Content-Type: application/json' \
  -d '{"estudianteId":1,"libroId":1}'
```

### Consultar préstamos

`GET /api/prestamos` devuelve `200 OK` y una lista de préstamos.

```bash
curl -i http://localhost:8080/api/prestamos
```

## Pruebas y empaquetado

```bash
./mvnw test
./mvnw package
```

Las evidencias de compilación, suite automatizada y llamadas HTTP locales están en `docs/evidencias/`. 

## Render

```text
URL pública: 
```

