package com.empresa.backend;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.empresa.server.*;

import java.util.List;



@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    @GetMapping(value = "/repuestos/{sku}", params = "ubicacion")
    public ResponseEntity<Repuesto> obtenerRepuestoEnUbicacion(
            @PathVariable int sku,
            @RequestParam("ubicacion") int idUbicacion) {

        Repuesto rep = Database.obtenerRepuestoPorUbicacion(idUbicacion, sku);
        if (rep == null) {
            throw new ResourceNotFoundException(String.format(
                "No existe el repuesto SKU %d en la ubicación %d", sku, idUbicacion));
        }
        return ResponseEntity.ok(rep);
    }
    // Repuestos
    @GetMapping("/repuestos")
    public List<Repuesto> obtenerTodosRepuestos() {
        return Database.obtenerTodosRepuestos();
    }
    @GetMapping("/reservas/{idReserva}")
    public ResponseEntity<Reserva> obtenerReservaPorId(@PathVariable int idReserva) {
        Reserva r = Database.obtenerReservaPorId(idReserva);
        if (r == null) {
            throw new ResourceNotFoundException("Reserva no encontrada");
        }
        return ResponseEntity.ok(r);
    }

    @GetMapping("/repuestos/{sku}")
    public Repuesto obtenerRepuestoPorSku(@PathVariable int sku) {
        Repuesto repuesto = Database.obtenerRepuestoPorSku(sku);
        if(repuesto == null) throw new ResourceNotFoundException("Repuesto no encontrado");
        return repuesto;
    }

    @PostMapping("/repuestos")
    public void crearRepuesto(@RequestBody RepuestoRequest request) {
        Database.insertarRepuesto(
            request.getIdUbicacion(),
            request.getSku(),
            request.getCantidad(),
            request.getPrecio(),
            request.getCategoria(),
            request.isDisponible(),
            request.getNombre()
        );
    }

    // Reservas
    @GetMapping("/reservas")
    public List<Reserva> obtenerTodasReservas() {
        return Database.obtenerTodasReservas();
    }

    @PostMapping("/reservas")
    public void crearReserva(@RequestBody ReservaRequest request) {
        Database.insertarReserva(
            request.getIdVehiculo(),
            request.getSku(),
            request.getCantidad()
        );
    }

    // Ubicaciones
    @GetMapping("/ubicaciones/{id}/stock")
    public int consultarStockUbicacion(@PathVariable int id) {
        return Database.obtenerStockTotalPorUbicacion(id);
    }
    // En com.empresa.backend.InventarioController

    // Obtener los datos completos de una ubicación
    @GetMapping("/ubicaciones/{id}")
    public ResponseEntity<Ubicacion> obtenerUbicacionPorId(@PathVariable int id) {
        Ubicacion ub = Database.obtenerUbicacionPorId(id);
        if (ub == null) {
            throw new ResourceNotFoundException("Ubicación no encontrada");
        }
        return ResponseEntity.ok(ub);
    }

    // Vehículos
    @GetMapping("/vehiculos/{id}")
    public Vehiculo obtenerVehiculo(@PathVariable int id) {
        Vehiculo vehiculo = Database.obtenerVehiculoPorId(id);
        if(vehiculo == null) throw new ResourceNotFoundException("Vehículo no encontrado");
        return vehiculo;
    }

    @PutMapping("/repuestos/{idUbicacion}/{sku}/liberar")
    public ResponseEntity<Void> liberarRepuesto(
            @PathVariable int idUbicacion,
            @PathVariable int sku,
            @RequestParam int cantidad) {
        Database.actualizarStock(idUbicacion, sku, -cantidad);
        return ResponseEntity.noContent().build();
    }

    // Eliminar (liberar) reserva
    @DeleteMapping("/reservas/{idReserva}")
    public ResponseEntity<Void> eliminarReserva(@PathVariable int idReserva) {
        Database.eliminarReserva(idReserva);
        return ResponseEntity.noContent().build();
    }

    public static class RepuestoRequest {
        private int idUbicacion;
        private int sku;
        private int cantidad;
        private int precio;
        private String categoria;
        private boolean disponible;
        private String nombre;

        
        public int getIdUbicacion() { return idUbicacion; }
        public void setIdUbicacion(int idUbicacion) { this.idUbicacion = idUbicacion; }

        public int getSku() { return sku; }
        public void setSku(int sku) { this.sku = sku; }

        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }

        public int getPrecio() { return precio; }
        public void setPrecio(int precio) { this.precio = precio; }

        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }

        public boolean isDisponible() { return disponible; }
        public void setDisponible(boolean disponible) { this.disponible = disponible; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
    }

    public static class ReservaRequest {
        private int idVehiculo;
        private int sku;
        private int cantidad;

        
        public int getIdVehiculo() { return idVehiculo; }
        public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }

        public int getSku() { return sku; }
        public void setSku(int sku) { this.sku = sku; }

        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    }

    
    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Recurso no encontrado")
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error: " + ex.getMessage());
    }
}