-- MySQL dump 10.13  Distrib 8.0.44, for macos15 (arm64)
--
-- Host: localhost    Database: xettuyen2026
-- ------------------------------------------------------
-- Server version	8.0.42

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
-- Table structure for table `xt_bangquydoi`
--

DROP TABLE IF EXISTS `xt_bangquydoi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_bangquydoi` (
  `idqd` int NOT NULL AUTO_INCREMENT,
  `d_phuongthuc` varchar(45) DEFAULT NULL,
  `d_tohop` varchar(45) DEFAULT NULL,
  `d_mon` varchar(45) DEFAULT NULL,
  `d_diema` decimal(6,2) DEFAULT NULL,
  `d_diemb` decimal(6,2) DEFAULT NULL,
  `d_diemc` decimal(6,2) DEFAULT NULL,
  `d_diemd` decimal(6,2) DEFAULT NULL,
  `d_maquydoi` varchar(45) DEFAULT NULL,
  `d_phanvi` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idqd`),
  UNIQUE KEY `d_maquydoi_UNIQUE` (`d_maquydoi`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_bangquydoi`
--

LOCK TABLES `xt_bangquydoi` WRITE;
/*!40000 ALTER TABLE `xt_bangquydoi` DISABLE KEYS */;
INSERT INTO `xt_bangquydoi`
(`d_phuongthuc`, `d_tohop`, `d_mon`, `d_diema`, `d_diemb`, `d_diemc`, `d_diemd`, `d_maquydoi`, `d_phanvi`)
VALUES
('THPT', 'A00', 'TO', 7.50, 8.00, 8.50, 9.00, 'QD_THPT_A00_TO', 'P50'),
('THPT', 'A01', 'N1', 6.50, 7.00, 7.50, 8.00, 'QD_THPT_A01_N1', 'P50'),
('DGNL', 'DGNL', 'NL1', 600.00, 700.00, 800.00, 900.00, 'QD_DGNL_NL1', 'P50'),
('VSAT', 'VSAT', 'TO', 60.00, 70.00, 80.00, 90.00, 'QD_VSAT_TO', 'P50'),
('THPT', 'D01', 'N1', 7.00, 7.50, 8.00, 8.50, 'QD_THPT_D01_N1', 'P50'),
('THPT', 'C00', 'VA', 7.00, 7.50, 8.00, 8.50, 'QD_THPT_C00_VA', 'P50');
/*!40000 ALTER TABLE `xt_bangquydoi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_diemcongxetuyen`
--

DROP TABLE IF EXISTS `xt_diemcongxetuyen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_diemcongxetuyen` (
  `iddiemcong` int unsigned NOT NULL AUTO_INCREMENT,
  `ts_cccd` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `manganh` varchar(20) DEFAULT '0.00',
  `matohop` varchar(10) DEFAULT '0.00',
  `phuongthuc` varchar(45) DEFAULT NULL,
  `diemCC` decimal(6,2) DEFAULT NULL,
  `diemUtxt` decimal(6,2) DEFAULT NULL,
  `diemTong` decimal(6,2) DEFAULT '0.00',
  `ghichu` text,
  `dc_keys` varchar(45) NOT NULL,
  PRIMARY KEY (`iddiemcong`),
  UNIQUE KEY `dc_keys_UNIQUE` (`dc_keys`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_diemcongxetuyen`
--

LOCK TABLES `xt_diemcongxetuyen` WRITE;
/*!40000 ALTER TABLE `xt_diemcongxetuyen` DISABLE KEYS */;
INSERT INTO `xt_diemcongxetuyen`
(`ts_cccd`, `manganh`, `matohop`, `phuongthuc`, `diemCC`, `diemUtxt`, `diemTong`, `ghichu`, `dc_keys`)
SELECT
  CONCAT('079000000', LPAD(s.n, 3, '0')) AS ts_cccd,
  CASE MOD(s.n, 5)
    WHEN 0 THEN '7340101'
    WHEN 1 THEN '7480201'
    WHEN 2 THEN '7220201'
    WHEN 3 THEN '7310101'
    ELSE '7320104'
  END AS manganh,
  CASE MOD(s.n, 4)
    WHEN 0 THEN 'A00'
    WHEN 1 THEN 'A01'
    WHEN 2 THEN 'D01'
    ELSE 'C00'
  END AS matohop,
  CASE MOD(s.n, 3)
    WHEN 0 THEN 'THPT'
    WHEN 1 THEN 'DGNL'
    ELSE 'VSAT'
  END AS phuongthuc,
  ROUND((MOD(s.n, 4) * 0.25), 2) AS diemCC,
  ROUND((MOD(s.n, 5) * 0.20), 2) AS diemUtxt,
  ROUND((MOD(s.n, 4) * 0.25) + (MOD(s.n, 5) * 0.20), 2) AS diemTong,
  'seed data' AS ghichu,
  CONCAT(CONCAT('079000000', LPAD(s.n, 3, '0')), '_',
    CASE MOD(s.n, 5)
      WHEN 0 THEN '7340101'
      WHEN 1 THEN '7480201'
      WHEN 2 THEN '7220201'
      WHEN 3 THEN '7310101'
      ELSE '7320104'
    END,
    '_',
    CASE MOD(s.n, 4)
      WHEN 0 THEN 'A00'
      WHEN 1 THEN 'A01'
      WHEN 2 THEN 'D01'
      ELSE 'C00'
    END
  ) AS dc_keys
FROM (
  SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
  UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
  UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
  UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
) s;
/*!40000 ALTER TABLE `xt_diemcongxetuyen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_diemthixettuyen`
--

DROP TABLE IF EXISTS `xt_diemthixettuyen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_diemthixettuyen` (
  `iddiemthi` int NOT NULL AUTO_INCREMENT,
  `cccd` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `sobaodanh` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `d_phuongthuc` varchar(10) DEFAULT NULL,
  `TO` decimal(8,2) DEFAULT '0.00',
  `LI` decimal(8,2) DEFAULT '0.00',
  `HO` decimal(8,2) DEFAULT '0.00',
  `SI` decimal(8,2) DEFAULT '0.00',
  `SU` decimal(8,2) DEFAULT '0.00',
  `DI` decimal(8,2) DEFAULT '0.00',
  `VA` decimal(8,2) DEFAULT '0.00',
  `N1_THI` decimal(8,2) DEFAULT NULL COMMENT 'Điểm thi gốc',
  `N1_CC` decimal(8,2) DEFAULT '0.00' COMMENT 'max(N1_Thi, N1_QD)',
  `CNCN` decimal(8,2) DEFAULT '0.00',
  `CNNN` decimal(8,2) DEFAULT '0.00',
  `TI` decimal(8,2) DEFAULT '0.00',
  `KTPL` decimal(8,2) DEFAULT '0.00',
  `NL1` decimal(8,2) DEFAULT NULL,
  `NK1` decimal(8,2) DEFAULT NULL,
  `NK2` decimal(8,2) DEFAULT NULL,
  PRIMARY KEY (`iddiemthi`),
  UNIQUE KEY `cccd_UNIQUE` (`cccd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_diemthixettuyen`
--

LOCK TABLES `xt_diemthixettuyen` WRITE;
/*!40000 ALTER TABLE `xt_diemthixettuyen` DISABLE KEYS */;
INSERT INTO `xt_diemthixettuyen`
(`cccd`, `sobaodanh`, `d_phuongthuc`, `TO`, `LI`, `HO`, `SI`, `SU`, `DI`, `VA`, `N1_THI`, `N1_CC`, `CNCN`, `CNNN`, `TI`, `KTPL`, `NL1`, `NK1`, `NK2`)
SELECT
  CONCAT('079000000', LPAD(s.n, 3, '0')),
  CONCAT('SBD', LPAD(s.n, 5, '0')),
  CASE MOD(s.n, 3)
    WHEN 0 THEN 'THPT'
    WHEN 1 THEN 'DGNL'
    ELSE 'VSAT'
  END,
  ROUND(6.0 + MOD(s.n, 4) * 0.75, 2),
  ROUND(5.5 + MOD(s.n + 1, 4) * 0.75, 2),
  ROUND(5.0 + MOD(s.n + 2, 4) * 0.75, 2),
  ROUND(5.0 + MOD(s.n, 3) * 0.80, 2),
  ROUND(5.0 + MOD(s.n + 1, 3) * 0.80, 2),
  ROUND(5.0 + MOD(s.n + 2, 3) * 0.80, 2),
  ROUND(6.0 + MOD(s.n, 5) * 0.60, 2),
  ROUND(6.0 + MOD(s.n, 5) * 0.50, 2),
  ROUND(6.0 + MOD(s.n, 5) * 0.50, 2),
  ROUND(5.5 + MOD(s.n, 5) * 0.60, 2),
  ROUND(5.5 + MOD(s.n + 1, 5) * 0.60, 2),
  ROUND(5.5 + MOD(s.n + 2, 5) * 0.60, 2),
  ROUND(5.5 + MOD(s.n + 3, 5) * 0.60, 2),
  ROUND(650 + MOD(s.n, 8) * 30, 2),
  ROUND(6.0 + MOD(s.n, 4) * 0.50, 2),
  ROUND(6.0 + MOD(s.n + 1, 4) * 0.50, 2)
FROM (
  SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
  UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
  UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
  UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
  UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25
) s;
/*!40000 ALTER TABLE `xt_diemthixettuyen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_nganh`
--

DROP TABLE IF EXISTS `xt_nganh`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_nganh` (
  `idnganh` int NOT NULL AUTO_INCREMENT,
  `manganh` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `tennganh` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `n_tohopgoc` varchar(3) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `n_chitieu` int NOT NULL DEFAULT '0',
  `n_diemsan` decimal(10,2) DEFAULT NULL,
  `n_diemtrungtuyen` decimal(10,2) DEFAULT NULL,
  `n_tuyenthang` varchar(1) COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `n_dgnl` varchar(1) COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `n_thpt` varchar(1) COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `n_vsat` varchar(1) COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `sl_xtt` int DEFAULT NULL,
  `sl_dgnl` int DEFAULT NULL,
  `sl_vsat` int DEFAULT NULL,
  `sl_thpt` varchar(45) COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idnganh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_nganh`
--

LOCK TABLES `xt_nganh` WRITE;
/*!40000 ALTER TABLE `xt_nganh` DISABLE KEYS */;
INSERT INTO `xt_nganh`
(`manganh`, `tennganh`, `n_tohopgoc`, `n_chitieu`, `n_diemsan`, `n_diemtrungtuyen`, `n_tuyenthang`, `n_dgnl`, `n_thpt`, `n_vsat`, `sl_xtt`, `sl_dgnl`, `sl_vsat`, `sl_thpt`)
VALUES
('7480201', 'Cong nghe thong tin', 'A00', 80, 20.00, 24.50, 'N', 'Y', 'Y', 'Y', 5, 25, 20, '30'),
('7340101', 'Quan tri kinh doanh', 'A01', 70, 18.00, 22.00, 'N', 'Y', 'Y', 'Y', 4, 20, 18, '28'),
('7220201', 'Ngon ngu Anh', 'D01', 60, 19.00, 23.00, 'N', 'Y', 'Y', 'Y', 3, 18, 15, '24'),
('7310101', 'Kinh te', 'A00', 75, 18.50, 22.50, 'N', 'Y', 'Y', 'Y', 4, 22, 18, '28'),
('7320104', 'Truyen thong da phuong tien', 'C00', 50, 19.50, 23.50, 'N', 'Y', 'Y', 'Y', 3, 15, 12, '20');
/*!40000 ALTER TABLE `xt_nganh` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_nganh_tohop`
--

DROP TABLE IF EXISTS `xt_nganh_tohop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_nganh_tohop` (
  `id` int NOT NULL AUTO_INCREMENT,
  `manganh` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,
  `matohop` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,
  `th_mon1` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `hsmon1` tinyint DEFAULT NULL,
  `th_mon2` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `hsmon2` tinyint DEFAULT NULL,
  `th_mon3` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `hsmon3` tinyint DEFAULT NULL,
  `tb_keys` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'manganh_matohop',
  `N1` tinyint(1) DEFAULT NULL,
  `TO` tinyint(1) DEFAULT NULL,
  `LI` tinyint(1) DEFAULT NULL,
  `HO` tinyint(1) DEFAULT NULL,
  `SI` tinyint(1) DEFAULT NULL,
  `VA` tinyint(1) DEFAULT NULL,
  `SU` tinyint(1) DEFAULT NULL,
  `DI` tinyint(1) DEFAULT NULL,
  `TI` tinyint(1) DEFAULT NULL,
  `KHAC` tinyint(1) DEFAULT NULL,
  `KTPL` tinyint(1) DEFAULT NULL,
  `dolech` decimal(6,2) DEFAULT '0.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_UNIQUE` (`tb_keys`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_nganh_tohop`
--

LOCK TABLES `xt_nganh_tohop` WRITE;
/*!40000 ALTER TABLE `xt_nganh_tohop` DISABLE KEYS */;
INSERT INTO `xt_nganh_tohop`
(`manganh`, `matohop`, `th_mon1`, `hsmon1`, `th_mon2`, `hsmon2`, `th_mon3`, `hsmon3`, `tb_keys`, `N1`, `TO`, `LI`, `HO`, `SI`, `VA`, `SU`, `DI`, `TI`, `KHAC`, `KTPL`, `dolech`)
VALUES
('7480201', 'A00', 'TO', 2, 'LI', 1, 'HO', 1, '7480201_A00', 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0.00),
('7480201', 'A01', 'TO', 2, 'LI', 1, 'N1', 1, '7480201_A01', 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0.00),
('7340101', 'A01', 'TO', 1, 'LI', 1, 'N1', 2, '7340101_A01', 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0.00),
('7340101', 'D01', 'TO', 1, 'N1', 2, 'VA', 1, '7340101_D01', 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0.00),
('7220201', 'D01', 'TO', 1, 'N1', 2, 'VA', 1, '7220201_D01', 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0.00),
('7310101', 'A00', 'TO', 2, 'LI', 1, 'HO', 1, '7310101_A00', 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0.00),
('7310101', 'C00', 'VA', 2, 'SU', 1, 'DI', 1, '7310101_C00', 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0.00),
('7320104', 'C00', 'VA', 2, 'SU', 1, 'DI', 1, '7320104_C00', 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0.00);
/*!40000 ALTER TABLE `xt_nganh_tohop` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_nguyenvongxettuyen`
--

DROP TABLE IF EXISTS `xt_nguyenvongxettuyen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_nguyenvongxettuyen` (
  `idnv` int NOT NULL AUTO_INCREMENT,
  `nn_cccd` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `nv_manganh` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `nv_tt` int NOT NULL,
  `diem_thxt` decimal(10,5) DEFAULT NULL COMMENT 'đã cộng điểm môn chính',
  `diem_utqd` decimal(10,5) DEFAULT NULL COMMENT 'Điểm UTQD theo tổ họp sẽ khác nhau.',
  `diem_cong` decimal(6,2) DEFAULT NULL COMMENT 'Tong 3 mon chua tinh mon chinh + diem uu tien\\\\\\\\n',
  `diem_xettuyen` decimal(10,5) DEFAULT NULL COMMENT 'đã cộng điểm ưu tiên',
  `nv_ketqua` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `nv_keys` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `tt_phuongthuc` varchar(45) DEFAULT NULL,
  `tt_thm` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idnv`),
  UNIQUE KEY `nv_keys_UNIQUE` (`nv_keys`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_nguyenvongxettuyen`
--

LOCK TABLES `xt_nguyenvongxettuyen` WRITE;
/*!40000 ALTER TABLE `xt_nguyenvongxettuyen` DISABLE KEYS */;
INSERT INTO `xt_nguyenvongxettuyen`
(`nn_cccd`, `nv_manganh`, `nv_tt`, `diem_thxt`, `diem_utqd`, `diem_cong`, `diem_xettuyen`, `nv_ketqua`, `nv_keys`, `tt_phuongthuc`, `tt_thm`)
SELECT
  CONCAT('079000000', LPAD(s.n, 3, '0')) AS nn_cccd,
  CASE MOD(s.n + p.nv_tt, 5)
    WHEN 0 THEN '7480201'
    WHEN 1 THEN '7340101'
    WHEN 2 THEN '7220201'
    WHEN 3 THEN '7310101'
    ELSE '7320104'
  END AS nv_manganh,
  p.nv_tt,
  ROUND(20.0 + MOD(s.n, 6) + (3 - p.nv_tt) * 0.4, 5) AS diem_thxt,
  ROUND(MOD(s.n, 4) * 0.25, 5) AS diem_utqd,
  ROUND(MOD(s.n, 5) * 0.20, 2) AS diem_cong,
  ROUND(20.0 + MOD(s.n, 6) + (3 - p.nv_tt) * 0.4 + MOD(s.n, 4) * 0.25, 5) AS diem_xettuyen,
  CASE
    WHEN p.nv_tt = 1 AND MOD(s.n, 4) <> 0 THEN 'TRUNG_TUYEN'
    WHEN p.nv_tt = 2 AND MOD(s.n, 4) = 0 THEN 'TRUNG_TUYEN'
    ELSE 'CHO_XU_LY'
  END AS nv_ketqua,
  CONCAT(CONCAT('079000000', LPAD(s.n, 3, '0')), '_NV', p.nv_tt) AS nv_keys,
  CASE MOD(s.n, 3)
    WHEN 0 THEN 'THPT'
    WHEN 1 THEN 'DGNL'
    ELSE 'VSAT'
  END AS tt_phuongthuc,
  CASE MOD(s.n, 4)
    WHEN 0 THEN 'A00'
    WHEN 1 THEN 'A01'
    WHEN 2 THEN 'D01'
    ELSE 'C00'
  END AS tt_thm
FROM (
  SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
  UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
  UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
  UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
  UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25
) s
JOIN (
  SELECT 1 AS nv_tt
  UNION ALL
  SELECT 2 AS nv_tt
) p;
/*!40000 ALTER TABLE `xt_nguyenvongxettuyen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_thisinhxettuyen25`
--

DROP TABLE IF EXISTS `xt_thisinhxettuyen25`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_thisinhxettuyen25` (
  `idthisinh` int NOT NULL AUTO_INCREMENT,
  `cccd` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sobaodanh` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `ho` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `ten` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `ngay_sinh` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `dien_thoai` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `password` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `gioi_tinh` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `noi_sinh` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `updated_at` date DEFAULT NULL,
  `doi_tuong` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `khu_vuc` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`idthisinh`),
  UNIQUE KEY `cccd_UNIQUE` (`cccd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_thisinhxettuyen25`
--

LOCK TABLES `xt_thisinhxettuyen25` WRITE;
/*!40000 ALTER TABLE `xt_thisinhxettuyen25` DISABLE KEYS */;
INSERT INTO `xt_thisinhxettuyen25`
(`cccd`, `sobaodanh`, `ho`, `ten`, `ngay_sinh`, `dien_thoai`, `password`, `gioi_tinh`, `email`, `noi_sinh`, `updated_at`, `doi_tuong`, `khu_vuc`)
SELECT
  CONCAT('079000000', LPAD(s.n, 3, '0')) AS cccd,
  CONCAT('SBD', LPAD(s.n, 5, '0')) AS sobaodanh,
  CASE MOD(s.n, 5)
    WHEN 0 THEN 'Nguyen Van'
    WHEN 1 THEN 'Tran Thi'
    WHEN 2 THEN 'Le Quang'
    WHEN 3 THEN 'Pham Gia'
    ELSE 'Do Minh'
  END AS ho,
  CONCAT('TS', LPAD(s.n, 2, '0')) AS ten,
  CONCAT(2003 + MOD(s.n, 4), '-', LPAD(1 + MOD(s.n, 12), 2, '0'), '-', LPAD(1 + MOD(s.n, 28), 2, '0')) AS ngay_sinh,
  CONCAT('09', LPAD(10000000 + s.n, 8, '0')) AS dien_thoai,
  CONCAT('hash_', LPAD(s.n, 3, '0')) AS password,
  CASE WHEN MOD(s.n, 2) = 0 THEN 'Nam' ELSE 'Nu' END AS gioi_tinh,
  CONCAT('thisinh', LPAD(s.n, 2, '0'), '@mhpl.edu.vn') AS email,
  CASE MOD(s.n, 4)
    WHEN 0 THEN 'Ha Noi'
    WHEN 1 THEN 'Da Nang'
    WHEN 2 THEN 'Hue'
    ELSE 'TP HCM'
  END AS noi_sinh,
  DATE_SUB(CURDATE(), INTERVAL MOD(s.n, 30) DAY) AS updated_at,
  CASE MOD(s.n, 3)
    WHEN 0 THEN 'UT1'
    WHEN 1 THEN 'UT2'
    ELSE 'UT3'
  END AS doi_tuong,
  CASE MOD(s.n, 3)
    WHEN 0 THEN 'KV1'
    WHEN 1 THEN 'KV2'
    ELSE 'KV3'
  END AS khu_vuc
FROM (
  SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
  UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
  UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
  UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
  UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25
) s;
/*!40000 ALTER TABLE `xt_thisinhxettuyen25` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_tohop_monthi`
--

DROP TABLE IF EXISTS `xt_tohop_monthi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_tohop_monthi` (
  `idtohop` int NOT NULL AUTO_INCREMENT,
  `matohop` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,
  `mon1` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `mon2` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `mon3` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `tentohop` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`idtohop`),
  UNIQUE KEY `matohop_UNIQUE` (`matohop`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_tohop_monthi`
--

LOCK TABLES `xt_tohop_monthi` WRITE;
/*!40000 ALTER TABLE `xt_tohop_monthi` DISABLE KEYS */;
INSERT INTO `xt_tohop_monthi`
(`matohop`, `mon1`, `mon2`, `mon3`, `tentohop`)
VALUES
('A00', 'TO', 'LI', 'HO', 'Toan - Ly - Hoa'),
('A01', 'TO', 'LI', 'N1', 'Toan - Ly - Tieng Anh'),
('D01', 'TO', 'N1', 'VA', 'Toan - Tieng Anh - Van'),
('C00', 'VA', 'SU', 'DI', 'Van - Su - Dia');
/*!40000 ALTER TABLE `xt_tohop_monthi` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-11 16:08:42
