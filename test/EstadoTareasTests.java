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

public class EstadoTareasTests {

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
    public void testTareaEstadoEsPendienteDAO() {
       running (app, () -> {
           JPA.withTransaction(() -> {
               Tarea tarea1 = TareaDAO.find(1);
               assertTrue(tarea1.estado.equals("pendiente"));
               Tarea tarea2 = TareaDAO.find(2);
               assertTrue(tarea2.estado.equals("pendiente"));
           });
         });
    }

    @Test
    public void testTareaEstadoEsPendienteService() {
       running (app, () -> {
           JPA.withTransaction(() -> {
             Tarea tarea1 = TareaService.findTarea(1);
             assertTrue(tarea1.estado.equals("pendiente"));
             Tarea tarea2 = TareaService.findTarea(2);
             assertTrue(tarea2.estado.equals("pendiente"));
           });
         });
    }

    @Test
    public void testModificarTareaEstadoRealizada() {
       running (app, () -> {
           JPA.withTransaction(() -> {
               Usuario usuario = UsuarioDAO.find(1);
               Tarea tarea = TareaDAO.find(2);
               tarea.estado = "realizada";
               TareaService.modificaTarea(tarea);
               List<Tarea> tareas = usuario.tareas;
               assertEquals(tareas.size(), 3);
               assertTrue(tareas.get(1).estado.equals("realizada"));
           });
       });
    }

    @Test
    public void testModificarTareaEstado2Cambios() {
       running (app, () -> {
           JPA.withTransaction(() -> {
               Usuario usuario = UsuarioDAO.find(1);
               Tarea tarea = TareaDAO.find(2);
               tarea.estado = "realizada";
               TareaService.modificaTarea(tarea);
               tarea.estado = "pendiente";
               TareaService.modificaTarea(tarea);
               List<Tarea> tareas = usuario.tareas;
               assertEquals(tareas.size(), 3);
               assertTrue(tareas.get(1).estado.equals("pendiente"));
           });
       });
    }
}
