# Hallazgos de seguridad

## Alcance y evidencia

Este documento describe el análisis estático de SeguridadAndroid y la revisión del código Java compartido durante su desarrollo. Distingue los hallazgos automatizados de las medidas observadas en el código.

Los reportes de MobSF 4.6.2 corresponden a:

| Versión | Archivo | MD5 | Puntaje |
|---|---|---|---:|
| Inicial | `app-debug.apk` | `d55e0efe16977f21eaa1d48d133ea82e` | 45/100 |
| Release sin Maps | `app-release.apk` | `a56db093db31f7069f820d401274bd35` | 73/100 |
| Release con Maps, vigente | `app-release.apk` | `4d4b21b25b259809a4d816becaceb300` | 73/100 |

El reporte vigente se generó para el análisis del 28 de octubre de 2026. Su APK mide 12,52 MB y tiene SHA-256 `b1115c2eaa5a6f83aec174a9d68e32762c3d4a81366ff70b59d16c100fc608ed`.

El reporte inicial registra dos hallazgos altos y tres medios; el final registra cero altos y dos medios. No se han confirmado tres vulnerabilidades críticas. Las severidades de la herramienta deben conservarse y evaluarse según el contexto de uso.

## 1. Compilación con depuración habilitada

**Evidencia:** MobSF identificó la aplicación inicial como depurable y clasificó el hallazgo con severidad alta.

**Riesgo:** distribuir una compilación depurable facilita la inspección del proceso y del estado interno mediante herramientas de desarrollo.

**Medida aplicada:** se generó y analizó un APK de tipo release.

**Resultado:** el hallazgo de aplicación depurable dejó de aparecer en el reporte final. Esta configuración es habitual durante el desarrollo; el problema sería utilizarla como versión de entrega.

## 2. APK firmado con certificado de depuración

**Evidencia:** el APK inicial utilizaba un certificado de depuración, señalado por MobSF con severidad alta.

**Riesgo:** una firma de desarrollo no proporciona la identidad de publicación que debe mantener la versión de entrega. Su uso debilita el control del proceso de distribución.

**Medida aplicada:** se generó un APK release firmado con un certificado propio.

**Resultado:** la advertencia de certificado de depuración desapareció del reporte final. La clave privada de firma debe permanecer fuera del repositorio.

## 3. Respaldo habilitado para datos locales sensibles

**Evidencia:** el manifiesto inicial contenía `android:allowBackup="true"`; MobSF mostró una advertencia de severidad media.

**Riesgo:** incluir la base de datos en respaldos o transferencias puede ampliar la exposición de los datos locales. Además, restaurar las notas sin la clave de Android Keystore puede dejarlas inaccesibles.

**Medida aplicada:** se configuró `android:allowBackup="false"` y se excluyó el dominio `database` en `backup_rules.xml`, así como en las reglas de respaldo en la nube y transferencia entre dispositivos de `data_extraction_rules.xml`.

**Resultado:** la advertencia dejó de aparecer en el reporte final. Se verificó la configuración y el resultado estático; no se documenta aquí una prueba de extracción de respaldos.

## Advertencias que permanecen

### Receptor de AndroidX

MobSF señala `androidx.profileinstaller.ProfileInstallReceiver`, asociado a una dependencia y protegido por el permiso `android.permission.DUMP`.

La presencia de un receptor exportado merece revisión, pero no demuestra por sí sola que cualquier aplicación pueda utilizarlo. Deben comprobarse el permiso efectivo y la versión de la dependencia antes de modificarlo. Se mantiene como advertencia pendiente de revisión contextual.

### Uso de consultas SQLite

MobSF advierte sobre el uso de SQLite. En `DatabaseHelper.java` se revisaron estas medidas:

- La autenticación consulta `WHERE username = ?` con argumentos separados.
- La consulta de notas utiliza `WHERE usuario_id = ?`.
- La eliminación exige `id = ? AND usuario_id = ?`.
- Las inserciones utilizan `ContentValues`.
- Las instrucciones `execSQL` crean estructuras fijas y no incorporan entradas del usuario.

No se confirmó una inyección SQL en esos métodos. La advertencia se conserva como punto de revisión, sin presentarla como una explotación demostrada.

## Revisión de Google Maps y ubicación

El reporte vigente registra `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_COARSE_LOCATION` y `ACCESS_FINE_LOCATION`. La categoría Android «dangerous» de los dos permisos de ubicación implica autorización durante el uso; no equivale a una vulnerabilidad crítica. La lista de permisos frecuentemente utilizados por malware tampoco demuestra que esta aplicación sea maliciosa.

En el código incorporado, la consulta se solicita al pulsar «Mostrar mi ubicación», admite permiso preciso o aproximado y muestra un mensaje si no obtiene una posición. La pantalla comprueba la sesión y `MapsActivity` se declara no exportada. No se solicita ubicación en segundo plano ni se escriben coordenadas en la base de datos de notas.

El manifiesto configura `android:usesCleartextTraffic="false"`. Es una política de rechazo de tráfico en claro para los componentes que la respetan; no demuestra por sí sola resistencia completa a ataques de red.

La API key se lee desde `local.properties`, excluido de Git, y se incorpora al manifiesto al compilar. Puede recuperarse del APK: la protección depende también de las restricciones de API, paquete y certificado configuradas por su administrador. No se han verificado esas restricciones ni se afirma que estén aplicadas. Es una revisión pendiente, no un nuevo hallazgo alto confirmado por MobSF.

El resumen muestra un «hotspot» sin detallar en este PDF su regla concreta. Se conserva como elemento por revisar y no se le asigna una severidad inventada. Las detecciones de APKiD incluyen avisos de análisis de DEX/YARA; no prueban que el proyecto haya implementado DexGuard.

## Controles adicionales observados

Las contraseñas se almacenan mediante PBKDF2-HMAC-SHA256 con sal aleatoria; las notas utilizan AES-GCM y una clave de Android Keystore. Las consultas se filtran por propietario y `NotasActivity` no se exporta.

Estos controles reducen riesgos de almacenamiento y acceso, pero su presencia no prueba que se haya explotado una vulnerabilidad previa. Tampoco convierten automáticamente el resultado final en una aplicación libre de fallos.

## Evidencia para la entrega

Conservar el reporte final como [vulnerability_report.pdf](vulnerability_report.pdf). Mantener también el reporte inicial permite justificar la comparación de resultados. Ambos deben corresponder a los APK identificados en este documento.

