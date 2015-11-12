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

public class ListadoTareasTests {

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
    public void testFindTareaDevuelveTarea() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                Tarea tarea = TareaDAO.find(1);
                assertEquals(tarea.descripcion,"Preparar el trabajo del tema 1 de biología");
                Usuario usuario = tarea.usuario;
                assertEquals(usuario.login, "pepito");
            });
        });
    }

    @Test
    public void testTareasUsuarioDevuelveSusTareas() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                Usuario usuario = UsuarioDAO.find(1);
                List<Tarea> tareas = usuario.tareas;
                assertEquals(tareas.size(), 3);
                assertTrue(tareas.contains(new
                    Tarea("Preparar el trabajo del tema 1 de biología", usuario)));
                assertTrue(tareas.contains(new
                    Tarea("Estudiar el parcial de matemáticas", usuario)));
                assertTrue(tareas.contains(new
                    Tarea("Leer el libro de inglés", usuario)));
            });
        });
    }

    @Test
    public void testTareaServiceFindAllTareasDevuelveTodasLasTareas() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                Integer usuarioId = 1;
                List<Tarea> tareas = TareaService.findAllTareasUsuario(usuarioId);

                Usuario pepito = new Usuario("pepito", "perez");
                assertTrue(tareas.contains(
                    new Tarea("Preparar el trabajo del tema 1 de biología",pepito)));
                assertTrue(tareas.contains(
                    new Tarea("Estudiar el parcial de matemáticas",pepito)));
                assertTrue(tareas.contains(
                    new Tarea("Leer el libro de inglés",pepito)));
                });
        });
    }

    @Test
    public void testWebPaginaListadoTareas() {
        running(testServer(3333, app), () -> {
            int timeout = 4000;
            WSResponse response = WS
                .url("http://localhost:3333/usuarios/1/tareas")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);
            assertEquals(OK, response.getStatus());
            String body = response.getBody();
            assertTrue(body.contains("<h2>Listado de tareas de pepito</h2>"));
        });
    }

    @Test
    public void testWebApiListadoTareas() {
        running(testServer(3333, app), () -> {
            int timeout = 4000;
            WSResponse response = WS
                .url("http://localhost:3333/usuarios/1/tareas")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);
            assertEquals(OK, response.getStatus());
            String body = response.getBody();
            assertTrue(body.contains(
                "Preparar el trabajo del tema 1 de biología"));
            assertTrue(body.contains(
                "Estudiar el parcial de matemáticas"));
            assertTrue(body.contains(
                "Leer el libro de inglés"));
        });
    }
}
