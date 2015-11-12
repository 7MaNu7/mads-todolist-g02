import org.junit.*;

import play.mvc.*;
import play.test.*;
import play.libs.F.*;

import play.libs.ws.*;
import play.Logger;

import static play.test.Helpers.*;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import play.mvc.Http.RequestBuilder;

public class WebServiceTest {

    //metodo helper que registra un nuevo usuario rapidamente
    private void registra(String login,String password) {
        int timeout = 20000;
        WSResponse response = WS.url("http://localhost:3333/registrarse")
                    .setFollowRedirects(true)
                    .setContentType("application/x-www-form-urlencoded")
                    .post("login=" + login + "&password=" + password)
                    .get(timeout);
    }

    @Before
    public void limpiaSesion() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), () -> {
            WS.url("http://localhost:3333/salir").get().get(20000);
         });
    }
    @Test
    public void testFormularioLogin() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), () -> {
            int timeout = 20000;
            WSResponse response = WS.url("http://localhost:3333/login").get().get(timeout);
            assertEquals(OK, response.getStatus());
            assertTrue(response.getBody().contains("<h1>Inicio de sesión</h1>"));
        });
    }

    @Test
    public void testdoLoginUsuarioNotFound() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), () -> {
            int timeout = 20000;
            WSResponse response = WS.url("http://localhost:3333/login")
                            .setContentType("application/x-www-form-urlencoded")
                            .post("login=domingo&password=gallardo")
                            .get(timeout);
            assertEquals(BAD_REQUEST, response.getStatus());
            assertTrue(response.getBody().contains("¡Nombre de usuario y/o contraseña incorrectos!"));
        });
    }

    @Test
    public void testRegistraNuevoUsuario() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), () -> {
            int timeout = 20000;
            String login = "domingo";
            WSResponse response = WS.url("http://localhost:3333/registrarse")
                        .setFollowRedirects(true)
                        .setContentType("application/x-www-form-urlencoded")
                        .post("login="+login+"&password=gallardo&eMail=domingo.gallardo@ua.es")
                        .get(timeout);
            assertTrue(response.getBody().contains("Listado de tareas de "+login));
        });
    }

    @Test
    public void testPrivilegiosSesion() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), () -> {
            int timeout = 20000;
            //comprobar que usuario NO puede entrar a la lista de usuarios
            if(WSUtils.getSessionCookie("domingo","gallardo")==null) //la memory database no guarda registros de test anteriores
            {
                registra("domingo","gallardo");
            }
            WSResponse response = WS.url("http://localhost:3333/usuarios")
            .setHeader("Cookie",WSUtils.getSessionCookie("domingo","gallardo"))
            .get()
            .get(timeout);
            assertTrue(response.getBody().contains("¡No tienes los privilegios necesarios para acceder a este recurso!"));

            //comprobar que admin puede entrar a la lista de usuarios
            response = WS.url("http://localhost:3333/usuarios")
            .setHeader("Cookie",WSUtils.getSessionCookie("admin","admin"))
            .get()
            .get(timeout);
            assertTrue(response.getBody().contains("<title>Listado de usuarios</title>"));
            assertTrue(response.getBody().contains("<td>domingo</td>")); //y que este el usuario listado
        });
    }

    @Test
    public void testEditaUsuario() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), () -> {
            int timeout=20000;
            //creamos un nuevo usuario
            registra("domingo","gallardo");

            //comprobamos que el usuario existe antes de modificar
            WSResponse response = WS.url("http://localhost:3333/usuarios/1")
            .setHeader("Cookie",WSUtils.getSessionCookie("admin","admin"))
            .get()
            .get(timeout);

            //comprobamos que es el usuario 1, es domingo y su nombre esta vacio de momento
            assertTrue(response.getBody().contains("<h1>Datos del usuario 1</h1>"));
            assertTrue(response.getBody().contains("<li><b>Login:</b> domingo</li>"));
            assertTrue(response.getBody().contains("<li><b>Nombre:</b> </li>"));

            //modificamos el nombre del usuario
            response = WS.url("http://localhost:3333/usuarios/modifica")
            .setFollowRedirects(false)
            .setHeader("Cookie",WSUtils.getSessionCookie("admin","admin"))
            .setContentType("application/x-www-form-urlencoded")
            .post("id=1&login=domingo&nombre=Hector")
            .get(timeout);

            //comprobamos que el usuario tiene el nombre modificado
            response = WS.url("http://localhost:3333/usuarios/1")
            .setHeader("Cookie",WSUtils.getSessionCookie("admin","admin"))
            .get()
            .get(timeout);

            assertTrue(response.getBody().contains("<li><b>Nombre:</b> Hector</li>"));
        });
    }

    @Test
    public void testBorraUsuario() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), () -> {
            int timeout=20000;
            //creamos un nuevo usuario
            registra("domingo","gallardo");
            //comprobamos que el usuario existe antes de borrar
            WSResponse response = WS.url("http://localhost:3333/usuarios/1")
            .setHeader("Cookie",WSUtils.getSessionCookie("admin","admin"))
            .get()
            .get(timeout);

            //comprobamos que es el usuario 1, es domingo y su nombre esta vacio de momento
            assertTrue(response.getBody().contains("<h1>Datos del usuario 1</h1>"));
            assertTrue(response.getBody().contains("<li><b>Login:</b> domingo</li>"));

            //borramos
            response = WS.url("http://localhost:3333/usuarios/1")
            .setHeader("Cookie",WSUtils.getSessionCookie("admin","admin"))
            .delete()
            .get(timeout);
            assertTrue(response.getBody().contains("El usuario se ha borrado sin problemas."));

            //comprobamos que el usuario NO EXISTE despues de borrar
            response = WS.url("http://localhost:3333/usuarios/1")
            .setHeader("Cookie",WSUtils.getSessionCookie("admin","admin"))
            .get()
            .get(timeout);
            assertEquals(BAD_REQUEST, response.getStatus());
            assertTrue(response.getBody().contains("El usuario con id=1 no existe"));
        });
    }

}
