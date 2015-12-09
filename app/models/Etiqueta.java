package models;

import javax.persistence.*;
import play.data.validation.Constraints;
import play.data.format.*;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Entity
public class Etiqueta {
        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
    	public Integer id;

        @Constraints.Required //no nula
        //@Column(unique=true) //unica
        public String nombre; //el nombre no podra ser nulo

        @ManyToOne
        @JoinColumn(name="usuarioId")
        public Usuario usuario;

        public Etiqueta() {}

        public Etiqueta(String nombre,Usuario usuario) {
            this.nombre = nombre;
            this.usuario=usuario;
        }

        @Override public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            Etiqueta otraEtiqueta = (Etiqueta) obj;

            //el nombre de una etiqueta debe ser UNICO, así que si
            //hay dos etiquetas con el mismo nombre, se consideran identicas.

            if (id != null && otraEtiqueta.id != null) return (id == otraEtiqueta.id);
            else return (nombre.equals(otraEtiqueta.nombre)) &&
                        (usuario.equals(otraEtiqueta.usuario));
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result +
                ((id == null) ? 0 : id);
            result = prime * result +
                ((nombre == null) ? 0 : nombre.hashCode());
            result = prime * result +
                ((usuario == null) ? 0 : usuario.hashCode());
            return result;
        }

        public String toString() {
            return String.format("Etiqueta id: %s nombre: %s",
                id,nombre);
        }
}
