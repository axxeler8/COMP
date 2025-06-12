SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

CREATE SCHEMA IF NOT EXISTS `bd_lyl` DEFAULT CHARACTER SET utf8 ;
USE `bd_lyl` ;

CREATE TABLE IF NOT EXISTS `bd_lyl`.`ubicaciones` (
  `idUbicacion` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NOT NULL,
  `direccion` VARCHAR(45) NOT NULL,
  `capacidad` INT NULL,
  PRIMARY KEY (`idUbicacion`),
  UNIQUE INDEX `idUbicacion_UNIQUE` (`idUbicacion` ASC) VISIBLE)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `bd_lyl`.`repuestos` (
  `idRepuesto` INT NOT NULL AUTO_INCREMENT,
  `idUbicacion` INT NOT NULL,
  `sku` INT NOT NULL,
  `cantidad` INT NOT NULL,
  `precio` INT NOT NULL,
  `categoria` VARCHAR(45) NULL,
  `disponible` ENUM('1', '0') NOT NULL,
  `nombre` VARCHAR(60) NOT NULL,
  PRIMARY KEY (`idRepuesto`),
  UNIQUE INDEX `idProducto_UNIQUE` (`idRepuesto` ASC) VISIBLE,
  UNIQUE INDEX `sku_UNIQUE` (`sku` ASC) VISIBLE,
  INDEX `idUbicacion_idx` (`idUbicacion` ASC) VISIBLE,
  CONSTRAINT `fk_repuestos_ubicacion`  -- Changed constraint name
    FOREIGN KEY (`idUbicacion`)
    REFERENCES `bd_lyl`.`ubicaciones` (`idUbicacion`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `bd_lyl`.`vehiculos` (
  `idVehiculo` INT NOT NULL AUTO_INCREMENT,
  `anio` INT NULL,
  `idUbicacion` INT NOT NULL,
  `nombre` VARCHAR(45) NOT NULL,
  `modelo` VARCHAR(45) NOT NULL,
  `cilindraje` VARCHAR(45) NULL,
  `color` VARCHAR(45) NULL,
  PRIMARY KEY (`idVehiculo`),
  UNIQUE INDEX `idvehiculo_UNIQUE` (`idVehiculo` ASC) VISIBLE,
  INDEX `idUbicacion_idx` (`idUbicacion` ASC) VISIBLE,
  CONSTRAINT `fk_vehiculos_ubicacion`  -- Changed constraint name
    FOREIGN KEY (`idUbicacion`)
    REFERENCES `bd_lyl`.`ubicaciones` (`idUbicacion`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `bd_lyl`.`reservas` (
  `idReserva` INT NOT NULL AUTO_INCREMENT,
  `idVehiculo` INT NOT NULL,
  `sku` INT NOT NULL,
  `cantidad` INT NOT NULL,
  `fecha` DATETIME NULL,
  PRIMARY KEY (`idReserva`),
  UNIQUE INDEX `idReserva_UNIQUE` (`idReserva` ASC) VISIBLE,
  INDEX `idVehiculo_idx` (`idVehiculo` ASC) VISIBLE,
  INDEX `sku_idx` (`sku` ASC) VISIBLE,
  CONSTRAINT `idVehiculo`
    FOREIGN KEY (`idVehiculo`)
    REFERENCES `bd_lyl`.`vehiculos` (`idVehiculo`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sku`
    FOREIGN KEY (`sku`)
    REFERENCES `bd_lyl`.`repuestos` (`sku`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;