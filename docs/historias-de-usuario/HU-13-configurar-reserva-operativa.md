# HU-13 — Configurar la reserva operativa de autonomía

**Prioridad:** Should

**Como** operador de control, **quiero** definir la reserva mínima de autonomía que debe conservar un dron después de cubrir la distancia planificada, **para** que el filtro de elegibilidad use un umbral explícito y ajustable a la política operativa.

## Validación INVEST

- **I:** Puede configurarse antes de solicitar asignaciones y aporta valor por sí misma.
- **N:** El valor puede ajustarse según la política de operación.
- **V:** Evita que el sistema considere elegible un dron cuyo margen energético no alcanza la reserva definida.
- **E:** Se limita a una configuración global sencilla, expresada en la misma unidad que la autonomía.
- **S:** No configura parámetros por tipo de misión, dron o condiciones ambientales.
- **T:** Se verifica el valor guardado, los límites inválidos y su efecto en la elegibilidad.

## Criterios de aceptación (Gherkin)

```gherkin
# language: es
Característica: Configuración de reserva operativa

  Escenario: Guardar una reserva válida
    Dado que el operador introduce una reserva igual o mayor que cero
    Y la expresa en la misma unidad definida para la autonomía
    Cuando guarda la configuración
    Entonces el sistema conserva ese valor como reserva operativa vigente

  Escenario: Rechazar una reserva negativa o con unidad incompatible
    Dado que el operador introduce un valor negativo o una unidad no admitida
    Cuando intenta guardar la configuración
    Entonces el sistema informa la validación correspondiente
    Y conserva la configuración vigente anterior

  Escenario: Aplicar reserva al evaluar elegibilidad
    Dado que la autonomía disponible del dron no cubre la distancia de la misión más la reserva vigente
    Cuando el sistema evalúa drones para esa misión
    Entonces el dron no se considera elegible
    Y el resultado explica que no alcanza la autonomía requerida
```

## Notas

- El MVP usa un único valor global configurable; la configuración por misión o tipo de dron queda para una evolución futura.
- No se fija un valor inicial numérico sin una decisión del caso/profesor. Mientras no se configure, la aplicación debe impedir asignar con un valor supuesto y comunicar la configuración faltante.
- Distancia, autonomía y reserva se expresan en km (ver [glosario](00-glosario.md)).
