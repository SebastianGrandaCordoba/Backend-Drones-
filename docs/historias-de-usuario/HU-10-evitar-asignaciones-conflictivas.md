# HU-10 — Evitar asignaciones dobles

**Prioridad:** Must

**Como** operador de control, **quiero** que un dron con una misión activa no pueda recibir otra misión, **para** evitar comprometer dos veces el mismo recurso.

## Validación INVEST

- **I:** Es una regla de consistencia propia, distinta del ranking de candidatos.
- **N:** La política puede extenderse a reservas futuras si el caso agrega programación.
- **V:** Evita asignaciones contradictorias.
- **E:** La condición de misión activa puede comprobarse de forma concreta.
- **S:** Un dron y una asignación a la vez en el MVP.
- **T:** Se verifican asignaciones sucesivas y solicitudes concurrentes.

## Criterios de aceptación (Gherkin)

```gherkin
# language: es
Característica: Exclusividad de drones en misiones activas

  Escenario: Excluir un dron ya asignado
    Dado que un dron está ASIGNADO o EN_MISION
    Cuando el sistema busca candidatos para otra misión
    Entonces excluye ese dron

  Escenario: Evitar doble asignación ante dos solicitudes simultáneas
    Dado que dos solicitudes intentan asignar el mismo dron disponible a misiones distintas al mismo tiempo
    Cuando el sistema intenta confirmar ambas asignaciones
    Entonces solo una de ellas queda confirmada con ese dron
    Y la otra no usa ese dron y vuelve a evaluar los candidatos restantes

  Escenario: Permitir reutilización cuando el dron se libera
    Dado que el dron terminó su misión y volvió a DISPONIBLE
    Cuando el operador solicita una nueva asignación
    Entonces el sistema puede volver a considerarlo si cumple las condiciones
```

## Notas

- No hay reservas de misiones futuras en el MVP; para eso se necesitarían fechas/ventanas de inicio y duración.
