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
import play.api.Logger;


import play.libs.ws.*;

public class EtiquetasWSTests {

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
    public void testWebCreaEtiqueta() {
        running(testServer(3333, app), () -> {
            int timeout = 4000;
            //una etiqueta con nombre vacio, bad request
            WSResponse response = WS
                .url("http://localhost:3333/etiquetas/nueva")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .setContentType("application/x-www-form-urlencoded")
                .post("nombre=&id_usuario=1")
                .get(timeout);

            assertEquals(BAD_REQUEST, response.getStatus());
            assertTrue(response.getBody().contains("Una etiqueta debe tener nombre obligatoriamente."));

            //una etiqueta con nombre, ok
            response = WS
                .url("http://localhost:3333/etiquetas/nueva")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .setContentType("application/x-www-form-urlencoded")
                .post("nombre=Para verano&id_usuario=1")
                .get(timeout);

            assertEquals(OK, response.getStatus());


            response = WS
                .url("http://localhost:3333/usuarios/1/etiquetas")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);

                assertTrue(response.getBody().contains("Para verano"));


                assertEquals(OK, response.getStatus());
        });
    }


    @Test
    public void testWebModificaEtiqueta() {
        running(testServer(3333, app), () -> {
            int timeout = 4000;

            //cambia el nombre de la etiqueta 1 , con un usuario que no es dueño de la etiqueta
            WSResponse response = WS
                .url("http://localhost:3333/etiquetas/modifica")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("julia","martinez"))
                .setContentType("application/x-www-form-urlencoded")
                .post("id=1&nombre=CAMBIADO&id_usuario=1")
                .get(timeout);

            assertEquals(401, response.getStatus()); //no autorizado




            //cambia el nombre de la etiqueta 1 a nulo o vacio
            response = WS
                .url("http://localhost:3333/etiquetas/modifica")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .setContentType("application/x-www-form-urlencoded")
                .post("id=1&nombre=&id_usuario=1")
                .get(timeout);

            assertEquals(BAD_REQUEST, response.getStatus()); //necesita un nombre



            //cambia el nombre de la etiqueta 1 , con usuario 1
            response = WS
                .url("http://localhost:3333/etiquetas/modifica")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .setContentType("application/x-www-form-urlencoded")
                .post("id=1&nombre=TAG-CAMBIADO&id_usuario=1")
                .get(timeout);

            assertEquals(OK, response.getStatus()); //ok



            response = WS
                .url("http://localhost:3333/usuarios/1/etiquetas")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);

                assertEquals(OK, response.getStatus());
                assertTrue(response.getBody().contains("TAG-CAMBIADO")); //que contenga el nombre actualizado
                assertTrue(!response.getBody().contains("ENCARNA")); //que no contenga el nombre anterior

        });
    }

    @Test
    public void testWebBorrarEtiqueta() {
        running(testServer(3333, app), () -> {

            int timeout = 4000;
            WSResponse response = WS
                .url("http://localhost:3333/usuarios/1/etiquetas")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);
            assertEquals(OK, response.getStatus());
            String body = response.getBody();
            assertTrue(body.contains("ENCARNA"));



            //borra la etiqueta 1 del usuario 1
            response = WS
                .url("http://localhost:3333/etiquetas/1")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .delete()
                .get(timeout);

            assertEquals(OK,response.getStatus());
            assertTrue(response.getBody().contains("La etiqueta se ha borrado sin problemas."));


            //intenta borrar etiqueta no existente
            response = WS
                .url("http://localhost:3333/etiquetas/1")
                .setFollowRedirects(false)
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .delete()
                .get(timeout);

            assertEquals(BAD_REQUEST,response.getStatus());
            assertTrue(response.getBody().contains("La etiqueta con id=1 no existe."));

            //comprobar que la etiqueta ha sido borrada del listado de la web
            response = WS
                .url("http://localhost:3333/usuarios/1/etiquetas")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);
            assertEquals(OK, response.getStatus());
            body = response.getBody();
            assertTrue(!body.contains("ENCARNA"));

        });
    }

    @Test
    public void testWebEtiquetaEnListaTareas() {
        running(testServer(3333, app), () -> {
            int timeout = 4000;

          WSResponse  response = WS
                .url("http://localhost:3333/usuarios/1/tareas")
                .setHeader("Cookie",WSUtils.getSessionCookie("pepito","perez"))
                .get()
                .get(timeout);

                assertEquals(OK, response.getStatus());
                assertTrue(response.getBody().contains("ENCARNA"));
                assertTrue(!response.getBody().contains("Importante1"));
                assertTrue(response.getBody().contains("Importante2"));

        });
    }



}
