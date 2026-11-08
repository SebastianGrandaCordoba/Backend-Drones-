package com.drones.centrocontrol.aplicacion.puerto.salida;

import com.drones.centrocontrol.dominio.modelo.EstadoMision;
import com.drones.centrocontrol.dominio.modelo.Mision;

import java.util.List;
import java.util.Optional;

/** Puerto de salida para persistir misiones. */
public interface MisionRepositorio {

    Mision guardar(Mision mision);

    Optional<Mision> buscarPorId(String id);

    List<Mision> buscarPorEstado(EstadoMision estado);

    /** Cambia la misión a ASIGNADA solo si sigue PENDIENTE, en una operación atómica. */
    boolean marcarAsignadaSiPendiente(String id);
}
