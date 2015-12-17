package controllers;

import java.util.List;

import play.*;
import play.mvc.*;
import views.html.*;
import play.data.Form;
import play.db.jpa.*;
import play.data.DynamicForm;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
        ObjectNode result = Json.newObject();
        for(Etiqueta e:etiquetas) {
            result.put(e.id.toString(),e.nombre);
        }
        return ok(result);
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

    @Transactional
    public Result grabaEtiquetaModificada()
    {
        String tipo = session("tipo");
        if(tipo==null) //si no esta logeado
            return ok(formLoginUsuario.render(new DynamicForm(),
                "¡Necesitas iniciar sesión para acceder a este recurso!"));

        DynamicForm requestData = Form.form().bindFromRequest();
        Integer id_etiqueta = Integer.parseInt(requestData.get("id"));
        String nombre = requestData.get("nombre");

        if(nombre==null || nombre.equals(""))
            return badRequest(error.render(BAD_REQUEST,"Una etiqueta debe tener nombre obligatoriamente."));

        Integer user_id = -99;
        try {
          user_id = Integer.parseInt(requestData.get("id_usuario"));
        } catch(NumberFormatException e) {
          return badRequest(error.render(BAD_REQUEST,"El id de usuario no puede estar vacío."));
        }

        Usuario usuario = UsuarioService.findUsuario(user_id);
        Etiqueta etiqueta = EtiquetaService.findEtiqueta(id_etiqueta);
        if(usuario==null || etiqueta==null)
            return badRequest(error.render(BAD_REQUEST,"El usuario y/o la etiqueta proporcionados no existen"));

        if(!tipo.equals("admin")) //el admin modifica las etiquetas que quiera
            if(Integer.parseInt(tipo)!=user_id) //si el user autenticado no coincide con id
                return unauthorized(error.render(UNAUTHORIZED,"No tienes permitido modificar etiquetas de otros usuarios"));

        //se modifica el nombre
        etiqueta.nombre=nombre;

        etiqueta = EtiquetaService.modificaEtiqueta(etiqueta);
        //flash("grabaTarea","La tarea se ha actualizado correctamente");
        //return redirect(controllers.routes.Tareas.listaTareas(user_id));
        return ok(etiqueta.toString());
    }

    @Transactional
    public Result borraEtiqueta(Integer id) {
        String tipo = session("tipo");
        if(tipo==null) //si no esta logeado
            return ok(formLoginUsuario.render(new DynamicForm(),
                "¡Necesitas iniciar sesión para acceder a este recurso!"));

        Etiqueta etiqueta = EtiquetaService.findEtiqueta(id);
        if(etiqueta==null) {
            return badRequest(error.render(BAD_REQUEST,"La etiqueta con id=" + id + " no existe."));
        }

        if(!tipo.equals("admin")) //el admin modifica las etiquetas que quiera
            if(Integer.parseInt(tipo)!=etiqueta.usuario.id) //si el user autenticado no coincide con id
                return unauthorized(error.render(BAD_REQUEST,"No tienes permitido borrar una etiqueta que no es tuya."));
        EtiquetaService.deleteEtiqueta(id);
        return ok("La etiqueta se ha borrado sin problemas.");

    }







}
