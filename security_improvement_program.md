# Programa de mejora de seguridad

## Objetivo

Mantener la protección de credenciales y notas privadas, reducir regresiones y registrar evidencia verificable antes de cada entrega.

Este programa propone tareas futuras y revisiones recurrentes. Las acciones planificadas no se presentan como implementadas.

## Situación de referencia

El APK release con Google Maps, analizado el 28 de octubre de 2026 con MobSF 4.6.2, obtuvo 73/100, sin hallazgos altos y con dos advertencias medias. El APK inicial obtuvo 45/100, con dos hallazgos altos y tres medios.

El release previo sin Maps también obtuvo 73/100. El APK vigente tiene MD5 `4d4b21b25b259809a4d816becaceb300`. Se registra además un «hotspot» que requiere identificar su regla antes de clasificarlo.

La mejora observada incluye el uso de una compilación release firmada y el cambio de las reglas de respaldo. Permanecen pendientes la revisión contextual del receptor de AndroidX y el seguimiento de la advertencia de SQLite.

## Acciones y responsables

Los responsables se expresan como funciones para que el equipo pueda asignarlas a sus integrantes.

| Prioridad | Acción | Responsable | Momento o frecuencia | Evidencia y criterio de cierre |
|---|---|---|---|---|
| Alta | Revisar cada nuevo acceso a SQLite: parámetros, validaciones y condición de propietario | Desarrollo y revisión de código | En cada cambio de persistencia | Revisión registrada; ninguna entrada concatenada en SQL |
| Alta | Comprobar separación de notas entre dos cuentas y cierre de sesión | Pruebas | Antes de cada entrega y tras cambios de sesión | Casos de prueba con resultado y versión identificada |
| Alta | Revisar el bloqueo local y su comportamiento al reiniciar el proceso | Desarrollo | Próxima iteración | Limitación documentada y decisión de mejora justificada |
| Media | Evaluar el receptor ProfileInstallReceiver y sus permisos efectivos | Revisión de seguridad | Próxima revisión de dependencias | Versión y permisos registrados; decisión de conservar, actualizar o modificar |
| Media | Repetir el análisis estático sobre el APK release de entrega | Pruebas | Cada entrega | PDF, hash del APK y comparación con la línea base |
| Media | Revisar dependencias y avisos de seguridad | Mantenimiento | Mensualmente | Registro de versiones revisadas y actualizaciones aplicadas |
| Media | Revisar manifiesto y exclusiones de respaldo | Desarrollo | Tras cambios de almacenamiento o configuración | Configuración revisada en fuente y APK final |
| Media | Evaluar pérdida de clave y recuperación de datos sin debilitar el cifrado | Desarrollo | Antes de diseñar exportación o restauración | Diseño y comportamiento esperado documentados |
| Alta | Verificar restricciones de Maps por API, paquete y firma | Administrador de la API y desarrollo | Antes de ampliar la distribución | Configuración revisada y prueba de debug/release; no publicar la clave |
| Media | Probar ubicación concedida, denegada, aproximada y desactivada | Pruebas | Tras cambios en Maps o permisos | Casos registrados; mapa utilizable sin ubicación |
| Media | Identificar la regla del hotspot y revisar el contexto | Revisión de seguridad | Próxima revisión del reporte completo | Regla, evidencia y decisión documentadas |
| Condicionada | Diseñar HTTPS y autenticación de servidor | Desarrollo y seguridad | Antes de incorporar una API | Requisitos y pruebas definidos para la nueva comunicación |

## Métricas de seguimiento

| Métrica | Línea base | Objetivo |
|---|---|---|
| Hallazgos altos de MobSF | 0 en el release analizado | Mantener 0 o justificar y resolver cualquier aparición |
| Advertencias medias de MobSF | 2 | Revisar ambas y registrar una decisión basada en evidencia |
| Consultas con entradas externas concatenadas | No observadas en los métodos revisados | Mantener 0 en el código revisado |
| Escenarios funcionales de seguridad | Evidencia manual pendiente de consolidar | Registrar al menos registro, duplicado, login inválido, notas vacías, separación de cuentas y cierre de sesión |
| Claves o credenciales incluidas en Git | Sin una auditoría completa documentada | Revisar cada entrega y retirar cualquier secreto detectado |
| Restricciones de la API key de Maps | No verificadas | Revisar con su administrador paquete, firma y APIs autorizadas |
| Casos de permisos de ubicación | Funcionamiento normal reportado; restantes sin consolidar | Registrar concesión, denegación, ubicación aproximada y no disponible |
| Revisiones de dependencias | Por iniciar como actividad periódica | Una revisión mensual registrada |

El puntaje de MobSF se usa como indicador complementario. No sustituye la revisión de código ni justifica aumentar o reducir una severidad sin evidencia.

## Registro de revisiones

En cada revisión, registrar:

1. Fecha, responsable y versión o commit.
2. APK evaluado y su hash.
3. Hallazgo, severidad de origen y evidencia.
4. Decisión: corregir, investigar o aceptar con justificación.
5. Cambio aplicado y resultado de la verificación.

## Criterios de entrega

- Analizar la versión release que se compartirá.
- Comprobar las funciones afectadas por los cambios, incluido el mapa y el comportamiento de los permisos.
- Configurar la API key localmente al reconstruir y verificar Maps también en release.
- Revisar que no se incorporen claves privadas ni credenciales.
- Actualizar la documentación cuando cambie la implementación.
- Adjuntar el reporte estático identificado como `vulnerability_report.pdf`.

Un hallazgo se considera cerrado cuando existe una corrección o una justificación técnica y se registra la verificación correspondiente. Las tareas propuestas permanecen pendientes hasta contar con esa evidencia.

