# Consejos de seguridad

Estos consejos permiten conservar los controles existentes y orientar futuras funciones de SeguridadAndroid.

## 1. Mantener las consultas parametrizadas

**Recomendación:** utilizar argumentos separados para cualquier dato externo que se incorpore a una consulta. Para insertar o actualizar registros, utilizar `ContentValues`.

**Aplicación al proyecto:** conservar el patrón `WHERE username = ?` y las condiciones de propietario en consultas y eliminaciones. No formar SQL concatenando nombres, notas u otros campos.

**Impacto esperado:** impedir que una entrada se interprete como parte de una instrucción SQL y reducir accesos o modificaciones no autorizados.

**Comprobación:** probar entradas con comillas y caracteres especiales, verificando que no alteren la estructura de la consulta. Revisar cada nuevo método de acceso a datos.

## 2. Separar autenticación y autorización

**Recomendación:** autenticar verifica quién inicia sesión; autorizar determina qué registros puede consultar o modificar. Ambas comprobaciones deben mantenerse.

**Aplicación al proyecto:** obtener el identificador desde la sesión validada, filtrar las notas por propietario y comprobarlo también al eliminar. No confiar en un identificador recibido desde un campo editable o un extra de navegación.

**Impacto esperado:** reducir el riesgo de que una cuenta acceda a información perteneciente a otra.

**Comprobación:** crear dos cuentas de prueba y verificar que cada una vea y elimine únicamente sus propias notas. Después de cerrar sesión, comprobar que el botón Atrás no recupere la pantalla privada.

## 3. Revisar la resistencia a intentos repetidos

**Recomendación:** conservar mensajes de autenticación genéricos y revisar los mecanismos de limitación de intentos.

**Aplicación al proyecto:** existe un bloqueo local de 30 segundos tras cinco intentos fallidos. El estado no constituye una protección persistente frente al reinicio del proceso. Si se incorpora un servidor, la limitación también debe aplicarse allí.

**Impacto esperado:** dificultar intentos repetidos de adivinar contraseñas sin presentar el bloqueo local como una defensa absoluta.

**Comprobación:** registrar el comportamiento ante intentos fallidos consecutivos, rotación de pantalla y reinicio de la aplicación.

## 4. Mantener la política de red segura

**Recomendación:** conservar el rechazo de tráfico en claro, utilizar HTTPS en futuras APIs y mantener la validación normal de certificados. No incorporar verificadores que acepten cualquier certificado.

**Aplicación al proyecto:** la versión con Maps utiliza Internet y declara `android:usesCleartextTraffic="false"`. Las cuentas y notas continúan en SQLite local. No se ha implementado pinning ni un servidor propio.

**Impacto esperado:** reducir el riesgo de exposición de información en conexiones en claro. Esta configuración no constituye una prueba completa frente a ataques de intermediario.

**Comprobación:** revisar el manifiesto resultante y comprobar la política de transporte cuando se agregue cualquier comunicación propia de red.

## 5. Cuidar las versiones publicadas y sus secretos

**Recomendación:** analizar el APK release que realmente se entregará, revisar los componentes exportados y mantener las dependencias actualizadas.

**Aplicación al proyecto:** mantener privada la clave de firma y sus contraseñas. No incluir claves de API, credenciales de prueba personales o información sensible en el repositorio ni en registros de ejecución.

**Impacto esperado:** preservar el control de las actualizaciones y reducir la exposición de secretos y funciones internas.

**Comprobación:** revisar los archivos preparados para el commit y el manifiesto resultante del APK, incluyendo componentes añadidos por dependencias.


## 6. Revisar las restricciones de la API key de Maps

**Recomendación:** mantener la clave fuera de Git y solicitar al administrador que autorice el paquete Android, las huellas SHA-1 de firma necesarias y únicamente las APIs utilizadas.

**Aplicación al proyecto:** la configuración local ya se lee mediante Gradle. Las restricciones del proyecto de Google Cloud están pendientes de verificación; no deben presentarse como implementadas. No modificar una clave compartida sin coordinación con su administrador.

**Impacto esperado:** limitar usos ajenos al proyecto, incluso si se extrae la clave del APK. Una clave Android incluida en una app no puede tratarse como un secreto inaccesible.

**Comprobación:** verificar Maps en debug y release, con sus firmas autorizadas, y registrar la configuración revisada sin publicar el valor de la clave.

## 7. Comprobar la denegación de ubicación

**Recomendación:** mantener utilizable el mapa aunque se deniegue la ubicación y no ampliar la solicitud a permisos en segundo plano.

**Aplicación al proyecto:** el botón inicia la solicitud y la pantalla informa de la denegación o de una posición no disponible. La ubicación precisa no es obligatoria para aceptar un resultado aproximado.

**Impacto esperado:** respetar la elección del usuario y reducir la recopilación innecesaria de datos.

**Comprobación:** probar permiso concedido, denegado, aproximado y ubicación desactivada. El funcionamiento normal del mapa y la ubicación fue confirmado por el desarrollador; los demás casos deben registrarse por separado, sin darlos por probados.
