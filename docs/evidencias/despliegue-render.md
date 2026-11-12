# Evidencia y estado de despliegue

Estado comprobado el 12 de noviembre de 2026: **no hay servicio público de Render creado**; por ello no existe URL de producción que pueda verificarse ni se incluye una URL simulada.

La configuración de despliegue preparada está en `Dockerfile` y `render.yaml`. El repositorio público GitHub `sharyAV421/SMARTLIBRARY` estaba vacío al momento de la comprobación y recibirá este proyecto. Para terminar el despliegue, el propietario debe conectar ese repositorio en una cuenta de Render y crear el Blueprint indicado en README. Render debe mostrar el servicio como `Live`; entonces se debe probar `GET /api/prestamos` desde una red externa y registrar esa URL aquí.

No se dispone de una sesión o herramienta de Render conectada en este entorno, ni de un servicio asociado donde inspeccionar build logs o dominio asignado. La evidencia existente de la API corresponde únicamente a pruebas locales y está identificada como tal en `endpoints-local.txt`.

## Limitación de datos

La aplicación usa H2 en memoria. El estado desaparece al parar, suspender, reiniciar o redesplegar el proceso. El servicio debe tratarse como demostración de taller, no como almacenamiento persistente.
