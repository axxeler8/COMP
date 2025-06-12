package com.empresa.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;

public class InventarioServiceImpl extends UnicastRemoteObject implements InventarioService {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://localhost:8080/api/inventario";

    public InventarioServiceImpl() throws RemoteException {
        super();
        this.restTemplate = new RestTemplate();
    }

    @Override
    public List<Repuesto> verRepuestos() throws RemoteException {
        try {
            return restTemplate.exchange(
                BASE_URL + "/repuestos",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Repuesto>>() {}
            ).getBody();
        } catch (Exception e) {
            throw new RemoteException("Error al obtener repuestos: " + e.getMessage());
        }
    }

    @Override
    public Repuesto consultarRepuesto(int sku) throws RemoteException {
        try {
            return restTemplate.getForEntity(
                BASE_URL + "/repuestos/" + sku,
                Repuesto.class
            ).getBody();
        } catch (HttpClientErrorException.NotFound nf) {
            
            return null;
        } catch (Exception e) {
            throw new RemoteException("Error al consultar repuesto: " + e.getMessage());
        }
    }

    @Override
    public void agregarRepuesto(int idUbicacion, int sku, int cantidad, int precio, 
                              String categoria, boolean disponible, String nombre) throws RemoteException {
        try {
            RepuestoRequest request = new RepuestoRequest();
            request.idUbicacion = idUbicacion;
            request.sku = sku;
            request.cantidad = cantidad;
            request.precio = precio;
            request.categoria = categoria;
            request.disponible = disponible;
            request.nombre = nombre;
            
            restTemplate.postForEntity(BASE_URL + "/repuestos", request, Void.class);
        } catch (Exception e) {
            throw new RemoteException("Error al crear repuesto: " + e.getMessage());
        }
    }

    @Override
    public void liberarRepuesto(int idUbicacion, int sku, int cantidad) throws RemoteException {
        try {
            restTemplate.put(
                BASE_URL + "/repuestos/" + idUbicacion + "/" + sku + "/liberar?cantidad=" + cantidad,
                null
            );
        } catch (Exception e) {
            throw new RemoteException("Error al liberar stock: " + e.getMessage());
        }
    }

    @Override
    public List<Reserva> verReservas() throws RemoteException {
        try {
            return restTemplate.exchange(
                BASE_URL + "/reservas",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Reserva>>() {}
            ).getBody();
        } catch (Exception e) {
            throw new RemoteException("Error al obtener reservas: " + e.getMessage());
        }
    }

    @Override
    public Reserva consultarReserva(int idReserva) throws RemoteException {
        try {
            return restTemplate.getForEntity(
                BASE_URL + "/reservas/" + idReserva,
                Reserva.class
            ).getBody();
        } catch (HttpClientErrorException.NotFound nf) {
          
            return null;
        } catch (Exception e) {
            throw new RemoteException("Error al consultar reserva: " + e.getMessage());
        }
    }

    @Override
    public void agregarReserva(int idVehiculo, int sku, int cantidad) throws RemoteException {
        try {
            ReservaRequest request = new ReservaRequest();
            request.idVehiculo = idVehiculo;
            request.sku = sku;
            request.cantidad = cantidad;
            
            restTemplate.postForEntity(BASE_URL + "/reservas", request, Void.class);
        } catch (Exception e) {
            throw new RemoteException("Error al crear reserva: " + e.getMessage());
        }
    }

    @Override
    public void liberarReserva(int idReserva) throws RemoteException {
        try {
            Reserva res = restTemplate.getForObject(BASE_URL + "/reservas/" + idReserva, Reserva.class);
            if (res == null) {
                throw new RemoteException("Reserva no encontrada.");
            }
            restTemplate.delete(BASE_URL + "/reservas/" + idReserva);
        } catch (HttpClientErrorException.NotFound nf) {
            throw new RemoteException("Reserva no encontrada.");
        } catch (Exception e) {
            throw new RemoteException("Error al liberar reserva: " + e.getMessage());
        }
    }

    @Override
    public Ubicacion consultarUbicacion(int idUbicacion) throws RemoteException {
        try {
            ResponseEntity<Ubicacion> response = restTemplate.getForEntity(
                BASE_URL + "/ubicaciones/" + idUbicacion,
                Ubicacion.class
            );
            return response.getBody();

        } catch (HttpClientErrorException.NotFound nf) {
           
            return null;

        } catch (Exception e) {
            throw new RemoteException("Error al consultar ubicación: " + e.getMessage());
        }
    }

    @Override
    public int consultarStockUbicacion(int idUbicacion) throws RemoteException {
        try {
            ResponseEntity<Integer> response = restTemplate.getForEntity(
                BASE_URL + "/ubicaciones/" + idUbicacion + "/stock",
                Integer.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RemoteException("Error al consultar stock: " + e.getMessage());
        }
    }

    @Override
    public Vehiculo consultarVehiculo(int idVehiculo) throws RemoteException {
        try {
            ResponseEntity<Vehiculo> response = restTemplate.getForEntity(
                BASE_URL + "/vehiculos/" + idVehiculo,
                Vehiculo.class
            );
            return response.getBody();

        } catch (HttpClientErrorException.NotFound nf) {
            
            return null;

        } catch (Exception e) {
            throw new RemoteException("Error al consultar vehículo: " + e.getMessage());
        }
    }

    @Override
    public Repuesto consultarRepuestoEnUbicacion(int idUbicacion, int sku) throws RemoteException {
        try {
            return restTemplate.getForEntity(
                BASE_URL + "/repuestos/" + sku + "?ubicacion=" + idUbicacion,
                Repuesto.class
            ).getBody();
        } catch (HttpClientErrorException.NotFound nf) {
            return null;
        } catch (Exception e) {
            throw new RemoteException("Error al consultar repuesto en ubicación: " + e.getMessage());
        }
    }

  
    private static class RepuestoRequest {
        public int idUbicacion;
        public int sku;
        public int cantidad;
        public int precio;
        public String categoria;
        public boolean disponible;
        public String nombre;
    }

    private static class ReservaRequest {
        public int idVehiculo;
        public int sku;
        public int cantidad;
    }
}