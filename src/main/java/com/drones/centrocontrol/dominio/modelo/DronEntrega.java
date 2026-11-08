package com.drones.centrocontrol.dominio.modelo;

/** Dron de entrega: su capacidad mide carga útil en kg. */
public class DronEntrega extends Dron {

    public DronEntrega(String id, double bateria, double autonomiaKm, double capacidad,
                  EstadoDron estado, Ubicacion ubicacion) {
        super(id, bateria, autonomiaKm, capacidad, estado, ubicacion);
    }

    @Override
    public TipoMision getTipo() {
        return TipoMision.ENTREGA;
    }

    @Override
    public String getUnidadCapacidad() {
        return "kg";
    }
}
