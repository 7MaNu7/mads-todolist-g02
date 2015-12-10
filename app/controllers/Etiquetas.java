package controllers;

import java.util.List;

import play.*;
import play.mvc.*;
import views.html.*;
import static play.libs.Json.*;
import play.data.Form;
import play.db.jpa.*;
import play.data.DynamicForm;

import models.*;

public class Etiquetas extends Controller {

    /*
    *  CRUD de etiquetas
    *
    */

    @Transactional(readOnly = true)
    // Devuelve una página con la lista de tareas
    public Result listaEtiquetas(Integer usuarioId) {
        String tipo = session("tipo");
        if(tipo==null) //si no esta logeado
            return ok(formLoginUsuario.render(new DynamicForm(),"¡Necesitas iniciar sesión para acceder a este recurso!"));

        //si otro usuario (no admin) quiere acceder a una list que no es suya...
        if(!tipo.equals("admin"))
        {
            if(Integer.parseInt(tipo)!=usuarioId)
            {
                return unauthorized(error.render(UNAUTHORIZED,"No tienes permiso para ver un recurso que no es tuyo."));
            }
        }
        List<Etiqueta> etiquetas = null;
        try {
            etiquetas = EtiquetaService.findAllEtiquetasUsuario(usuarioId);
        } catch(NullPointerException e) {
            return badRequest(error.render(BAD_REQUEST,"El usuario con id=" + usuarioId + " no existe."));
        }
        //Usuario usuario = UsuarioService.findUsuario(usuarioId);
        //String mensaje = flash("grabaTarea");
        //return ok(listaTareas.render(tareas,usuario,mensaje));

        return ok(etiquetas.toString());
    }

    @Transactional
    public Result grabaNuevaEtiqueta() {
        String tipo = session("tipo");
        if(tipo==null) //si no esta logeado
          return ok(formLoginUsuario.render(new DynamicForm(),"¡Necesitas iniciar sesión para acceder a este recurso!"));
        DynamicForm requestData = Form.form().bindFromRequest();
        String nombre = requestData.get("nombre");
        if(nombre==null || nombre.equals("")) {
            return badRequest(error.render(BAD_REQUEST,"Una etiqueta debe tener nombre obligatoriamente."));
        }
        String user_id = requestData.get("id_usuario");
        if(!tipo.equals("admin")) //el admin puede grabar las etiquetas
            if(!tipo.equals(user_id)) //si el user autenticado no coincide con id
                return unauthorized(error.render(UNAUTHORIZED,"No tienes permitido crear etiquetas a otros usuarios"));
        Etiqueta etiqueta = new Etiqueta(nombre,UsuarioService.findUsuario(Integer.parseInt(user_id)));
        etiqueta = EtiquetaService.grabaEtiqueta(etiqueta);
        //flash("grabaTarea","La tarea se ha grabado correctamente");
        //return redirect(controllers.routes.Tareas.listaTareas(tarea.usuario.id));
        return ok(etiqueta.toString());
    }

}
