# HU-07 — Seleccionar una estrategia de asignación

**Prioridad:** Must

**Como** operador de control, **quiero** elegir entre estrategias de asignación disponibles, **para** aplicar un criterio claro y comparar la decisión resultante.

## Validación INVEST

- **I:** Se puede añadir tras disponer de una estrategia predeterminada.
- **N:** El catálogo puede ampliarse según se justifique.
- **V:** Da control y hace visibles los objetivos distintos de selección.
- **E:** Dos estrategias iniciales tienen reglas explícitas.
- **S:** Se limita a elegir una estrategia registrada; no permite programar algoritmos en tiempo de ejecución.
- **T:** Cada estrategia se prueba con candidatos elegibles y resultados conocidos.

## Estrategias disponibles

- **Menor distancia al destino:** selecciona el candidato elegible más cercano; desempata por mayor batería.
- **Mayor batería restante:** selecciona el candidato elegible con mayor batería; desempata por menor distancia al destino.

## Criterios de aceptación (Gherkin)

```gherkin
# language: es
Característica: Selección de estrategia de asignación

  Escenario: Aplicar la estrategia de menor distancia
    Dado que existen varios drones elegibles a distancias distintas del destino
    Cuando el operador elige Menor distancia al destino y solicita la asignación
    Entonces el sistema selecciona el dron elegible más cercano
    Y en caso de empate selecciona el de mayor batería restante

  Escenario: Aplicar la estrategia de mayor batería
    Dado que existen varios drones elegibles con baterías distintas
    Cuando el operador elige Mayor batería restante y solicita la asignación
    Entonces el sistema selecciona el dron elegible con mayor batería
    Y en caso de empate selecciona el más cercano al destino

  Escenario: No permitir una estrategia desconocida
    Dado que el operador selecciona un criterio no disponible
    Cuando solicita la asignación
    Entonces el sistema informa que la estrategia no existe
    Y no asigna un dron con un criterio alternativo silencioso
```

## Notas

- Si el operador no elige, se aplica Menor distancia al destino (HU-06).
- Agregar una estrategia nueva no debe modificar el motor de asignación: basta con una nueva implementación de la estrategia.
- Las dos estrategias son heurísticas sencillas para un MVP educativo, no optimizadores globales ni planificadores de trayectoria.
- La elegibilidad se evalúa antes del ranking; una estrategia nunca puede escoger un dron incompatible.
