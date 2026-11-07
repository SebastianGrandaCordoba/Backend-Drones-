# HU-12 — Consultar el historial de misiones y asignaciones

**Prioridad:** Should

**Como** operador de control, **quiero** consultar misiones anteriores con sus estados y asignaciones, **para** revisar qué ocurrió y explicar decisiones pasadas.

## Validación INVEST

- **I:** Puede entregarse tras el flujo operativo principal.
- **N:** Filtros y eventos guardados se pueden ampliar.
- **V:** Permite trazabilidad y revisión.
- **E:** Un historial por misión y filtros básicos son estimables.
- **S:** Se limita a consulta, sin analítica avanzada.
- **T:** Se comprueba que se conserva una asignación tras cambiar el estado.

## Criterios de aceptación (Gherkin)

```gherkin
# language: es
Característica: Historial de misiones

  Escenario: Consultar el historial de una misión
    Dado que una misión tuvo una o más actualizaciones
    Cuando el operador consulta el historial
    Entonces el sistema presenta las fechas y cambios de estado registrados
    Y presenta el dron, la estrategia y los criterios usados en la asignación

  Escenario: Filtrar historial por dron
    Dado que existen asignaciones para varios drones
    Cuando el operador filtra por un dron
    Entonces el sistema muestra solo las misiones relacionadas con ese dron

  Escenario: No hay resultados para un filtro
    Dado que no hay misiones relacionadas con el filtro indicado
    Cuando el operador consulta el historial
    Entonces el sistema informa que no hay resultados
```

## Notas

- El historial debe conservar una instantánea de datos de asignación; reportes agregados y métricas quedan fuera del MVP.
