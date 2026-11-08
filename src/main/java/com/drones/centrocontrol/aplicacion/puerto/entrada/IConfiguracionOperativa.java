package com.drones.centrocontrol.aplicacion.puerto.entrada;

import com.drones.centrocontrol.dominio.modelo.ReservaOperativa;

import java.util.Optional;

/** Configuración de la reserva operativa por parte del operador (HU-13). */
public interface IConfiguracionOperativa {

    ReservaOperativa configurarReserva(double valor, String unidad);

    Optional<ReservaOperativa> consultarReserva();
}
