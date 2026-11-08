# Centro de Control de Drones — Backend

Proyecto final de Diseño de Software. El sistema administra una flota de drones y **asigna automáticamente el mejor dron disponible para cada misión**, con algoritmos de asignación intercambiables.

El alcance es un backend REST. No se conecta con drones físicos ni ejecuta vuelos: el operador registra manualmente los datos de la flota y el estado de las misiones.

## Caso de estudio

Una empresa tiene una flota de drones de cinco tipos: **Entrega, Fotografía, Vigilancia, Agricultura y Cartografía**.

- Cada dron tiene batería, capacidad, autonomía, estado y ubicación.
- Cada misión tiene prioridad, distancia, tipo y tiempo límite.

## Historias de usuario

Las historias, con validación INVEST y criterios de aceptación en Gherkin, están en [`docs/historias-de-usuario/`](docs/historias-de-usuario/00-resumen.md).

| ID | Historia | Prioridad |
|---|---|---|
| [HU-01](docs/historias-de-usuario/HU-01-registrar-un-dron.md) | Registrar un dron | Must |
| [HU-02](docs/historias-de-usuario/HU-02-consultar-y-filtrar-la-flota.md) | Consultar y filtrar la flota | Must |
| [HU-03](docs/historias-de-usuario/HU-03-actualizar-datos-operativos-de-un-dron.md) | Actualizar datos operativos del dron | Must |
| [HU-04](docs/historias-de-usuario/HU-04-registrar-una-mision.md) | Registrar una misión | Must |
| [HU-05](docs/historias-de-usuario/HU-05-consultar-misiones-pendientes.md) | Consultar y ordenar misiones pendientes | Must |
| [HU-06](docs/historias-de-usuario/HU-06-solicitar-asignacion-automatica.md) | Solicitar asignación automática | Must |
| [HU-07](docs/historias-de-usuario/HU-07-seleccionar-estrategia-de-asignacion.md) | Elegir una estrategia de asignación | Must |
| [HU-08](docs/historias-de-usuario/HU-08-consultar-resultado-y-justificacion.md) | Consultar resultado y justificación | Must |
| [HU-09](docs/historias-de-usuario/HU-09-informar-que-no-hay-dron-elegible.md) | Informar que no hay dron elegible | Must |
| [HU-10](docs/historias-de-usuario/HU-10-evitar-asignaciones-conflictivas.md) | Evitar asignaciones dobles | Must |
| [HU-11](docs/historias-de-usuario/HU-11-actualizar-estado-de-una-mision.md) | Actualizar estado de una misión | Must |
| [HU-12](docs/historias-de-usuario/HU-12-consultar-historial-de-misiones-y-asignaciones.md) | Consultar historial | Should |
| [HU-13](docs/historias-de-usuario/HU-13-configurar-reserva-operativa.md) | Configurar reserva operativa de autonomía | Should |

Documentos de apoyo: [decisiones de negocio](docs/historias-de-usuario/00-decisiones-de-negocio.md), [glosario y unidades](docs/historias-de-usuario/00-glosario.md) y [matriz de trazabilidad](docs/matriz-trazabilidad.md).

## Reglas centrales

1. Una misión usa un solo dron, y un dron tiene como máximo una misión activa.
2. Solo se asignan drones DISPONIBLES del mismo tipo que la misión.
3. El dron debe tener capacidad suficiente y autonomía que cubra la distancia más la reserva operativa.
4. La estrategia (menor distancia o mayor batería) ordena únicamente a los drones elegibles.
5. Sin candidato, la misión sigue pendiente y se explican las causas.

## Diseño

Los cinco diagramas están modelados en Visual Paradigm: vista de contexto, vista funcional (componentes), vista lógica (clases), vista de desarrollo (paquetes) y vista de despliegue. El código usa **exactamente los mismos nombres** de paquetes, interfaces y clases que los diagramas.

- **Polimorfismo:** `Dron` es abstracta y cada tipo (`DronEntrega`, `DronFotografia`, `DronVigilancia`, `DronAgricultura`, `DronCartografia`) define su tipo de misión y su unidad de capacidad.
- **Patrón Strategy:** `EstrategiaAsignacion` con `MenorDistancia` (predeterminada) y `MayorBateria`. Para agregar otra estrategia basta con una nueva clase y registrarla en `BeansConfig`; el `MotorAsignacion` no cambia.
- **Encapsulamiento:** los estados de `Dron` y `Mision` solo cambian mediante operaciones de negocio (`marcarAsignado()`, `iniciar()`, `completar()`, `cancelar()`…), que rechazan transiciones no permitidas.
- **Arquitectura hexagonal:** el dominio y la aplicación no dependen de Spring ni de MongoDB; los adaptadores REST y MongoDB se conectan a través de puertos.

### Estructura de paquetes (vista de desarrollo)

```
com.drones.centrocontrol
├── dominio                      ← reglas de negocio, sin frameworks
│   ├── modelo                   Dron (+5 tipos), Mision, Asignacion, Ubicacion, ReservaOperativa,
│   │                            ResultadoAsignacion y enumeraciones
│   ├── estrategia               EstrategiaAsignacion, MenorDistancia, MayorBateria
│   └── servicio                 MotorAsignacion
├── aplicacion                   ← casos de uso
│   ├── puerto.entrada           IGestionFlota, IConsultaFlota, IGestionMisiones, IConsultaMisiones,
│   │                            IAsignacion, IConfiguracionOperativa, IConsultaReserva
│   ├── servicio                 GestionFlotaService, GestionMisionesService, AsignacionService,
│   │                            ConfiguracionService
│   └── puerto.salida            DronRepositorio, MisionRepositorio, AsignacionRepositorio,
│                                ConfiguracionRepositorio
└── infraestructura              ← adaptadores
    ├── entrada.rest             DronController, MisionController, AsignacionController,
    │                            ConfiguracionController (+ ManejadorErrores)
    ├── salida.mongo             DronMongoAdapter, MisionMongoAdapter, AsignacionMongoAdapter,
    │                            ConfiguracionMongoAdapter
    └── configuracion            BeansConfig (ensambla puertos y adaptadores)
```

Cada componente de la vista funcional corresponde a un servicio de aplicación:

| Componente | Servicio | Ofrece | Necesita |
|---|---|---|---|
| Gestión de Flota | `GestionFlotaService` | `IGestionFlota`, `IConsultaFlota` | — |
| Gestión de Misiones | `GestionMisionesService` | `IGestionMisiones`, `IConsultaMisiones` | `IConsultaFlota` (dron asignado) |
| Motor de Asignación | `AsignacionService` + `MotorAsignacion` | `IAsignacion` | `IConsultaFlota`, `IConsultaMisiones`, `IConsultaReserva` |
| Configuración Operativa | `ConfiguracionService` | `IConfiguracionOperativa`, `IConsultaReserva` | — |

## Tecnología

Java 21 · Spring Boot 3.3 · MongoDB 7 · Maven · JUnit 5.

## Cómo ejecutar

1. Inicie MongoDB en `localhost:27017`. Con Docker: `docker compose up -d`. También sirve una instalación local de MongoDB.
2. Ejecute la aplicación:

   ```bash
   mvn spring-boot:run
   ```

   o compile el artefacto de la vista de despliegue y ejecútelo:

   ```bash
   mvn package
   java -jar target/centro-control-drones.jar
   ```

3. La API queda en `http://localhost:8080/api`. Para usar otra base de datos defina la variable `MONGODB_URI`.

Antes de solicitar asignaciones hay que configurar la reserva operativa (HU-13); el sistema no asume un valor por defecto.

## Pruebas

```bash
mvn test
```

Las pruebas no necesitan MongoDB: las de dominio verifican las estrategias, la elegibilidad, los motivos de rechazo y las transiciones de estado; las de aplicación recorren los casos de uso completos con repositorios en memoria, incluida la doble asignación simultánea (HU-10).

## API REST

| Método | Ruta | Historia | Descripción |
|---|---|---|---|
| POST | `/api/drones` | HU-01 | Registrar un dron |
| GET | `/api/drones?tipo=&estado=` | HU-02 | Consultar y filtrar la flota |
| GET | `/api/drones/{id}` | HU-02 | Consultar un dron |
| PUT | `/api/drones/{id}/datos-operativos` | HU-03 | Actualizar batería, autonomía y ubicación |
| PUT | `/api/drones/{id}/estado` | HU-03 | Pasar a MANTENIMIENTO o volver a DISPONIBLE |
| POST | `/api/misiones` | HU-04 | Registrar una misión (queda PENDIENTE) |
| GET | `/api/misiones/pendientes` | HU-05 | Bandeja ordenada por prioridad y límite de tiempo |
| GET | `/api/misiones/{id}` | HU-05 | Consultar una misión |
| POST | `/api/misiones/{id}/iniciar` | HU-11 | ASIGNADA → EN_EJECUCION (dron EN_MISION) |
| POST | `/api/misiones/{id}/completar` | HU-11 | EN_EJECUCION → COMPLETADA (dron DISPONIBLE) |
| POST | `/api/misiones/{id}/cancelar` | HU-11 | PENDIENTE/ASIGNADA → CANCELADA |
| POST | `/api/asignaciones` | HU-06, 07, 09, 10 | Asignar automáticamente: `{"misionId": "...", "estrategia": "MENOR_DISTANCIA"}` |
| GET | `/api/asignaciones/estrategias` | HU-07 | Estrategias disponibles |
| GET | `/api/asignaciones/mision/{misionId}` | HU-08 | Resultado y justificación de la asignación |
| GET | `/api/asignaciones?dronId=&misionId=` | HU-12 | Historial de asignaciones |
| PUT | `/api/configuracion/reserva` | HU-13 | Configurar la reserva: `{"valor": 5, "unidad": "km"}` |
| GET | `/api/configuracion/reserva` | HU-13 | Consultar la reserva vigente |

Códigos de respuesta: `201` creado, `200` correcto (incluye "sin candidatos", que es un resultado válido del negocio), `400` datos inválidos, `404` no encontrado, `409` regla de negocio o transición no permitida.

### Ejemplo de uso

```bash
# 1. Reserva operativa
curl -X PUT localhost:8080/api/configuracion/reserva -H "Content-Type: application/json" \
     -d '{"valor": 5, "unidad": "km"}'

# 2. Dron de entrega (capacidad en kg)
curl -X POST localhost:8080/api/drones -H "Content-Type: application/json" -d '{
  "id": "ENT-01", "tipo": "ENTREGA", "bateria": 85, "autonomiaKm": 40, "capacidad": 5,
  "estado": "DISPONIBLE", "ubicacion": {"latitud": 6.2442, "longitud": -75.5812}}'

# 3. Misión de entrega
curl -X POST localhost:8080/api/misiones -H "Content-Type: application/json" -d '{
  "tipo": "ENTREGA", "prioridad": "ALTA", "distanciaKm": 12, "limiteTiempo": "2026-11-25T18:00:00",
  "destino": {"latitud": 6.2518, "longitud": -75.5636}, "capacidadRequerida": 4}'

# 4. Asignación automática con la estrategia predeterminada
curl -X POST localhost:8080/api/asignaciones -H "Content-Type: application/json" \
     -d '{"misionId": "<id de la misión>"}'
```
