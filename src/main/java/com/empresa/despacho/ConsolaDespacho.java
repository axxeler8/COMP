package com.empresa.despacho;

import com.empresa.server.InventarioService;
import com.empresa.server.Repuesto;
import com.empresa.despacho.dto.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

public class ConsolaDespacho {
    private static InventarioService stub;
    private final String host = "localhost";
    private final int primaryPort = 1099;
    private final int backupPort  = 1100;
    private boolean connectedToPrimary = true;
    private volatile boolean running = true;

    private static final String RUT_HEADER = "76211240";
    private static final String CLAVE_HEADER = "key";
    private static final String API_BASE = "https://restservices-qa.starken.cl/apiqa/starkenservices/rest/";

    public static void main(String[] args) {
        try {
            new ConsolaDespacho().start();
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
        RestTemplate rest = new RestTemplate();

        while (true) {
            System.out.println("\n--- Consola Despacho ---");
            System.out.println("1) Realizar despacho");
            System.out.println("2) Salir");
            int op = leerEntero(sc, "Selecciona una opción: ", 1, 2);

            try {
                switch (op) {
                    case 1: realizarDespacho(sc, rest); break;
                    case 2: System.out.println("Saliendo..."); sc.close(); System.exit(0);
                }
            } catch (RemoteException e) {
                System.err.println("Error en operación RMI: " + e.getMessage());
                if (connectedToPrimary) {
                    cambiarSvRespaldo();
                }
            }
        }
    }

    private void realizarDespacho(Scanner sc, RestTemplate rest) throws RemoteException {
        List<CiudadDTO> ciudadesOrigen = getCiudades(rest, "listarCiudadesOrigen");
        List<CiudadDTO> ciudadesDestino = getCiudades(rest, "listarCiudadesDestino");

        System.out.println("Ciudades de ORIGEN disponibles:");
        mostrarCiudades(ciudadesOrigen);
        int codOrigen = leerCodigoCiudad(sc, ciudadesOrigen, "Código ciudad ORIGEN: ");

        System.out.println("Ciudades de DESTINO disponibles:");
        mostrarCiudades(ciudadesDestino);
        int codDestino = leerCodigoCiudad(sc, ciudadesDestino, "Código ciudad DESTINO: ");

        System.out.println("Elige tipo de caja:");
        int i = 1;
        for (CajaTipo c : CajaTipo.values()) {
            System.out.printf("%d) %s (%dx%dx%d cm)\n", i++, c.name(), c.largo, c.ancho, c.alto);
        }
        int cajaIdx = leerEntero(sc, "Opción: ", 1, CajaTipo.values().length) - 1;
        CajaTipo caja = CajaTipo.values()[cajaIdx];

        Repuesto repuesto = null;
        while (repuesto == null) {
            int sku = leerEntero(sc, "SKU del repuesto: ");
            repuesto = stub.consultarRepuesto(sku);
            if (repuesto == null) System.out.println("SKU no encontrado.");
        }

        double peso;
        while (true) {
            System.out.print("Peso del repuesto (kg): ");
            try {
                peso = Double.parseDouble(sc.nextLine().trim());
                if (peso <= 0) throw new Exception();
                break;
            } catch (Exception e) {
                System.out.println("Peso inválido.");
            }
        }

        String tipoEntrega;
        int tipoEntregaCodigo;
        while (true) {
            System.out.print("Tipo de entrega (1=Agencia, 2=Domicilio): ");
            String op = sc.nextLine().trim();
            if ("1".equals(op)) { tipoEntrega = "AGENCIA"; tipoEntregaCodigo = 1; break; }
            if ("2".equals(op)) { tipoEntrega = "DOMICILIO"; tipoEntregaCodigo = 2; break; }
            System.out.println("Opción inválida.");
        }

        TarifaRequest req = new TarifaRequest();
        req.codigoCiudadOrigen = codOrigen;
        req.codigoCiudadDestino = codDestino;
        req.alto = caja.alto;
        req.ancho = caja.ancho;
        req.largo = caja.largo;
        req.kilos = peso;

        HttpHeaders headers = new HttpHeaders();
        headers.set("rut", RUT_HEADER);
        headers.set("clave", CLAVE_HEADER);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TarifaRequest> entity = new HttpEntity<>(req, headers);

        ResponseEntity<TarifaResponse> resp = rest.exchange(
            API_BASE + "consultarTarifas",
            HttpMethod.POST,
            entity,
            TarifaResponse.class
        );

        TarifaResponse tarifaResp = resp.getBody();
        if (tarifaResp == null || tarifaResp.listaTarifas == null) {
            System.out.println("No se pudo obtener tarifa.");
            return;
        }

        Optional<TarifaResponse.ListaTarifa> tarifaOpt = tarifaResp.listaTarifas.stream()
            .filter(t -> t.tipoEntrega.codigoTipoEntrega == tipoEntregaCodigo)
            .findFirst();

        if (!tarifaOpt.isPresent()) {
            System.out.println("No hay tarifa para el tipo de entrega seleccionado.");
            return;
        }

        double costoEnvio = tarifaOpt.get().costoTotal;
        double precioFinal = costoEnvio + repuesto.getPrecio();

        System.out.println("\n--- RESUMEN DE DESPACHO ---");
        System.out.printf("Caja: %s (%dx%dx%d cm)\n", caja.name(), caja.largo, caja.ancho, caja.alto);
        System.out.printf("Repuesto: %s (SKU %d)\n", repuesto.getNombre(), repuesto.getSku());
        System.out.printf("Peso: %.2f kg\n", peso);
        System.out.printf("Origen: %s\n", buscarCiudad(ciudadesOrigen, codOrigen));
        System.out.printf("Destino: %s\n", buscarCiudad(ciudadesDestino, codDestino));
        System.out.printf("Tipo entrega: %s\n", tipoEntrega);
        System.out.printf("Costo envío: $%.2f\n", costoEnvio);
        System.out.printf("Costo repuesto: $%.2f\n", (double)repuesto.getPrecio());
        System.out.printf("PRECIO FINAL: $%.2f\n", precioFinal);
    }

    enum CajaTipo {
        CHICA(20, 20, 20),
        MEDIANA(40, 30, 30),
        GRANDE(60, 40, 40);

        public final int largo, ancho, alto;
        CajaTipo(int l, int a, int h) { largo = l; ancho = a; alto = h; }
    }

    public static class ListaCiudadesOrigenResponse {
        public String type;
        public int codigoRespuesta;
        public String mensajeRespuesta;
        public java.util.List<CiudadDTO> listaCiudadesOrigen;
    }

    public static class ListaCiudadesDestinoResponse {
        public String type;
        public int codigoRespuesta;
        public String mensajeRespuesta;
        public java.util.List<CiudadDTO> listaCiudadesDestino;
    }

    private static List<CiudadDTO> getCiudades(RestTemplate rest, String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("rut", RUT_HEADER);
        headers.set("clave", CLAVE_HEADER);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        if (endpoint.equals("listarCiudadesOrigen")) {
            ResponseEntity<ListaCiudadesOrigenResponse> resp = rest.exchange(
                API_BASE + endpoint,
                HttpMethod.GET,
                entity,
                ListaCiudadesOrigenResponse.class
            );
            ListaCiudadesOrigenResponse body = resp.getBody();
            return body != null ? body.listaCiudadesOrigen : Collections.emptyList();
        } else if (endpoint.equals("listarCiudadesDestino")) {
            ResponseEntity<ListaCiudadesDestinoResponse> resp = rest.exchange(
                API_BASE + endpoint,
                HttpMethod.GET,
                entity,
                ListaCiudadesDestinoResponse.class
            );
            ListaCiudadesDestinoResponse body = resp.getBody();
            return body != null ? body.listaCiudadesDestino : Collections.emptyList();
        } else {
            return Collections.emptyList();
        }
    }

    private static void mostrarCiudades(List<CiudadDTO> ciudades) {
        for (CiudadDTO c : ciudades) {
            System.out.printf("%d) %s\n", c.codigoCiudad, c.nombreCiudad);
        }
    }

    private static int leerCodigoCiudad(Scanner sc, List<CiudadDTO> ciudades, String prompt) {
        Set<Integer> codigos = ciudades.stream().map(c -> c.codigoCiudad).collect(Collectors.toSet());
        while (true) {
            int cod = leerEntero(sc, prompt);
            if (codigos.contains(cod)) return cod;
            System.out.println("Código inválido.");
        }
    }

    private static String buscarCiudad(List<CiudadDTO> ciudades, int codigo) {
        return ciudades.stream()
            .filter(c -> c.codigoCiudad == codigo)
            .map(c -> c.nombreCiudad)
            .findFirst().orElse("?");
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
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (Exception e) { System.out.println("Inválido."); }
        }
    }
}