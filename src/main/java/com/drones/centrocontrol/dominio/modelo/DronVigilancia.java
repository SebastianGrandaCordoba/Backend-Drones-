package com.drones.centrocontrol.dominio.modelo;

/** Dron de vigilancia: su capacidad mide tiempo de grabación en h. */
public class DronVigilancia extends Dron {

    public DronVigilancia(String id, double bateria, double autonomiaKm, double capacidad,
                  EstadoDron estado, Ubicacion ubicacion) {
        super(id, bateria, autonomiaKm, capacidad, estado, ubicacion);
    }

    @Override
    public TipoMision getTipo() {
        return TipoMision.VIGILANCIA;
    }

    @Override
    public String getUnidadCapacidad() {
        return "h";
    }
}
