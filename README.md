# SeguridadAndroid

Aplicación académica Android desarrollada en Java para registrar usuarios, iniciar y cerrar sesión y administrar notas privadas almacenadas localmente en SQLite.

## Funcionalidades

- Registro con nombre de usuario único y contraseña.
- Inicio de sesión mediante verificación de hash.
- Creación, consulta y eliminación de notas asociadas al usuario autenticado.
- Validación de campos y rechazo de notas vacías.
- Cierre de sesión con limpieza de la sesión y de la navegación anterior.

La aplicación funciona con una base de datos local. No implementa un servidor de autenticación ni sincronización entre dispositivos.

## Tecnologías y componentes

| Componente | Uso |
|---|---|
| Java y Android SDK | Interfaz y lógica de la aplicación |
| SQLiteOpenHelper | Creación y acceso a la base de datos |
| PBKDF2-HMAC-SHA256 | Derivación de hashes de contraseñas |
| AES-256-GCM | Cifrado autenticado de las notas |
| Android Keystore | Generación y custodia de la clave de cifrado |
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
| `Sesion.java` | Estado de autenticación en memoria |

Las reglas de respaldo se encuentran en `app/src/main/res/xml/` y la configuración de componentes en `app/src/main/AndroidManifest.xml`.

## Cómo ejecutar

1. Descargar o clonar este repositorio y abrir su carpeta raíz en Android Studio.
2. Completar la sincronización de Gradle e instalar los componentes del SDK que solicite el proyecto.
3. Seleccionar un dispositivo compatible con el `minSdk` definido en `app/build.gradle.kts`.
4. Ejecutar el módulo `app`.
5. Crear una cuenta de prueba, iniciar sesión y guardar una nota.

No se incluyen credenciales predeterminadas: cada persona debe crear su cuenta. Las notas y los usuarios permanecen en el almacenamiento local de esa instalación. Al desinstalar la aplicación pueden perderse los datos y la clave necesaria para descifrar las notas.

Para obtener una versión de entrega, generar un APK de tipo `release` firmado con una clave propia. La clave de firma del APK es distinta de la clave de cifrado generada dentro de Android Keystore.

## Resultados del análisis estático

Se compararon dos APK con MobSF 4.6.2:

| Indicador | APK inicial de desarrollo | APK final release |
|---|---:|---:|
| Puntaje MobSF | 45/100 | 73/100 |
| Clasificación de riesgo | Medio | Bajo |
| Hallazgos de severidad alta | 2 | 0 |
| Hallazgos de severidad media | 3 | 2 |

La comparación incluye el cambio de una compilación de desarrollo a una versión release y la modificación de la política de respaldos. El puntaje de MobSF no representa una garantía de ausencia de vulnerabilidades.

Las dos advertencias restantes requieren revisión contextual: el receptor de AndroidX protegido por el permiso `DUMP` y el uso de SQLite. En el código revisado, los datos de usuario se pasan mediante parámetros o `ContentValues`.

## Documentación

- [Hallazgos y medidas aplicadas](vulnerabilities.md)
- [Buenas prácticas implementadas](best_practices.md)
- [Consejos de seguridad](security_tips.md)
- [Programa de mejora](security_improvement_program.md)
- [Reporte estático final de MobSF](vulnerability_report.pdf)

Para completar el último enlace, guardar el PDF estático de `app-release.apk` en la raíz del repositorio con el nombre `vulnerability_report.pdf`. Estos archivos Markdown no incluyen ese PDF.

## Manejo del repositorio

Excluir claves de firma (`.jks` y `.keystore`), contraseñas, claves de API, `local.properties` y datos personales de prueba. Compartir el código fuente y la documentación necesarios para reconstruir el proyecto.

