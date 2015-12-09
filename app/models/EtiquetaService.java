package models;

import play.*;
import play.mvc.*;
import play.db.jpa.*;
import java.util.List;
import javax.persistence.EntityNotFoundException;

public class EtiquetaService {
    public static Etiqueta grabaEtiqueta(Etiqueta etiqueta) {
        return EtiquetaDAO.create(etiqueta);
    }

    public static List<Etiqueta> findAllEtiquetasUsuario(Integer usuarioId) {
        Usuario usuario = UsuarioDAO.find(usuarioId);
        return usuario.etiquetas;
    }

    public static Etiqueta findEtiqueta(Integer id) {
        return EtiquetaDAO.find(id);
    }

    public static boolean deleteEtiqueta(Integer id) {
        try {
            EtiquetaDAO.delete(id);
        } catch(EntityNotFoundException e) {
            return false;
        }
        return true;
    }

    public static Etiqueta modificaEtiqueta(Etiqueta etiqueta) {
        return EtiquetaDAO.update(etiqueta);
    }
}
