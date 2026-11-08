package com.drones.centrocontrol.dominio.estrategia;

import com.drones.centrocontrol.dominio.modelo.Dron;
import com.drones.centrocontrol.dominio.modelo.Mision;

import java.util.Comparator;
import java.util.List;

/** El candidato con mayor batería restante; empate por menor distancia al destino. */
public class MayorBateria implements EstrategiaAsignacion {

    public static final String NOMBRE = "MAYOR_BATERIA";

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public Dron seleccionar(List<Dron> candidatos, Mision mision) {
        Comparator<Dron> criterio = Comparator
                .comparingDouble(Dron::getBateria).reversed()
                .thenComparingDouble(d -> d.getUbicacion().distanciaKmA(mision.getDestino()));
        return candidatos.stream().min(criterio).orElseThrow();
    }
}
