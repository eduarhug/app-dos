# Pantalla Negra

App Android que pone una capa negra sobre cualquier otra app (YouTube, Spotify, etc.)
para ahorrar batería en pantallas OLED/AMOLED mientras solo escuchas.

- Botón flotante arrastrable: un toque activa/desactiva el negro.
- Con el negro activo, desliza el dedo para ver una zona; a los 4 segundos vuelve a negro.

## Compilar el APK con GitHub (sin instalar nada)

1. En GitHub crea un repositorio nuevo (privado o público).
2. Sube TODO el contenido de esta carpeta (incluida la carpeta oculta `.github`).
3. Entra a la pestaña **Actions** → **Compilar APK** → **Run workflow**
   (también se ejecuta solo cada vez que subes cambios).
4. Cuando termine (3-6 min), abre la ejecución y descarga **PantallaNegra-apk** en "Artifacts".
5. Descomprime el zip, pasa el `.apk` al celular e instálalo
   (Android pedirá permitir "instalar apps de origen desconocido").

## Primer uso

1. Abre la app y toca **Dar permiso** → activa "Permitir sobre otras apps".
2. Vuelve y toca **Activar botón flotante**.
3. Abre tu video y usa el botón.
