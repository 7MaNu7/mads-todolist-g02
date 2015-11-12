package models;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.EntityNotFoundException;

public class UsuarioService {
    public static Usuario grabaUsuario(Usuario usuario) {
        return UsuarioDAO.create(usuario);
    }

    public static Usuario modificaUsuario(Usuario usuario) {
        return UsuarioDAO.update(usuario);
    }

    public static List<Usuario> findAllUsuarios() {
        return UsuarioDAO.findAll();
    }

    public static Usuario loginUsuario(String login,String password) {

        if(password==null) password="";
        return UsuarioDAO.findLogin(login,password);
    }

    public static Usuario findUsuario(Integer id) {
        return UsuarioDAO.find(id);
    }

    public static boolean deleteUsuario(Integer id) {
        try {
            UsuarioDAO.delete(id);
        } catch(EntityNotFoundException e) {
            return false;
        }
        return true;
    }
}
