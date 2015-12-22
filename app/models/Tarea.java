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

        @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
        @JoinTable(name="tarea_etiqueta", joinColumns=@JoinColumn(name="tarea_id"), inverseJoinColumns=@JoinColumn(name="etiqueta_id"))
        public List<Etiqueta> etiquetas;

        public String descripcion;
        @Constraints.Required
        public String estado;

        public String anotacion;

        public Integer prioridad;

        public Tarea() {}

        public Tarea(String descripcion, Usuario usuario) {
            this.descripcion = descripcion;
            this.usuario = usuario;
            //Por defecto el estado es pendiente
            this.estado = "pendiente";
            this.prioridad = 3;

        }

        public Tarea(String descripcion, Usuario usuario,List<Etiqueta> etiquetas) {
            this.descripcion = descripcion;
            this.usuario = usuario;
            this.etiquetas = etiquetas;
            this.estado = "pendiente";
            this.prioridad = 3;
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
                        (estado.equals(otraTarea.estado)) &&
                        (prioridad.equals(otraTarea.prioridad));
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
            result = prime * result +
                ((anotacion == null) ? 0 : anotacion.hashCode());
            result = prime * result +
                ((prioridad == null) ? 0 : prioridad.hashCode());
            return result;
        }

        //Sustituye por null las cadenas vacias que pueda tener
        //una tarea en sus atributos
        public void nulificaAtributos() {
            if(descripcion!=null && descripcion.isEmpty()) descripcion=null;
            if(anotacion!=null && anotacion.isEmpty()) anotacion=null;
            if (estado != null && estado.isEmpty()) estado = "pendiente";
            if (prioridad != null && (prioridad<1 || prioridad > 3)) prioridad = 3;
        }

        public String toString() {
            String cadena = String.format("Tarea id: %s descripcion: %s UsuarioId: %s estado: %s anotacion %s prioridad: %s",
                id,descripcion,usuario.id,estado, anotacion, prioridad);
            cadena+=" \nEtiquetas:\n";
            for(Etiqueta e:etiquetas)
                cadena+=e + "\n";
            return cadena;
        }
}
