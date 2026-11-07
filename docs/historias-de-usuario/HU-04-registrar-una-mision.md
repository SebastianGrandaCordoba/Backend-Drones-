# HU-04 — Registrar una misión

**Prioridad:** Must

**Como** operador de control, **quiero** registrar una misión con tipo, prioridad, distancia, límite de tiempo, destino y capacidad requerida, **para** que el sistema pueda buscar un dron adecuado.

## Validación INVEST

- **I:** El registro se puede completar antes de asignar.
- **N:** Se pueden ajustar campos operativos sin cambiar la finalidad.
- **V:** Hace explícita la demanda que debe atender la flota.
- **E:** La validación está acotada; unidades deben quedar documentadas.
- **S:** Se limita a registrar una misión.
- **T:** Hay criterios para datos válidos, faltantes y límites.

## Criterios de aceptación (Gherkin)

```gherkin
# language: es
Característica: Registro de misiones

  Escenario: Registrar una misión con datos válidos
    Dado que el operador proporciona tipo, prioridad, distancia planificada, límite de tiempo, destino y capacidad requerida válidos
    Cuando solicita registrar la misión
    Entonces el sistema crea la misión con un identificador único
    Y la deja en estado PENDIENTE

  Escenario: Rechazar campos obligatorios faltantes
    Dado que falta el tipo, prioridad, destino, distancia, límite de tiempo o capacidad requerida
    Cuando el operador solicita registrar la misión
    Entonces el sistema rechaza la solicitud
    Y señala los campos faltantes

  Escenario: Rechazar valores no válidos
    Dado que la distancia no es positiva, la capacidad requerida es negativa o el límite de tiempo ya pasó
    Cuando el operador solicita registrar la misión
    Entonces el sistema rechaza la solicitud
    Y no crea la misión
```

## Notas

- Se agregan destino y capacidad requerida porque sin ellos no es posible comparar la ubicación/capacidad del dron con la necesidad de la misión.
- Los tipos de misión son los mismos cinco tipos de dron: Entrega, Fotografía, Vigilancia, Agricultura y Cartografía.
- Prioridad: CRITICA, ALTA, MEDIA, BAJA.
- La capacidad requerida usa la misma unidad que la capacidad del tipo de dron correspondiente y la distancia se expresa en km (ver [glosario](00-glosario.md)).
