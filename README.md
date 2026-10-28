# SeguridadAndroid

Aplicación académica Android desarrollada en Java para registrar usuarios, iniciar y cerrar sesión y administrar notas privadas almacenadas localmente en SQLite. Incluye Google Maps y consulta de ubicación mediante los servicios de ubicación de Android.

## Funcionalidades

- Registro con nombre de usuario único y contraseña.
- Inicio de sesión mediante verificación de hash.
- Creación, consulta y eliminación de notas asociadas al usuario autenticado.
- Validación de campos y rechazo de notas vacías.
- Cierre de sesión con limpieza de la sesión y de la navegación anterior.
- Acceso al mapa desde el botón «Abrir mapa» de la pantalla de notas.
- Consulta de ubicación al pulsar «Mostrar mi ubicación», con permiso solicitado durante el uso.
- Posibilidad de explorar el mapa sin conceder acceso a la ubicación.

Las cuentas y las notas se almacenan localmente. El mapa utiliza conexión a Internet y los servicios de Google Maps; no se implementa un servidor propio de autenticación ni sincronización de notas. El código de la aplicación no guarda las coordenadas en SQLite.

No se incluyen funciones de cámara ni marcadores personalizados: la pantalla muestra el marcador de ubicación al solicitarlo.

## Tecnologías y componentes

| Componente | Uso |
|---|---|
| Java y Android SDK | Interfaz y lógica de la aplicación |
| SQLiteOpenHelper | Creación y acceso a la base de datos |
| PBKDF2-HMAC-SHA256 | Derivación de hashes de contraseñas |
| AES-256-GCM | Cifrado autenticado de las notas |
| Android Keystore | Generación y custodia de la clave de cifrado |
| Maps SDK for Android 20.1.0 | Visualización del mapa |
| Google Play services Location 21.4.0 | Consulta de ubicación |
| MobSF | Análisis estático de los APK |

## Organización del código

Las clases están en `app/src/main/java/com/example/seguridadandroid/`.

| Clase | Responsabilidad |
|---|---|
| `MainActivity.java` | Registro, validación e inicio de sesión |
| `NotasActivity.java` | Administración de notas y cierre de sesión |
| `DatabaseHelper.java` | Tablas, consultas y operaciones de SQLite |
| `PasswordUtils.java` | Creación y verificación de hashes |
| `CryptoUtils.java` | Cifrado y descifrado de notas |
| `MapsActivity.java` | Mapa, permisos y consulta de ubicación |
| `Sesion.java` | Estado de autenticación en memoria |

Las reglas de respaldo se encuentran en `app/src/main/res/xml/` y la configuración de componentes en `app/src/main/AndroidManifest.xml`.

## Configurar Google Maps

El proyecto utiliza AGP **9.3.0**, `compileSdk` y `targetSdk` 37, `minSdk` 30 y compatibilidad de código Java 11. Utilizar una versión de Android Studio compatible e instalar los componentes del SDK indicados por Gradle.

1. Obtener una API key autorizada para **Maps SDK for Android**, con el servicio habilitado en el proyecto de Google Cloud correspondiente.
2. Abrir `local.properties` en la raíz y conservar su configuración de `sdk.dir`.
3. Añadir una línea `MAPS_API_KEY=TU_CLAVE`, sustituyendo el marcador por la clave autorizada, sin comillas.
4. Sincronizar Gradle. El módulo lee la propiedad y la inserta mediante `${MAPS_API_KEY}` en el manifiesto.

`local.properties` se excluye de Git. Cada equipo que reconstruya el proyecto debe configurar su propia copia. La configuración local evita publicar la clave en el código fuente, pero no la oculta dentro del APK compilado. El administrador de la clave debe revisar las restricciones de paquete, certificado de firma y API. Estas restricciones de Google Cloud no se han verificado en este trabajo.

## Cómo ejecutar

1. Descargar o clonar este repositorio y abrir su carpeta raíz en Android Studio.
2. Completar la sincronización de Gradle e instalar los componentes del SDK que solicite el proyecto.
3. Seleccionar un dispositivo o emulador API 30 o superior con Google Play services; para el mapa, debe tener conexión a Internet.
4. Ejecutar el módulo `app`.
5. Crear una cuenta de prueba, iniciar sesión y guardar una nota.
6. Pulsar «Abrir mapa» y comprobar que carguen los nombres y calles.
7. Pulsar «Mostrar mi ubicación» y conceder acceso mientras se utiliza la app. También se admite ubicación aproximada. En un emulador, la posición puede ser simulada.
8. Si se deniega el permiso o no se obtiene una posición, comprobar el mensaje mostrado y conservar la navegación del mapa.

No se incluyen credenciales predeterminadas: cada persona debe crear su cuenta. Las notas y los usuarios permanecen en el almacenamiento local de esa instalación. Al desinstalar la aplicación pueden perderse los datos y la clave necesaria para descifrar las notas.

Para obtener una versión de entrega, generar un APK de tipo `release` firmado con una clave propia. La clave de firma del APK es distinta de la clave de cifrado generada dentro de Android Keystore.

## Resultados del análisis estático

Se analizaron tres versiones con MobSF 4.6.2. El reporte vigente corresponde al release con Maps del **28 de octubre de 2026**:

| Indicador | Debug inicial | Release sin Maps | Release con Maps (vigente) |
|---|---:|---:|---:|
| Puntaje MobSF | 45/100 | 73/100 | 73/100 |
| Clasificación de riesgo | Medio | Bajo | Bajo |
| Hallazgos de severidad alta | 2 | 0 | 0 |
| Hallazgos de severidad media | 3 | 2 | 2 |

APK vigente: `app-release.apk`, 12,52 MB. MD5: `4d4b21b25b259809a4d816becaceb300`. SHA-256: `b1115c2eaa5a6f83aec174a9d68e32762c3d4a81366ff70b59d16c100fc608ed`.

El resumen del reporte vigente también muestra un elemento «secure» y un «hotspot». La presencia de permisos de ubicación clasificados como «dangerous» indica que requieren autorización del usuario; por sí sola no demuestra una vulnerabilidad ni que la aplicación sea maliciosa.

La comparación incluye el cambio de una compilación de desarrollo a una versión release y la modificación de la política de respaldos. El puntaje de MobSF no representa una garantía de ausencia de vulnerabilidades.

Las dos advertencias restantes requieren revisión contextual: el receptor de AndroidX protegido por el permiso `DUMP` y el uso de SQLite. En el código revisado, los datos de usuario se pasan mediante parámetros o `ContentValues`.

## Documentación

- [Hallazgos y medidas aplicadas](vulnerabilities.md)
- [Buenas prácticas implementadas](best_practices.md)
- [Consejos de seguridad](security_tips.md)
- [Programa de mejora](security_improvement_program.md)
- [Reporte estático final de MobSF](vulnerability_report.pdf)

Conservar en la raíz `vulnerability_report.pdf`, correspondiente al APK con Maps identificado arriba, y reemplazar el informe anterior. El paquete de documentación contiene los cinco Markdown; el PDF se conserva por separado.

## Manejo del repositorio

La visualización del mapa y la consulta de ubicación fueron confirmadas manualmente durante el desarrollo. El PDF aporta evidencia de análisis estático; no demuestra por sí solo el comportamiento de la app durante su uso.

Excluir claves de firma (`.jks` y `.keystore`), contraseñas, claves de API, `local.properties` y datos personales de prueba. Compartir el código fuente y la documentación necesarios para reconstruir el proyecto.

