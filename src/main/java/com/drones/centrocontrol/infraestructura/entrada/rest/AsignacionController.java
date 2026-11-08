package com.drones.centrocontrol.infraestructura.entrada.rest;

import com.drones.centrocontrol.aplicacion.puerto.entrada.IAsignacion;
import com.drones.centrocontrol.dominio.estrategia.MayorBateria;
import com.drones.centrocontrol.dominio.estrategia.MenorDistancia;
import com.drones.centrocontrol.dominio.modelo.Asignacion;
import com.drones.centrocontrol.dominio.modelo.EstadoMision;
import com.drones.centrocontrol.dominio.modelo.MotivoRechazo;
import com.drones.centrocontrol.dominio.modelo.ResultadoAsignacion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/** Adaptador de entrada REST para la asignación automática (HU-06 a HU-10, HU-12). */
@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionController {

    private final IAsignacion asignacion;

    public AsignacionController(IAsignacion asignacion) {
        this.asignacion = asignacion;
    }

    /** 201 si se asignó un dron; 200 con los motivos si no hubo candidato (resultado válido, no error). */
    @PostMapping
    public ResponseEntity<ResultadoRespuesta> asignar(@Valid @RequestBody SolicitudAsignacion solicitud) {
        ResultadoAsignacion resultado = asignacion.asignar(solicitud.misionId(), solicitud.estrategia());
        ResultadoRespuesta cuerpo = ResultadoRespuesta.desde(resultado);
        return ResponseEntity.status(resultado.fueExitoso() ? HttpStatus.CREATED : HttpStatus.OK).body(cuerpo);
    }

    /** Detalle de la asignación de una misión (HU-08). */
    @GetMapping("/mision/{misionId}")
    public DetalleMision detalleDeMision(@PathVariable String misionId) {
        return asignacion.consultarAsignacionDeMision(misionId)
                .map(a -> new DetalleMision(misionId, a.getMision().getEstado(), true,
                        "Misión con dron asignado", AsignacionRespuesta.desde(a)))
                .orElseGet(() -> new DetalleMision(misionId, null, false, "La misión sigue sin asignar", null));
    }

    /** Historial filtrable por dron y/o misión (HU-12). */
    @GetMapping
    public List<AsignacionRespuesta> historial(@RequestParam(required = false) String dronId,
                                               @RequestParam(required = false) String misionId) {
        return asignacion.consultarHistorial(dronId, misionId).stream().map(AsignacionRespuesta::desde).toList();
    }

    /** Estrategias que el operador puede elegir (HU-07). */
    @GetMapping("/estrategias")
    public List<EstrategiaRespuesta> estrategias() {
        return List.of(
                new EstrategiaRespuesta(MenorDistancia.NOMBRE, true,
                        "Elige el dron elegible más cercano al destino; en empate, el de mayor batería"),
                new EstrategiaRespuesta(MayorBateria.NOMBRE, false,
                        "Elige el dron elegible con mayor batería; en empate, el más cercano al destino"));
    }

    public record SolicitudAsignacion(@NotBlank String misionId, String estrategia) {
    }

    public record EstrategiaRespuesta(String nombre, boolean predeterminada, String descripcion) {
    }

    public record MotivoRespuesta(MotivoRechazo codigo, String descripcion) {
        static MotivoRespuesta desde(MotivoRechazo m) {
            String texto = switch (m) {
                case NO_DISPONIBLE -> "No hay drones DISPONIBLES del tipo requerido";
                case TIPO_INCOMPATIBLE -> "Ningún dron de la flota es del tipo de la misión";
                case CAPACIDAD_INSUFICIENTE -> "La capacidad de los drones no cubre la capacidad requerida";
                case AUTONOMIA_INSUFICIENTE -> "La autonomía no alcanza para la distancia más la reserva operativa";
            };
            return new MotivoRespuesta(m, texto);
        }
    }

    public record AsignacionRespuesta(String id, String misionId, EstadoMision estadoMision, String dronId,
                                      String estrategia, LocalDateTime fecha, double distanciaAlDestinoKm,
                                      double bateriaAlAsignar, boolean activa, String justificacion) {
        static AsignacionRespuesta desde(Asignacion a) {
            String criterio = MayorBateria.NOMBRE.equals(a.getEstrategia())
                    ? "mayor batería restante (en empate, menor distancia al destino)"
                    : "menor distancia al destino (en empate, mayor batería restante)";
            String justificacion = String.format("Se eligió el dron %s por %s: estaba a %.2f km del destino con %.1f %% "
                    + "de batería al momento de asignar.", a.getDron().getId(), criterio, a.getDistanciaAlDestinoKm(),
                    a.getBateriaAlAsignar());
            return new AsignacionRespuesta(a.getId(), a.getMision().getId(), a.getMision().getEstado(), a.getDron().getId(),
                    a.getEstrategia(), a.getFecha(), a.getDistanciaAlDestinoKm(), a.getBateriaAlAsignar(), a.isActiva(),
                    justificacion);
        }
    }

    public record ResultadoRespuesta(boolean asignado, String mensaje, AsignacionRespuesta asignacion,
                                     List<MotivoRespuesta> motivos) {
        static ResultadoRespuesta desde(ResultadoAsignacion r) {
            if (r.fueExitoso()) {
                return new ResultadoRespuesta(true, "Misión asignada", AsignacionRespuesta.desde(r.getAsignacion()), List.of());
            }
            return new ResultadoRespuesta(false, "No hay candidatos elegibles; la misión sigue PENDIENTE", null,
                    r.getMotivos().stream().map(MotivoRespuesta::desde).toList());
        }
    }

    public record DetalleMision(String misionId, EstadoMision estadoMision, boolean asignada, String mensaje,
                                AsignacionRespuesta asignacion) {
    }
}
