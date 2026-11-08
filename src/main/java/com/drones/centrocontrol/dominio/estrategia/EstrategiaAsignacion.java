package com.drones.centrocontrol.dominio.estrategia;

import com.drones.centrocontrol.dominio.modelo.Dron;
import com.drones.centrocontrol.dominio.modelo.Mision;

import java.util.List;

/**
 * Patrón Strategy: criterio intercambiable para elegir un dron entre candidatos
 * que ya cumplen la elegibilidad (HU-07). Agregar un criterio nuevo solo requiere
 * otra implementación; el motor de asignación no cambia.
 */
public interface EstrategiaAsignacion {

    /** Nombre con el que el operador selecciona la estrategia. */
    String getNombre();

    /** Elige un dron entre candidatos elegibles; la lista nunca llega vacía. */
    Dron seleccionar(List<Dron> candidatos, Mision mision);
}
