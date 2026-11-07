# HU-02 — Consultar y filtrar la flota

**Prioridad:** Must

**Como** operador de control, **quiero** consultar los drones y filtrarlos por tipo o estado, **para** conocer los recursos disponibles y entender por qué otros no pueden asignarse.

## Validación INVEST

- **I:** Es útil sin el motor de asignación.
- **N:** Los filtros y campos visibles se pueden ajustar.
- **V:** Da visibilidad operativa de la flota.
- **E:** Listar y filtrar es acotable.
- **S:** No incluye edición ni analítica.
- **T:** Se comprueba con filtros y resultados vacíos.

## Criterios de aceptación (Gherkin)

```gherkin
# language: es
Característica: Consulta de flota

  Escenario: Consultar todos los drones registrados
    Dado que hay drones registrados
    Cuando el operador consulta la flota
    Entonces el sistema muestra identificador, tipo, estado, batería, capacidad, autonomía y ubicación

  Escenario: Filtrar por tipo y estado
    Dado que existen drones de varios tipos y estados
    Cuando el operador filtra por un tipo y un estado
    Entonces el sistema muestra solo los drones que cumplen ambos filtros

  Escenario: Consultar una flota vacía
    Dado que no hay drones registrados
    Cuando el operador consulta la flota
    Entonces el sistema informa que no hay drones registrados
```

## Notas

- La paginación y búsqueda avanzada se dejan fuera del MVP hasta que el volumen esperado lo justifique.
