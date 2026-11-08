package com.drones.centrocontrol.aplicacion.puerto.salida;

import com.drones.centrocontrol.dominio.modelo.Asignacion;

import java.util.List;
import java.util.Optional;

/** Puerto de salida para persistir asignaciones y su instantánea de decisión. */
public interface AsignacionRepositorio {

    Asignacion guardar(Asignacion asignacion);

    Optional<Asignacion> buscarActivaPorMision(String misionId);

    /** Asignaciones filtradas por dron y/o misión, de la más reciente a la más antigua. */
    List<Asignacion> buscar(String dronId, String misionId);
}
