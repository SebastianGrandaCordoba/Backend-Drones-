# Historias de usuario — Centro de Control de Drones

**Versión:** 1.0

## Propósito y trazabilidad

Estas historias definen el alcance funcional del sistema. Son la base de la vista de contexto y de los demás diagramas (Visual Paradigm), y luego del backend. Cada criterio de aceptación debe poder rastrearse a un elemento del contexto, de los componentes o de las clases (ver [matriz de trazabilidad](../matriz-trazabilidad.md)); no se dibuja ningún elemento sin una razón en los requisitos.

## Actor principal

**Operador de control:** persona que gestiona los datos operativos de los drones, registra misiones, solicita asignaciones y consulta sus resultados. Se usa un único rol operativo; no se agregan roles de administración, autenticación ni permisos diferenciados sin un requisito que lo justifique.

Como el sistema no se conecta con drones físicos, el actor que gestiona misiones representa mejor la interacción prevista. Es una decisión de diseño, explicada en [decisiones de negocio](00-decisiones-de-negocio.md).

## Reglas de negocio

1. Una misión requiere un solo dron y un dron solo puede tener una misión activa a la vez.
2. Hay cinco tipos de dron y de misión: Entrega, Fotografía, Vigilancia, Agricultura y Cartografía. El tipo de misión debe coincidir con el tipo de dron.
3. Un dron solo es candidato si está DISPONIBLE, su tipo coincide con el de la misión, su capacidad cubre la capacidad requerida y su autonomía cubre la distancia más la reserva operativa configurada.
4. La asignación aplica una estrategia sobre los candidatos elegibles: **menor distancia al destino** (predeterminada; desempate por mayor batería) o **mayor batería restante** (desempate por menor distancia).
5. Las misiones pendientes se muestran por prioridad descendente y, a igual prioridad, por el límite de tiempo más próximo.
6. Si no hay candidato elegible, la misión sigue pendiente y el sistema informa las razones.
7. No hay telemetría ni integración con drones físicos: el operador mantiene manualmente los datos y estados.

## Datos añadidos al enunciado

El enunciado nombra ubicación, capacidad y autonomía del dron y distancia de la misión. Para poder compararlos se añaden a la misión la **ubicación de destino** y la **capacidad requerida**. La ubicación se representa con coordenadas; la proximidad es una aproximación geométrica, no una ruta aérea.

El límite de tiempo sirve para ordenar misiones. No se promete cumplir el plazo, porque faltan velocidad y duración estimada. Las unidades están en el [glosario](00-glosario.md).

## Estados

- **Dron:** DISPONIBLE, ASIGNADO, EN_MISION, MANTENIMIENTO.
- **Misión:** PENDIENTE, ASIGNADA, EN_EJECUCION, COMPLETADA, CANCELADA.
- Una asignación cambia la misión de PENDIENTE a ASIGNADA y el dron de DISPONIBLE a ASIGNADO. El operador informa inicio, finalización o cancelación; al completar o cancelar, el dron vuelve a DISPONIBLE (HU-11).

## Historias

| ID | Historia | Prioridad |
|---|---|---|
| [HU-01](HU-01-registrar-un-dron.md) | Registrar un dron | Must |
| [HU-02](HU-02-consultar-y-filtrar-la-flota.md) | Consultar y filtrar la flota | Must |
| [HU-03](HU-03-actualizar-datos-operativos-de-un-dron.md) | Actualizar datos operativos del dron | Must |
| [HU-04](HU-04-registrar-una-mision.md) | Registrar una misión | Must |
| [HU-05](HU-05-consultar-misiones-pendientes.md) | Consultar y ordenar misiones pendientes | Must |
| [HU-06](HU-06-solicitar-asignacion-automatica.md) | Solicitar asignación automática | Must |
| [HU-07](HU-07-seleccionar-estrategia-de-asignacion.md) | Elegir una estrategia de asignación | Must |
| [HU-08](HU-08-consultar-resultado-y-justificacion.md) | Consultar resultado y justificación | Must |
| [HU-09](HU-09-informar-que-no-hay-dron-elegible.md) | Informar que no hay dron elegible | Must |
| [HU-10](HU-10-evitar-asignaciones-conflictivas.md) | Evitar asignaciones dobles | Must |
| [HU-11](HU-11-actualizar-estado-de-una-mision.md) | Actualizar estado de una misión | Must |
| [HU-12](HU-12-consultar-historial-de-misiones-y-asignaciones.md) | Consultar historial | Should |
| [HU-13](HU-13-configurar-reserva-operativa.md) | Configurar reserva operativa de autonomía | Should |
