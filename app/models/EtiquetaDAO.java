package models;

import play.*;
import play.mvc.*;
import play.db.jpa.*;
import java.util.List;
import java.util.Date;

import javax.persistence.*;

public class EtiquetaDAO {
    public static Etiqueta find(Integer idEtiqueta) {
        return JPA.em().find(Etiqueta.class, idEtiqueta);
    }

    @play.db.jpa.Transactional
    public static Etiqueta create (Etiqueta etiqueta) {

        JPA.em().persist(etiqueta);
        //hacemos un flush y refresh para asegurarnos
        //de que se realiza la creacion de la db y se devuelve el id
        //inicializado
        JPA.em().flush();
        JPA.em().refresh(etiqueta);
        Logger.debug(etiqueta.toString());
        return etiqueta;
    }

    public static Etiqueta update(Etiqueta e) {
        return JPA.em().merge(e);
    }

    public static void delete(Integer id) {
        Etiqueta e = JPA.em().getReference(Etiqueta.class, id);
        JPA.em().remove(e);
        Logger.debug("Se ha borrado la etiqueta " + id);

        //hacemos un flush y refresh para asegurarnos
        //de que se actualiza ese borrado en el usuario creador
        JPA.em().flush();
        JPA.em().refresh(e.usuario);
    }


}
