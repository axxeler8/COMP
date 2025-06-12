package com.empresa.server;

import java.io.Serializable;

public class Vehiculo implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idVehiculo;
    private int anio;
    private int idUbicacion;
    private String nombre;
    private String modelo;
    private String cilindraje;
    private String color;


    public Vehiculo() {
    }

    public Vehiculo(int idVehiculo, int anio, int idUbicacion, String nombre, String modelo, String cilindraje, String color) {
        this.idVehiculo  = idVehiculo;
        this.anio        = anio;
        this.idUbicacion = idUbicacion;
        this.nombre      = nombre;
        this.modelo      = modelo;
        this.cilindraje  = cilindraje;
        this.color       = color;
    }

    public int getIdVehiculo()    { return idVehiculo; }
    public int getAnio()          { return anio;       }
    public int getIdUbicacion()   { return idUbicacion;}
    public String getNombre()     { return nombre;    }
    public String getModelo()     { return modelo;    }
    public String getCilindraje() { return cilindraje;}
    public String getColor()      { return color;     }
}
