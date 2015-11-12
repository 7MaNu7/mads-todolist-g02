package models;

import play.db.jpa.*;
import play.Logger;
import play.*;
import play.mvc.*;
import javax.persistence.*;
import java.util.List;

public class UsuarioDAO {
    @play.db.jpa.Transactional
    public static Usuario create (Usuario usuario) {

        usuario.nulificaAtributos();
        JPA.em().persist(usuario);
        //hacemos un flush y refresh para asegurarnos
        //de que se realiza la creacion de la db y se devuelve el id
        //inicializado
        JPA.em().flush();
        JPA.em().refresh(usuario);
        Logger.debug(usuario.toString());
        return usuario;
    }

    public static Usuario update(Usuario usuario) {
        return JPA.em().merge(usuario);
    }

    public static Usuario find(Integer idUsuario) {
        return JPA.em().find(Usuario.class, idUsuario);
    }

    public static List<Usuario> findAll() {
        return (List<Usuario>) JPA.em().createQuery("select u from Usuario u ORDER BY id").getResultList();
    }

    public static Usuario findLogin(String login,String password) {
        try {
            Query query = JPA.em().createQuery("SELECT c FROM Usuario c WHERE c.login = '" + login + "' AND c.password = '" + password + "'");
            Usuario c = (Usuario)query.getSingleResult();
            return c;
        } catch(NoResultException e) {
            return null;
        }
    }

    public static void delete(Integer idUsuario) {
        Usuario usuario = JPA.em().getReference(Usuario.class, idUsuario);
        JPA.em().remove(usuario);
        Logger.debug("Se ha borrado el usuario " + idUsuario);
    }
}
