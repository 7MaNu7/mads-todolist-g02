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

public class BorrarTareasTests {

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
    public void testBorraTareaDAO() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                TareaDAO.delete(4); //borra la tarea 4, que es la unica del usuario 2
                Usuario usuario = UsuarioDAO.find(2);
                List<Tarea> tareas = usuario.tareas;
                assertEquals(0,tareas.size());

                TareaDAO.delete(2); //borra la tarea 2 del user 1
                //le quedarian 2 tareas restantes
                usuario = UsuarioDAO.find(1);
                tareas = usuario.tareas;
                assertEquals(2,tareas.size());
            });
        });
    }

    @Test
    public void testTareaServiceBorraTarea() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                boolean borrado = false;
                borrado = TareaService.deleteTarea(4); //borra la tarea 4, que es la unica del usuario 2
                assertTrue(borrado);
                Usuario usuario = UsuarioService.findUsuario(2);
                List<Tarea> tareas = usuario.tareas;
                assertEquals(0,tareas.size());

                borrado = TareaService.deleteTarea(2); //borra la tarea 2 del user 1
                assertTrue(borrado);
                //le quedarian 2 tareas restantes
                usuario = UsuarioService.findUsuario(1);
                tareas = usuario.tareas;
                assertEquals(2,tareas.size());

                borrado = TareaService.deleteTarea(-99); //borra tarea no existente
                assertTrue(!borrado);

                borrado = TareaService.deleteTarea(4); //borra tarea ya borrada anteriormente
                assertTrue(!borrado);

            });
        });
    }

    @Test
    public void testWebPaginaBorrarTarea() {
        running(testServer(3333, app), () -> {

            int timeout = 4000;
            WSResponse response = WS
                .url("http://localhost:3333/usuarios/1/tareas")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);
            assertEquals(OK, response.getStatus());
            String body = response.getBody();
            assertTrue(body.contains("Preparar el trabajo del tema 1 de biología"));

            //borra la tarea 1 del usuario 1
            response = WS
                .url("http://localhost:3333/tareas/1")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .delete()
                .get(timeout);

            assertEquals(OK,response.getStatus());
            assertTrue(response.getBody().contains("La tarea se ha borrado sin problemas."));

            //intenta borrar tarea no existente
            response = WS
                .url("http://localhost:3333/tareas/99")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .delete()
                .get(timeout);

            assertEquals(BAD_REQUEST,response.getStatus());
            assertTrue(response.getBody().contains("La tarea con id=99 no existe."));

            //comprobar que la tarea ha sido borrada del listado de la web
            response = WS
                .url("http://localhost:3333/usuarios/1/tareas")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);
            assertEquals(OK, response.getStatus());
            body = response.getBody();
            assertTrue(!body.contains("Preparar el trabajo del tema 1 de biología"));
        });
    }
}
