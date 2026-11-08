package com.drones.centrocontrol.aplicacion.puerto.entrada;

import com.drones.centrocontrol.dominio.modelo.Mision;

import java.util.List;

/** Consultas sobre misiones (HU-05). También la usa el motor de asignación. */
public interface IConsultaMisiones {

    /** Misiones PENDIENTES por prioridad descendente y, a igual prioridad, límite de tiempo más próximo. */
    List<Mision> consultarPendientes();

    Mision buscarPorId(String id);
}
