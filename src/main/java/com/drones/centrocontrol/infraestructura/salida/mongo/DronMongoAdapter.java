package com.drones.centrocontrol.infraestructura.salida.mongo;

import com.drones.centrocontrol.aplicacion.puerto.salida.DronRepositorio;
import com.drones.centrocontrol.dominio.modelo.Dron;
import com.drones.centrocontrol.dominio.modelo.DronAgricultura;
import com.drones.centrocontrol.dominio.modelo.DronCartografia;
import com.drones.centrocontrol.dominio.modelo.DronEntrega;
import com.drones.centrocontrol.dominio.modelo.DronFotografia;
import com.drones.centrocontrol.dominio.modelo.DronVigilancia;
import com.drones.centrocontrol.dominio.modelo.EstadoDron;
import com.drones.centrocontrol.dominio.modelo.TipoMision;
import com.drones.centrocontrol.dominio.modelo.Ubicacion;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: implementa {@link DronRepositorio} sobre la colección «drones» de MongoDB. */
public class DronMongoAdapter implements DronRepositorio {

    private final MongoTemplate mongo;

    public DronMongoAdapter(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public Dron guardar(Dron dron) {
        mongo.save(DronDocumento.desde(dron));
        return dron;
    }

    @Override
    public Optional<Dron> buscarPorId(String id) {
        return Optional.ofNullable(mongo.findById(id, DronDocumento.class)).map(DronDocumento::aDominio);
    }

    @Override
    public boolean existe(String id) {
        return mongo.exists(Query.query(Criteria.where("_id").is(id)), DronDocumento.class);
    }

    @Override
    public List<Dron> buscar(TipoMision tipo, EstadoDron estado) {
        Criteria criterio = new Criteria();
        if (tipo != null) {
            criterio = criterio.and("tipo").is(tipo.name());
        }
        if (estado != null) {
            criterio = criterio.and("estado").is(estado.name());
        }
        Query consulta = Query.query(criterio).with(Sort.by("_id"));
        return mongo.find(consulta, DronDocumento.class).stream().map(DronDocumento::aDominio).toList();
    }

    @Override
    public boolean marcarAsignadoSiDisponible(String id) {
        Query soloSiDisponible = Query.query(Criteria.where("_id").is(id).and("estado").is(EstadoDron.DISPONIBLE.name()));
        Update asignar = Update.update("estado", EstadoDron.ASIGNADO.name());
        return mongo.updateFirst(soloSiDisponible, asignar, DronDocumento.class).getModifiedCount() == 1;
    }

    /** Representación persistente de un dron; el dominio no conoce MongoDB. */
    @Document(collection = "drones")
    static class DronDocumento {
        @Id
        String id;
        String tipo;
        double bateria;
        double autonomiaKm;
        double capacidad;
        String unidadCapacidad;
        String estado;
        double latitud;
        double longitud;

        static DronDocumento desde(Dron dron) {
            DronDocumento d = new DronDocumento();
            d.id = dron.getId();
            d.tipo = dron.getTipo().name();
            d.bateria = dron.getBateria();
            d.autonomiaKm = dron.getAutonomiaKm();
            d.capacidad = dron.getCapacidad();
            d.unidadCapacidad = dron.getUnidadCapacidad();
            d.estado = dron.getEstado().name();
            d.latitud = dron.getUbicacion().getLatitud();
            d.longitud = dron.getUbicacion().getLongitud();
            return d;
        }

        Dron aDominio() {
            EstadoDron e = EstadoDron.valueOf(estado);
            Ubicacion u = new Ubicacion(latitud, longitud);
            return switch (TipoMision.valueOf(tipo)) {
                case ENTREGA -> new DronEntrega(id, bateria, autonomiaKm, capacidad, e, u);
                case FOTOGRAFIA -> new DronFotografia(id, bateria, autonomiaKm, capacidad, e, u);
                case VIGILANCIA -> new DronVigilancia(id, bateria, autonomiaKm, capacidad, e, u);
                case AGRICULTURA -> new DronAgricultura(id, bateria, autonomiaKm, capacidad, e, u);
                case CARTOGRAFIA -> new DronCartografia(id, bateria, autonomiaKm, capacidad, e, u);
            };
        }
    }
}
