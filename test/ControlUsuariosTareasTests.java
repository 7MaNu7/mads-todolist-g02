import org.junit.*;
import play.test.*;
import play.Application;
import play.mvc.*;
import static play.test.Helpers.*;
import static org.junit.Assert.*;
import play.db.jpa.*;
import java.util.List;
import models.*;
import org.dbunit.*;
import org.dbunit.dataset.*;
import org.dbunit.dataset.xml.*;
import java.util.HashMap;
import java.io.FileInputStream;
import java.util.Map;
import java.util.ArrayList;
import play.libs.ws.*;

public class ControlUsuariosTareasTests {

    JndiDatabaseTester databaseTester;
    Application app;

    // Devuelve los settings necesarios para crear la aplicación fake
    // usando la base de datos de integración
    private static HashMap<String, String> settings() {
        HashMap<String, String> settings = new HashMap<String, String>();
        settings.put("db.default.url", "jdbc:mysql://localhost:3306/mads_test");
        settings.put("db.default.username", "root");
        settings.put("db.default.password", "mads"); //no puse password a root
        settings.put("db.default.jndiName", "DefaultDS");
        settings.put("jpa.default", "mySqlPersistenceUnit");
        return(settings);
    }

    // Crea la conexión con la base de datos de prueba y
    // la inicializa con las tablas definidas por las entidades JPA
    @BeforeClass
    public static void createTables() {
        Application fakeApp = Helpers.fakeApplication(settings());
        // Abrimos una transacción para que JPA cree en la BD
        // las tablas correspondientes a las entidades
        running (fakeApp, () -> {
            JPA.withTransaction(() -> {});
        });
    }

    // Se ejecuta antes de cada tests, inicializando la BD con los
    // datos del dataset
    @Before
    public void inicializaBaseDatos() throws Exception {
        app = Helpers.fakeApplication(settings());
        databaseTester = new JndiDatabaseTester("DefaultDS");
        IDataSet initialDataSet = new FlatXmlDataSetBuilder().build(new
            FileInputStream("test/resources/tareas_dataset_1.xml"));
        databaseTester.setDataSet(initialDataSet);
        databaseTester.onSetup();
    }

    @After
    public void cierraBaseDatos() throws Exception {
        databaseTester.onTearDown();
    }


    @Test
    public void testControllerLogin1Devuelve1() {
        running(testServer(3333, app), () -> {
            String token = WSUtils.getSessionCookie("pepito","perez");
            assertTrue(token.contains("tipo=1"));
        });
    }

    @Test
    public void testControllerControlListaTareas() {
        running(testServer(3333, app), () -> {
            int timeout = 4000;

            //el admin puede ver tareas del usuario que quiera
            WSResponse response = WS
                .url("http://localhost:3333/usuarios/1/tareas")
                .setHeader("Cookie",WSUtils.getSessionCookie("admin","admin"))
                .get()
                .get(timeout);
            assertEquals(OK, response.getStatus());
            String body = response.getBody();
            assertTrue(body.contains("Preparar el trabajo del tema 1 de biología"));
            //el usuario 1 podra ver sus tareas
            response = WS
                .url("http://localhost:3333/usuarios/1/tareas")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);
            assertEquals(OK, response.getStatus());
            body = response.getBody();
            assertTrue(body.contains("Preparar el trabajo del tema 1 de biología"));

            //el usuario 2 no puede ver las tareas del usuario 1
            response = WS
                .url("http://localhost:3333/usuarios/1/tareas")
                .setHeader("Cookie",WSUtils.getSessionCookie("julia","martinez"))
                .get()
                .get(timeout);
            assertEquals(UNAUTHORIZED, response.getStatus());
            body = response.getBody();
            assertTrue(!body.contains("Preparar el trabajo del tema 1 de biología"));


        });
    }

    @Test
    public void testControllerControlCrearTareas() {
        running(testServer(3333, app), () -> {
            int timeout = 4000;

            //el usuario 1 podra crear tareas para él mismo
            WSResponse response = WS
                .url("http://localhost:3333/tareas/nueva")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .setContentType("application/x-www-form-urlencoded")
                .post("descripcion=Arreglar la bd&id_usuario=1")
                .get(timeout);

            response = WS
                .url("http://localhost:3333/usuarios/1/tareas")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);

            assertEquals(OK, response.getStatus());
            assertTrue(response.getBody().contains("Arreglar la bd"));


            //el usuario 2 julia NO podra crear tareas para el usuario 1 pepito
            response = WS
                .url("http://localhost:3333/tareas/nueva")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("julia","martinez"))
                .setContentType("application/x-www-form-urlencoded")
                .post("descripcion=Hola a todos&id_usuario=1")
                .get(timeout);

            assertEquals(UNAUTHORIZED, response.getStatus());
            assertTrue(response.getBody()
                .contains("No tienes permitido crear tareas a otros usuarios"));


            //el usuario 2 tampoco puede entrar al form de crea tarea para el user 1
            response = WS
                .url("http://localhost:3333/usuarios/1/tareas/nueva")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("julia","martinez"))
                .get()
                .get(timeout);

            assertEquals(UNAUTHORIZED, response.getStatus());
            assertTrue(response.getBody()
                .contains("No tienes permiso para ver un recurso que no es tuyo."));

        });
    }

    @Test
    public void testControllerControlModificarTareas() {
        running(testServer(3333, app), () -> {
            int timeout = 4000;
            //el usuario 1 podra modificar sus tareas
            WSResponse response = WS
                .url("http://localhost:3333/tareas/modifica")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .setContentType("application/x-www-form-urlencoded")
                .post("id=1&descripcion=Hay que refactorizar amigos, la nueva moda es la cloud&id_usuario=1")
                .get(timeout);

            response = WS
                .url("http://localhost:3333/usuarios/1/tareas")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);

            assertEquals(OK, response.getStatus());
            assertTrue(response.getBody().contains("Hay que refactorizar amigos, la nueva moda es la cloud"));

            //el usuario 2 NO podra acceder al formulario de modificar tarea del user 1
            response = WS
                .url("http://localhost:3333/usuarios/1/tareas/1/editar")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("julia","martinez"))
                .get()
                .get(timeout);
            assertEquals(UNAUTHORIZED, response.getStatus());
            assertTrue(response.getBody()
                .contains("No tienes permiso para ver un recurso que no es tuyo."));

            //el usuario 2 NO podra modificar tareas del user 1
            response = WS
                .url("http://localhost:3333/tareas/modifica")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("julia","martinez"))
                .setContentType("application/x-www-form-urlencoded")
                .post("id=1&descripcion=Hay que refactorizar amigos, la nueva moda es windows&id_usuario=1")
                .get(timeout);
            assertEquals(UNAUTHORIZED, response.getStatus());
            assertTrue(response.getBody()
                .contains("No tienes permitido modificar tareas de otros usuarios"));
        });
    }

    @Test
    public void testControllerControlBorrarTareas() {
        running(testServer(3333, app), () -> {
            int timeout = 4000;

            //borra la tarea 1 del usuario 1, siendo ADMIN
            WSResponse response = WS
                .url("http://localhost:3333/tareas/1")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("admin","admin"))
                .delete()
                .get(timeout);
            assertEquals(OK,response.getStatus());
            assertTrue(response.getBody().contains("La tarea se ha borrado sin problemas."));

            //borra la tarea 2 del usuario 1, siendo PEPITO (el 1)
            response = WS
                .url("http://localhost:3333/tareas/2")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .delete()
                .get(timeout);
            assertEquals(OK,response.getStatus());
            assertTrue(response.getBody().contains("La tarea se ha borrado sin problemas."));

            //INTENTA borrar la tarea 3 del usuario 1, siendo JULIA (la 2)
            response = WS
                .url("http://localhost:3333/tareas/3")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("julia","martinez"))
                .delete()
                .get(timeout);
            assertEquals(UNAUTHORIZED,response.getStatus());
            assertTrue(response.getBody().contains("No tienes permitido borrar una tarea que no es tuya."));

        });
    }

}
