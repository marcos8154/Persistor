SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `cadastro` ;
CREATE SCHEMA IF NOT EXISTS `cadastro` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `cadastro` ;

-- -----------------------------------------------------
-- Table `cadastro`.`veiculo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `cadastro`.`veiculo` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `nome` VARCHAR(45) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `cadastro`.`pessoa`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `cadastro`.`pessoa` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `nome` VARCHAR(100) NULL,
  `telefone` VARCHAR(15) NULL,
  `email` VARCHAR(100) NULL,
  `endereco` VARCHAR(200) NULL,
  `version` INT NULL,
  `veiculo_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_pessoa_veiculo_idx` (`veiculo_id` ASC),
  CONSTRAINT `fk_pessoa_veiculo`
    FOREIGN KEY (`veiculo_id`)
    REFERENCES `cadastro`.`veiculo` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `cadastro`.`profissao`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `cadastro`.`profissao` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `pessoa_id` INT NOT NULL,
  `descricao` VARCHAR(45) NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_profissao_pessoa1_idx` (`pessoa_id` ASC),
  CONSTRAINT `fk_profissao_pessoa1`
    FOREIGN KEY (`pessoa_id`)
    REFERENCES `cadastro`.`pessoa` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
