import org.junit.*;
import play.test.*;
import play.Application;
import play.mvc.*;
import static play.test.Helpers.*;
import static org.junit.Assert.*;

import play.db.jpa.*;
import java.util.List;
import models.*;

public class WithFakeApplicationTest {

    @Test
    public void testfindAllUsuariosDevuelveListaVacia() {
        running (fakeApplication(inMemoryDatabase()), () -> {
            JPA.withTransaction(() -> {
                List<Usuario> listaUsuarios = UsuarioService.findAllUsuarios();
                assertTrue(listaUsuarios.isEmpty());
            });
        });
    }

    @Test
    public void testfindAllUsuariosDevuelveUnUsuario() {
        running (fakeApplication(inMemoryDatabase()), () -> {
            JPA.withTransaction(() -> {
                Usuario usuario = new Usuario();
                usuario.login = "pepe";
                usuario.password = "pepe";
                UsuarioService.grabaUsuario(usuario);
                List<Usuario> listaUsuarios = UsuarioService.findAllUsuarios();
                assertTrue(listaUsuarios.size() == 1);
            });
        });
    }

    @Test
    public void testFindUsuarioByIdDevuelveUnUsuario() {
        running (fakeApplication(inMemoryDatabase()), () -> {
            JPA.withTransaction(() -> {
                Usuario usuario = new Usuario();
                usuario.login = "paco";
                usuario.password = "paco";
                //cogemos el usuario que devuelve con la id
                usuario = UsuarioService.grabaUsuario(usuario);

                //comprobamos que el findUsuario1 es el que hemos creado
                assertTrue(usuario==UsuarioService.findUsuario(1));
            });
        });
    }

    @Test
    public void testLogeaModificaYElimina() {
        running (fakeApplication(inMemoryDatabase()), () -> {
            JPA.withTransaction(() -> {
                Usuario usuario = new Usuario();
                usuario.login = "paco";
                usuario.password = "paquito";
                //cogemos el usuario que devuelve con la id
                usuario = UsuarioService.grabaUsuario(usuario);
                //comprobamos q el logeo es correcto
                assertTrue(UsuarioService.loginUsuario("paco","paquito")!=null);
                //modificamos su nombre
                usuario.nombre="PACOO";

                //comprobamos q modificar devuelve un usuario con nombre PACOO
                usuario = UsuarioService.modificaUsuario(usuario);
                assertTrue(usuario.nombre.equals("PACOO"));
                assertTrue(usuario.id.equals(1));

                //borrar el user id 1 deberia funcionar correctamente
                assertTrue(UsuarioService.deleteUsuario(1));
                //y la lista deberia estar vacia
                List<Usuario> listaUsuarios = UsuarioService.findAllUsuarios();
                assertTrue(listaUsuarios.isEmpty());
            });
        });
    }

    @Test
    public void testCompruebaConsultasDAO() {
        running (fakeApplication(inMemoryDatabase()), () -> {
            JPA.withTransaction(() -> {
                //al principio vacia
                List<Usuario> listaUsuarios = UsuarioDAO.findAll();
                assertTrue(listaUsuarios.isEmpty());

                //creamos user
                Usuario usuario = new Usuario();
                usuario.login = "paco";
                usuario.password = "paquito";
                UsuarioDAO.create(usuario);
                //deberia haber 1 user en la lista
                assertEquals(1,UsuarioDAO.findAll().size());

                Usuario consulta_id = UsuarioDAO.find(1);
                Usuario consulta_login = UsuarioDAO.findLogin("paco","paquito");
                //tendrian que apuntar a la misma referencia
                assertTrue(consulta_id==consulta_login);
            });
        });
    }

    @Test
    public void testModificaYEliminaDAO() {
        running (fakeApplication(inMemoryDatabase()), () -> {
            JPA.withTransaction(() -> {
                //creamos user
                Usuario usuario = new Usuario();
                usuario.login = "paco";
                usuario.password = "paquito";
                usuario.nombre = "paquerito";

                //cogemos el user con su id
                usuario = UsuarioDAO.create(usuario);
                assertTrue(usuario.id.equals(1));

                usuario.nombre = "pacote";
                usuario = UsuarioDAO.update(usuario);
                //al modificar, comprobamos su id y su nuevo nombre
                assertTrue(usuario.id.equals(1));
                assertEquals("pacote",usuario.nombre);

            });
        });
    }

    @Test
    public void testMetodosUsuarioEntidadNegocio() {
        //no necesitamos usar ni fakeApplication ni transacciones
        Usuario usuario = new Usuario();
        usuario.login="pepe";
        usuario.nombre="";
        usuario.eMail="pepe@gmail.com";
        usuario.nulificaAtributos();
        //al nulificar, deberia convertir el nombre de vacio a null
        assertTrue(usuario.nombre==null);
        //el toString deberia devolver esa cadena
        assertEquals("Usuario id: null login: pepe password:  nombre: null apellidos: null eMail: pepe@gmail.com fechaNacimiento: null",
                    usuario.toString());
    }

}
