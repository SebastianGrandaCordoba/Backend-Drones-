package com.drones.centrocontrol.dominio.modelo;

/** Dron de cartografía: su capacidad mide área de cobertura por vuelo en ha. */
public class DronCartografia extends Dron {

    public DronCartografia(String id, double bateria, double autonomiaKm, double capacidad,
                  EstadoDron estado, Ubicacion ubicacion) {
        super(id, bateria, autonomiaKm, capacidad, estado, ubicacion);
    }

    @Override
    public TipoMision getTipo() {
        return TipoMision.CARTOGRAFIA;
    }

    @Override
    public String getUnidadCapacidad() {
        return "ha";
    }
}
