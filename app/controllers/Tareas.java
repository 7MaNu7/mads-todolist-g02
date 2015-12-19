package controllers;

import java.util.List;

import play.*;
import play.mvc.*;
import views.html.*;
import static play.libs.Json.*;
import play.data.Form;
import play.db.jpa.*;
import play.data.DynamicForm;
import java.util.ArrayList;
import java.util.Arrays;

import models.*;

public class Tareas extends Controller {

    /*
    *  CRUD de tareas
    *
    */

    @Transactional(readOnly = true)
    // Devuelve una página con la lista de tareas
    public Result listaTareas(Integer usuarioId) {
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
        List<Tarea> tareas = null;
        try {
            tareas = TareaService.findAllTareasUsuario(usuarioId);
        } catch(NullPointerException e) {
            return badRequest(error.render(BAD_REQUEST,"El usuario con id=" + usuarioId + " no existe."));
        }
        List<Etiqueta> etiquetas = null;
        try {
            etiquetas = EtiquetaService.findAllEtiquetasUsuario(usuarioId);
        } catch(NullPointerException e) {
            return badRequest(error.render(BAD_REQUEST,"El usuario con id=" + usuarioId + " no existe."));
        }
        Usuario usuario = UsuarioService.findUsuario(usuarioId);
        String mensaje = flash("grabaTarea");
        return ok(listaTareas.render(tareas,etiquetas,usuario,mensaje));
    }

    @Transactional(readOnly = true)
    // Devuelve un formulario para crear una nueva tarea
    public Result formularioNuevaTarea(Integer id) {
        String tipo = session("tipo");
        if(tipo==null) //si no esta logeado
            return ok(formLoginUsuario.render(new DynamicForm(),"¡Necesitas iniciar sesión para acceder a este recurso!"));

        Usuario usuario = UsuarioService.findUsuario(id);
        if(usuario==null) //si no existe la id
            return badRequest(error.render(BAD_REQUEST,"El usuario con id=" + id + " no existe."));

        if(!tipo.equals("admin")) //el admin crea las tareas que quiera
            if(Integer.parseInt(tipo)!=id) //si el user autenticado no coincide con id
                return unauthorized(error.render(UNAUTHORIZED,"No tienes permiso para ver un recurso que no es tuyo."));
        return ok(formCreacionTarea.render(Form.form(Tarea.class),usuario,""));
      }

    @Transactional
    public Result grabaNuevaTarea() {
        String tipo = session("tipo");
        if(tipo==null) //si no esta logeado
          return ok(formLoginUsuario.render(new DynamicForm(),"¡Necesitas iniciar sesión para acceder a este recurso!"));
        DynamicForm requestData = Form.form().bindFromRequest();
        String descripcion = requestData.get("descripcion");
        String user_id = requestData.get("id_usuario");

        //etiquetas////////////////////
        List<Etiqueta> tags = new ArrayList<Etiqueta>();
        try {
            if(requestData.get("tags")!=null && requestData.get("tags").length()>0)
            {
                String[] tags_array = requestData.get("tags").split(";");
                for(int i=0;i<tags_array.length;i++)
                {
                    Etiqueta e = EtiquetaService.findEtiqueta(Integer.parseInt(tags_array[i]));
                    if(e==null) //no existe
                        return badRequest(error.render(BAD_REQUEST,
                            "Alguna de las etiquetas introducidas no existe"));
                    else
                        tags.add(e);
                }
            }
        } catch(NumberFormatException e) {
            return badRequest(error.render(BAD_REQUEST,"La lista de etiquetas debe estar definida por enteros separados por ; -> 1;2;3;"));
        }

        ///////////////////////


        Integer prioridad = 3;
        if(requestData.get("prioridad")!=null)
         prioridad = Integer.parseInt(requestData.get("prioridad"));
        if(!tipo.equals("admin")) //el admin puede grabar las tareas que quiera
            if(!tipo.equals(user_id)) //si el user autenticado no coincide con id
                return unauthorized(error.render(UNAUTHORIZED,"No tienes permitido crear tareas a otros usuarios"));




        Usuario usuario = UsuarioService.findUsuario(Integer.parseInt(user_id));
        for(Etiqueta tag:tags) {
            if(!usuario.etiquetas.contains(tag))
                return unauthorized(error.render(UNAUTHORIZED,"No tienes permitido usar etiquetas de otros usuarios"));
        }


        Tarea tarea = new Tarea(descripcion,usuario,tags);

        tarea.prioridad=prioridad;
        tarea = TareaService.grabaTarea(tarea);
        flash("grabaTarea","La tarea se ha grabado correctamente");
        return redirect(controllers.routes.Tareas.listaTareas(tarea.usuario.id));
    }

    @Transactional
    public Result editarTarea(Integer id_user,Integer id_tarea)
    {
        String tipo = session("tipo");
        if(tipo==null) //si no esta logeado
            return ok(formLoginUsuario.render(new DynamicForm(),
                "¡Necesitas iniciar sesión para acceder a este recurso!"));

        Usuario usuario = UsuarioService.findUsuario(id_user);
        Tarea tarea = TareaService.findTarea(id_tarea);
        if(usuario==null || tarea==null)
            return badRequest(error.render(BAD_REQUEST,"El usuario y/o la tarea proporcionados no existen"));

        if(!tipo.equals("admin")) //el admin modifica las tareas que quiera
            if(Integer.parseInt(tipo)!=id_user) //si el user autenticado no coincide con id
                return unauthorized(error.render(UNAUTHORIZED,"No tienes permiso para ver un recurso que no es tuyo."));
        if(!usuario.tareas.contains(tarea))
            return badRequest(error.render(BAD_REQUEST,"Ese usuario no puede editar una tarea que no es suya"));
        return ok(formModificacionTarea.render(usuario,tarea));
    }

    @Transactional
    public Result grabaTareaModificada()
    {
        String tipo = session("tipo");
        if(tipo==null) //si no esta logeado
            return ok(formLoginUsuario.render(new DynamicForm(),
                "¡Necesitas iniciar sesión para acceder a este recurso!"));

        DynamicForm requestData = Form.form().bindFromRequest();
        Integer id_tarea = Integer.parseInt(requestData.get("id"));
        String descripcion = requestData.get("descripcion");
        String estado = requestData.get("estado");
        String anotacion = requestData.get("anotacion");

        Integer prioridad = -1;
        if(requestData.get("prioridad")!=null)
        prioridad = Integer.parseInt(requestData.get("prioridad"));

        Integer user_id = -99;
        try {
          user_id = Integer.parseInt(requestData.get("id_usuario"));
        } catch(NumberFormatException e) {
          return badRequest(error.render(BAD_REQUEST,"El id de usuario no puede estar vacío."));
        }

        Usuario usuario = UsuarioService.findUsuario(user_id);
        Tarea tarea = TareaService.findTarea(id_tarea);
        if(usuario==null || tarea==null)
            return badRequest(error.render(BAD_REQUEST,"El usuario y/o la tarea proporcionados no existen"));

        if(!tipo.equals("admin")) //el admin modifica las tareas que quiera
            if(Integer.parseInt(tipo)!=user_id) //si el user autenticado no coincide con id
                return unauthorized(error.render(UNAUTHORIZED,"No tienes permitido modificar tareas de otros usuarios"));

        //se modifica la tarea
        tarea.descripcion = descripcion;
        tarea.estado = estado;
        if(prioridad>0 && prioridad<4)
          tarea.prioridad = prioridad;
        tarea.anotacion = anotacion;

        tarea = TareaService.modificaTarea(tarea);
        flash("grabaTarea","La tarea se ha actualizado correctamente");
        return redirect(controllers.routes.Tareas.listaTareas(user_id));
    }

      @Transactional
      public Result borraTarea(Integer id) {
          String tipo = session("tipo");
          if(tipo==null) //si no esta logeado
              return ok(formLoginUsuario.render(new DynamicForm(),
                  "¡Necesitas iniciar sesión para acceder a este recurso!"));

          Tarea tarea = TareaService.findTarea(id);
          if(tarea==null) {
              return badRequest(error.render(BAD_REQUEST,"La tarea con id=" + id + " no existe."));
          }

          if(!tipo.equals("admin")) //el admin modifica las tareas que quiera
              if(Integer.parseInt(tipo)!=tarea.usuario.id) //si el user autenticado no coincide con id
                  return unauthorized(error.render(BAD_REQUEST,"No tienes permitido borrar una tarea que no es tuya."));
          TareaService.deleteTarea(id);
          return ok("La tarea se ha borrado sin problemas.");

      }




}
