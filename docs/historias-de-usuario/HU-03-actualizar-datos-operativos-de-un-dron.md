# HU-03 — Actualizar datos operativos de un dron

**Prioridad:** Must

**Como** operador de control, **quiero** actualizar manualmente batería, autonomía disponible, capacidad operativa, ubicación o estado del dron, **para** que el motor use la información más reciente registrada.

## Validación INVEST

- **I:** Aporta valor separadamente del algoritmo de asignación.
- **N:** Los campos que cambien en operación se pueden ajustar.
- **V:** Reduce decisiones basadas en datos antiguos.
- **E:** La actualización y validación son acotables.
- **S:** Modifica únicamente la ficha de un dron.
- **T:** Se verifica cambio, valor inválido y dron inexistente.

## Criterios de aceptación (Gherkin)

```gherkin
# language: es
Característica: Actualización de drones

  Escenario: Actualizar datos operativos válidos
    Dado que el dron está registrado
    Cuando el operador guarda una batería, autonomía o ubicación válidas
    Entonces el sistema conserva los nuevos datos
    Y las consultas posteriores muestran los valores actualizados

  Escenario: Marcar un dron en mantenimiento
    Dado que el dron está disponible
    Cuando el operador cambia su estado a MANTENIMIENTO
    Entonces el sistema guarda el nuevo estado
    Y el dron deja de ser elegible para asignaciones

  Escenario: Rechazar cambios para un dron inexistente
    Dado que no existe el identificador indicado
    Cuando el operador intenta actualizarlo
    Entonces el sistema informa que no encontró el dron
    Y no crea otro registro
```

## Notas

- La primera versión no recibe telemetría; las actualizaciones las registra el operador.
- No permitir que el operador marque DISPONIBLE a un dron que aún tiene una misión activa. La HU-11 gobierna su liberación.
