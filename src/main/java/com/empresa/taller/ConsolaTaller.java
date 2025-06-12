package com.empresa.taller;

import com.empresa.server.InventarioService;
import com.empresa.server.Repuesto;
import com.empresa.server.Reserva;
import com.empresa.server.Ubicacion;
import com.empresa.server.Vehiculo;

import java.rmi.Naming;
import java.util.List;
import java.util.Scanner;

public class ConsolaTaller {
    public static void main(String[] args) throws Exception {
        InventarioService svc = (InventarioService) Naming.lookup("rmi://localhost:1099/InventarioService");
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

            switch (op) {
                case 1: mostrarRepuestos(svc); break;
                case 2: consultarRepuestoPorSku(svc, sc); break;
                case 3: agregarRepuesto(svc, sc); break;
                case 4: liberarRepuesto(svc, sc); break;
                case 5: mostrarReservas(svc); break;
                case 6: consultarReservaPorId(svc, sc); break;
                case 7: agregarReserva(svc, sc); break;
                case 8: liberarReserva(svc, sc); break;
                case 9:
                    System.out.println("Saliendo...");
                    sc.close();
                    System.exit(0);
            }
        }
    }

    private static int leerEntero(Scanner sc, String prompt, int min, int max) {
        Integer val = null;
        while (val == null) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                int parsed = Integer.parseInt(line);
                if (parsed < min || parsed > max) {
                    System.out.printf("⚠ Ingresa un número entre %d y %d.%n", min, max);
                } else {
                    val = parsed;
                }
            } catch (NumberFormatException e) {
                System.out.println("⚠ Formato inválido. Ingresa un número entero.");
            }
        }
        return val;
    }

    private static int leerEntero(Scanner sc, String prompt) {
        Integer val = null;
        while (val == null) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                val = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("⚠ Formato inválido. Ingresa un número entero.");
            }
        }
        return val;
    }

    private static void mostrarRepuestos(InventarioService svc) throws Exception {
        List<Repuesto> list = svc.verRepuestos();
        System.out.println("\n-- Lista de Repuestos --");
        for (Repuesto r : list) {
            System.out.printf("[SKU %d] %s | Cant: %d | Precio: %d | Cat: %s | Disp: %b%n",
                r.getSku(), r.getNombre(), r.getCantidad(), r.getPrecio(), r.getCategoria(), r.isDisponible());
        }
    }

    private static void consultarRepuestoPorSku(InventarioService svc, Scanner sc) throws Exception {
        Repuesto r = null;
        while (r == null) {
            int sku = leerEntero(sc, "\nSKU: ");
            r = svc.consultarRepuesto(sku);
            if (r == null) System.out.println("No existe ese SKU. Intenta nuevamente.");
        }
        System.out.printf(
            "\nDetalles del Repuesto:\n" +
            "SKU: %d%n" +
            "Nombre: %s%n" +
            "Cantidad: %d%n" +
            "Precio: %d%n" +
            "Categoría: %s%n" +
            "Disponible: %b%n",
            r.getSku(), r.getNombre(), r.getCantidad(), r.getPrecio(), r.getCategoria(), r.isDisponible()
        );
    }

    private static void agregarRepuesto(InventarioService svc, Scanner sc) throws Exception {
        System.out.println("\n-- Agregar Repuesto --");
        Integer idUb = null;
        while (idUb == null) {
            int parsed = leerEntero(sc, "ID de Ubicación: ");
            if (svc.consultarUbicacion(parsed) == null) {
                System.out.println("⚠ Ubicación inexistente. Intenta de nuevo.");
            } else {
                idUb = parsed;
            }
        }
        Ubicacion ub = svc.consultarUbicacion(idUb);

        int sku;
        while (true) {
            sku = leerEntero(sc, "SKU: ");
            if (svc.consultarRepuesto(sku) != null) {
                System.out.println("⚠ SKU ya existe. Intenta otro.");
            } else {
                break;
            }
        }

        int cantidad;
        while (true) {
            cantidad = leerEntero(sc, "Cantidad: ");
            int stockActual = svc.consultarStockUbicacion(idUb);
            if (cantidad < 1) {
                System.out.println("⚠ La cantidad debe ser al menos 1. Intenta de nuevo.");
            } else if (stockActual + cantidad > ub.getCapacidad()) {
                System.out.printf("⚠ Excede capacidad. Actual: %d, Máxima: %d. Intenta de nuevo.%n",
                    stockActual, ub.getCapacidad());
            } else {
                break;
            }
        }

        int precio;
        while (true) {
            precio = leerEntero(sc, "Precio: ");
            if (precio < 0) System.out.println("⚠ El precio no puede ser negativo. Intenta de nuevo.");
            else break;
        }

        System.out.print("Categoría (texto): ");
        String categoria = sc.nextLine().trim();

        boolean disponible;
        while (true) {
            System.out.print("Disponible (1=si / 0=no): ");
            String in = sc.nextLine().trim();
            if ("1".equals(in)) { disponible = true; break; }
            if ("0".equals(in)) { disponible = false; break; }
            System.out.println("⚠ Responde 1 o 0. Intenta de nuevo.");
        }

        System.out.print("Nombre: ");
        String nombre = sc.nextLine().trim();

        svc.agregarRepuesto(idUb, sku, cantidad, precio, categoria, disponible, nombre);
        System.out.println("✔ Repuesto agregado.");
    }

    private static void liberarRepuesto(InventarioService svc, Scanner sc) throws Exception {
        System.out.println("\n-- Liberar Repuesto --");
        Integer idUb = null;
        while (idUb == null) {
            int parsed = leerEntero(sc, "ID de Ubicación: ");
            if (svc.consultarUbicacion(parsed) == null) {
                System.out.println("⚠ Ubicación inexistente. Intenta de nuevo.");
            } else {
                idUb = parsed;
            }
        }
        // ya validada existencia

        Repuesto rep;
        int sku;
        while (true) {
            sku = leerEntero(sc, "SKU: ");
            rep = svc.consultarRepuestoEnUbicacion(idUb, sku);
            if (rep == null) {
                System.out.printf("⚠ No existe el SKU %d en la ubicación %d. Intenta de nuevo.%n", sku, idUb);
            } else {
                break;
            }
        }

        int cantidad;
        while (true) {
            cantidad = leerEntero(sc, String.format("Cantidad a liberar (máx %d): ", rep.getCantidad()));
            if (cantidad < 1 || cantidad > rep.getCantidad()) {
                System.out.printf("⚠ Cantidad inválida. Debe estar entre 1 y %d. Intenta de nuevo.%n", rep.getCantidad());
            } else {
                svc.liberarRepuesto(idUb, sku, cantidad);
                System.out.println("✔ Repuesto liberado.");
                break;
            }
        }
    }

    private static void mostrarReservas(InventarioService svc) throws Exception {
        List<Reserva> lista = svc.verReservas();
        System.out.println("\n-- Lista de Reservas --");
        for (Reserva r : lista) {
            System.out.printf("[Reserva %d] Vehículo ID: %d | SKU: %d | Cantidad: %d%n",
                r.getIdReserva(), r.getIdVehiculo(), r.getSku(), r.getCantidad());
        }
    }

    private static void consultarReservaPorId(InventarioService svc, Scanner sc) throws Exception {
        Reserva r = null;
        while (r == null) {
            int id = leerEntero(sc, "\nID de Reserva: ");
            r = svc.consultarReserva(id);
            if (r == null) System.out.println("No existe una reserva con ese ID. Intenta nuevamente.");
        }
        System.out.printf(
            "\nDetalles de la Reserva:\nID Reserva: %d%nVehículo ID: %d%nSKU: %d%nCantidad: %d%n",
            r.getIdReserva(), r.getIdVehiculo(), r.getSku(), r.getCantidad()
        );
    }

    private static void agregarReserva(InventarioService svc, Scanner sc) throws Exception {
        System.out.println("\n-- Agregar Reserva --");
        int idVeh;
        while (true) {
            idVeh = leerEntero(sc, "ID de Vehículo: ");
            if (svc.consultarVehiculo(idVeh) == null) System.out.println("⚠ Vehículo no encontrado. Intenta de nuevo.");
            else break;
        }

        Repuesto rep;
        int sku;
        while (true) {
            sku = leerEntero(sc, "SKU: ");
            rep = svc.consultarRepuesto(sku);
            if (rep == null) System.out.println("⚠ No existe el SKU " + sku + ". Intenta de nuevo.");
            else if (rep.getCantidad() < 1) {
                System.out.println("⚠ No hay stock disponible para el SKU " + sku + ".");
                return;
            } else break;
        }

        int cantidad;
        while (true) {
            cantidad = leerEntero(sc, String.format("Cantidad a reservar (máx %d): ", rep.getCantidad()));
            if (cantidad < 1 || cantidad > rep.getCantidad()) {
                System.out.printf("⚠ Cantidad inválida. Debe estar entre 1 y %d. Intenta de nuevo.%n", rep.getCantidad());
            } else break;
        }

        svc.agregarReserva(idVeh, sku, cantidad);
        System.out.println("✔ Reserva creada.");
    }

    private static void liberarReserva(InventarioService svc, Scanner sc) throws Exception {
        System.out.println("\n-- Liberar Reserva --");
        int idRes;
        while (true) {
            idRes = leerEntero(sc, "ID de Reserva: ");
            if (idRes < 0) {
                System.out.println("⚠ El ID no puede ser negativo. Intenta de nuevo.");
                continue;
            }
            Reserva r = svc.consultarReserva(idRes);
            if (r == null) {
                System.out.println("⚠ No existe una reserva con ese ID. Intenta de nuevo.");
            } else {
                break;
            }
        }

        svc.liberarReserva(idRes);
        System.out.println("✔ Reserva liberada.");
    }
}
