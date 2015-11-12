package models;

import play.*;
import play.mvc.*;
import play.db.jpa.*;
import java.util.List;
import java.util.Date;

import javax.persistence.*;

public class TareaDAO {
    public static Tarea find(Integer idTarea) {
        return JPA.em().find(Tarea.class, idTarea);
    }

    @play.db.jpa.Transactional
    public static Tarea create (Tarea tarea) {

        tarea.nulificaAtributos();
        JPA.em().persist(tarea);
        //hacemos un flush y refresh para asegurarnos
        //de que se realiza la creacion de la db y se devuelve el id
        //inicializado
        JPA.em().flush();
        JPA.em().refresh(tarea);
        Logger.debug(tarea.toString());
        return tarea;
    }

    public static Tarea update(Tarea tarea) {
        return JPA.em().merge(tarea);
    }

    public static void delete(Integer id) {
        Tarea tarea = JPA.em().getReference(Tarea.class, id);
        JPA.em().remove(tarea);
        Logger.debug("Se ha borrado la tarea " + id);

        //hacemos un flush y refresh para asegurarnos
        //de que se actualiza ese borrado en el usuario creador
        JPA.em().flush();
        JPA.em().refresh(tarea.usuario);
    }

}
