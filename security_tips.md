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

## 4. Proteger cualquier futura comunicación de red

**Recomendación:** si se agrega sincronización o una API, utilizar HTTPS, validar certificados y rechazar conexiones inseguras. No incorporar verificadores que acepten cualquier certificado.

**Aplicación al proyecto:** el alcance actual es local; este consejo se aplica a una futura integración de red. El cifrado de notas en SQLite no sustituye la protección del transporte.

**Impacto esperado:** reducir exposición y manipulación de datos durante su transmisión, incluidos ataques de intermediario.

**Comprobación:** verificar la configuración de red y probar el rechazo de conexiones HTTP y certificados no confiables cuando exista esa funcionalidad.

## 5. Cuidar las versiones publicadas y sus secretos

**Recomendación:** analizar el APK release que realmente se entregará, revisar los componentes exportados y mantener las dependencias actualizadas.

**Aplicación al proyecto:** mantener privada la clave de firma y sus contraseñas. No incluir claves de API, credenciales de prueba personales o información sensible en el repositorio ni en registros de ejecución.

**Impacto esperado:** preservar el control de las actualizaciones y reducir la exposición de secretos y funciones internas.

**Comprobación:** revisar los archivos preparados para el commit y el manifiesto resultante del APK, incluyendo componentes añadidos por dependencias.

