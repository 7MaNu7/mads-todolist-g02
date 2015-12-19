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

public class CrearTareasTests {

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
    public void testCreaTareaDevuelveTarea() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                Usuario usuario = UsuarioDAO.find(1);
                Tarea tarea = new Tarea("Una tarea muy chula",usuario);
                tarea = TareaDAO.create(tarea);
                assertEquals(tarea.descripcion,"Una tarea muy chula");
                usuario = tarea.usuario;
                assertEquals(usuario.login, "pepito");
                assertEquals(4,usuario.tareas.size());
            });
        });
    }

    @Test
    public void testTareaServiceCreaTareaDevuelveTarea() {
        running (app, () -> {
            JPA.withTransaction(() -> {
                Usuario usuario = UsuarioDAO.find(2);
                Tarea tarea = new Tarea("Una tarea muy chula",usuario);
                tarea = TareaService.grabaTarea(tarea);
                assertEquals(tarea.descripcion,"Una tarea muy chula");
                usuario = tarea.usuario;
                assertEquals(usuario.login, "julia");
                assertEquals(2,usuario.tareas.size());
            });
        });
    }

    @Test
    public void testWebPaginaCrearTarea() {
        running(testServer(3333, app), () -> {
            int timeout = 4000;
            WSResponse response = WS
                .url("http://localhost:3333/usuarios/1/tareas/nueva")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);
            assertEquals(OK, response.getStatus());
            String body = response.getBody();
            assertTrue(body.contains("<h1>Nueva tarea para el usuario pepito</h1>"));
        });
    }

    @Test
    public void testWebCreaTareaEnForm() {
        running(testServer(3333, app), () -> {
            int timeout = 4000;
            WSResponse response = WS
                .url("http://localhost:3333/tareas/nueva")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .setContentType("application/x-www-form-urlencoded")
                .post("descripcion=Hay que refactorizar amigos&id_usuario=1")
                .get(timeout);

            response = WS
                .url("http://localhost:3333/usuarios/1/tareas")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);

                assertEquals(OK, response.getStatus());
                assertTrue(response.getBody().contains("Hay que refactorizar amigos"));

        });
    }


    @Test
    public void testWebCreaTareaEnFormConTag() {
        running(testServer(3333, app), () -> {
            JPA.withTransaction(() -> {
                int timeout = 4000;
                WSResponse response = WS
                    .url("http://localhost:3333/tareas/nueva")
                    .setFollowRedirects(false)
                    .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                    .setContentType("application/x-www-form-urlencoded")
                    .post("descripcion=Hay que refactorizar amigos&tags=1;2;3;&&id_usuario=1")
                    .get(timeout);

                assertEquals(UNAUTHORIZED, response.getStatus()); //la tag 2 no es suya

                response = WS
                    .url("http://localhost:3333/tareas/nueva")
                    .setFollowRedirects(false)
                    .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                    .setContentType("application/x-www-form-urlencoded")
                    .post("descripcion=Hay que refactorizar amigos&tags=hola;&&id_usuario=1")
                    .get(timeout);

                assertEquals(BAD_REQUEST, response.getStatus()); //"hola" no es una lista de tags valida

                response = WS
                    .url("http://localhost:3333/tareas/nueva")
                    .setFollowRedirects(false)
                    .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                    .setContentType("application/x-www-form-urlencoded")
                    .post("descripcion=Hay que refactorizar amigos&tags=1;3;&&id_usuario=1")
                    .get(timeout);

                assertEquals(303, response.getStatus()); //las tags 1 y 3 son suyas, asi que redirect


              //obtenemos las tareas del usuario
              List<Tarea> tareas = TareaService.findAllTareasUsuario(1);
              Tarea tarea = null;

              //buscamos la recien creada
              for(int i=0; i<tareas.size(); i++)
              {
                if(tareas.get(i).descripcion.equals("Hay que refactorizar amigos"))
                {
                  tarea=tareas.get(i);
                }
              }
              //comprobamos que tiene las dos etiquetas
              assertEquals(2, tarea.etiquetas.size());
            });
        });
    }

    @Test
    public void testWebCreaTareaPropagaBorradoTag() {
        running(testServer(3333, app), () -> {
            JPA.withTransaction(() -> {
                int timeout = 4000;
                WSResponse response = WS
                    .url("http://localhost:3333/tareas/nueva")
                    .setFollowRedirects(false)
                    .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                    .setContentType("application/x-www-form-urlencoded")
                    .post("descripcion=Hay que refactorizar amigos&tags=1;&&id_usuario=1")
                    .get(timeout);

                assertEquals(303, response.getStatus());

                response = WS
                    .url("http://localhost:3333/usuarios/1/tareas")
                    .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                    .get()
                    .get(timeout);

                    assertEquals(OK, response.getStatus());
                    assertTrue(response.getBody().contains("Hay que refactorizar amigos"));

                Etiqueta e =  EtiquetaDAO.find(1);
                EtiquetaDAO.delete(1); //borramos el tag 1


                //obtenemos las etiquetas del usuario 1
                List<Tarea> tareas = TareaService.findAllTareasUsuario(1);
                Tarea tarea = null;

                //Buscamos la recien creada
                for(int i=0; i<tareas.size(); i++)
                {
                  if(tareas.get(i).descripcion.equals("Hay que refactorizar amigos"))
                  {
                    tarea=tareas.get(i);
                  }
                }

                //comprobamos que al borrar la etiqueta se quita de la tarea tambien
                assertEquals(0, tarea.etiquetas.size());


            });
        });
    }




}
