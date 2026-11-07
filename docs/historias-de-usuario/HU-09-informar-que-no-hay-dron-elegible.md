# HU-09 — Informar cuando no hay dron elegible

**Prioridad:** Must

**Como** operador de control, **quiero** conocer por qué no se encontró un dron elegible, **para** corregir información, esperar disponibilidad o tomar otra decisión.

## Validación INVEST

- **I:** Complementa el flujo exitoso con un resultado alternativo de valor.
- **N:** La explicación puede ampliarse al añadir reglas.
- **V:** Evita asignaciones inválidas y ayuda al operador a actuar.
- **E:** Los motivos se derivan de las reglas de elegibilidad.
- **S:** Se limita a explicar la ausencia de candidato.
- **T:** Se prueba cada condición que elimina candidatos.

## Criterios de aceptación (Gherkin)

```gherkin
# language: es
Característica: Sin candidatos elegibles

  Escenario: No hay drones disponibles
    Dado que ningún dron está DISPONIBLE
    Cuando el operador solicita asignar la misión
    Entonces el sistema informa que no hay candidatos elegibles
    Y mantiene la misión en PENDIENTE

  Escenario: Hay drones pero todos fallan una condición
    Dado que existen drones registrados
    Pero ninguno cumple tipo, capacidad o autonomía más reserva operativa
    Cuando el operador solicita asignar la misión
    Entonces el sistema informa las condiciones incumplidas que puede identificar
    Y no asigna ningún dron

  Escenario: Reintentar luego de actualizar disponibilidad
    Dado que la misión sigue PENDIENTE tras un intento sin candidato
    Cuando un dron elegible queda DISPONIBLE y el operador reintenta
    Entonces el sistema vuelve a evaluar los datos actuales
```

## Notas

- No convertir el resultado sin candidato en error técnico; es un resultado válido del negocio.
