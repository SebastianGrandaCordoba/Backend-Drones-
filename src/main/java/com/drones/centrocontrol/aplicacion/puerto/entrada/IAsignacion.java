package com.drones.centrocontrol.aplicacion.puerto.entrada;

import com.drones.centrocontrol.dominio.modelo.Asignacion;
import com.drones.centrocontrol.dominio.modelo.ResultadoAsignacion;

import java.util.List;
import java.util.Optional;

/** Casos de uso de la asignación automática (HU-06 a HU-10, HU-12). */
public interface IAsignacion {

    /** Asigna la misión con la estrategia indicada; si es nula se usa la predeterminada. */
    ResultadoAsignacion asignar(String misionId, String estrategia);

    /** Asignación más reciente de la misión, si la tiene (HU-08). */
    Optional<Asignacion> consultarAsignacionDeMision(String misionId);

    /** Historial filtrado por dron y/o misión; un filtro nulo no se aplica (HU-12). */
    List<Asignacion> consultarHistorial(String dronId, String misionId);
}
