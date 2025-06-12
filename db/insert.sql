INSERT INTO ubicaciones (nombre, direccion, capacidad) VALUES
('Taller Central', 'Av. Principal 123', 500),
('Sucursal Norte', 'Calle Secundaria 456', 300);

INSERT INTO repuestos (idUbicacion, sku, cantidad, precio, categoria, disponible, nombre) VALUES
(1, 1001, 15, 25, 'Frenos', '1', 'Pastillas de Freno Delanteras'),
(1, 1002, 8, 40, 'Motor', '1', 'Filtro de Aceite'),
(2, 1003, 5, 120, 'Suspensión', '1', 'Amortiguadores Delanteros'),
(2, 1004, 20, 10, 'Electricidad', '1', 'Bujías estándar');

INSERT INTO vehiculos (anio, idUbicacion, nombre, modelo, cilindraje, color) VALUES
(2020, 1, 'Toyota', 'Corolla', '1600cc', 'Blanco'),
(2018, 1, 'Honda', 'Civic', '1800cc', 'Gris'),
(2022, 2, 'Yamaha', 'MT-07', '700cc', 'Azul');

INSERT INTO reservas (idVehiculo, sku, cantidad, fecha) VALUES
(1, 1001, 2, '2025-05-15 10:00:00'),
(3, 1004, 4, '2025-05-16 14:30:00'),
(2, 1002, 1, '2025-05-17 09:15:00');