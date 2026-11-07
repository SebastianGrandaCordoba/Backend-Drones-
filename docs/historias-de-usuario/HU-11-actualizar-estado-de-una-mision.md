# HU-11 — Actualizar el estado de una misión

**Prioridad:** Must

**Como** operador de control, **quiero** registrar manualmente el inicio, finalización o cancelación permitida de una misión, **para** mantener el estado operativo de la misión y del dron coherente.

## Validación INVEST

- **I:** Se entrega después de registrar y asignar misiones.
- **N:** Las reglas de transición se pueden ajustar con el dominio.
- **V:** Cierra el ciclo básico y libera el dron cuando corresponde.
- **E:** Estados y transiciones son explícitos.
- **S:** Se limita a transiciones válidas.
- **T:** Se prueban inicio, finalización, cancelación y transición inválida.

## Transiciones permitidas

- PENDIENTE → ASIGNADA (HU-06).
- PENDIENTE → CANCELADA; no hay dron que liberar.
- ASIGNADA → EN_EJECUCION.
- ASIGNADA → CANCELADA; libera el dron.
- EN_EJECUCION → COMPLETADA; libera el dron.
- La cancelación desde EN_EJECUCION queda fuera del MVP: requiere reglas de seguridad y evaluación del estado físico del dron, que no están disponibles sin telemetría.

## Criterios de aceptación (Gherkin)

```gherkin
# language: es
Característica: Ciclo de vida de misión

  Escenario: Iniciar una misión asignada
    Dado que la misión está ASIGNADA a un dron
    Cuando el operador registra su inicio
    Entonces la misión cambia a EN_EJECUCION
    Y el dron cambia a EN_MISION

  Escenario: Completar una misión
    Dado que la misión está EN_EJECUCION
    Cuando el operador registra su finalización
    Entonces la misión cambia a COMPLETADA
    Y el dron vuelve a DISPONIBLE

  Escenario: Cancelar una misión antes de iniciar
    Dado que la misión está ASIGNADA y aún no está en ejecución
    Cuando el operador la cancela
    Entonces la misión cambia a CANCELADA
    Y el dron vuelve a DISPONIBLE

  Escenario: Cancelar una misión pendiente
    Dado que la misión está PENDIENTE y no tiene dron asignado
    Cuando el operador la cancela
    Entonces la misión cambia a CANCELADA
    Y no cambia el estado de ningún dron

  Escenario: Rechazar una transición no permitida
    Dado que la misión está COMPLETADA
    Cuando el operador intenta iniciarla otra vez
    Entonces el sistema rechaza la transición
    Y conserva el estado COMPLETADA
```

## Notas

- La actualización es manual; no se implica comunicación con hardware.
- El dron vuelve a DISPONIBLE cuando su misión se completa o se cancela antes de iniciar; después, el operador puede cambiarlo a MANTENIMIENTO con HU-03 si detecta que no debe volver al servicio.
- El modelo no representa parada de emergencia ni orden de vuelo; la cancelación en ejecución queda fuera de alcance explícitamente.
