package controllers;

import play.*;
import play.mvc.*;
import views.html.*;
import play.data.Form;
import models.*;
import play.db.jpa.*;
import play.data.DynamicForm;

import java.util.List;

public class Usuarios extends Controller {

    @Transactional(readOnly = true)
    // Devuelve una página con la lista de usuarios
    public Result listaUsuarios() {
        String tipo = session("tipo");
        if(tipo==null) //si no esta logeado
            return ok(formLoginUsuario.render(new DynamicForm(),"¡Necesitas iniciar sesión para acceder a este recurso!"));
        if(!tipo.equals("admin"))
            return unauthorized(error.render(UNAUTHORIZED,"¡No tienes los privilegios necesarios para acceder a este recurso!"));
        // Obtenemos el mensaje flash guardado en la petición
        // por el controller grabaUsuario
        String mensaje = flash("grabaUsuario");
        List<Usuario> usuarios = UsuarioService.findAllUsuarios();
        return ok(listaUsuarios.render(usuarios, mensaje));
    }

  // Devuelve un formulario para crear un nuevo usuario
    public Result formularioNuevoUsuario() {
        String tipo = session("tipo");
        if(tipo==null) //si no esta logeado
            return ok(formLoginUsuario.render(new DynamicForm(),"¡Necesitas iniciar sesión para acceder a este recurso!"));
        if(!tipo.equals("admin"))
            return unauthorized(error.render(UNAUTHORIZED,"¡No tienes los privilegios necesarios para acceder a este recurso!"));
        return ok(formCreacionUsuario.render(Form.form(Usuario.class),""));
    }

    //Devuelve un formulario para registrarse como usuario
    public Result formularioRegUsuario() {
        return ok(formRegistroUsuario.render(Form.form(Usuario.class), ""));
    }
    //devuelve un formulario para logerse
    public Result formularioLogin() {
        if(session("tipo")!=null)
            return unauthorized(error.render(UNAUTHORIZED,"Ya tienes una sesión iniciada."));
        return ok(formLoginUsuario.render(new DynamicForm(),""));
    }


    @Transactional(readOnly = true)
    public Result logeaUsuario() {
        DynamicForm requestData = Form.form().bindFromRequest();
        String login = requestData.get("login");
        String password = requestData.get("password");

        //LOGIN DE ADMINISTRADOR
        if(login.equals("admin") && password.equals("admin"))
        {
            session("tipo", "admin");
            return redirect(controllers.routes.Usuarios.listaUsuarios());
        }

        //LOGIN DE USUARIO SIN PRIVILEGIOS
        else
        {
            Usuario usuario = UsuarioService.loginUsuario(login,password);
            if(usuario==null)
                return badRequest(formLoginUsuario.render(new DynamicForm(),
                    "¡Nombre de usuario y/o contraseña incorrectos!"));
            else {
                //se guarda el id del usuario
                session("tipo",String.valueOf(usuario.id));
                return redirect(controllers.routes.Tareas.listaTareas(usuario.id));
            }
        }
    }

    public Result cerrarSesion() {
        //si tiene sesion abierta, se cierra
        if(session("tipo")!=null)
            session().remove("tipo");
        return redirect(controllers.routes.Usuarios.formularioLogin());
    }

    @Transactional
    public Result grabaNuevoUsuario() {
        Form<Usuario> usuarioForm =
            Form.form(Usuario.class).bindFromRequest();
        if(usuarioForm.hasErrors()) {
            return badRequest(formCreacionUsuario.render(usuarioForm,
            "Hay errores en el formulario"));
        }
        Usuario usuario = usuarioForm.get();
        usuario = UsuarioService.grabaUsuario(usuario);
        flash("grabaUsuario","El usuario se ha grabado correctamente");
        return redirect(controllers.routes.Usuarios.listaUsuarios());
    }

    @Transactional
    public Result registraNuevoUsuario() {
        Form<Usuario> usuarioForm =
                Form.form(Usuario.class).bindFromRequest();
        if(usuarioForm.hasErrors()) {
            return badRequest(formRegistroUsuario.render(usuarioForm,
                    "Hay errores en el formulario"));
        }

        Usuario usuario = usuarioForm.get();
        usuario = UsuarioService.grabaUsuario(usuario);

        //Además de añadir el usuario lo logeamos
        usuario = UsuarioService.loginUsuario(usuario.login, usuario.password);
        if(usuario==null)
            return badRequest(formLoginUsuario.render(new DynamicForm(),
                "¡Nombre de usuario y/o contraseña incorrectos!"));
        else {
            //Se guarda el id del usuario en la sesión
            session("tipo",String.valueOf(usuario.id));
            return redirect(controllers.routes.Tareas.listaTareas(usuario.id));
        }
    }

    @Transactional
    public Result grabaUsuarioModificado() {
        Form<Usuario> usuarioForm =
            Form.form(Usuario.class).bindFromRequest();
        if(usuarioForm.hasErrors()) {
            return badRequest(formModificacionUsuario.render(usuarioForm,
            "Hay errores en el formulario"));
        }
        Usuario usuario = usuarioForm.get();
        usuario = UsuarioService.modificaUsuario(usuario);
        flash("grabaUsuario","El usuario se ha actualizado correctamente");
        return redirect(controllers.routes.Usuarios.listaUsuarios());
    }

    @Transactional
    public Result borraUsuario(Integer id) {
        boolean borrado = UsuarioService.deleteUsuario(id);
        if(borrado)
            return ok("El usuario se ha borrado sin problemas.");
        else
            return badRequest(error.render(BAD_REQUEST,"El usuario con id=" + id + " no existe."));


    }

    @Transactional
    public Result detalleUsuario(Integer id) {
        String tipo = session("tipo");
        if(tipo==null) //si no esta logeado
            return ok(formLoginUsuario.render(new DynamicForm(),"¡Necesitas iniciar sesión para acceder a este recurso!"));
        if(!tipo.equals("admin"))
            return unauthorized(error.render(UNAUTHORIZED,"¡No tienes los privilegios necesarios para acceder a este recurso!"));
        Usuario user = UsuarioService.findUsuario(id);
        if(user==null)
            return badRequest(error.render(BAD_REQUEST,"El usuario con id=" + id + " no existe."));
        else
            return ok(detalleUsuario.render(user));
    }

    @Transactional
    public Result editarUsuario(Integer id) {
        String tipo = session("tipo");
        if(tipo==null) //si no esta logeado
            return ok(formLoginUsuario.render(new DynamicForm(),"¡Necesitas iniciar sesión para acceder a este recurso!"));
        if(!tipo.equals("admin"))
            return unauthorized(error.render(UNAUTHORIZED,"¡No tienes los privilegios necesarios para acceder a este recurso!"));
        Usuario usuario = UsuarioService.findUsuario(id);
        Form<Usuario> f = Form.form(Usuario.class);
        Form<Usuario> definitivo = f.fill(usuario);
        return ok(formModificacionUsuario.render(definitivo,""));
    }
}
