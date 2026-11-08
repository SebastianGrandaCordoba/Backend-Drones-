package com.drones.centrocontrol.aplicacion.puerto.entrada;

import com.drones.centrocontrol.dominio.modelo.Mision;
import com.drones.centrocontrol.dominio.modelo.Prioridad;
import com.drones.centrocontrol.dominio.modelo.TipoMision;
import com.drones.centrocontrol.dominio.modelo.Ubicacion;

import java.time.LocalDateTime;

/** Casos de uso del ciclo de vida de una misión (HU-04, HU-11). */
public interface IGestionMisiones {

    Mision registrar(TipoMision tipo, Prioridad prioridad, double distanciaKm, LocalDateTime limiteTiempo,
                     Ubicacion destino, double capacidadRequerida);

    Mision iniciar(String id);

    Mision completar(String id);

    Mision cancelar(String id);
}
