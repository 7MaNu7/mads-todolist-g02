
function actualizar_etiquetas(idusuario) {
  $.ajax({
        url: '/usuarios/' + idusuario + '/etiquetas',
        type: 'GET',
        success: function(results) {

            /*
            var select = document.getElementById('sel1');
            var select2 = document.getElementById('sel2');
            select.innerHTML='<option value="" disabled selected>Selecciona una etiqueta</option>'; //vaciamos
            select2.innerHTML='<option value="" disabled selected>Selecciona una etiqueta</option>'; //vaciamos
            var plantilla='';
            $.each(results, function(key, value) {
                plantilla = '<option value="' + key + '">' + value + '</option>'
                select.innerHTML+=plantilla;
                select2.innerHTML+=plantilla;
            });
            */

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
            });

        }
    });
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

/*
function pon_amarillo(event) {
    //$(event.target).switchClass( "btn-success", "btn-warning");
    //$(event.target.children).switchClass("glyphicon-tag","glyphicon-minus-sign");

    $(event.target).addClass("btn-warning");
    $(event.target).removeClass("btn-info");
    $(event.target.children).addClass("glyphicon-minus-sign");
    $(event.target.children).removeClass("glyphicon-tag");
}
*/

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
