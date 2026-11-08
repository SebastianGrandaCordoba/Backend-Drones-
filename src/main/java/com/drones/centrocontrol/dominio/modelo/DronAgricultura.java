package com.drones.centrocontrol.dominio.modelo;

/** Dron de agricultura: su capacidad mide tanque de aspersión en L. */
public class DronAgricultura extends Dron {

    public DronAgricultura(String id, double bateria, double autonomiaKm, double capacidad,
                  EstadoDron estado, Ubicacion ubicacion) {
        super(id, bateria, autonomiaKm, capacidad, estado, ubicacion);
    }

    @Override
    public TipoMision getTipo() {
        return TipoMision.AGRICULTURA;
    }

    @Override
    public String getUnidadCapacidad() {
        return "L";
    }
}
