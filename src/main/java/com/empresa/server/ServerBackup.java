package com.empresa.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerBackup {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1100);
            InventarioService svc = new InventarioServiceImpl();
            registry.rebind("InventarioService", svc);
            System.out.println("Servidor RMI de respaldo iniciado en puerto 1100...");
            System.out.println("Presione ENTER para apagar el servidor");
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}