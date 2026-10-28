# Buenas prácticas de seguridad

Las siguientes prácticas se observan en el código de SeguridadAndroid. Se explica qué riesgo atiende cada una y cuáles son sus límites.

## 1. Almacenar hashes de contraseñas

**Implementación:** `PasswordUtils.java` utiliza PBKDF2-HMAC-SHA256 con 600.000 iteraciones, una sal aleatoria de 16 bytes y un resultado de 256 bits. La verificación compara los resultados mediante `MessageDigest.isEqual`. La tabla de usuarios almacena `password_hash`.

**Impacto:** evita guardar la contraseña original y aumenta el costo de probar contraseñas sobre una copia de la base de datos. La sal permite que contraseñas iguales produzcan registros diferentes.

**Límite:** un hash no impide todos los ataques de diccionario. Las contraseñas deben ser suficientemente fuertes y el costo de derivación debe revisarse periódicamente. Base64 solo representa bytes como texto; no cifra ni protege por sí mismo.

## 2. Cifrar las notas con una clave administrada por Android

**Implementación:** `CryptoUtils.java` genera una clave AES de 256 bits en Android Keystore. Utiliza `AES/GCM/NoPadding`, un IV aleatorio generado por el proveedor y una etiqueta de autenticación de 128 bits. El registro contiene versión, IV y contenido cifrado con su etiqueta.

El identificador del propietario se incorpora como dato autenticado adicional mediante `notas-v1:<idUsuario>`.

**Impacto:** protege la confidencialidad de las notas almacenadas y permite detectar modificaciones incompatibles con su etiqueta de autenticación.

**Límite:** existe una clave de aplicación, no una clave distinta por usuario. Asociar el propietario mediante datos autenticados no sustituye la comprobación de sesión y permisos. No se afirma que todos los dispositivos almacenen la clave en hardware. La pérdida de la clave puede impedir recuperar las notas.

## 3. Utilizar parámetros y validar las entradas

**Implementación:** `DatabaseHelper.java` separa las entradas del texto SQL mediante `?` y argumentos; las inserciones usan `ContentValues`. El nombre de usuario admite letras minúsculas, números y guion bajo, con longitud de 3 a 40 caracteres. El registro exige contraseñas de 10 a 128 caracteres y las notas no pueden estar vacías ni superar 5.000 caracteres.

**Impacto:** la parametrización evita interpretar las entradas como instrucciones SQL. Las validaciones reducen registros inválidos y consumo innecesario de recursos.

**Límite:** validar campos no reemplaza la parametrización. Las futuras consultas deben mantener la misma separación entre instrucciones y datos.

## 4. Comprobar la sesión y la propiedad de los datos

**Implementación:** `Sesion.java` mantiene el usuario autenticado en memoria. `NotasActivity.java` verifica la sesión; la consulta de notas filtra por `usuario_id` y la eliminación exige tanto el identificador de la nota como el del propietario. Las actividades de notas y mapa tienen `android:exported="false"`. La pantalla del mapa también comprueba que la sesión siga perteneciendo al usuario autenticado.

**Impacto:** evita que la interfaz consulte o elimine notas de otra cuenta mediante operaciones que omitan al propietario. Al cerrar sesión se limpia el estado de autenticación y la pila de navegación.

**Límite:** validar que un identificador sea positivo no autoriza por sí solo una operación. Los métodos deben recibir el identificador desde la sesión validada. La sesión se pierde cuando termina el proceso.

## 5. Controlar respaldos y compilaciones de entrega

**Implementación:** se deshabilitó el respaldo y se excluyeron las bases de datos de los mecanismos de respaldo y transferencia configurados. La versión final es un APK release con certificado propio.

**Impacto:** reduce la copia involuntaria de información sensible y evita entregar una compilación de desarrollo depurable.

**Límite:** estas medidas deben mantenerse en futuras versiones. La firma identifica al publicador y permite controlar actualizaciones; no cifra el contenido del APK.

## 6. Pedir ubicación únicamente al utilizar su función

**Implementación:** `MapsActivity.java` solicita los permisos de ubicación al pulsar el botón correspondiente, acepta ubicación precisa o aproximada y permite explorar el mapa si se deniega el acceso. Utiliza una consulta de ubicación y cancela la solicitud pendiente cuando la pantalla deja de estar visible. No solicita permiso de ubicación en segundo plano.

**Impacto:** permite al usuario decidir cuándo compartir su posición y limita el acceso de la aplicación a la función utilizada. El código no incorpora las coordenadas a las notas ni a la base de datos.

**Límite:** la ubicación depende del dispositivo y puede ser aproximada o simulada. Google Maps utiliza servicios externos; no debe afirmarse que todo el funcionamiento sea exclusivamente local.

## 7. Configurar red y API key fuera del código fuente

**Implementación:** el manifiesto establece `android:usesCleartextTraffic="false"`. Gradle lee `MAPS_API_KEY` desde `local.properties`, que se mantiene fuera de Git, y utiliza un placeholder en el manifiesto.

**Impacto:** expresa la política de rechazo de conexiones en claro y evita escribir la clave directamente en archivos fuente compartidos.

**Límite:** la clave está incluida en el APK. El administrador de Google Cloud debe revisar restricciones de paquete, certificado y API; esa configuración no ha sido verificada. No se implementa pinning de certificados ni se afirma protección completa frente a MITM.

## Trabajo fuera de la interfaz

Las operaciones de base de datos y derivación criptográfica se ejecutan mediante un ejecutor, lo que evita bloquear la interfaz durante tareas costosas.

## Referencias

- [Android: Android Keystore](https://developer.android.com/privacy-and-security/keystore)
- [Android: respaldos automáticos](https://developer.android.com/identity/data/autobackup)
- [Android: firma de aplicaciones](https://developer.android.com/studio/publish/app-signing)


- [Google Maps: protección y restricciones de API keys](https://developers.google.com/maps/api-security-best-practices)
