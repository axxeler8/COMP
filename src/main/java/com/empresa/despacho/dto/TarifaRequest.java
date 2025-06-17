package com.empresa.despacho.dto;

import java.io.Serializable;

public class TarifaRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    public int codigoCiudadOrigen;
    public int codigoCiudadDestino;
    public int codigoAgenciaDestino = 0;
    public int codigoAgenciaOrigen = 0;
    public double alto;
    public double ancho;
    public double largo;
    public double kilos;
    public String cuentaCorriente = "";
    public String cuentaCorrienteDV = "";
    public String rutCliente = "1";
}