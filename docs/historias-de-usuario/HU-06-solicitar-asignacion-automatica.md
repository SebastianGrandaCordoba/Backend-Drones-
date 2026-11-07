# HU-06 — Solicitar asignación automática

**Prioridad:** Must

**Como** operador de control, **quiero** solicitar una asignación automática para una misión pendiente, **para** que el sistema elija un dron que cumpla sus condiciones y aplique la estrategia seleccionada.

## Validación INVEST

- **I:** Se entrega después del registro de drones y misiones.
- **N:** La estrategia puede cambiar sin cambiar la finalidad de asignar.
- **V:** Es el propósito central del producto.
- **E:** La selección es estimable con reglas de elegibilidad y ranking explícitas.
- **S:** Una misión por solicitud; asignación masiva queda fuera del MVP.
- **T:** Se verifican selección, no elegibilidad y misión ya asignada.

## Criterios de aceptación (Gherkin)

```gherkin
# language: es
Característica: Asignación automática de una misión

  Escenario: Asignar el mejor candidato elegible
    Dado que la misión está PENDIENTE
    Y existen drones DISPONIBLES
    Y uno o más drones cumplen tipo, capacidad y autonomía suficiente para la distancia más la reserva operativa
    Cuando el operador solicita la asignación con una estrategia disponible
    Entonces el sistema aplica la estrategia solo a los candidatos elegibles
    Y vincula un único dron a la misión
    Y cambia la misión a ASIGNADA y el dron a ASIGNADO
    Y guarda la estrategia y los datos que justifican el resultado

  Escenario: Usar la estrategia predeterminada cuando no se especifica otra
    Dado que hay candidatos elegibles
    Cuando el operador solicita la asignación sin indicar estrategia
    Entonces el sistema aplica la estrategia predeterminada Menor distancia al destino
    Y la registra junto al resultado

  Escenario: No volver a asignar una misión ya asignada
    Dado que la misión ya está ASIGNADA o EN_EJECUCION
    Cuando el operador vuelve a solicitar asignación
    Entonces el sistema conserva la asignación existente
    Y comunica que la misión ya no está pendiente
```

## Regla de elegibilidad

1. Estado del dron = DISPONIBLE.
2. Tipo de dron igual al tipo de misión.
3. Capacidad del dron ≥ capacidad requerida por misión.
4. Autonomía disponible ≥ distancia planificada más la reserva operativa configurada.

## Notas

- La reserva operativa se expresa en km, igual que la autonomía (HU-13). La batería no es un filtro de elegibilidad: es un criterio de ordenamiento de las estrategias.
- La estrategia predeterminada es Menor distancia al destino.
- Un solo dron por misión y una sola misión activa por dron en el MVP.
