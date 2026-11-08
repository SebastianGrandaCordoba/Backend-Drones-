package com.drones.centrocontrol.aplicacion.puerto.entrada;

import com.drones.centrocontrol.dominio.modelo.Dron;
import com.drones.centrocontrol.dominio.modelo.EstadoDron;
import com.drones.centrocontrol.dominio.modelo.TipoMision;

import java.util.List;

/** Consultas sobre la flota (HU-02). También la usan el motor y la gestión de misiones. */
public interface IConsultaFlota {

    /** Drones que cumplen los filtros; un filtro nulo no se aplica. */
    List<Dron> consultar(TipoMision tipo, EstadoDron estado);

    Dron buscarPorId(String id);
}
