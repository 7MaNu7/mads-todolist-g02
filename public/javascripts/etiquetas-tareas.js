
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

            var tags_usuario = document.getElementById('tags_usuario');
            tags_usuario.innerHTML=""; //vaciamos

            var almacen_tags_tarea = document.getElementById('almacen_tags').value;
            almacen_tags_tarea = almacen_tags_tarea.split(";");

            $.each(results, function(key, value) {
                if($.inArray(key,almacen_tags_tarea)<0) //si esa tag no esta añadida al almacen, la añadimos a nuestra lista de tags
                {
                    var boton = "<div class='btn-group'>";
                    boton+= "<a href='#' data-toggle='tooltip' onclick='add_tag(" + key + ")' title='Click para añadir' onmouseenter='pon_verde(event)' onmouseleave='pon_azul(event)' class='btn btn-xs btn-info'><span class='glyphicon glyphicon-tag'></span>" + value + "</a>";
                    boton+="<a href='#' data-toggle='tooltip' onclick='borrar_tag_bd(" + key + ")' title='Click para eliminar completamente' class='btn btn-xs btn-warning'><span class='glyphicon glyphicon-trash'></span></a>";
                    boton+="</div>";
                    var plantilla = '<li id=usuariotag' + key + '>' + boton + '</li>';
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
  console.log("voy a mostrar_tags");

  $.ajax({
        url: '/tareas/' + tareaId + '/etiquetas',
        type: 'GET',
        success: function(results) {

            var listaTags= document.getElementById('tags-selecc');
            listaTags.innerHTML="";
            //añadir las etiquetas que ya tenia
            $.each(results, function(key, value) {
                var boton = "<div data-toggle='tooltip' title='Click para eliminar' class='btn-xs btn-info'><span class='glyphicon glyphicon-tag'></span>" + value + "</a><a href='#' class='btn-xs btn-info' onclick='remove_tag(" + key + ")'><span class='glyphicon glyphicon-remove'></span></a></div>";
                var plantilla = '<li id=' + key + '>' + boton + '</li>';
                document.getElementById('tags-selecc').innerHTML+=plantilla;
                actualizar_almacen_tags();
            });
        }
      });

      actualizar_etiquetas(idusuario);
}

function pon_verde(event)
{
    $(event.target).addClass("btn-success");
    $(event.target).removeClass("btn-info");
    $(event.target.children).addClass("glyphicon-plus-sign");
    $(event.target.children).removeClass("glyphicon-tag");
}

function pon_azul(event)
{
    $(event.target).removeClass( "btn-success"); //si esta en verde, cambia a azul
    $(event.target).removeClass( "btn-warning"); //si esta en amarillo, cambia a azul
    $(event.target).addClass( "btn-info");


    $(event.target.children).removeClass("glyphicon-plus-sign"); //lo mismo
    $(event.target.children).removeClass("glyphicon-minus-sign");
    $(event.target.children).addClass("glyphicon-tag");
}

function add_tag(id) {

    var text = $("#usuariotag" + id)[0].innerText;

    var boton = "<div data-toggle='tooltip' title='Click para eliminar' class='btn-xs btn-info'><span class='glyphicon glyphicon-tag'></span>" + text + "</a><a href='#' class='btn-xs btn-info' onclick='remove_tag(" + id + ")'><span class='glyphicon glyphicon-remove'></span></a></div>";
    var plantilla = '<li id=' + id + '>' + boton + '</li>';
    document.getElementById('tags-selecc').innerHTML+=plantilla;
    actualizar_almacen_tags();
    $("#usuariotag" + id).remove(); //lo eliminamos de nuestras etiquetas

}

function remove_tag(id) {
     $("#" + id).remove();
     actualizar_almacen_tags();
     var idusuario = $('[name="id_usuario"]').val();
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

    console.log(almacen_tags_tarea);
    console.log("LA TAG ES " + tag_id);
    if($.inArray(tag_id,almacen_tags_tarea)>=0) //si esta en el almacen de tags, ahi hay problema
    {
        console.log("SALSEO");
        var text = $('#modif-tag').val();
        var boton = "<div data-toggle='tooltip' title='Click para eliminar' class='btn-xs btn-info'><span class='glyphicon glyphicon-tag'></span>" + text + "</a><a href='#' class='btn-xs btn-info' onclick='remove_tag(" + tag_id + ")'><span class='glyphicon glyphicon-remove'></span></a></div>";
        $('#' + tag_id).html(boton);
        actualizar_almacen_tags();

    }



}




function actualizar_almacen_tags() {
    var almacen = document.getElementById("almacen_tags");
    var listItems = $("#tags-selecc li");

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
            actualizar_etiquetas(idusuario); //actualizamos listas
            remove_tag(id); //borramos de los tags seleccionados, ya que no puede tener seleccionada una tag no existente
            actualizar_almacen_tags(); //actualizamos el almacen oculto de tags para enviar en el post

            },
          error: function(results) {
              console.log(results);
          }
        });
    }
}
