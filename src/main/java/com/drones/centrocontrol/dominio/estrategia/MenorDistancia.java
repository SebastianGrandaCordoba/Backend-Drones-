package com.drones.centrocontrol.dominio.estrategia;

import com.drones.centrocontrol.dominio.modelo.Dron;
import com.drones.centrocontrol.dominio.modelo.Mision;

import java.util.Comparator;
import java.util.List;

/** Estrategia predeterminada: el candidato más cercano al destino; empate por mayor batería. */
public class MenorDistancia implements EstrategiaAsignacion {

    public static final String NOMBRE = "MENOR_DISTANCIA";

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public Dron seleccionar(List<Dron> candidatos, Mision mision) {
        Comparator<Dron> criterio = Comparator
                .comparingDouble((Dron d) -> d.getUbicacion().distanciaKmA(mision.getDestino()))
                .thenComparing(Comparator.comparingDouble(Dron::getBateria).reversed());
        return candidatos.stream().min(criterio).orElseThrow();
    }
}
