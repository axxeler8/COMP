package com.empresa.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface InventarioService extends Remote {
    List<Repuesto> verRepuestos() throws RemoteException;
    Repuesto consultarRepuesto(int sku) throws RemoteException;
    void agregarRepuesto(int idUbicacion, int sku, int cantidad, int precio, String categoria, boolean disponible, String nombre) throws RemoteException;
    void liberarRepuesto(int idUbicacion, int sku, int cantidad) throws RemoteException;

    List<Reserva> verReservas() throws RemoteException;
    Reserva consultarReserva(int idReserva) throws RemoteException;
    void agregarReserva(int idVehiculo, int sku, int cantidad) throws RemoteException;
    void liberarReserva(int idReserva) throws RemoteException;

    Ubicacion consultarUbicacion(int idUbicacion) throws RemoteException;
    int consultarStockUbicacion(int idUbicacion) throws RemoteException;
    Vehiculo consultarVehiculo(int idVehiculo) throws RemoteException;
    Repuesto consultarRepuestoEnUbicacion(int idUbicacion, int sku) throws RemoteException;
}
