package com.empresa.backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.empresa.server.Repuesto;
import com.empresa.server.Reserva;
import com.empresa.server.Ubicacion;
import com.empresa.server.Vehiculo;

public class Database {
    private static final String URL  = "jdbc:mysql://localhost:3306/bd_lyl?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "axeler8";

    static {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static List<Repuesto> obtenerTodosRepuestos() {
        String sql = "SELECT sku, nombre, cantidad, precio, categoria, disponible FROM repuestos";
        List<Repuesto> list = new ArrayList<>();
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String cat  = rs.getString("categoria");
                boolean disp = "1".equals(rs.getString("disponible"));
                list.add(new Repuesto(
                    rs.getInt("sku"),
                    rs.getString("nombre"),
                    rs.getInt("cantidad"),
                    rs.getInt("precio"),
                    cat,
                    disp
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Repuesto obtenerRepuestoPorSku(int sku) {
        String sql = "SELECT sku, nombre, cantidad, precio, categoria, disponible FROM repuestos WHERE sku = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String cat  = rs.getString("categoria");
                    boolean disp = "1".equals(rs.getString("disponible"));
                    return new Repuesto(
                        rs.getInt("sku"),
                        rs.getString("nombre"),
                        rs.getInt("cantidad"),
                        rs.getInt("precio"),
                        cat,
                        disp
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void insertarRepuesto(int idUbicacion, int sku, int cantidad, int precio, String categoria, boolean disponible, String nombre) {
        String sql = "INSERT INTO repuestos(idUbicacion, sku, cantidad, precio, categoria, disponible, nombre) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, idUbicacion);
            ps.setInt(2, sku);
            ps.setInt(3, cantidad);
            ps.setInt(4, precio);
            ps.setString(5, categoria);
            ps.setString(6, disponible ? "1" : "0");
            ps.setString(7, nombre);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void actualizarStock(int idUbicacion, int sku, int delta) {
        String sql = "UPDATE repuestos SET cantidad = cantidad + ? WHERE idUbicacion = ? AND sku = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, delta);
            ps.setInt(2, idUbicacion);
            ps.setInt(3, sku);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Reserva> obtenerTodasReservas() {
        String sql = "SELECT idReserva, idVehiculo, sku, cantidad FROM reservas";
        List<Reserva> list = new ArrayList<>();
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Reserva(
                    rs.getInt("idReserva"),
                    rs.getInt("idVehiculo"),
                    rs.getInt("sku"),
                    rs.getInt("cantidad")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Reserva obtenerReservaPorId(int idReserva) {
        String sql = "SELECT idReserva, idVehiculo, sku, cantidad FROM reservas WHERE idReserva = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, idReserva);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Reserva(
                        rs.getInt("idReserva"),
                        rs.getInt("idVehiculo"),
                        rs.getInt("sku"),
                        rs.getInt("cantidad")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void insertarReserva(int idVehiculo, int sku, int cantidad) {
        String sql = "INSERT INTO reservas(idVehiculo, sku, cantidad, fecha) VALUES(?,?,?,NOW())";
        try (Connection c = getConnection()) {
            
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, idVehiculo);
                ps.setInt(2, sku);
                ps.setInt(3, cantidad);
                ps.executeUpdate();
            }
            
            int idUbicacion;
            String selUb = "SELECT idUbicacion FROM repuestos WHERE sku = ?";
            try (PreparedStatement ps2 = c.prepareStatement(selUb)) {
                ps2.setInt(1, sku);
                try (ResultSet rs = ps2.executeQuery()) {
                    if (!rs.next()) return;
                    idUbicacion = rs.getInt("idUbicacion");
                }
            }
          
            actualizarStock(idUbicacion, sku, -cantidad);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void eliminarReserva(int idReserva) {
        try (Connection c = getConnection()) {
            
            String sel = "SELECT sku, cantidad FROM reservas WHERE idReserva = ?";
            int sku, cantidad;
            try (PreparedStatement ps = c.prepareStatement(sel)) {
                ps.setInt(1, idReserva);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return; 
                    sku = rs.getInt("sku");
                    cantidad = rs.getInt("cantidad");
                }
            }

           
            String selUb = "SELECT idUbicacion FROM repuestos WHERE sku = ?";
            int idUbicacion;
            try (PreparedStatement ps2 = c.prepareStatement(selUb)) {
                ps2.setInt(1, sku);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (!rs2.next()) return; 
                    idUbicacion = rs2.getInt("idUbicacion");
                }
            }

            
            actualizarStock(idUbicacion, sku, cantidad);

           
            String del = "DELETE FROM reservas WHERE idReserva = ?";
            try (PreparedStatement ps3 = c.prepareStatement(del)) {
                ps3.setInt(1, idReserva);
                ps3.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static int obtenerStockTotalPorUbicacion(int idUbicacion) {
        String sql = "SELECT COALESCE(SUM(cantidad),0) AS total FROM repuestos WHERE idUbicacion = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, idUbicacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static Ubicacion obtenerUbicacionPorId(int id) {
        String sql = "SELECT idUbicacion, nombre, direccion, capacidad FROM ubicaciones WHERE idUbicacion = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Ubicacion(
                        rs.getInt("idUbicacion"),
                        rs.getString("nombre"),
                        rs.getString("direccion"),
                        rs.getInt("capacidad")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Vehiculo obtenerVehiculoPorId(int id) {
        String sql = "SELECT idVehiculo, anio, idUbicacion, nombre, modelo, cilindraje, color FROM vehiculos WHERE idVehiculo = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Vehiculo(
                        rs.getInt("idVehiculo"),
                        rs.getInt("anio"),
                        rs.getInt("idUbicacion"),
                        rs.getString("nombre"),
                        rs.getString("modelo"),
                        rs.getString("cilindraje"),
                        rs.getString("color")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Repuesto obtenerRepuestoPorUbicacion(int idUbicacion, int sku) {
        String sql = "SELECT sku, nombre, cantidad, precio, categoria, disponible FROM repuestos WHERE idUbicacion = ? AND sku = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, idUbicacion);
            ps.setInt(2, sku);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String cat  = rs.getString("categoria");
                    boolean disp = "1".equals(rs.getString("disponible"));
                    return new Repuesto(
                        rs.getInt("sku"),
                        rs.getString("nombre"),
                        rs.getInt("cantidad"),
                        rs.getInt("precio"),
                        cat,
                        disp
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
