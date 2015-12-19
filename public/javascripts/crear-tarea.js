
function actualizar_etiquetas(idusuario) {
  $.ajax({
        url: '/usuarios/' + idusuario + '/etiquetas',
        type: 'GET',
        success: function(results) {

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
            //var plantilla= listaTags.innerHTML;
            $.each(results, function(key, value) {
              console.log("en bucle, key: "+key);
                listaTags.innerHTML = listaTags.innerHTML + "<li id='" + key + "'>"
                + "<a href='#' data-toggle='tooltip' title='Click para eliminar' onclick='remove_tag(" + key + ")' class='btn-xs btn-info'><span class='glyphicon glyphicon-tag'></span>" + value + "</a>";
                + '</li>';
                console.log(listaTags.innerHTML);
              //  actualizar_almacen_tags();
            });
        }
      });

      actualizar_etiquetas(idusuario);
}

function add_tag() {
    var id = document.getElementById('sel1').value;
    var text = $("#sel1 option:selected").text();

    if(id && $("#" + id).length == 0) //si no se ha añadido ese tag, añadirlo
    {
        var boton = "<a href='#' data-toggle='tooltip' title='Click para eliminar' onclick='remove_tag(" + id + ")' class='btn-xs btn-info'><span class='glyphicon glyphicon-tag'></span>" + text + "</a>";
        var plantilla = '<li id=' + id + '>' + boton + '</li>';
        document.getElementById('tags-selecc').innerHTML+=plantilla;
        actualizar_almacen_tags();
    }
}

function remove_tag(id) {
     $("#" + id).remove();
     actualizar_almacen_tags();
}

function actualizar_almacen_tags() {
    var almacen = document.getElementById("almacen_tags");
    var listItems = $("#tags-selecc li");

    almacen.value=""; //limpiamos el almacen
    listItems.each(function(idx, li)
    {
        var tag_id = $(li).attr('id'); //obtenemos el id del tag
        almacen.value+=tag_id + ";"; //los vamos guardando y separando con ;
        console.log("almacen: "+almacen.value);
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

function borrar_tag_bd(idusuario) {
    var id = document.getElementById('sel2').value;
    var text = $("#sel2 option:selected").text();

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
