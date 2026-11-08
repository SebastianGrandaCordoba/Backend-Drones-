package com.drones.centrocontrol.aplicacion.servicio;

import com.drones.centrocontrol.aplicacion.puerto.entrada.IAsignacion;
import com.drones.centrocontrol.aplicacion.puerto.entrada.IConsultaFlota;
import com.drones.centrocontrol.aplicacion.puerto.entrada.IConsultaMisiones;
import com.drones.centrocontrol.aplicacion.puerto.entrada.IConsultaReserva;
import com.drones.centrocontrol.aplicacion.puerto.salida.AsignacionRepositorio;
import com.drones.centrocontrol.aplicacion.puerto.salida.DronRepositorio;
import com.drones.centrocontrol.aplicacion.puerto.salida.MisionRepositorio;
import com.drones.centrocontrol.dominio.modelo.Asignacion;
import com.drones.centrocontrol.dominio.modelo.Dron;
import com.drones.centrocontrol.dominio.modelo.Mision;
import com.drones.centrocontrol.dominio.modelo.ReservaOperativa;
import com.drones.centrocontrol.dominio.modelo.ResultadoAsignacion;
import com.drones.centrocontrol.dominio.servicio.MotorAsignacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Componente «Motor de Asignación» en la capa de aplicación: reúne la misión, la flota
 * y la reserva a través de los puertos de consulta, delega la decisión en el
 * {@link MotorAsignacion} del dominio y confirma el resultado de forma atómica.
 */
public class AsignacionService implements IAsignacion {

    private final IConsultaMisiones consultaMisiones;
    private final IConsultaFlota consultaFlota;
    private final IConsultaReserva consultaReserva;
    private final MotorAsignacion motor;
    private final DronRepositorio dronRepositorio;
    private final MisionRepositorio misionRepositorio;
    private final AsignacionRepositorio asignacionRepositorio;

    public AsignacionService(IConsultaMisiones consultaMisiones, IConsultaFlota consultaFlota,
                             IConsultaReserva consultaReserva, MotorAsignacion motor,
                             DronRepositorio dronRepositorio, MisionRepositorio misionRepositorio,
                             AsignacionRepositorio asignacionRepositorio) {
        this.consultaMisiones = consultaMisiones;
        this.consultaFlota = consultaFlota;
        this.consultaReserva = consultaReserva;
        this.motor = motor;
        this.dronRepositorio = dronRepositorio;
        this.misionRepositorio = misionRepositorio;
        this.asignacionRepositorio = asignacionRepositorio;
    }

    @Override
    public ResultadoAsignacion asignar(String misionId, String estrategia) {
        ReservaOperativa reserva = consultaReserva.obtenerReservaVigente();
        List<Dron> flota = new ArrayList<>(consultaFlota.consultar(null, null));
        while (true) {
            Mision mision = consultaMisiones.buscarPorId(misionId);
            ResultadoAsignacion resultado = motor.asignar(mision, flota, reserva, estrategia);
            if (!resultado.fueExitoso()) {
                return resultado;
            }
            Asignacion asignacion = resultado.getAsignacion();
            Dron elegido = asignacion.getDron();
            // HU-10: si otra solicitud tomó el dron entre la consulta y la confirmación, se descarta y se reevalúa.
            if (!dronRepositorio.marcarAsignadoSiDisponible(elegido.getId())) {
                flota.removeIf(d -> d.getId().equals(elegido.getId()));
                continue;
            }
            if (!misionRepositorio.marcarAsignadaSiPendiente(misionId)) {
                elegido.liberar();
                dronRepositorio.guardar(elegido);
                throw new IllegalStateException("La misión " + misionId + " fue asignada por otra solicitud");
            }
            asignacionRepositorio.guardar(asignacion);
            return resultado;
        }
    }

    @Override
    public Optional<Asignacion> consultarAsignacionDeMision(String misionId) {
        consultaMisiones.buscarPorId(misionId);
        return asignacionRepositorio.buscar(null, misionId).stream().findFirst();
    }

    @Override
    public List<Asignacion> consultarHistorial(String dronId, String misionId) {
        return asignacionRepositorio.buscar(dronId, misionId);
    }
}
