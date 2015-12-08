package models;

import javax.persistence.*;
import play.data.validation.Constraints;
import play.data.format.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Entity
public class Etiqueta {
        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
    	public Integer id;
        @ManyToMany(cascade = CascadeType.ALL)
        @JoinTable(name = "tareas-etiquetas")
        public List<Tarea> tareas;

        @Constraints.Required //no nula
        @Column(unique=true) //unica
        public String nombre; //el nombre no podra ser nulo

        public Etiqueta() {}

        public Etiqueta(String nombre) {
            this.nombre = nombre;
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

            if(nombre.equals(otraEtiqueta.nombre))
                return true;
            else
                if (id != null && otraEtiqueta.id != null) return (id == otraEtiqueta.id);
            return false;
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result +
                ((id == null) ? 0 : id);
            result = prime * result +
                ((nombre == null) ? 0 : nombre.hashCode());
            return result;
        }
}
