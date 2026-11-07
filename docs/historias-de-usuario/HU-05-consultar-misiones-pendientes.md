# HU-05 — Consultar y ordenar misiones pendientes

**Prioridad:** Must

**Como** operador de control, **quiero** consultar misiones pendientes ordenadas por prioridad y límite de tiempo, **para** atender primero las solicitudes más urgentes.

## Validación INVEST

- **I:** La consulta es útil antes de la asignación.
- **N:** Filtros secundarios se pueden negociar.
- **V:** Ayuda a enfocar el trabajo operativo.
- **E:** La regla de ordenamiento es concreta.
- **S:** Se limita a consultar misiones PENDIENTES.
- **T:** El orden se comprueba con prioridades y fechas distintas.

## Criterios de aceptación (Gherkin)

```gherkin
# language: es
Característica: Bandeja de misiones pendientes

  Escenario: Ordenar por prioridad y límite de tiempo
    Dado que hay misiones pendientes con prioridades y límites de tiempo distintos
    Cuando el operador consulta la bandeja
    Entonces las misiones de mayor prioridad aparecen primero
    Y, entre misiones de igual prioridad, aparece primero el límite más próximo

  Escenario: No mostrar misiones que ya no están pendientes
    Dado que existen misiones asignadas, en ejecución o completadas
    Cuando el operador consulta la bandeja de pendientes
    Entonces el sistema no incluye esas misiones

  Escenario: No hay misiones pendientes
    Dado que no existen misiones PENDIENTES
    Cuando el operador consulta la bandeja
    Entonces el sistema informa que no hay misiones pendientes
```

## Notas

- El límite organiza el orden de atención. El sistema no garantiza cumplimiento de plazo hasta contar con estimación de duración/velocidad.
