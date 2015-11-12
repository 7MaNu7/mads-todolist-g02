package models;

import play.*;
import play.mvc.*;
import play.db.jpa.*;
import java.util.List;
import javax.persistence.EntityNotFoundException;

public class TareaService {
    public static List<Tarea> findAllTareasUsuario(Integer usuarioId) {
        Usuario usuario = UsuarioDAO.find(usuarioId);
        return usuario.tareas;
    }

    public static Tarea findTarea(Integer id) {
        return TareaDAO.find(id);
    }

    public static Tarea grabaTarea(Tarea tarea) {
        return TareaDAO.create(tarea);
    }

    public static Tarea modificaTarea(Tarea tarea) {
        return TareaDAO.update(tarea);
    }

    public static boolean deleteTarea(Integer id) {
        try {
            TareaDAO.delete(id);
        } catch(EntityNotFoundException e) {
            return false;
        }
        return true;
    }
}
