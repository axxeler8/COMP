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

![alt text](images/{BC562AF2-1D4F-4C10-BC56-7845F2F4B260}.png)

Si no llegasen a salir esos 5, hay que apretar en `run configurations` lo cual llevará a

![alt text](images/{01E7F8F9-9057-4967-909B-9728C85D26CD}.png)

Ahi hay que apretar click derecho en Java Application y apretar en New Configuration, despues elegir el proyecto `inventario-paralela` y seleccionar el main class correspondiente a agregar al `run`

![alt text](images/{A9273128-1759-4AB8-A8E1-F27C69CB2130}.png)

El orden de ejecución es el siguiente:

1. ServerMain
2. ServerBackup
3. Application
4. ConsolaTaller
5. ConsolaDespacho

**cada ejecutable tiene su propia consola por lo que si se quiere ver por separado hay una flecha donde se puede cambiar las vistas entre consolas**

![alt text](images/{3C9810A7-595D-41A2-8337-329D7D8C16D8}.png)

![alt text](images/{58AE7697-AE7D-403C-A5A3-C391BB684DB4}.png)

## Nota adicional

Para poder ver los SKU, id de reserva, id de vehículo, id de ubicación etc.. es necesario ver las tablas y columnas de la base de datos. Nosotros ocupamos phpmyadmin para gestionar