package com.drones.centrocontrol.infraestructura.salida.mongo;

import com.drones.centrocontrol.aplicacion.puerto.salida.ConfiguracionRepositorio;
import com.drones.centrocontrol.dominio.modelo.ReservaOperativa;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Optional;

/** Adaptador de salida: guarda la configuración operativa global en la colección «configuracion». */
public class ConfiguracionMongoAdapter implements ConfiguracionRepositorio {

    private static final String ID_RESERVA = "reserva-operativa";

    private final MongoTemplate mongo;

    public ConfiguracionMongoAdapter(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public ReservaOperativa guardarReserva(ReservaOperativa reserva) {
        ConfiguracionDocumento d = new ConfiguracionDocumento();
        d.id = ID_RESERVA;
        d.reservaKm = reserva.getKm();
        mongo.save(d);
        return reserva;
    }

    @Override
    public Optional<ReservaOperativa> obtenerReserva() {
        return Optional.ofNullable(mongo.findById(ID_RESERVA, ConfiguracionDocumento.class))
                .map(d -> new ReservaOperativa(d.reservaKm));
    }

    @Document(collection = "configuracion")
    static class ConfiguracionDocumento {
        @Id
        String id;
        double reservaKm;
    }
}
