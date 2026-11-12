# SmartLibrary

API REST académica para administrar estudiantes, libros y préstamos bibliotecarios. El caso de uso permite registrar un préstamo y consultar el historial. La fecha se genera en el servidor y cada libro puede tener como máximo un préstamo activo.

## Tecnologías

- Java 21 y Spring Boot 3.5.7.
- Maven, Spring Web, Spring Data JPA y Spring Validation.
- H2 en memoria.
- JUnit 5 y Spring Boot Test.
- Render con Docker para el despliegue.

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

## Ejecutar localmente

Requisitos: JDK 21 y Maven 3.9 o posterior.

```bash
mvn spring-boot:run
```

La aplicación usa `PORT` si está definido y, de lo contrario, el puerto `8080`. Para comprobar otro puerto en PowerShell:

```powershell
$env:PORT = "18080"
mvn spring-boot:run
```

H2 inicia con dos estudiantes (IDs 1 y 2) y tres libros (IDs 1, 2 y 3); el libro 3 empieza no disponible. La consola H2 local está en `http://localhost:8080/h2-console`, con JDBC URL `jdbc:h2:mem:smartlibrary`, usuario `sa` y contraseña vacía.

## API

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
mvn test
mvn package
```

Las evidencias de compilación, suite automatizada y llamadas HTTP locales están en `docs/evidencias/`. En el entorno de desarrollo de esta entrega no había Maven instalado ni un Maven Wrapper funcional. Las pruebas se ejecutaron con Maven 3.9.11 descargado temporalmente desde Apache. Para otros entornos se requiere Maven 3.9+; puede generarse un wrapper oficial con `mvn wrapper:wrapper` si se desea evitar la instalación global.

## Render

El repositorio incluye `Dockerfile` y `render.yaml`. Para publicar:

1. Sube el repositorio a GitHub o GitLab accesible para tu cuenta de Render.
2. En Render elige **New → Blueprint**, conecta ese repositorio y confirma el servicio `smartlibrary-api` descrito en `render.yaml`.
3. Render construye la imagen con Maven y Java 21. La app escucha en `0.0.0.0` y en el puerto `$PORT` (por defecto local `8080`; Render configura el puerto del servicio).
4. Espera el estado **Live** y comprueba `https://<servicio>.onrender.com/api/prestamos` y los POST de ejemplo.

No se ha asignado una URL pública: el servicio Render no fue creado en esta entrega porque no hay una sesión de Render disponible para conectar y crear el Blueprint. El código se publica en el repositorio GitHub `sharyAV421/SMARTLIBRARY`; anota el dominio aquí solo después de verificar el servicio:

```text
URL pública: pendiente de crear el servicio en Render.
```

### Variables de entorno

- `PORT`: puerto HTTP dinámico de Render; la aplicación lo toma automáticamente. En local es opcional.
- No se requieren credenciales para H2.

### Persistencia desplegada

H2 se configura en memoria (`jdbc:h2:mem:smartlibrary`) y Hibernate recrea el esquema al iniciar. Los préstamos se pierden cuando el proceso se detiene, reinicia o se redespliega; los estudiantes/libros de ejemplo se cargan de nuevo. Esta limitación es adecuada para la demostración académica, pero no para conservar datos de producción. El plan gratuito de Render también puede suspender servicios inactivos.
