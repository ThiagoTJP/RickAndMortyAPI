# Rick & Morty Explorer - Android App
Esta aplicación permite buscar y explorar personajes de la serie *Rick & Morty*.  
Utiliza la API oficial [Rick and Morty API](https://rickandmortyapi.com/) para obtener información actualizada de cada personaje.

## Características
- Buscar personajes por nombre.
- Ver lista de personajes con imagen, nombre y especie.
- Agregar o quitar personajes de favoritos (persistencia local con SQLite).
- Ver solo los personajes marcados como favoritos.
- Detalle de cada personaje al hacer clic, incluyendo:
  - Nombre
  - Estado
  - Especie
  - Género
  - Origen
  - Ubicación
  - Imagen
- Manejo de errores si no se encuentra ningún personaje.
- Diseño responsive y moderno.

## Tecnologías usadas
- Kotlin
- Kotlin
- Android Studio
- Retrofit2 + Gson → para llamadas HTTP a la API
- Glide → para la carga eficiente de imágenes
- RecyclerView → para la visualización de listas
- SQLite (SQLiteOpenHelper) → para almacenar los favoritos localmente
- Material Components → para botones y diseño visual

## Instalación
1. Clonar el repositorio:
git clone https://github.com/ThiagoTJP/RickAndMortyAPI.git

## Video de demostración
https://youtu.be/mg0490FEfsc
