package com.drones.centrocontrol.aplicacion.puerto.entrada;

import com.drones.centrocontrol.dominio.modelo.Dron;
import com.drones.centrocontrol.dominio.modelo.EstadoDron;
import com.drones.centrocontrol.dominio.modelo.TipoMision;
import com.drones.centrocontrol.dominio.modelo.Ubicacion;

/** Casos de uso para administrar la flota (HU-01, HU-03). */
public interface IGestionFlota {

    Dron registrar(String id, TipoMision tipo, double bateria, double autonomiaKm, double capacidad,
                   EstadoDron estadoInicial, Ubicacion ubicacion);

    Dron actualizarDatosOperativos(String id, double bateria, double autonomiaKm, Ubicacion ubicacion);

    Dron cambiarEstado(String id, EstadoDron nuevoEstado);
}
