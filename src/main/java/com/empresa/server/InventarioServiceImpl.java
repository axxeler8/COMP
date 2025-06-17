package com.empresa.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import com.empresa.despacho.dto.CiudadDTO;
import com.empresa.despacho.dto.TarifaRequest;
import com.empresa.despacho.dto.TarifaResponse;

public class InventarioServiceImpl extends UnicastRemoteObject implements InventarioService {

    private static final String BASE_URL = "http://localhost:8080/api/inventario";
    private static final String STARKEN_API_BASE = "https://restservices-qa.starken.cl/apiqa/starkenservices/rest/";
    private static final String RUT_HEADER = "76211240";
    private static final String CLAVE_HEADER = "key";
    private final RestTemplate restTemplate;
    private final Lock mutex = new ReentrantLock();

    public InventarioServiceImpl() throws RemoteException {
        super();
        this.restTemplate = new RestTemplate();
    }

    @FunctionalInterface
    private interface OperationWithReturn<T> {
        T execute() throws RemoteException;
    }

    @FunctionalInterface
    private interface OperationWithoutReturn {
        void execute() throws RemoteException;
    }

    private <T> T executeWithMutexReturn(OperationWithReturn<T> operation) throws RemoteException {
        mutex.lock();
        try {
            return operation.execute();
        } finally {
            mutex.unlock();
        }
    }

    private void executeWithMutex(OperationWithoutReturn operation) throws RemoteException {
        mutex.lock();
        try {
            operation.execute();
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public List<Ubicacion> verUbicaciones() throws RemoteException {
        return executeWithMutexReturn(() -> {
            try {
                return restTemplate.exchange(
                    BASE_URL + "/ubicaciones",
                    HttpMethod.GET,
                    null,
                    new org.springframework.core.ParameterizedTypeReference<List<Ubicacion>>() {}
                ).getBody();
            } catch (Exception e) {
                throw new RemoteException("Error al obtener ubicaciones: " + e.getMessage());
            }
        });
    }

    @Override
    public List<Repuesto> verRepuestos() throws RemoteException {
        return executeWithMutexReturn(() -> {
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
        });
    }

    @Override
    public Repuesto consultarRepuesto(int sku) throws RemoteException {
        return executeWithMutexReturn(() -> {
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
        });
    }

    @Override
    public void agregarRepuesto(int idUbicacion, int sku, int cantidad, 
                                int precio, String categoria, boolean disponible, String nombre) throws RemoteException {
        executeWithMutex(() -> {
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
        });
    }

    @Override
    public void liberarRepuesto(int idUbicacion, int sku, int cantidad) throws RemoteException {
        executeWithMutex(() -> {
            try {
                restTemplate.put(
                    BASE_URL + "/repuestos/" + idUbicacion + "/" + sku + "/liberar?cantidad=" + cantidad,
                    null
                );
            } catch (Exception e) {
                throw new RemoteException("Error al liberar stock: " + e.getMessage());
            }
        });
    }

    @Override
    public List<Reserva> verReservas() throws RemoteException {
        return executeWithMutexReturn(() -> {
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
        });
    }

    @Override
    public Reserva consultarReserva(int idReserva) throws RemoteException {
        return executeWithMutexReturn(() -> {
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
        });
    }

    @Override
    public void agregarReserva(int idVehiculo, int sku, int cantidad) throws RemoteException {
        executeWithMutex(() -> {
            try {
                ReservaRequest request = new ReservaRequest();
                request.idVehiculo = idVehiculo;
                request.sku = sku;
                request.cantidad = cantidad;
                restTemplate.postForEntity(BASE_URL + "/reservas", request, Void.class);
            } catch (Exception e) {
                throw new RemoteException("Error al crear reserva: " + e.getMessage());
            }
        });
    }

    @Override
    public void liberarReserva(int idReserva) throws RemoteException {
        executeWithMutex(() -> {
            try {
                restTemplate.delete(BASE_URL + "/reservas/" + idReserva);
            } catch (HttpClientErrorException.NotFound nf) {
                throw new RemoteException("Reserva no encontrada.");
            } catch (Exception e) {
                throw new RemoteException("Error al liberar reserva: " + e.getMessage());
            }
        });
    }

    @Override
    public Ubicacion consultarUbicacion(int idUbicacion) throws RemoteException {
        return executeWithMutexReturn(() -> {
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
        });
    }

    @Override
    public int consultarStockUbicacion(int idUbicacion) throws RemoteException {
        return executeWithMutexReturn(() -> {
            try {
                ResponseEntity<Integer> response = restTemplate.getForEntity(
                    BASE_URL + "/ubicaciones/" + idUbicacion + "/stock",
                    Integer.class
                );
                return response.getBody();
            } catch (Exception e) {
                throw new RemoteException("Error al consultar stock: " + e.getMessage());
            }
        });
    }

    @Override
    public Vehiculo consultarVehiculo(int idVehiculo) throws RemoteException {
        return executeWithMutexReturn(() -> {
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
        });
    }

    @Override
    public Repuesto consultarRepuestoEnUbicacion(int idUbicacion, int sku) throws RemoteException {
        return executeWithMutexReturn(() -> {
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
        });
    }

    private List<CiudadDTO> getCiudades(String endpoint) throws RemoteException {
        try {
            String url = STARKEN_API_BASE + endpoint;
            HttpHeaders headers = new HttpHeaders();
            headers.set("rut", RUT_HEADER);
            headers.set("clave", CLAVE_HEADER);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
            );
            Map<String, Object> body = response.getBody();
            if (body == null) return List.of();
            String key = endpoint.contains("Destino") ? "listaCiudadesDestino" : "listaCiudadesOrigen";
            List<Map<String, Object>> lista = (List<Map<String, Object>>) body.get(key);
            List<CiudadDTO> ciudades = new java.util.ArrayList<>();
            if (lista != null) {
                for (Map<String, Object> ciudadMap : lista) {
                    CiudadDTO ciudad = new CiudadDTO();
                    ciudad.codigoCiudad = (int) ciudadMap.getOrDefault("codigoCiudad", 0);
                    ciudad.codigoRegion = (int) ciudadMap.getOrDefault("codigoRegion", 0);
                    ciudad.codigoZonaGeografica = (int) ciudadMap.getOrDefault("codigoZonaGeografica", 0);
                    ciudad.nombreCiudad = (String) ciudadMap.getOrDefault("nombreCiudad", "");
                    ciudades.add(ciudad);
                }
            }
            return ciudades;
        } catch (Exception e) {
            throw new RemoteException("Error obteniendo ciudades desde Starken", e);
        }
    }

    @Override
    public List<CiudadDTO> listarCiudadesOrigen() throws RemoteException {
        return getCiudades("listarCiudadesOrigen");
    }

    @Override
    public List<CiudadDTO> listarCiudadesDestino() throws RemoteException {
        return getCiudades("listarCiudadesDestino");
    }

    @Override
    public TarifaResponse consultarTarifas(TarifaRequest req) throws RemoteException {
        try {
            String url = STARKEN_API_BASE + "consultarTarifas";
            HttpHeaders headers = new HttpHeaders();
            headers.set("rut", RUT_HEADER);
            headers.set("clave", CLAVE_HEADER);
            HttpEntity<TarifaRequest> entity = new HttpEntity<>(req, headers);
            ResponseEntity<TarifaResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                TarifaResponse.class
            );
            TarifaResponse body = response.getBody();
            if (body == null) {
                TarifaResponse error = new TarifaResponse();
                error.codigoRespuesta = -1;
                error.mensajeRespuesta = "Respuesta vacía de la API";
                error.listaTarifas = java.util.Collections.emptyList();
                return error;
            }
            return body;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    TarifaResponse error = mapper.readValue(responseBody, TarifaResponse.class);
                    return error;
                } catch (Exception ex) {
                    TarifaResponse error = new TarifaResponse();
                    error.codigoRespuesta = -1;
                    error.mensajeRespuesta = "Error al parsear respuesta de error: " + responseBody;
                    error.listaTarifas = java.util.Collections.emptyList();
                    return error;
                }
            } else {
                TarifaResponse error = new TarifaResponse();
                error.codigoRespuesta = -1;
                error.mensajeRespuesta = "Error HTTP: " + e.getStatusCode();
                error.listaTarifas = java.util.Collections.emptyList();
                return error;
            }
        } catch (Exception e) {
            TarifaResponse error = new TarifaResponse();
            error.codigoRespuesta = -1;
            error.mensajeRespuesta = "Error inesperado: " + e.getMessage();
            error.listaTarifas = java.util.Collections.emptyList();
            return error;
        }
    }

    public void heartbeat() throws RemoteException {
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
