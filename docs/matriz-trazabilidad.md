# Matriz de trazabilidad para historias y diagramas

Esta matriz sirve como control de coherencia antes de modelar. No prescribe detalles de implementación: identifica los elementos que el modelo debe explicar.

| Requisito / historia | Contexto (caso de uso) | Componentes | Modelo de dominio/clases | Desarrollo / paquetes |
|---|---|---|---|---|
| HU-01 Registrar dron | Gestionar flota | Gestión de flota | Dron (abstracto) y sus 5 tipos, Ubicacion, EstadoDron | Entrada REST, aplicación, dominio, persistencia |
| HU-02 Consultar flota | Consultar flota | Gestión de flota | Dron, filtros por tipo/estado | Igual que arriba |
| HU-03 Actualizar datos operativos | Actualizar dron | Gestión de flota | Dron y transición de disponibilidad | Igual que arriba |
| HU-04 Registrar misión | Registrar misión | Gestión de misiones | Mision, TipoMision, Prioridad, Ubicacion destino, capacidad requerida | Entrada REST, aplicación, dominio, persistencia |
| HU-05 Consultar pendientes | Consultar misiones | Gestión de misiones | Mision pendiente; orden prioridad/fecha límite | Gestión de misiones y repositorio |
| HU-06 Solicitar asignación | Asignar dron automáticamente | Motor de asignación | Asignacion, Dron, Mision, filtro de elegibilidad | Caso de uso de asignación y puertos de repositorio |
| HU-07 Elegir estrategia | Configurar/elegir estrategia | Motor de asignación | EstrategiaAsignacion, MenorDistancia (predeterminada), MayorBateria | Dominio/política; seleccionada por aplicación |
| HU-08 Ver resultado | Consultar asignación | Motor de asignación | Resultado/Asignacion y criterios de decisión | Respuesta del caso de uso |
| HU-09 Sin candidato | Consultar resultado sin candidato | Motor de asignación | Motivo de inelegibilidad/resultado vacío | Resultado controlado, sin crear asignación |
| HU-10 Evitar duplicidad | Asignar dron automáticamente | Motor + gestión de misiones | Invariante: máximo una misión activa por dron | Operación coordinada y persistencia consistente |
| HU-11 Actualizar misión | Cambiar estado de misión | Gestión de misiones | EstadoMision, EstadoDron, reglas de transición | Caso de uso y persistencia |
| HU-12 Consultar historial | Consultar historial | Gestión de misiones/asignaciones | Mision y Asignacion histórica | Consulta de historial |
| HU-13 Configurar reserva | Configurar política operativa | Configuración de asignación | ReservaOperativa como configuración/valor | Configuración en aplicación; política consumida por dominio |

## Decisiones que deben reflejarse visualmente

- Actor primario único: Operador de control.
- MongoDB es detalle de persistencia, no actor del contexto.
- El sistema no tiene adaptador para drones físicos en el alcance.
- Elegibilidad precede al ranking; Strategy representa solo el ranking intercambiable.
- Polimorfismo: cada tipo de dron evalúa su capacidad en su propia unidad.
- Las asociaciones y multiplicidades de clase expresan el límite de una misión activa por dron; distinguir histórico de activo si el modelo requiere conservar más de una asignación pasada.
- La vista de desarrollo muestra la arquitectura hexagonal: dependencias hacia el dominio y adaptadores (REST, MongoDB) implementando puertos.
- El despliegue representa operador/cliente REST, backend Spring Boot y MongoDB; no debe añadir frontend obligatorio ni nube/proveedor no especificados.

