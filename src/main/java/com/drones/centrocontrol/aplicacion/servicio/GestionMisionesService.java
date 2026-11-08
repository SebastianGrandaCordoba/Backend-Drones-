package com.drones.centrocontrol.aplicacion.servicio;

import com.drones.centrocontrol.aplicacion.puerto.entrada.IConsultaFlota;
import com.drones.centrocontrol.aplicacion.puerto.entrada.IConsultaMisiones;
import com.drones.centrocontrol.aplicacion.puerto.entrada.IGestionMisiones;
import com.drones.centrocontrol.aplicacion.puerto.salida.AsignacionRepositorio;
import com.drones.centrocontrol.aplicacion.puerto.salida.DronRepositorio;
import com.drones.centrocontrol.aplicacion.puerto.salida.MisionRepositorio;
import com.drones.centrocontrol.dominio.modelo.Asignacion;
import com.drones.centrocontrol.dominio.modelo.Dron;
import com.drones.centrocontrol.dominio.modelo.EstadoMision;
import com.drones.centrocontrol.dominio.modelo.Mision;
import com.drones.centrocontrol.dominio.modelo.Prioridad;
import com.drones.centrocontrol.dominio.modelo.TipoMision;
import com.drones.centrocontrol.dominio.modelo.Ubicacion;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Componente «Gestión de Misiones»: registra misiones, ordena la bandeja de pendientes
 * y gobierna su ciclo de vida. Consulta el dron asignado mediante IConsultaFlota.
 */
public class GestionMisionesService implements IGestionMisiones, IConsultaMisiones {

    private static final Comparator<Mision> ORDEN_DE_ATENCION = Comparator
            .comparing(Mision::getPrioridad)
            .thenComparing(Mision::getLimiteTiempo);

    private final MisionRepositorio misionRepositorio;
    private final AsignacionRepositorio asignacionRepositorio;
    private final DronRepositorio dronRepositorio;
    private final IConsultaFlota consultaFlota;
    private final Clock reloj;

    public GestionMisionesService(MisionRepositorio misionRepositorio, AsignacionRepositorio asignacionRepositorio,
                                  DronRepositorio dronRepositorio, IConsultaFlota consultaFlota, Clock reloj) {
        this.misionRepositorio = misionRepositorio;
        this.asignacionRepositorio = asignacionRepositorio;
        this.dronRepositorio = dronRepositorio;
        this.consultaFlota = consultaFlota;
        this.reloj = reloj;
    }

    @Override
    public Mision registrar(TipoMision tipo, Prioridad prioridad, double distanciaKm, LocalDateTime limiteTiempo,
                            Ubicacion destino, double capacidadRequerida) {
        if (limiteTiempo != null && !limiteTiempo.isAfter(LocalDateTime.now(reloj))) {
            throw new IllegalArgumentException("El límite de tiempo ya pasó");
        }
        Mision mision = new Mision(UUID.randomUUID().toString(), tipo, prioridad, distanciaKm, limiteTiempo, destino,
                capacidadRequerida, EstadoMision.PENDIENTE);
        return misionRepositorio.guardar(mision);
    }

    @Override
    public Mision iniciar(String id) {
        Mision mision = buscarPorId(id);
        mision.iniciar();
        Dron dron = dronAsignado(mision);
        dron.iniciarMision();
        dronRepositorio.guardar(dron);
        return misionRepositorio.guardar(mision);
    }

    @Override
    public Mision completar(String id) {
        Mision mision = buscarPorId(id);
        mision.completar();
        cerrarAsignacionYLiberarDron(mision);
        return misionRepositorio.guardar(mision);
    }

    @Override
    public Mision cancelar(String id) {
        Mision mision = buscarPorId(id);
        boolean teniaDron = mision.getEstado() == EstadoMision.ASIGNADA;
        mision.cancelar();
        if (teniaDron) {
            cerrarAsignacionYLiberarDron(mision);
        }
        return misionRepositorio.guardar(mision);
    }

    @Override
    public List<Mision> consultarPendientes() {
        return misionRepositorio.buscarPorEstado(EstadoMision.PENDIENTE).stream()
                .sorted(ORDEN_DE_ATENCION)
                .toList();
    }

    @Override
    public Mision buscarPorId(String id) {
        return misionRepositorio.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la misión " + id));
    }

    private void cerrarAsignacionYLiberarDron(Mision mision) {
        Asignacion asignacion = asignacionActiva(mision);
        Dron dron = consultaFlota.buscarPorId(asignacion.getDron().getId());
        dron.liberar();
        asignacion.cerrar();
        dronRepositorio.guardar(dron);
        asignacionRepositorio.guardar(asignacion);
    }

    private Dron dronAsignado(Mision mision) {
        return consultaFlota.buscarPorId(asignacionActiva(mision).getDron().getId());
    }

    private Asignacion asignacionActiva(Mision mision) {
        return asignacionRepositorio.buscarActivaPorMision(mision.getId())
                .orElseThrow(() -> new IllegalStateException("La misión " + mision.getId()
                        + " no tiene un dron asignado"));
    }
}
