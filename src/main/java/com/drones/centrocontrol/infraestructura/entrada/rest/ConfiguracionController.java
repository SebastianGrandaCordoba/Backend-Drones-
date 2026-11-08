package com.drones.centrocontrol.infraestructura.entrada.rest;

import com.drones.centrocontrol.aplicacion.puerto.entrada.IConfiguracionOperativa;
import com.drones.centrocontrol.dominio.modelo.ReservaOperativa;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST para la configuración operativa (HU-13). */
@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final IConfiguracionOperativa configuracion;

    public ConfiguracionController(IConfiguracionOperativa configuracion) {
        this.configuracion = configuracion;
    }

    @PutMapping("/reserva")
    public ReservaRespuesta configurarReserva(@Valid @RequestBody ReservaSolicitud solicitud) {
        ReservaOperativa reserva = configuracion.configurarReserva(solicitud.valor(), solicitud.unidad());
        return new ReservaRespuesta(true, reserva.getKm(), "km");
    }

    @GetMapping("/reserva")
    public ReservaRespuesta consultarReserva() {
        return configuracion.consultarReserva()
                .map(r -> new ReservaRespuesta(true, r.getKm(), "km"))
                .orElseGet(() -> new ReservaRespuesta(false, null, "km"));
    }

    public record ReservaSolicitud(@NotNull Double valor, @NotBlank String unidad) {
    }

    public record ReservaRespuesta(boolean configurada, Double valor, String unidad) {
    }
}
