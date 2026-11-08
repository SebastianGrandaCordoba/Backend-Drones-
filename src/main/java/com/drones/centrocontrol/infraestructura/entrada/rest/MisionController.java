package com.drones.centrocontrol.infraestructura.entrada.rest;

import com.drones.centrocontrol.aplicacion.puerto.entrada.IConsultaMisiones;
import com.drones.centrocontrol.aplicacion.puerto.entrada.IGestionMisiones;
import com.drones.centrocontrol.dominio.modelo.EstadoMision;
import com.drones.centrocontrol.dominio.modelo.Mision;
import com.drones.centrocontrol.dominio.modelo.Prioridad;
import com.drones.centrocontrol.dominio.modelo.TipoMision;
import com.drones.centrocontrol.infraestructura.entrada.rest.DronController.UbicacionDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/** Adaptador de entrada REST para misiones (HU-04, HU-05, HU-11). */
@RestController
@RequestMapping("/api/misiones")
public class MisionController {

    private final IGestionMisiones gestionMisiones;
    private final IConsultaMisiones consultaMisiones;

    public MisionController(IGestionMisiones gestionMisiones, IConsultaMisiones consultaMisiones) {
        this.gestionMisiones = gestionMisiones;
        this.consultaMisiones = consultaMisiones;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MisionRespuesta registrar(@Valid @RequestBody RegistroMision s) {
        return MisionRespuesta.desde(gestionMisiones.registrar(s.tipo(), s.prioridad(), s.distanciaKm(),
                s.limiteTiempo(), s.destino().aDominio(), s.capacidadRequerida()));
    }

    @GetMapping("/pendientes")
    public List<MisionRespuesta> pendientes() {
        return consultaMisiones.consultarPendientes().stream().map(MisionRespuesta::desde).toList();
    }

    @GetMapping("/{id}")
    public MisionRespuesta buscar(@PathVariable String id) {
        return MisionRespuesta.desde(consultaMisiones.buscarPorId(id));
    }

    @PostMapping("/{id}/iniciar")
    public MisionRespuesta iniciar(@PathVariable String id) {
        return MisionRespuesta.desde(gestionMisiones.iniciar(id));
    }

    @PostMapping("/{id}/completar")
    public MisionRespuesta completar(@PathVariable String id) {
        return MisionRespuesta.desde(gestionMisiones.completar(id));
    }

    @PostMapping("/{id}/cancelar")
    public MisionRespuesta cancelar(@PathVariable String id) {
        return MisionRespuesta.desde(gestionMisiones.cancelar(id));
    }

    public record RegistroMision(@NotNull TipoMision tipo, @NotNull Prioridad prioridad, @NotNull Double distanciaKm,
                                 @NotNull LocalDateTime limiteTiempo, @NotNull @Valid UbicacionDto destino,
                                 @NotNull Double capacidadRequerida) {
    }

    public record MisionRespuesta(String id, TipoMision tipo, Prioridad prioridad, double distanciaKm,
                                  LocalDateTime limiteTiempo, UbicacionDto destino, double capacidadRequerida,
                                  EstadoMision estado) {
        static MisionRespuesta desde(Mision m) {
            return new MisionRespuesta(m.getId(), m.getTipo(), m.getPrioridad(), m.getDistanciaKm(),
                    m.getLimiteTiempo(), UbicacionDto.desde(m.getDestino()), m.getCapacidadRequerida(), m.getEstado());
        }
    }
}
