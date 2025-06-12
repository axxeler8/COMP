package com.empresa.server;

import java.io.Serializable;

public class Ubicacion implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idUbicacion;
    private String nombre;
    private String direccion;
    private int capacidad;

   
    public Ubicacion() {
    }

    public Ubicacion(int idUbicacion, String nombre, String direccion, int capacidad) {
        this.idUbicacion = idUbicacion;
        this.nombre      = nombre;
        this.direccion   = direccion;
        this.capacidad   = capacidad;
    }

    public int getIdUbicacion() { return idUbicacion; }
    public String getNombre()    { return nombre;      }
    public String getDireccion() { return direccion;   }
    public int getCapacidad()    { return capacidad;   }
}
