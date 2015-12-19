/* CRUD CON LAS TAREAS DE UN USUARIO */

/* Para eliminar una tarea */

function del(urlBorrar) {
  $.ajax({
      url: urlBorrar,
      type: 'DELETE',
      success: function(results) {
          //refresh the page
          location.reload();
      }
  });
}

/* Para modificar el estado de una tarea */

function modificarEstado(urlModificar, id, descripcion, estado, idusuario, anotacion, prioridad) {
  if(estado == 'pendiente') nuevo_estado = 'realizada';
  else nuevo_estado = 'pendiente';
  $.ajax({
   url: urlModificar,
   data : { id : id, descripcion : descripcion, estado : nuevo_estado, id_usuario : idusuario, anotacion : anotacion, prioridad : prioridad },
   type: 'POST',
   success: function(results) {
     location.reload();
   }
  });
}

/* Para modificar una tarea */

function modificarTareaAnotacion(urlModificar, id, descripcion, estado, idusuario, anotacionA, prioridad) {
  var anotacion;
  if(anotacionA=='borrar') anotacion = "";
  else anotacion = document.getElementById("textoanotacion"+id).value;

  $.ajax({
   url: urlModificar,
   data : { id : id, descripcion : descripcion, estado : estado, id_usuario : idusuario, anotacion: anotacion, prioridad : prioridad },
   type: 'POST',
   success: function(results) {
     //Sin recargar la página y cerramos acordeon
     document.getElementById("textoanotacion"+id).value = anotacion;
     cerrarAnotacion(id);
     mostrarSuccessAnotacion();
   }
  });
}

/* Para modificar la prioridad de una tarea */

function guardarTareaPrioridad(urlModificar, id, descripcion, estado, idusuario, anotacion, prioridad) {
  $.ajax({
   url: urlModificar,
   data : { id : id, descripcion : descripcion, estado : estado, id_usuario : idusuario, anotacion: anotacion, prioridad : prioridad },
   type: 'POST',
   success: function(results) {
     location.reload();
   }
  });
}


/* OPERACIONES PARA FILTRAR ETIQUETAS DE LAS TAREAS */

function ocultarTareasSinEtiqueta(etiqueta){
  $(".tarea").each(function (index){
    var etiquetas = $(this).attr("id");
     if(etiquetas.indexOf(etiqueta)>-1){
        $(this).show();
     }else{
       $(this).hide();
     }
   })
  }
  function mostrarTodasTareasConSinEtiqueta(){
    $(".tarea").each(function (index){
          $(this).show();
     })
    }




/* OPERACIONES CON ACORDEÓN PARA ANOTACIÓN TAREA */

/* Para cerrar/abrir/efectos del acordeón de la anotación de cada tarea */

function abrirAnotacion(id) {
  $(".accordion-content"+id).slideDown(function() {
    $("#iconoabriranotacion"+id).addClass("iconoinvisible");
    $("#iconocerraranotacion"+id).removeClass("iconoinvisible");
  });
}

function cerrarAnotacion(id) {
  $(".accordion-content"+id).slideUp(function() {
    $("#iconocerraranotacion"+id).addClass("iconoinvisible");
    $("#iconoabriranotacion"+id).removeClass("iconoinvisible");
  });
}

function mostrarAcordeon(id) {
    var contenido=$(".accordion-content"+id);
     if(contenido.css("display")=="none"){ //Abrimos acordeon
       abrirAnotacion(id);
     }
     else{ //Cerramos acordeon
       cerrarAnotacion(id);
    }
}

function mostrarSuccessAnotacion() {
  $.smkAlert({
    text: 'Anotación modificada correctamente.',
    type: 'success',
    position:'top-center',
    time: 2
  });
}
