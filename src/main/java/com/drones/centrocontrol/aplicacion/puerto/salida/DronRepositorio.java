package com.drones.centrocontrol.aplicacion.puerto.salida;

import com.drones.centrocontrol.dominio.modelo.Dron;
import com.drones.centrocontrol.dominio.modelo.EstadoDron;
import com.drones.centrocontrol.dominio.modelo.TipoMision;

import java.util.List;
import java.util.Optional;

/** Puerto de salida para persistir drones. */
public interface DronRepositorio {

    Dron guardar(Dron dron);

    Optional<Dron> buscarPorId(String id);

    boolean existe(String id);

    List<Dron> buscar(TipoMision tipo, EstadoDron estado);

    /**
     * Cambia el dron a ASIGNADO solo si sigue DISPONIBLE, en una operación atómica.
     * Evita que dos solicitudes simultáneas asignen el mismo dron (HU-10).
     */
    boolean marcarAsignadoSiDisponible(String id);
}
