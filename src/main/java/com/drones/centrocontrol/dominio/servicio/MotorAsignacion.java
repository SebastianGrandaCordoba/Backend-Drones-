package com.drones.centrocontrol.dominio.servicio;

import com.drones.centrocontrol.dominio.estrategia.EstrategiaAsignacion;
import com.drones.centrocontrol.dominio.estrategia.MenorDistancia;
import com.drones.centrocontrol.dominio.modelo.Asignacion;
import com.drones.centrocontrol.dominio.modelo.Dron;
import com.drones.centrocontrol.dominio.modelo.EstadoDron;
import com.drones.centrocontrol.dominio.modelo.Mision;
import com.drones.centrocontrol.dominio.modelo.MotivoRechazo;
import com.drones.centrocontrol.dominio.modelo.ReservaOperativa;
import com.drones.centrocontrol.dominio.modelo.ResultadoAsignacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Servicio de dominio que decide la asignación (HU-06, HU-07, HU-09).
 * Primero filtra candidatos elegibles y solo después aplica la estrategia,
 * de modo que una estrategia nunca puede escoger un dron incompatible.
 */
public class MotorAsignacion {

    private final Map<String, EstrategiaAsignacion> estrategias = new LinkedHashMap<>();

    public MotorAsignacion(List<EstrategiaAsignacion> estrategias) {
        for (EstrategiaAsignacion estrategia : estrategias) {
            this.estrategias.put(estrategia.getNombre(), estrategia);
        }
        if (!this.estrategias.containsKey(MenorDistancia.NOMBRE)) {
            throw new IllegalArgumentException("Falta la estrategia predeterminada " + MenorDistancia.NOMBRE);
        }
    }

    /**
     * Asigna la misión al mejor candidato según la estrategia indicada.
     * Si {@code estrategia} es nula o vacía se usa MENOR_DISTANCIA.
     * Si no hay candidatos devuelve un resultado sin asignación con los motivos.
     */
    public ResultadoAsignacion asignar(Mision mision, List<Dron> flota, ReservaOperativa reserva, String estrategia) {
        if (!mision.estaPendiente()) {
            throw new IllegalStateException("La misión " + mision.getId() + " ya no está pendiente (estado "
                    + mision.getEstado() + ")");
        }
        String nombre = (estrategia == null || estrategia.isBlank()) ? MenorDistancia.NOMBRE : estrategia.trim().toUpperCase();
        EstrategiaAsignacion criterio = estrategias.get(nombre);
        if (criterio == null) {
            throw new IllegalArgumentException("La estrategia '" + estrategia + "' no existe. Disponibles: "
                    + estrategias.keySet());
        }

        List<Dron> elegibles = filtrarElegibles(mision, flota, reserva);
        if (elegibles.isEmpty()) {
            return new ResultadoAsignacion(null, motivosDeRechazo(mision, flota, reserva));
        }

        Dron elegido = criterio.seleccionar(elegibles, mision);
        Asignacion asignacion = new Asignacion(UUID.randomUUID().toString(), mision, elegido, criterio.getNombre(),
                LocalDateTime.now(), elegido.getUbicacion().distanciaKmA(mision.getDestino()), elegido.getBateria(), true);
        elegido.marcarAsignado();
        mision.marcarAsignada();
        return new ResultadoAsignacion(asignacion, List.of());
    }

    private List<Dron> filtrarElegibles(Mision mision, List<Dron> flota, ReservaOperativa reserva) {
        List<Dron> elegibles = new ArrayList<>();
        for (Dron dron : flota) {
            if (dron.puedeAtender(mision, reserva)) {
                elegibles.add(dron);
            }
        }
        return elegibles;
    }

    /**
     * Explica por qué no hubo candidato. Si ningún dron es del tipo de la misión, ese es el motivo;
     * si los hay, se reúnen las condiciones que incumplen los drones de ese tipo.
     */
    private List<MotivoRechazo> motivosDeRechazo(Mision mision, List<Dron> flota, ReservaOperativa reserva) {
        Set<MotivoRechazo> motivos = EnumSet.noneOf(MotivoRechazo.class);
        List<Dron> mismoTipo = flota.stream().filter(d -> d.getTipo() == mision.getTipo()).toList();
        if (flota.isEmpty()) {
            motivos.add(MotivoRechazo.NO_DISPONIBLE);
        } else if (mismoTipo.isEmpty()) {
            motivos.add(MotivoRechazo.TIPO_INCOMPATIBLE);
        }
        for (Dron dron : mismoTipo) {
            if (dron.getEstado() != EstadoDron.DISPONIBLE) {
                motivos.add(MotivoRechazo.NO_DISPONIBLE);
            }
            if (dron.getCapacidad() < mision.getCapacidadRequerida()) {
                motivos.add(MotivoRechazo.CAPACIDAD_INSUFICIENTE);
            }
            if (!reserva.cubre(dron.getAutonomiaKm(), mision.getDistanciaKm())) {
                motivos.add(MotivoRechazo.AUTONOMIA_INSUFICIENTE);
            }
        }
        return new ArrayList<>(motivos);
    }
}
