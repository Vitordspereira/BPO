-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: hds_bpo
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cartao`
--

DROP TABLE IF EXISTS `cartao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cartao` (
  `dia_fechamento` int DEFAULT NULL,
  `dia_vencimento` int DEFAULT NULL,
  `limite_total` decimal(10,2) DEFAULT NULL,
  `saldo_entrada` decimal(38,2) DEFAULT NULL,
  `saldo_saida` decimal(38,2) DEFAULT NULL,
  `data_criacao` datetime(6) DEFAULT NULL,
  `id_cartao` bigint NOT NULL AUTO_INCREMENT,
  `numero_mascara` varchar(19) NOT NULL,
  `nome_cartao` varchar(100) NOT NULL,
  `bandeira` enum('AMEX','ELO','HIPERCARD','MASTERCARD','OUTRA','VISA') DEFAULT NULL,
  `categoria` enum('BLACK','CLASSIC','GOLD','INFINITE','PLATINUM') DEFAULT NULL,
  `id_usuario` bigint NOT NULL,
  `status_cartao` varchar(20) DEFAULT 'ATIVO',
  `cor` varchar(20) DEFAULT '#000000',
  `icone` varchar(50) DEFAULT 'fas fa-credit-card',
  `perfil_financeiro` enum('EMPRESA','PESSOAL') NOT NULL,
  PRIMARY KEY (`id_cartao`),
  KEY `FKsh2yncqa0c513ksjq8jqs8178` (`id_usuario`),
  CONSTRAINT `FKsh2yncqa0c513ksjq8jqs8178` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `categoria`
--

DROP TABLE IF EXISTS `categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria` (
  `id_categoria` bigint NOT NULL AUTO_INCREMENT,
  `cor` varchar(20) DEFAULT NULL,
  `icone` varchar(50) DEFAULT NULL,
  `nome` varchar(100) NOT NULL,
  `tipo` enum('DESPESA','RECEITA') NOT NULL,
  `id_usuario` bigint NOT NULL,
  `perfil_financeiro` enum('EMPRESA','PESSOAL') NOT NULL,
  `slug` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_categoria`),
  UNIQUE KEY `slug` (`slug`),
  KEY `FK7lt8y9vjot9xktxh3fdp6hll7` (`id_usuario`),
  CONSTRAINT `FK7lt8y9vjot9xktxh3fdp6hll7` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `categoria_n8n`
--

DROP TABLE IF EXISTS `categoria_n8n`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria_n8n` (
  `id_categoria_n8n` bigint NOT NULL AUTO_INCREMENT,
  `telefone` varchar(50) NOT NULL,
  `nome` varchar(100) NOT NULL,
  `tipo` varchar(30) DEFAULT NULL,
  `icone` varchar(50) DEFAULT NULL,
  `cor` varchar(20) DEFAULT NULL,
  `perfil_financeiro` varchar(30) DEFAULT NULL,
  `criado_em` datetime DEFAULT CURRENT_TIMESTAMP,
  `atualizado_em` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `slug` varchar(120) DEFAULT NULL,
  PRIMARY KEY (`id_categoria_n8n`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dashboard`
--

DROP TABLE IF EXISTS `dashboard`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dashboard` (
  `id_dashboard` bigint NOT NULL AUTO_INCREMENT,
  `data` date NOT NULL,
  `descricao` varchar(255) NOT NULL,
  `meio_pagamento` enum('PIX','BOLETO','CARTAO_CREDITO','CARTAO_DEBITO','TRANSFERENCIA') DEFAULT NULL,
  `tipo` enum('DESPESA','RECEITA') NOT NULL,
  `valor` decimal(38,2) NOT NULL,
  `id_usuario` bigint NOT NULL,
  `id_cartao` bigint DEFAULT NULL,
  `id_categoria` bigint NOT NULL,
  `perfil_financeiro` enum('EMPRESA','PESSOAL') NOT NULL,
  PRIMARY KEY (`id_dashboard`),
  KEY `FKocl15xd7fonr93qq8588eknvh` (`id_usuario`),
  KEY `FKchidpjvy64prbai0o08ner2xi` (`id_cartao`),
  KEY `FKpj9hqu5xmj4ldtjga3mjv3eeu` (`id_categoria`),
  CONSTRAINT `FKchidpjvy64prbai0o08ner2xi` FOREIGN KEY (`id_cartao`) REFERENCES `cartao` (`id_cartao`),
  CONSTRAINT `FKocl15xd7fonr93qq8588eknvh` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `FKpj9hqu5xmj4ldtjga3mjv3eeu` FOREIGN KEY (`id_categoria`) REFERENCES `categoria` (`id_categoria`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `esqueci_senha`
--

DROP TABLE IF EXISTS `esqueci_senha`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `esqueci_senha` (
  `id_reset` bigint NOT NULL AUTO_INCREMENT,
  `criado_em` datetime(6) NOT NULL,
  `email` varchar(180) NOT NULL,
  `expiracao` datetime(6) NOT NULL,
  `token` varchar(180) NOT NULL,
  `usado` bit(1) NOT NULL,
  PRIMARY KEY (`id_reset`),
  UNIQUE KEY `UK4k8wj6jaoej37891go2mnb9gv` (`token`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lancamento`
--

DROP TABLE IF EXISTS `lancamento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lancamento` (
  `id_lancamento` bigint NOT NULL AUTO_INCREMENT,
  `data_transacao` date NOT NULL,
  `descricao` varchar(255) DEFAULT NULL,
  `forma_pagamento` enum('BOLETO','CREDITO','DEBITO','PIX') DEFAULT NULL,
  `id_dashboard` bigint DEFAULT NULL,
  `movimentacao` enum('DESPESA','RECEITA') NOT NULL,
  `tipo_gasto` enum('EMPRESA','PESSOAL') NOT NULL,
  `valor` decimal(10,2) NOT NULL,
  `id_categoria` bigint NOT NULL,
  `id_usuario` bigint NOT NULL,
  `transaction_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_lancamento`),
  KEY `FKr651pitjvnxinfpcgbd9g71sf` (`id_categoria`),
  KEY `FKt2a5b4jc8powehfmsyeufarkr` (`id_usuario`),
  CONSTRAINT `FKr651pitjvnxinfpcgbd9g71sf` FOREIGN KEY (`id_categoria`) REFERENCES `categoria` (`id_categoria`),
  CONSTRAINT `FKt2a5b4jc8powehfmsyeufarkr` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pre_cadastro`
--

DROP TABLE IF EXISTS `pre_cadastro`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pre_cadastro` (
  `id_pre_cadastro` bigint NOT NULL AUTO_INCREMENT,
  `token` varchar(100) NOT NULL,
  `payer_email` varchar(100) NOT NULL,
  `plano` varchar(50) NOT NULL,
  `valor_plano` decimal(10,2) NOT NULL,
  `periodicidade_plano` varchar(50) NOT NULL,
  `mp_preapproval_id` varchar(100) DEFAULT NULL,
  `mp_external_reference` varchar(100) DEFAULT NULL,
  `mp_status` varchar(80) DEFAULT NULL,
  `usado` tinyint(1) NOT NULL DEFAULT '0',
  `expiracao` datetime NOT NULL,
  `criado_em` datetime NOT NULL,
  `atualizado_em` datetime DEFAULT NULL,
  PRIMARY KEY (`id_pre_cadastro`),
  UNIQUE KEY `token` (`token`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `transacoes_n8n`
--

DROP TABLE IF EXISTS `transacoes_n8n`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transacoes_n8n` (
  `transaction_id` varchar(255) NOT NULL,
  `categoria` varchar(100) DEFAULT NULL,
  `data_transacao` date NOT NULL,
  `descricao` varchar(255) NOT NULL,
  `draft_id` varchar(255) NOT NULL,
  `forma_pagamento` varchar(100) DEFAULT NULL,
  `movimentacao` varchar(100) DEFAULT NULL,
  `status` varchar(100) NOT NULL,
  `telefone` varchar(50) NOT NULL,
  `tipo_gasto` varchar(100) DEFAULT NULL,
  `valor` decimal(38,2) NOT NULL,
  `perfil_financeiro` enum('EMPRESA','PESSOAL') NOT NULL,
  PRIMARY KEY (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `assinatura_ativa` bit(1) DEFAULT NULL,
  `atualizado_em` datetime(6) DEFAULT NULL,
  `criado_em` datetime(6) DEFAULT NULL,
  `id_usuario` bigint NOT NULL AUTO_INCREMENT,
  `telefone` varchar(20) NOT NULL,
  `hott_transaction` varchar(50) DEFAULT NULL,
  `senha` varchar(100) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `nome_completo` varchar(150) DEFAULT NULL,
  `data_inatividade` datetime(6) DEFAULT NULL,
  `tem_empresa` int NOT NULL DEFAULT '0',
  `mp_assinatura_atualizada_em` datetime(6) DEFAULT NULL,
  `mp_external_reference` varchar(100) DEFAULT NULL,
  `mp_preapproval_id` varchar(100) DEFAULT NULL,
  `mp_status` varchar(30) DEFAULT NULL,
  `periodicidade_plano` varchar(20) DEFAULT NULL,
  `tipo_plano` varchar(20) DEFAULT NULL,
  `valor_plano` decimal(10,2) DEFAULT NULL,
  `ambiente_usuario` enum('EMPRESA','PESSOAL') DEFAULT NULL,
  `bandeira_cartao` enum('AMEX','ELO','HIPERCARD','MASTERCARD','OUTRA','VISA') DEFAULT NULL,
  `cpf` varchar(14) DEFAULT NULL,
  `bio` varchar(300) DEFAULT NULL,
  `cidade` varchar(100) DEFAULT NULL,
  `profissao` varchar(70) DEFAULT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `uk_usuario_email` (`email`),
  UNIQUE KEY `uk_usuario_cpf` (`cpf`),
  UNIQUE KEY `uk_usuario_integracpf` (`cpf`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-31 11:06:32
