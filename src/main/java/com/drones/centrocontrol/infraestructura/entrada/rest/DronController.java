package com.drones.centrocontrol.infraestructura.entrada.rest;

import com.drones.centrocontrol.aplicacion.puerto.entrada.IConsultaFlota;
import com.drones.centrocontrol.aplicacion.puerto.entrada.IGestionFlota;
import com.drones.centrocontrol.dominio.modelo.Dron;
import com.drones.centrocontrol.dominio.modelo.EstadoDron;
import com.drones.centrocontrol.dominio.modelo.TipoMision;
import com.drones.centrocontrol.dominio.modelo.Ubicacion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Adaptador de entrada REST para la flota (HU-01, HU-02, HU-03). */
@RestController
@RequestMapping("/api/drones")
public class DronController {

    private final IGestionFlota gestionFlota;
    private final IConsultaFlota consultaFlota;

    public DronController(IGestionFlota gestionFlota, IConsultaFlota consultaFlota) {
        this.gestionFlota = gestionFlota;
        this.consultaFlota = consultaFlota;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DronRespuesta registrar(@Valid @RequestBody RegistroDron solicitud) {
        Dron dron = gestionFlota.registrar(solicitud.id(), solicitud.tipo(), solicitud.bateria(), solicitud.autonomiaKm(),
                solicitud.capacidad(), solicitud.estado(), solicitud.ubicacion().aDominio());
        return DronRespuesta.desde(dron);
    }

    @GetMapping
    public List<DronRespuesta> consultar(@RequestParam(required = false) TipoMision tipo,
                                         @RequestParam(required = false) EstadoDron estado) {
        return consultaFlota.consultar(tipo, estado).stream().map(DronRespuesta::desde).toList();
    }

    @GetMapping("/{id}")
    public DronRespuesta buscar(@PathVariable String id) {
        return DronRespuesta.desde(consultaFlota.buscarPorId(id));
    }

    @PutMapping("/{id}/datos-operativos")
    public DronRespuesta actualizarDatos(@PathVariable String id, @Valid @RequestBody DatosOperativos solicitud) {
        return DronRespuesta.desde(gestionFlota.actualizarDatosOperativos(id, solicitud.bateria(),
                solicitud.autonomiaKm(), solicitud.ubicacion().aDominio()));
    }

    @PutMapping("/{id}/estado")
    public DronRespuesta cambiarEstado(@PathVariable String id, @Valid @RequestBody CambioEstado solicitud) {
        return DronRespuesta.desde(gestionFlota.cambiarEstado(id, solicitud.estado()));
    }

    public record UbicacionDto(@NotNull Double latitud, @NotNull Double longitud) {
        Ubicacion aDominio() {
            return new Ubicacion(latitud, longitud);
        }

        static UbicacionDto desde(Ubicacion u) {
            return new UbicacionDto(u.getLatitud(), u.getLongitud());
        }
    }

    public record RegistroDron(@NotBlank String id, @NotNull TipoMision tipo, @NotNull Double bateria,
                               @NotNull Double autonomiaKm, @NotNull Double capacidad, @NotNull EstadoDron estado,
                               @NotNull @Valid UbicacionDto ubicacion) {
    }

    public record DatosOperativos(@NotNull Double bateria, @NotNull Double autonomiaKm,
                                  @NotNull @Valid UbicacionDto ubicacion) {
    }

    public record CambioEstado(@NotNull EstadoDron estado) {
    }

    public record DronRespuesta(String id, TipoMision tipo, EstadoDron estado, double bateria, double capacidad,
                                String unidadCapacidad, double autonomiaKm, UbicacionDto ubicacion) {
        static DronRespuesta desde(Dron d) {
            return new DronRespuesta(d.getId(), d.getTipo(), d.getEstado(), d.getBateria(), d.getCapacidad(),
                    d.getUnidadCapacidad(), d.getAutonomiaKm(), UbicacionDto.desde(d.getUbicacion()));
        }
    }
}
