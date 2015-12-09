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

public class EtiquetasTests {

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
    public void testCreaEtiquetaDevuelveEtiqueta() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                Usuario usuario = UsuarioDAO.find(2);
                Etiqueta etiqueta = new Etiqueta("Trabajo fin de Grado",usuario);
                etiqueta = EtiquetaDAO.create(etiqueta);



                assertEquals(etiqueta.nombre,"Trabajo fin de Grado");
                assertEquals(etiqueta.usuario.login,"julia");
            });
        });
    }

    @Test
    public void testEtiquetaServiceCreaEtiquetaDevuelveEtiqueta() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                Usuario usuario = UsuarioDAO.find(2);
                Etiqueta etiqueta = new Etiqueta("Cosas",usuario);
                etiqueta = EtiquetaDAO.create(etiqueta);
                List<Etiqueta> tags = new ArrayList<Etiqueta>();
                tags.add(etiqueta);
                Etiqueta etique = new Etiqueta("Para vacaciones",usuario);
                etique = EtiquetaService.grabaEtiqueta(etique);
                tags.add(etique);
                Tarea tarea = new Tarea("Una tarea muy chulilla",usuario,tags);
                tarea = TareaService.grabaTarea(tarea);

                assertEquals(tarea.descripcion,"Una tarea muy chulilla");
                assertEquals(2,tarea.etiquetas.size());

            });
        });
    }

    @Test
    public void testEtiquetaModificar() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                Etiqueta etiqueta = EtiquetaDAO.find(1);
                etiqueta.nombre="Deportes";
                etiqueta = EtiquetaDAO.update(etiqueta);
                Usuario usuario = UsuarioDAO.find(1);
                assertEquals(usuario.etiquetas.get(0).nombre,"Deportes");
            });
        });
    }

    @Test
    public void testEtiquetaBorrar() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                Etiqueta etiqueta = EtiquetaDAO.find(1);
                EtiquetaDAO.delete(etiqueta.id);
                Usuario usuario = UsuarioDAO.find(1);
                assertEquals(0,usuario.etiquetas.size());
            });
        });
    }






    @Test
    public void testCreaEtiquetaService() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                Usuario usuario = UsuarioDAO.find(2);
                Etiqueta etiqueta = new Etiqueta("Trabajo fin de Grado",usuario);
                etiqueta = EtiquetaService.grabaEtiqueta(etiqueta);

                assertEquals(etiqueta.nombre,"Trabajo fin de Grado");
                assertEquals(etiqueta.usuario.login,"julia");
            });
        });
    }

    @Test
    public void testEtiquetaModificarService() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                Etiqueta etiqueta = EtiquetaDAO.find(1);
                etiqueta.nombre="Deportes";
                etiqueta = EtiquetaService.modificaEtiqueta(etiqueta);
                Usuario usuario = UsuarioDAO.find(1);
                assertEquals(usuario.etiquetas.get(0).nombre,"Deportes");
            });
        });
    }

    @Test
    public void testEtiquetaBorrarService() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                Etiqueta etiqueta = EtiquetaDAO.find(1);
                EtiquetaService.deleteEtiqueta(etiqueta.id);
                Usuario usuario = UsuarioDAO.find(1);
                assertEquals(0,usuario.etiquetas.size());
            });
        });
    }




}
