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

public class AnotacionTareasTests {

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
    public void testTareaAnotacionDAO() {
       running (app, () -> {
           JPA.withTransaction(() -> {
               Tarea tarea1 = TareaDAO.find(1);
               assertTrue(tarea1.anotacion.equals(""));
               Tarea tarea2 = TareaDAO.find(2);
               assertTrue(tarea2.anotacion.equals("Este examen cuenta el 40% de la asignatura"));
           });
         });
    }

    @Test
    public void testTareaAnotacionService() {
       running (app, () -> {
           JPA.withTransaction(() -> {
             Tarea tarea1 = TareaService.findTarea(1);
             assertTrue(tarea1.anotacion.equals(""));
             Tarea tarea2 = TareaService.findTarea(2);
             assertTrue(tarea2.anotacion.equals("Este examen cuenta el 40% de la asignatura"));
           });
         });
    }

    @Test
    public void testCreaTareaDevuelveTareaAnotacionVacia() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                Usuario usuario = UsuarioDAO.find(1);
                Tarea tarea = new Tarea("Una tarea muy chula",usuario);
                tarea = TareaDAO.create(tarea);
                assertEquals(null,tarea.anotacion);
            });
        });
    }

    @Test
    public void testTareaServiceCreaTareaDevuelveTareaAnotacionVacia() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                Usuario usuario = UsuarioDAO.find(2);
                Tarea tarea = new Tarea("Una tarea muy chula",usuario);
                tarea = TareaService.grabaTarea(tarea);
                assertEquals(null,tarea.anotacion);
            });
        });
    }

    @Test
    public void testModificarTareaAnotacion() {
       running (app, () -> {
           JPA.withTransaction(() -> {
               Usuario usuario = UsuarioDAO.find(1);
               Tarea tarea = TareaDAO.find(2);
               tarea.anotacion = "Este examen tiene mucha materia";
               TareaService.modificaTarea(tarea);
               List<Tarea> tareas = usuario.tareas;
               assertEquals(tareas.size(), 3);
               assertTrue(tareas.get(1).anotacion.equals("Este examen tiene mucha materia"));
           });
       });
    }

    @Test
    public void testModificarTareaAnotacion2Cambios() {
       running (app, () -> {
           JPA.withTransaction(() -> {
               Usuario usuario = UsuarioDAO.find(1);
               Tarea tarea = TareaDAO.find(2);
               tarea.anotacion = "Este examen tiene mucha materia";
               TareaService.modificaTarea(tarea);
               tarea.anotacion = "Este examen consta de 6 temas";
               TareaService.modificaTarea(tarea);
               List<Tarea> tareas = usuario.tareas;
               assertEquals(tareas.size(), 3);
               assertTrue(tareas.get(1).anotacion.equals("Este examen consta de 6 temas"));
           });
       });
    }

}
