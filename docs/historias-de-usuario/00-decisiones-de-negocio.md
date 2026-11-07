# Decisiones de negocio — Centro de Control de Drones

Estas decisiones hacen que las historias y los diagramas sean comprobables. El enunciado pide asignar «el mejor dron disponible»; aquí se define qué significa «mejor» para que no quede ambiguo.

## Actor

El actor principal es el **Operador de control**: administra la disponibilidad operativa, registra misiones, solicita asignaciones y revisa resultados. Se usa un solo rol porque no se especificó separación de permisos. La NASA separa en operaciones con varios drones la gestión de misión, la gestión de trayectoria, las operaciones tácticas y el control del vehículo; como este sistema no controla vehículos, el operador se limita a la gestión de misión.

## Tipos de dron

Los cinco tipos del enunciado se modelan como **tipos distintos de dron**, porque la «capacidad» no significa lo mismo en cada uno:

| Tipo | Qué mide la capacidad | Unidad |
|---|---|---|
| Entrega | Carga útil | kg |
| Fotografía | Almacenamiento de imágenes | GB |
| Vigilancia | Tiempo de grabación | h |
| Agricultura | Tanque de aspersión | L |
| Cartografía | Área de cobertura por vuelo | ha |

Cada tipo sabe evaluar si su capacidad cubre lo que pide una misión de su mismo tipo. Por eso el tipo de misión debe coincidir con el tipo de dron, y la capacidad requerida de la misión se expresa en la unidad de ese tipo.

## Política de asignación

**Filtro de elegibilidad (obligatorio):** dron DISPONIBLE; tipo igual al de la misión; capacidad suficiente; autonomía disponible ≥ distancia planificada + reserva operativa.

**Ranking intercambiable (patrón Strategy), solo sobre los elegibles:**

1. `MenorDistancia` (**predeterminada**): menor distancia geométrica al destino; empate por mayor batería.
2. `MayorBateria`: mayor batería restante; empate por menor distancia al destino.

El operador elige la estrategia al solicitar la asignación; si no elige, se usa la predeterminada. Agregar otra estrategia solo requiere una nueva implementación, sin cambiar el motor.

**Orden de atención de misiones:** prioridad descendente y, a igual prioridad, límite de tiempo ascendente. La prioridad ordena misiones; no es una estrategia para elegir drones.

La literatura sobre asignación de tareas a UAV heterogéneos considera recursos, tiempo y energía, y muestra que optimizar uno puede perjudicar al otro. Se adopta una versión deliberadamente sencilla y explicable, no un optimizador global ni un planificador de rutas.

## Datos añadidos

- `ubicacionDestino` en Misión, para comparar con la ubicación del dron.
- `capacidadRequerida` en Misión, para comparar con la capacidad del dron.
- Reserva operativa configurable en km; no se inventa un valor fijo.

La distancia geométrica es solo una aproximación: no modela ruta, obstáculos, clima, velocidad ni consumo real.

## Fuera de alcance

- Telemetría o API de drones reales y envío de órdenes de vuelo.
- Planificación de rutas, obstáculos, clima o certificación operacional.
- Varios drones por misión o varias misiones simultáneas por dron.
- Garantizar el cumplimiento del límite de tiempo (faltan velocidad y duración).
- Autenticación, roles, notificaciones e interfaz gráfica (el frontend es opcional).

## Fuentes

- Zhai et al. (2021), *Real-Time Task Allocation of Heterogeneous Unmanned Aerial Vehicles for Search and Prosecute Mission*: https://onlinelibrary.wiley.com/doi/10.1155/2021/5516086
- NASA, *Defining Roles in Multi-Vehicle Operations* (2025): https://ntrs.nasa.gov/api/citations/20250008934/downloads/mN%20July%202025%20Meeting%20Report.pdf?attachment=true
