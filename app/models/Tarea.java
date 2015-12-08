package models;

import javax.persistence.*;
import play.data.validation.Constraints;
import play.data.format.*;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Entity
public class Tarea {
        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
    	  public Integer id;
        @ManyToOne
        @JoinColumn(name="usuarioId")
        public Usuario usuario;
        @ManyToMany(cascade = CascadeType.ALL)
        @JoinTable(name = "tagtask")
        public List<Etiqueta> etiquetas;

        public String descripcion;
        @Constraints.Required
        public String estado;

        public Tarea() {}

        public Tarea(String descripcion, Usuario usuario) {
            this.descripcion = descripcion;
            this.usuario = usuario;
            //Por defecto el estado es pendiente
            this.estado = "pendiente";
        }

        @Override public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            Tarea otraTarea = (Tarea) obj;

            // Si las dos tareas tienen id (ya se han grabado en la base
            // de datos) comparamos los id. En otro caso, comparamos los
            // atributos no nulos.

            if (id != null && otraTarea.id != null) return (id == otraTarea.id);
            else return (descripcion.equals(otraTarea.descripcion)) &&
                        (usuario.equals(otraTarea.usuario)) &&
                        (estado.equals(otraTarea.estado));
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result +
                ((id == null) ? 0 : id);
            result = prime * result +
                ((descripcion == null) ? 0 : descripcion.hashCode());
            result = prime * result +
                ((estado == null) ? 0 : estado.hashCode());
            return result;
        }

        //Sustituye por null las cadenas vacias que pueda tener
        //una tarea en sus atributos
        public void nulificaAtributos() {
            if(descripcion!=null && descripcion.isEmpty()) descripcion=null;
            if (estado != null && estado.isEmpty()) estado = "pendiente";
        }

        public String toString() {
            return String.format("Tarea id: %s descripcion: %s UsuarioId: %s estado: %s",
                id,descripcion,usuario.id,estado);
        }
}
