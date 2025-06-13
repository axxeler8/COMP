package com.empresa.test;

import com.empresa.server.InventarioService;
import com.empresa.server.Repuesto;
import java.rmi.Naming;

public class DataIntegrityTest {
    public static void main(String[] args) {
        final int INITIAL_STOCK = 450;
        final int THREAD_COUNT = 20;
        final int OPERATIONS_PER_THREAD = 20;
        
        try {
            InventarioService svc = (InventarioService) Naming.lookup("rmi://localhost:1099/InventarioService");
            
            // Crear repuesto de prueba
            svc.agregarRepuesto(1, 999, INITIAL_STOCK, 100, "Test", true, "Repuesto de prueba");
            
            // Crear hilos que modifican el stock
            Thread[] threads = new Thread[THREAD_COUNT];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(() -> {
                    try {
                        for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                            // Operaciones conflictivas
                            if (j % 2 == 0) {
                                svc.liberarRepuesto(1, 999, 1);
                            } else {
                                svc.agregarReserva(1, 999, 1);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                threads[i].start();
            }
            
            // Esperar a que todos terminen
            for (Thread t : threads) {
                t.join();
            }
            
            // Verificar resultado final
            Repuesto repuesto = svc.consultarRepuesto(999);
            int expectedStock = INITIAL_STOCK - (THREAD_COUNT * OPERATIONS_PER_THREAD);
            
            System.out.println("Stock esperado: " + expectedStock);
            System.out.println("Stock real: " + repuesto.getCantidad());
            System.out.println("Resultado: " + 
                (repuesto.getCantidad() == expectedStock ? "ÉXITO" : "FALLO"));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}