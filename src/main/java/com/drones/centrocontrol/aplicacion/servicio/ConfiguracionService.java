package com.drones.centrocontrol.aplicacion.servicio;

import com.drones.centrocontrol.aplicacion.puerto.entrada.IConfiguracionOperativa;
import com.drones.centrocontrol.aplicacion.puerto.entrada.IConsultaReserva;
import com.drones.centrocontrol.aplicacion.puerto.salida.ConfiguracionRepositorio;
import com.drones.centrocontrol.dominio.modelo.ReservaOperativa;

import java.util.Optional;

/** Componente «Configuración Operativa»: mantiene la reserva operativa vigente (HU-13). */
public class ConfiguracionService implements IConfiguracionOperativa, IConsultaReserva {

    private static final String UNIDAD_ADMITIDA = "km";

    private final ConfiguracionRepositorio configuracionRepositorio;

    public ConfiguracionService(ConfiguracionRepositorio configuracionRepositorio) {
        this.configuracionRepositorio = configuracionRepositorio;
    }

    @Override
    public ReservaOperativa configurarReserva(double valor, String unidad) {
        if (unidad == null || !UNIDAD_ADMITIDA.equalsIgnoreCase(unidad.trim())) {
            throw new IllegalArgumentException("La reserva debe expresarse en km, la misma unidad de la autonomía");
        }
        return configuracionRepositorio.guardarReserva(new ReservaOperativa(valor));
    }

    @Override
    public Optional<ReservaOperativa> consultarReserva() {
        return configuracionRepositorio.obtenerReserva();
    }

    @Override
    public ReservaOperativa obtenerReservaVigente() {
        return configuracionRepositorio.obtenerReserva()
                .orElseThrow(() -> new IllegalStateException(
                        "La reserva operativa no está configurada; configúrela antes de asignar misiones"));
    }
}
