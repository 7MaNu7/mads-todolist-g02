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

public class ModificarTareasTests {

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
    public void testModificaTareaDevuelveTarea() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                Tarea tarea_antigua = TareaDAO.find(4);
                tarea_antigua.descripcion="Ahora esta tarea se llama asi";
                Tarea tarea = TareaDAO.update(tarea_antigua);
                assertTrue(tarea.id.equals(4));
                assertEquals("Ahora esta tarea se llama asi",tarea.descripcion);
                assertEquals(tarea.usuario,tarea_antigua.usuario);

            });
        });
    }

    @Test
    public void testTareaServiceModificaTareaDevuelveTarea() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                Tarea tarea_antigua = TareaService.findTarea(4);
                Usuario usuario = UsuarioService.findUsuario(1);
                tarea_antigua.usuario = usuario;
                Tarea tarea = TareaService.modificaTarea(tarea_antigua);

                assertTrue(tarea.id==4);
                //assertEquals(4,tarea.id);
                //assertEquals(tarea.usuario,usuario);
                assertTrue(tarea.usuario.equals(usuario));
            });
        });
    }

    @Test
    public void testWebPaginaModificarTarea() {
        running(testServer(3333, app), () -> {
            int timeout = 4000;
            WSResponse response = WS
                .url("http://localhost:3333/usuarios/1/tareas/1/editar")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);
            assertEquals(OK, response.getStatus());
            assertTrue(response.getBody().contains("<h1>Modificar tarea 1 del usuario pepito</h1>"));

            response = WS
                .url("http://localhost:3333/usuarios/99/tareas/88/editar")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);
            assertEquals(BAD_REQUEST,response.getStatus());
            assertTrue(response.getBody().contains("El usuario y/o la tarea proporcionados no existen"));

            response = WS
                .url("http://localhost:3333/usuarios/1/tareas/4/editar")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);
            assertEquals(BAD_REQUEST,response.getStatus());
            assertTrue(response.getBody().contains("Ese usuario no puede editar una tarea que no es suya"));

        });
    }

    @Test
    public void testWebModificaTareaEnForm() {
        running(testServer(3333, app), () -> {
            int timeout = 4000;

            //cambia la descripcion de la tarea 1 del usuario 1
            WSResponse response = WS
                .url("http://localhost:3333/tareas/modifica")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .setContentType("application/x-www-form-urlencoded")
                .post("id=1&estado=pendiente&descripcion=Hay que refactorizar amigos, la nueva moda es la cloud&id_usuario=1")
                .get(timeout);

            response = WS
                .url("http://localhost:3333/usuarios/1/tareas")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);

                assertEquals(OK, response.getStatus());
                assertTrue(response.getBody().contains("Hay que refactorizar amigos, la nueva moda es la cloud"));

        });
    }
}
