package models;

import play.*;
import play.mvc.*;
import play.db.jpa.*;
import java.util.List;
import javax.persistence.EntityNotFoundException;

public class EtiquetaService {
    public static Etiqueta grabaEtiqueta(Etiqueta etiqueta) {
        return EtiquetaDAO.create(etiqueta);
    }
}
