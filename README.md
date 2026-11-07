# Centro de Control de Drones — Backend

Proyecto final de Diseño de Software. El sistema administra una flota de drones y **asigna automáticamente el mejor dron disponible para cada misión**, con algoritmos de asignación intercambiables.

El alcance es un backend REST. No se conecta con drones físicos ni ejecuta vuelos: el operador registra manualmente los datos de la flota y el estado de las misiones.

## Caso de estudio

Una empresa tiene una flota de drones de cinco tipos: **Entrega, Fotografía, Vigilancia, Agricultura y Cartografía**.

- Cada dron tiene batería, capacidad, autonomía, estado y ubicación.
- Cada misión tiene prioridad, distancia, tipo y tiempo límite.

## Historias de usuario

Las historias, con validación INVEST y criterios de aceptación en Gherkin, están en [`docs/historias-de-usuario/`](docs/historias-de-usuario/00-resumen.md).

| ID | Historia | Prioridad |
|---|---|---|
| [HU-01](docs/historias-de-usuario/HU-01-registrar-un-dron.md) | Registrar un dron | Must |
| [HU-02](docs/historias-de-usuario/HU-02-consultar-y-filtrar-la-flota.md) | Consultar y filtrar la flota | Must |
| [HU-03](docs/historias-de-usuario/HU-03-actualizar-datos-operativos-de-un-dron.md) | Actualizar datos operativos del dron | Must |
| [HU-04](docs/historias-de-usuario/HU-04-registrar-una-mision.md) | Registrar una misión | Must |
| [HU-05](docs/historias-de-usuario/HU-05-consultar-misiones-pendientes.md) | Consultar y ordenar misiones pendientes | Must |
| [HU-06](docs/historias-de-usuario/HU-06-solicitar-asignacion-automatica.md) | Solicitar asignación automática | Must |
| [HU-07](docs/historias-de-usuario/HU-07-seleccionar-estrategia-de-asignacion.md) | Elegir una estrategia de asignación | Must |
| [HU-08](docs/historias-de-usuario/HU-08-consultar-resultado-y-justificacion.md) | Consultar resultado y justificación | Must |
| [HU-09](docs/historias-de-usuario/HU-09-informar-que-no-hay-dron-elegible.md) | Informar que no hay dron elegible | Must |
| [HU-10](docs/historias-de-usuario/HU-10-evitar-asignaciones-conflictivas.md) | Evitar asignaciones dobles | Must |
| [HU-11](docs/historias-de-usuario/HU-11-actualizar-estado-de-una-mision.md) | Actualizar estado de una misión | Must |
| [HU-12](docs/historias-de-usuario/HU-12-consultar-historial-de-misiones-y-asignaciones.md) | Consultar historial | Should |
| [HU-13](docs/historias-de-usuario/HU-13-configurar-reserva-operativa.md) | Configurar reserva operativa de autonomía | Should |

Documentos de apoyo: [decisiones de negocio](docs/historias-de-usuario/00-decisiones-de-negocio.md), [glosario y unidades](docs/historias-de-usuario/00-glosario.md) y [matriz de trazabilidad](docs/matriz-trazabilidad.md).

## Reglas centrales

1. Una misión usa un solo dron, y un dron tiene como máximo una misión activa.
2. Solo se asignan drones DISPONIBLES del mismo tipo que la misión.
3. El dron debe tener capacidad suficiente y autonomía que cubra la distancia más la reserva operativa.
4. La estrategia (menor distancia o mayor batería) ordena únicamente a los drones elegibles.
5. Sin candidato, la misión sigue pendiente y se explican las causas.

## Diseño

Los diagramas se modelan en Visual Paradigm: vista de contexto, vista funcional (componentes), vista lógica (clases), vista de desarrollo (paquetes) y vista de despliegue.

- **Polimorfismo:** cada tipo de dron evalúa su capacidad en su propia unidad.
- **Patrón Strategy:** algoritmos de asignación intercambiables.
- **Arquitectura hexagonal:** el dominio no depende de Spring ni de MongoDB.

## Tecnología

Java + Spring Boot + MongoDB.
