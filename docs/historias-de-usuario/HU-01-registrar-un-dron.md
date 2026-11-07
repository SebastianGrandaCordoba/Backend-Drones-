# HU-01 — Registrar un dron

**Prioridad:** Must

**Como** operador de control, **quiero** registrar un dron de un tipo determinado con sus datos operativos, **para** incluirlo en la flota que el sistema puede evaluar para misiones.

## Validación INVEST

- **I — Independiente:** El registro entrega valor antes de activar la asignación automática.
- **N — Negociable:** La ficha y validaciones se pueden ajustar al dominio.
- **V — Valiosa:** Permite representar los recursos de la empresa.
- **E — Estimable:** El alcance de alta y validación está acotado.
- **S — Pequeña:** Trata únicamente el alta de un dron.
- **T — Comprobable:** Se puede verificar alta, duplicado y datos inválidos.

## Criterios de aceptación (Gherkin)

```gherkin
# language: es
Característica: Registro de drones

  Escenario: Registrar un dron válido
    Dado que el operador proporciona un identificador único, tipo, batería, capacidad, autonomía, ubicación y estado inicial válidos
    Cuando solicita registrar el dron
    Entonces el sistema crea el dron
    Y el dron aparece en la consulta de la flota

  Escenario: Rechazar identificador duplicado
    Dado que existe un dron con el identificador proporcionado
    Cuando el operador intenta registrarlo de nuevo
    Entonces el sistema rechaza el alta
    Y explica que el identificador ya existe

  Escenario: Rechazar datos fuera de rango
    Dado que la batería no está entre 0 y 100 por ciento, o que capacidad o autonomía no son positivas
    Cuando el operador solicita el registro
    Entonces el sistema rechaza la solicitud
    Y señala los valores inválidos
```

## Notas

- Tipos de dron: Entrega, Fotografía, Vigilancia, Agricultura y Cartografía.
- La capacidad se expresa en la unidad propia de cada tipo (ver [glosario](00-glosario.md)): kg de carga, GB de almacenamiento, horas de grabación, litros de tanque o hectáreas de cobertura. La autonomía siempre se expresa en km.
- El estado inicial debe ser DISPONIBLE o MANTENIMIENTO, seleccionado por el operador.
