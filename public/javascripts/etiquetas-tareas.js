
function actualizar_etiquetas(idusuario) {
  $.ajax({
        url: '/usuarios/' + idusuario + '/etiquetas',
        type: 'GET',
        success: function(results) {

            var select3 = document.getElementById('sel3');
            if(select3!=null)
            {
              select3.innerHTML='<option value="" disabled selected>Selecciona una etiqueta</option>';
            }

            var tags_usuario = document.getElementById('right-events');
            tags_usuario.innerHTML=""; //vaciamos

            var almacen_tags_tarea = document.getElementById('almacen_tags').value;
            almacen_tags_tarea = almacen_tags_tarea.split(";");

            $.each(results, function(key, value) {
                if($.inArray(key,almacen_tags_tarea)<0) //si esa tag no esta añadida al almacen, la añadimos a nuestra lista de tags
                {
                    var boton = "<a href='#' data-toggle='tooltip' class='btn btn-xs btn-info'><span class='glyphicon glyphicon-tag'></span>" + value + "</a>";
                    var plantilla = '<li class="etiquetaendiv" id=' + key + '>' + boton + '</li>';
                    tags_usuario.innerHTML+=plantilla;
                }
                plantilla = '<option value="' + key + '">' + value + '</option>'
                if(select3!=null)
                {
                  select3.innerHTML+=plantilla;
                }
            });

        }
    });
}


function mostrar_tags(tareaId, idusuario) {
  $.ajax({
        url: '/tareas/' + tareaId + '/etiquetas',
        type: 'GET',
        success: function(results) {
            //añadir las etiquetas que ya tenia
            $.each(results, function(key, value) {
                var boton = "<a href='#' data-toggle='tooltip' class='btn btn-xs btn-info'><span class='glyphicon glyphicon-tag'></span>" + value + "</a>";
                var plantilla = '<li class="etiquetaendiv" id=' + key + '>' + boton + '</li>';
                document.getElementById('left-events').innerHTML+=plantilla;
                actualizar_almacen_tags();
            });
        }
      });
      actualizar_etiquetas(idusuario);
}


function editar_etiqueta(idusuario) {
    var data = {};
    var form = document.getElementById('modif-tag');
    var nombre = form.value;

    data.id=document.getElementById('sel3').value;
    var text = $("#sel3 option:selected").text();

    data.nombre = nombre;
    data.id_usuario=idusuario;

    if(data.id)
    {
      $.ajax({
        url: '/etiquetas/modifica',
        type: 'POST',
        data: data,
        success: function(results) {
          replace_tag();
          actualizar_etiquetas(idusuario);
          form.value=""; //limpiamos el inputtext
          },
        error: function(results) {
            console.log(results);
        }
      });
    }
}

function replace_tag() {
    var form = document.getElementById('modif-tag');
    var newTag = form.value;
    var tag_id=document.getElementById('sel3').value;

    var almacen_tags_tarea = document.getElementById('almacen_tags').value;
    almacen_tags_tarea = almacen_tags_tarea.split(";");

    if($.inArray(tag_id,almacen_tags_tarea)>=0) //si esta en el almacen de tags, ahi hay problema
    {
        var text = $('#modif-tag').val();
        var boton = "<a href='#' data-toggle='tooltip' class='btn btn-xs btn-info'><span class='glyphicon glyphicon-tag'></span>" + text + "</a>";
        $('#' + tag_id).html(boton);
        actualizar_almacen_tags();
    }
}

function actualizar_almacen_tags() {
    var almacen = document.getElementById("almacen_tags");
    var listItems = $("#left-events li");

    almacen.value=""; //limpiamos el almacen
    listItems.each(function(idx, li)
    {
        var tag_id = $(li).attr('id'); //obtenemos el id del tag
        almacen.value+=tag_id + ";"; //los vamos guardando y separando con ;
    });
}


function crear_etiqueta(idusuario) {
    var data = {};
    var form = document.getElementById('nueva-tag');
    var nombre = form.value;
    data.nombre = nombre;
    data.id_usuario=idusuario;

    $.ajax({
      url: '/etiquetas/nueva',
      type: 'POST',
      data: data,
      success: function(results) {
        actualizar_etiquetas(idusuario);
        form.value=""; //limpiamos el inputtext
        },
      error: function(results) {
          console.log(results);
      }
    });

}

function borrar_tag_bd(id) {
    var idusuario = $('[name="id_usuario"]').val();

    if(id)
    {
        $.ajax({
          url: '/etiquetas/' + id,
          type: 'DELETE',
          success: function(results) {
            $("#" + id).remove();
            actualizar_etiquetas(idusuario); //actualizamos listas
            actualizar_almacen_tags(); //actualizamos el almacen oculto de tags para enviar en el post

            },
          error: function(results) {
              console.log(results);
          }
        });
    }
}

/////////////////////
//DRAGULA DRAG & DROP
/////////////////////

var containers = [document.getElementById('left-events'), document.getElementById('right-events'), document.getElementById('eliminar-events')];
var options = {
    invalid: function (el, target) {
        return el.id == "basurita"; //el icono de la basurita no se puede arrastrar
    }
};
dragula(containers,options)
  .on('drag', function (el) {
    el.className = el.className.replace('ex-moved', '');
  })
  .on('drop', function (el) {
    el.className += ' ex-moved';
    var padre = el.parentNode;

    //Si se ha soltado arriba se añade la etiqueta a la tarea
    if(padre.id=="left-events") {
      actualizar_almacen_tags();
    }

    //Si se ha soltado abajo se elimina la etiqueta de la tarea
    if (padre.id=="right-events") {
      actualizar_almacen_tags();
    }

    //Si se ha soltado en la papelera se elimina la etiqueta de la BD
    if (padre.id=="eliminar-events") {
        borrar_tag_bd(el.id);
    }
  })
  /*.on('over', function (el, contenedor) {
    contenedor.className += ' ex-over';
  })
  .on('out', function (el, contenedor) {
    contenedor.className = contenedor.className.replace('ex-over', '');
  })*/;
