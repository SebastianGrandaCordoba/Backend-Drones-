# HU-08 — Consultar el resultado y la justificación

**Prioridad:** Must

**Como** operador de control, **quiero** ver el dron asignado, la estrategia y los criterios decisivos, **para** entender y explicar por qué el sistema produjo ese resultado.

## Validación INVEST

- **I:** Separa la consulta del mecanismo que produce la decisión.
- **N:** El detalle puede ampliarse según nuevas estrategias.
- **V:** Hace comprensible la asignación y detectables datos erróneos.
- **E:** Los datos de decisión guardados están definidos.
- **S:** Muestra una asignación concreta, sin analítica agregada.
- **T:** Se puede contrastar con la regla de la estrategia aplicada.

## Criterios de aceptación (Gherkin)

```gherkin
# language: es
Característica: Explicación de una asignación

  Escenario: Consultar una misión asignada
    Dado que la misión tiene un dron asignado
    Cuando el operador consulta el detalle
    Entonces el sistema muestra el dron, fecha, estrategia y estado de la misión

  Escenario: Explicar el criterio de selección
    Dado que existe una asignación con una estrategia registrada
    Cuando el operador consulta su resultado
    Entonces el sistema muestra la distancia y batería consideradas
    Y explica el desempate si se aplicó

  Escenario: Consultar una misión sin asignación
    Dado que la misión no tiene un dron asignado
    Cuando el operador consulta su detalle
    Entonces el sistema indica que sigue sin asignar
```

## Notas

- Registrar una instantánea de los valores usados evita que cambios posteriores de ubicación o batería alteren la explicación histórica.
