package com.empresa.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            InventarioService svc = new InventarioServiceImpl();
            registry.rebind("InventarioService", svc);
            System.out.println("Servidor RMI iniciado en puerto 1099...");
            System.out.println("Presione ENTER para apagar el servidor");
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}