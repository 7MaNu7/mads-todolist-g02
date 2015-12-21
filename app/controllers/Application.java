package controllers;

import play.*;
import play.mvc.*;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;

import views.html.*;
import javax.inject.Inject;
import java.io.File;
import org.apache.commons.mail.EmailAttachment;
import play.data.DynamicForm;
import play.data.Form;

public class Application   extends Controller {



    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public Result saludo(String nombre) {
      return ok(saludo.render(nombre));
    }


    public Result webMarketing() {
      return ok(webMarketing.render());
    }

    public Result webMarketingSendEmail(){
      DynamicForm requestData = Form.form().bindFromRequest();

      String emailio = requestData.get("email");
      String telefono = requestData.get("telefono");
      String mensaje = requestData.get("mensaje");
      String nombre = requestData.get("nombre");
      Email email = new Email();
      email.setFrom("madstodolist FROM <todolistmads@gmail.com>");
      email.addTo("madstodolist TO <todolistmads@gmail.com>");
       email.setSubject("Web Marketing email");
       email.setBodyHtml("<html><body><p>Email :"+emailio+"</p><p>Telf :"+telefono+"</p><p>Mesanje: "+mensaje+"</p><p>nombre: "+nombre+"</p></body></html>");
      MailerPlugin.send(email);
      return ok("Enviado correctamente ");
    }

}
