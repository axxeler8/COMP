package com.empresa.despacho.dto;

import java.util.List;

public class TarifaResponse {
    public String type;
    public int codigoRespuesta;
    public String mensajeRespuesta;
    public List<ListaTarifa> listaTarifas;

    public static class ListaTarifa {
        public double costoTotal;
        public int diasEntrega;
        public TipoEntrega tipoEntrega;
        public TipoServicio tipoServicio;
    }

    public static class TipoEntrega {
        public int codigoTipoEntrega;
        public String descripcionTipoEntrega;
    }

    public static class TipoServicio {
        public int codigoTipoServicio;
        public String descripcionTipoServicio;
    }
}