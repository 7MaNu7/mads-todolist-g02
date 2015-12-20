package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public Result saludo(String nombre) {
      return ok(saludo.render(nombre));
    }


    public Result webMarketing() {
      return ok(webMarketing.render());
    }


}
