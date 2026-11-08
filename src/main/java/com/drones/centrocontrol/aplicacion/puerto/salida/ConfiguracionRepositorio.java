package com.drones.centrocontrol.aplicacion.puerto.salida;

import com.drones.centrocontrol.dominio.modelo.ReservaOperativa;

import java.util.Optional;

/** Puerto de salida para la configuración operativa. */
public interface ConfiguracionRepositorio {

    ReservaOperativa guardarReserva(ReservaOperativa reserva);

    Optional<ReservaOperativa> obtenerReserva();
}
