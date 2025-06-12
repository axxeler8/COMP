# Entrega parcial proyecto computación paralela

## 1. Inicio de servicio mysql + creación BD + inserción de tablas a la BD

 **El servicio mysql fue creado con el usuario `root` y la contraseña `password`  (se puede cambiar en Database.java) por lo que habria que cambiar esto si se quiere usar con otras credenciales. Los pasos que se explicarán fueron hechos usando wsl**

### Inicio de Mysql + BD

```bash
# creacion de BD
sudo systemctl start mysql
cd inventario-paralela/db
mysql -u root -p < db.sql
```

Fue usado phpmyadmin para ver la base de datos

![alt text](images/{EF412B3E-9E9D-4094-B236-C008E16BD415}.png)


```bash
# Insertar datos iniciales a la BD
mysql -u root -p bd_lyl < insert.sql
```

## 2. Ejecucion de código en eclipse

Seleccionar File y luego Open Projects from File System

![alt text](images/{317EC2EA-158C-4DD3-A35A-FC9A6CD39929}.png)

Importar desde Directory y elegir la carpeta `inventario-paralela`

![alt text](images/{8E7BBAE0-D658-4B4E-BBD3-A92E907922C8}.png)

Al apretar en la flecha al lado del botón de run debería salir los ejecutables correspondientes

![alt text](images/{8B6C9356-A020-4E28-9875-A78FCD6C3987}.png)

El orden de ejecución es el siguiente:

1. ServerMain
2. Application
3. ConsolaTaller

    **cada ejecutable tiene su propia consola por lo que si se quiere ver por separado hay una flecha donde se puede cambiar las vistas entre consolas**

![alt text](images/{3C9810A7-595D-41A2-8337-329D7D8C16D8}.png)

![alt text](images/{7CD2801E-07C6-4EFA-B633-786C2BB62602}.png)

## Nota adicional

Para poder ver los SKU, id de reserva, id de vehículo, id de ubicación etc.. es necesario ver las tablas y columnas de la base de datos. Nosotros ocupamos phpmyadmin para gestionar