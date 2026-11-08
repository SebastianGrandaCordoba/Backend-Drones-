package com.drones.centrocontrol.aplicacion.puerto.entrada;

import com.drones.centrocontrol.dominio.modelo.ReservaOperativa;

/** Reserva vigente que necesita el motor para evaluar la autonomía (HU-13). */
public interface IConsultaReserva {

    /** Falla si la reserva aún no se ha configurado: no se asigna con un valor supuesto. */
    ReservaOperativa obtenerReservaVigente();
}
