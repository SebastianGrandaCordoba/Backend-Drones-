package com.drones.centrocontrol.aplicacion;

import com.drones.centrocontrol.aplicacion.puerto.salida.AsignacionRepositorio;
import com.drones.centrocontrol.aplicacion.puerto.salida.ConfiguracionRepositorio;
import com.drones.centrocontrol.aplicacion.puerto.salida.DronRepositorio;
import com.drones.centrocontrol.aplicacion.puerto.salida.MisionRepositorio;
import com.drones.centrocontrol.aplicacion.servicio.AsignacionService;
import com.drones.centrocontrol.aplicacion.servicio.ConfiguracionService;
import com.drones.centrocontrol.aplicacion.servicio.GestionFlotaService;
import com.drones.centrocontrol.aplicacion.servicio.GestionMisionesService;
import com.drones.centrocontrol.dominio.estrategia.MayorBateria;
import com.drones.centrocontrol.dominio.estrategia.MenorDistancia;
import com.drones.centrocontrol.dominio.modelo.Asignacion;
import com.drones.centrocontrol.dominio.modelo.Dron;
import com.drones.centrocontrol.dominio.modelo.EstadoDron;
import com.drones.centrocontrol.dominio.modelo.EstadoMision;
import com.drones.centrocontrol.dominio.modelo.Mision;
import com.drones.centrocontrol.dominio.modelo.Prioridad;
import com.drones.centrocontrol.dominio.modelo.ReservaOperativa;
import com.drones.centrocontrol.dominio.modelo.ResultadoAsignacion;
import com.drones.centrocontrol.dominio.modelo.TipoMision;
import com.drones.centrocontrol.dominio.modelo.Ubicacion;
import com.drones.centrocontrol.dominio.servicio.MotorAsignacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prueba los casos de uso completos con repositorios en memoria (sin Spring ni MongoDB). */
class FlujoDeAsignacionTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-25T13:00:00Z"), ZoneId.of("America/Bogota"));
    private static final LocalDateTime AHORA = LocalDateTime.now(RELOJ);
    private static final Ubicacion BASE = new Ubicacion(6.2442, -75.5812);

    private DronesEnMemoria drones;
    private GestionFlotaService flota;
    private GestionMisionesService misiones;
    private ConfiguracionService configuracion;
    private AsignacionService asignacion;

    @BeforeEach
    void preparar() {
        drones = new DronesEnMemoria();
        MisionesEnMemoria misionesRepo = new MisionesEnMemoria();
        AsignacionesEnMemoria asignaciones = new AsignacionesEnMemoria();
        flota = new GestionFlotaService(drones);
        configuracion = new ConfiguracionService(new ConfiguracionEnMemoria());
        misiones = new GestionMisionesService(misionesRepo, asignaciones, drones, flota, RELOJ);
        asignacion = new AsignacionService(misiones, flota, configuracion,
                new MotorAsignacion(List.of(new MenorDistancia(), new MayorBateria())), drones, misionesRepo, asignaciones);
    }

    private Mision nuevaMisionEntrega() {
        return misiones.registrar(TipoMision.ENTREGA, Prioridad.ALTA, 10, AHORA.plusHours(3),
                new Ubicacion(6.25, -75.58), 5);
    }

    @Test
    void flujoCompletoRegistrarAsignarIniciarYCompletar() {
        configuracion.configurarReserva(5, "km");
        flota.registrar("ENT-1", TipoMision.ENTREGA, 90, 60, 10, EstadoDron.DISPONIBLE, BASE);
        Mision mision = nuevaMisionEntrega();

        ResultadoAsignacion r = asignacion.asignar(mision.getId(), null);
        assertTrue(r.fueExitoso());
        assertEquals(EstadoDron.ASIGNADO, flota.buscarPorId("ENT-1").getEstado());
        assertEquals(EstadoMision.ASIGNADA, misiones.buscarPorId(mision.getId()).getEstado());

        misiones.iniciar(mision.getId());
        assertEquals(EstadoDron.EN_MISION, flota.buscarPorId("ENT-1").getEstado());

        misiones.completar(mision.getId());
        assertEquals(EstadoDron.DISPONIBLE, flota.buscarPorId("ENT-1").getEstado());
        assertEquals(EstadoMision.COMPLETADA, misiones.buscarPorId(mision.getId()).getEstado());
        assertFalse(asignacion.consultarHistorial("ENT-1", null).get(0).isActiva());
    }

    @Test
    void noSeAsignaSinReservaOperativaConfigurada() {
        flota.registrar("ENT-1", TipoMision.ENTREGA, 90, 60, 10, EstadoDron.DISPONIBLE, BASE);
        Mision mision = nuevaMisionEntrega();
        assertThrows(IllegalStateException.class, () -> asignacion.asignar(mision.getId(), null));
    }

    @Test
    void siOtraSolicitudTomaElDronSeReevaluaConLosRestantes() {
        configuracion.configurarReserva(5, "km");
        flota.registrar("CERCA", TipoMision.ENTREGA, 50, 60, 10, EstadoDron.DISPONIBLE, new Ubicacion(6.2501, -75.5801));
        flota.registrar("LEJOS", TipoMision.ENTREGA, 50, 60, 10, EstadoDron.DISPONIBLE, new Ubicacion(6.40, -75.70));
        Mision mision = nuevaMisionEntrega();
        drones.tomadoPorOtraSolicitud.add("CERCA");

        ResultadoAsignacion r = asignacion.asignar(mision.getId(), null);

        assertEquals("LEJOS", r.getAsignacion().getDron().getId());
    }

    @Test
    void cancelarMisionAsignadaLiberaElDron() {
        configuracion.configurarReserva(0, "km");
        flota.registrar("ENT-1", TipoMision.ENTREGA, 90, 60, 10, EstadoDron.DISPONIBLE, BASE);
        Mision mision = nuevaMisionEntrega();
        asignacion.asignar(mision.getId(), "MAYOR_BATERIA");

        misiones.cancelar(mision.getId());

        assertEquals(EstadoDron.DISPONIBLE, flota.buscarPorId("ENT-1").getEstado());
        assertEquals(EstadoMision.CANCELADA, misiones.buscarPorId(mision.getId()).getEstado());
    }

    @Test
    void pendientesSeOrdenanPorPrioridadYLimite() {
        Mision baja = misiones.registrar(TipoMision.ENTREGA, Prioridad.BAJA, 5, AHORA.plusHours(1), BASE, 1);
        Mision altaTarde = misiones.registrar(TipoMision.ENTREGA, Prioridad.ALTA, 5, AHORA.plusHours(5), BASE, 1);
        Mision altaPronto = misiones.registrar(TipoMision.ENTREGA, Prioridad.ALTA, 5, AHORA.plusHours(2), BASE, 1);
        Mision critica = misiones.registrar(TipoMision.ENTREGA, Prioridad.CRITICA, 5, AHORA.plusHours(9), BASE, 1);

        List<String> orden = misiones.consultarPendientes().stream().map(Mision::getId).toList();

        assertEquals(List.of(critica.getId(), altaPronto.getId(), altaTarde.getId(), baja.getId()), orden);
    }

    @Test
    void validacionesDeRegistro() {
        flota.registrar("ENT-1", TipoMision.ENTREGA, 90, 60, 10, EstadoDron.DISPONIBLE, BASE);
        assertThrows(IllegalStateException.class,
                () -> flota.registrar("ENT-1", TipoMision.ENTREGA, 90, 60, 10, EstadoDron.DISPONIBLE, BASE));
        assertThrows(IllegalArgumentException.class,
                () -> flota.registrar("ENT-2", TipoMision.ENTREGA, 90, 60, 10, EstadoDron.ASIGNADO, BASE));
        assertThrows(IllegalArgumentException.class, () -> misiones.registrar(TipoMision.ENTREGA, Prioridad.ALTA, 10,
                AHORA.minusMinutes(1), BASE, 1));
        assertThrows(IllegalArgumentException.class, () -> configuracion.configurarReserva(-1, "km"));
        assertThrows(IllegalArgumentException.class, () -> configuracion.configurarReserva(3, "millas"));
    }

    @Test
    void elOperadorNoPuedeLiberarUnDronConMisionActiva() {
        configuracion.configurarReserva(0, "km");
        flota.registrar("ENT-1", TipoMision.ENTREGA, 90, 60, 10, EstadoDron.DISPONIBLE, BASE);
        asignacion.asignar(nuevaMisionEntrega().getId(), null);
        assertThrows(IllegalStateException.class, () -> flota.cambiarEstado("ENT-1", EstadoDron.DISPONIBLE));
    }

    // ---------------------------------------------------------------- repositorios en memoria

    /** Simula MongoDB; permite forzar que otra solicitud tome un dron entre la consulta y la confirmación. */
    static class DronesEnMemoria implements DronRepositorio {
        final Map<String, Dron> datos = new LinkedHashMap<>();
        final Set<String> tomadoPorOtraSolicitud = new HashSet<>();

        public Dron guardar(Dron dron) { datos.put(dron.getId(), copia(dron)); return dron; }
        public Optional<Dron> buscarPorId(String id) { return Optional.ofNullable(datos.get(id)).map(DronesEnMemoria::copia); }
        public boolean existe(String id) { return datos.containsKey(id); }
        public List<Dron> buscar(TipoMision tipo, EstadoDron estado) {
            return datos.values().stream().filter(d -> tipo == null || d.getTipo() == tipo)
                    .filter(d -> estado == null || d.getEstado() == estado).map(DronesEnMemoria::copia).toList();
        }
        public boolean marcarAsignadoSiDisponible(String id) {
            Dron d = datos.get(id);
            if (tomadoPorOtraSolicitud.remove(id)) {
                d.marcarAsignado();
                return false;
            }
            if (d.getEstado() != EstadoDron.DISPONIBLE) return false;
            d.marcarAsignado();
            return true;
        }
        static Dron copia(Dron d) {
            Dron c = new com.drones.centrocontrol.dominio.modelo.DronEntrega(d.getId(), d.getBateria(), d.getAutonomiaKm(),
                    d.getCapacidad(), EstadoDron.DISPONIBLE, d.getUbicacion());
            switch (d.getEstado()) {
                case ASIGNADO -> c.marcarAsignado();
                case EN_MISION -> { c.marcarAsignado(); c.iniciarMision(); }
                case MANTENIMIENTO -> c.enviarAMantenimiento();
                default -> { }
            }
            return c;
        }
    }

    static class MisionesEnMemoria implements MisionRepositorio {
        final Map<String, Mision> datos = new LinkedHashMap<>();
        public Mision guardar(Mision m) { datos.put(m.getId(), copia(m)); return m; }
        public Optional<Mision> buscarPorId(String id) { return Optional.ofNullable(datos.get(id)).map(MisionesEnMemoria::copia); }
        public List<Mision> buscarPorEstado(EstadoMision e) {
            return datos.values().stream().filter(m -> m.getEstado() == e).map(MisionesEnMemoria::copia).toList();
        }
        public boolean marcarAsignadaSiPendiente(String id) {
            Mision m = datos.get(id);
            if (!m.estaPendiente()) return false;
            m.marcarAsignada();
            return true;
        }
        static Mision copia(Mision m) {
            return new Mision(m.getId(), m.getTipo(), m.getPrioridad(), m.getDistanciaKm(), m.getLimiteTiempo(),
                    m.getDestino(), m.getCapacidadRequerida(), m.getEstado());
        }
    }

    static class AsignacionesEnMemoria implements AsignacionRepositorio {
        final Map<String, Asignacion> datos = new LinkedHashMap<>();
        public Asignacion guardar(Asignacion a) { datos.put(a.getId(), a); return a; }
        public Optional<Asignacion> buscarActivaPorMision(String misionId) {
            return datos.values().stream().filter(a -> a.isActiva() && a.getMision().getId().equals(misionId)).findFirst();
        }
        public List<Asignacion> buscar(String dronId, String misionId) {
            List<Asignacion> r = new ArrayList<>(datos.values().stream()
                    .filter(a -> dronId == null || a.getDron().getId().equals(dronId))
                    .filter(a -> misionId == null || a.getMision().getId().equals(misionId)).toList());
            r.sort(Comparator.comparing(Asignacion::getFecha).reversed());
            return r;
        }
    }

    static class ConfiguracionEnMemoria implements ConfiguracionRepositorio {
        ReservaOperativa reserva;
        public ReservaOperativa guardarReserva(ReservaOperativa r) { reserva = r; return r; }
        public Optional<ReservaOperativa> obtenerReserva() { return Optional.ofNullable(reserva); }
    }
}
