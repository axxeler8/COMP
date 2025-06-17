package com.empresa.despacho.dto;

import java.io.Serializable;
import java.util.List;

public class TarifaResponse implements Serializable {
 private static final long serialVersionUID = 1L;
 public String type;
 public int codigoRespuesta;
 public String mensajeRespuesta;
 public List<ListaTarifa> listaTarifas;

 public static class ListaTarifa implements Serializable {
  private static final long serialVersionUID = 1L;
  public double costoTotal;
  public int diasEntrega;
  public TipoEntrega tipoEntrega;
  public TipoServicio tipoServicio;
 }

 public static class TipoEntrega implements Serializable {
  private static final long serialVersionUID = 1L;
  public int codigoTipoEntrega;
  public String descripcionTipoEntrega;
 }

 public static class TipoServicio implements Serializable {
  private static final long serialVersionUID = 1L;
  public int codigoTipoServicio;
  public String descripcionTipoServicio;
 }
}