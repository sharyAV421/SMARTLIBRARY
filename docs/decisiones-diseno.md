# Decisiones de diseño

## Arquitectura y GRASP

Se conserva la separación `Controller → Service → Repository`. `PrestamoController` actúa como Controller GRASP: recibe las operaciones HTTP, valida el formato de entrada y delega el caso de uso. `PrestamoService` concentra la coordinación de negocio y transacción. Los repositorios mantienen la persistencia fuera de la lógica de presentación y las entidades.

La responsabilidad de cambiar el estado de disponibilidad pertenece a `Libro`, que ofrece `marcarComoPrestado()`. `PrestamoService` es el experto en la regla del caso de uso porque tiene la información necesaria sobre estudiante, libro, disponibilidad y préstamos activos. `PrestamoResponse` evita exponer entidades JPA o proxies en el contrato HTTP.

## Modelo y reglas

- `Prestamo` tiene una referencia obligatoria a un `Estudiante` y a un `Libro` (`ManyToOne`); `fechaDevolucion` es nullable.
- Un estudiante y un libro se pueden referenciar en muchos préstamos históricos. No hay composición ni agregación.
- R1: el servicio rechaza un libro con `disponible=false`.
- R2: el constructor de `Prestamo` recibe estudiante, libro y fecha generada con `LocalDate.now()`.
- R3: el servicio marca el libro como prestado antes de persistir el préstamo.
- R4: validación del request ocurre antes del servicio; entidad inexistente o disponibilidad inválida provoca excepción antes de crear el préstamo.
- R5: además de `disponible`, se comprueba la ausencia de un préstamo con `fechaDevolucion IS NULL`; el bloqueo del libro serializa intentos concurrentes.

No se implementa devolución en el alcance actual; por ello los préstamos creados permanecen activos con fecha de devolución nula.

## Transacción y concurrencia

`PrestamoService.registrar` tiene `@Transactional`. Se busca primero al estudiante, luego se obtiene el libro con `PESSIMISTIC_WRITE`. Bajo el bloqueo se comprueban ambas condiciones, se modifica disponibilidad y se inserta el préstamo. Una excepción hace rollback de la transacción completa. La prueba de concurrencia lanza dos POST al mismo libro y verifica un `201`, un `409` y un solo registro.

El bloqueo pesimista requiere soporte del motor de base de datos. H2 en memoria soporta este caso para pruebas y demostración. El motor configurado en el despliegue también es H2; si en el futuro se cambia, se debe verificar su implementación de locks e aislamiento.

## HTTP

`POST /api/prestamos` devuelve `201`, errores de validación `400`, recursos inexistentes `404` y conflicto de disponibilidad `409`. `GET /api/prestamos` devuelve `200`. `GlobalExceptionHandler` traduce excepciones y validaciones a JSON uniforme.

## Despliegue y persistencia

Render ejecuta la imagen definida en `Dockerfile`; el puerto y la dirección del servidor se configuran para el `PORT` inyectado por la plataforma y `0.0.0.0`. El esquema es `create-drop` y la URL H2 es `jdbc:h2:mem:smartlibrary`; por tanto, la base y los préstamos desaparecen al parar o reiniciar el proceso. Los datos de ejemplo se vuelven a cargar en cada inicio. H2 se conserva intencionalmente por el alcance académico.

La configuración lista para importar está en `render.yaml`. El servicio público todavía debe crearse desde una cuenta de Render conectada al repositorio; la URL se añadirá solo después de una comprobación HTTP real.

## UML

Los diagramas reflejan las entidades, cardinalidades, DTOs, excepciones, operaciones de las capas y consultas del flujo implementado. La secuencia describe el alta transaccional y no incorpora rutas de devolución.
