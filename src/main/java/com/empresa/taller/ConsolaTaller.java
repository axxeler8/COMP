package com.empresa.taller;

import com.empresa.server.InventarioService;
import com.empresa.server.Repuesto;
import com.empresa.server.Reserva;
import com.empresa.server.Ubicacion;
import com.empresa.server.Vehiculo;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class ConsolaTaller {
    private static InventarioService stub;
    private final String host = "localhost";
    private final int primaryPort = 1099;
    private final int backupPort  = 1100;
    private boolean connectedToPrimary = true;
    private volatile boolean running = true;

    public static void main(String[] args) {
        try {
            new ConsolaTaller().start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void start() throws Exception {
        conectarSvPrincipal();
        startHeartbeat();
        atenderConsola();
    }

    private void conectarSvPrincipal() throws Exception {
        stub = establecerConexion(host, primaryPort, "InventarioService");
        if (stub == null) {
            System.out.println("No hay principal, intentando respaldo...");
            cambiarSvRespaldo();
        } else {
            connectedToPrimary = true;
            System.out.println("✔ Conectado al servidor principal.");
        }
        if (stub == null) {
            throw new RemoteException("No se pudo conectar a ningún servidor.");
        }
    }

    private void cambiarSvRespaldo() {
        InventarioService backup = establecerConexion(host, backupPort, "InventarioService");
        if (backup == null) {
            System.err.println("No se pudo conectar al servidor de respaldo.");
        } else {
            stub = backup;
            connectedToPrimary = false;
            System.out.println("✔ Conectado al servidor de respaldo.");
        }
    }

    private InventarioService establecerConexion(String host, int port, String bindingName) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            return (InventarioService) registry.lookup(bindingName);
        } catch (Exception e) {
            System.err.println("Fallo conectando a " + bindingName + " en " + port + ": " + e.getMessage());
            return null;
        }
    }

    private void startHeartbeat() {
        Thread hb = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                    stub.heartbeat();
                } catch (RemoteException e) {
                    System.err.println("Heartbeat fallido (" + (connectedToPrimary ? "principal" : "respaldo") + ")");
                    if (connectedToPrimary) {
                        cambiarSvRespaldo();
                    } else {
                        System.err.println("Ambos servidores inaccesibles, deteniendo heartbeat.");
                        System.exit(1); 
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        hb.setDaemon(true);
        hb.start();
    }

    private void atenderConsola() throws Exception {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Consola Taller ---");
            System.out.println("1) Ver repuestos");
            System.out.println("2) Consultar repuesto por SKU");
            System.out.println("3) Agregar repuesto");
            System.out.println("4) Liberar repuesto");
            System.out.println("5) Ver reservas");
            System.out.println("6) Consultar reserva por ID");
            System.out.println("7) Agregar reserva");
            System.out.println("8) Liberar reserva");
            System.out.println("9) Salir");
            int op = leerEntero(sc, "Selecciona una opción: ", 1, 9);

            try {
                switch (op) {
                    case 1: mostrarRepuestos();           break;
                    case 2: consultarRepuestoPorSku(sc);  break;
                    case 3: agregarRepuesto(sc);          break;
                    case 4: liberarRepuesto(sc);          break;
                    case 5: mostrarReservas();            break;
                    case 6: consultarReservaPorId(sc);    break;
                    case 7: agregarReserva(sc);           break;
                    case 8: liberarReserva(sc);           break;
                    case 9: System.out.println("Saliendo..."); sc.close(); System.exit(0);
                }
            } catch (RemoteException e) {
                System.err.println("Error en operación RMI: " + e.getMessage());
                if (connectedToPrimary) {
                    cambiarSvRespaldo();
                }
            }
        }
    }

    private static int leerEntero(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int val = Integer.parseInt(sc.nextLine().trim());
                if (val < min || val > max) {
                    System.out.printf("⚠ Debe ser entre %d y %d.%n", min, max);
                } else {
                    return val;
                }
            } catch (NumberFormatException e) {
                System.out.println("⚠ Formato inválido.");
            }
        }
    }

    private static int leerEntero(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("⚠ Formato inválido.");
            }
        }
    }

    private void mostrarRepuestos() throws RemoteException {
        List<Repuesto> list = stub.verRepuestos();
        System.out.println("\n-- Lista de Repuestos --");
        list.forEach(r ->
            System.out.printf("[SKU %d] %s | Cant: %d | Precio: %d | Cat: %s | Disp: %b%n",
                r.getSku(), r.getNombre(), r.getCantidad(), r.getPrecio(), r.getCategoria(), r.isDisponible())
        );
    }

    private void consultarRepuestoPorSku(Scanner sc) throws RemoteException {
        Repuesto r;
        do {
            int sku = leerEntero(sc, "\nSKU: ");
            r = stub.consultarRepuesto(sku);
            if (r == null) System.out.println("No existe ese SKU.");
        } while (r == null);
        System.out.printf("Detalles: SKU=%d, Nombre=%s, Cant=%d, Precio=%d, Cat=%s, Disp=%b%n",
            r.getSku(), r.getNombre(), r.getCantidad(), r.getPrecio(), r.getCategoria(), r.isDisponible());
    }

    private void agregarRepuesto(Scanner sc) throws RemoteException {
        System.out.println("\n-- Agregar Repuesto --");
        int idUb;
        while (true) {
            idUb = leerEntero(sc, "ID de Ubicación: ");
            if (stub.consultarUbicacion(idUb) == null) {
                System.out.println("⚠ Ubicación inexistente.");
            } else break;
        }
        Ubicacion ub = stub.consultarUbicacion(idUb);

        int sku;
        do {
            sku = leerEntero(sc, "SKU: ");
            if (stub.consultarRepuesto(sku) != null)
                System.out.println("⚠ SKU ya existe.");
            else break;
        } while (true);

        int cantidad;
        do {
            cantidad = leerEntero(sc, "Cantidad: ");
            int stock = stub.consultarStockUbicacion(idUb);
            if (cantidad < 1)
                System.out.println("⚠ Mínimo 1.");
            else if (stock + cantidad > ub.getCapacidad())
                System.out.printf("⚠ Excede capacidad (%d/%d).%n", stock, ub.getCapacidad());
            else break;
        } while (true);

        int precio;
        do {
            precio = leerEntero(sc, "Precio: ");
            if (precio < 0) System.out.println("⚠ No negativo.");
            else break;
        } while (true);

        System.out.print("Categoría: ");
        String categoria = sc.nextLine().trim();
        boolean disponible = leerEntero(sc, "Disponible (1=si/0=no): ", 0, 1) == 1;
        System.out.print("Nombre: ");
        String nombre = sc.nextLine().trim();

        stub.agregarRepuesto(idUb, sku, cantidad, precio, categoria, disponible, nombre);
        System.out.println("✔ Repuesto agregado.");
    }

    private void liberarRepuesto(Scanner sc) throws RemoteException {
        System.out.println("\n-- Liberar Repuesto --");
        int idUb;
        do {
            idUb = leerEntero(sc, "ID de Ubicación: ");
            if (stub.consultarUbicacion(idUb) == null)
                System.out.println("⚠ Ubicación inexistente.");
            else break;
        } while (true);

        Repuesto rep;
        int sku;
        do {
            sku = leerEntero(sc, "SKU: ");
            rep = stub.consultarRepuestoEnUbicacion(idUb, sku);
            if (rep == null)
                System.out.println("⚠ SKU no existe en ubicación.");
            else break;
        } while (true);

        int cantidad;
        do {
            cantidad = leerEntero(sc, String.format("Cantidad a liberar (máx %d): ", rep.getCantidad()));
            if (cantidad < 1 || cantidad > rep.getCantidad())
                System.out.println("⚠ Fuera de rango.");
            else {
                stub.liberarRepuesto(idUb, sku, cantidad);
                System.out.println("✔ Liberado.");
                break;
            }
        } while (true);
    }

    private void mostrarReservas() throws RemoteException {
        List<Reserva> lista = stub.verReservas();
        System.out.println("\n-- Lista de Reservas --");
        lista.forEach(r ->
            System.out.printf("[Res %d] Vehículo=%d | SKU=%d | Cant=%d%n",
                r.getIdReserva(), r.getIdVehiculo(), r.getSku(), r.getCantidad())
        );
    }

    private void consultarReservaPorId(Scanner sc) throws RemoteException {
        Reserva r;
        do {
            int id = leerEntero(sc, "\nID Reserva: ");
            r = stub.consultarReserva(id);
            if (r == null) System.out.println("No existe.");
        } while (r == null);
        System.out.printf("Res %d: Veh=%d, SKU=%d, Cant=%d%n",
            r.getIdReserva(), r.getIdVehiculo(), r.getSku(), r.getCantidad());
    }

    private void agregarReserva(Scanner sc) throws RemoteException {
        System.out.println("\n-- Agregar Reserva --");
        int idVeh;
        do {
            idVeh = leerEntero(sc, "ID Vehículo: ");
            if (stub.consultarVehiculo(idVeh) == null)
                System.out.println("⚠ Vehículo no encontrado.");
            else break;
        } while (true);

        Repuesto rep;
        int sku;
        do {
            sku = leerEntero(sc, "SKU: ");
            rep = stub.consultarRepuesto(sku);
            if (rep == null) System.out.println("⚠ SKU no existe.");
            else if (rep.getCantidad() < 1) {
                System.out.println("⚠ Sin stock."); return;
            } else break;
        } while (true);

        int cantidad;
        do {
            cantidad = leerEntero(sc, String.format("Cantidad (máx %d): ", rep.getCantidad()));
            if (cantidad < 1 || cantidad > rep.getCantidad())
                System.out.println("⚠ Fuera de rango.");
            else break;
        } while (true);

        stub.agregarReserva(idVeh, sku, cantidad);
        System.out.println("✔ Reserva creada.");
    }

    private void liberarReserva(Scanner sc) throws RemoteException {
        System.out.println("\n-- Liberar Reserva --");
        int idRes;
        do {
            idRes = leerEntero(sc, "ID Reserva: ");
            if (idRes < 0) System.out.println("⚠ No negativo.");
            else if (stub.consultarReserva(idRes) == null)
                System.out.println("⚠ No existe.");
            else break;
        } while (true);

        stub.liberarReserva(idRes);
        System.out.println("✔ Reserva liberada.");
    }
}
