use xettuyen2026;

DROP TABLE IF EXISTS `xt_quydoi_tienganh`;

CREATE TABLE `xt_quydoi_tienganh` (
    `cccd` VARCHAR(20) NOT NULL,
    `chung_chi` VARCHAR(255) DEFAULT NULL,
    `diem_goc` VARCHAR(20) DEFAULT NULL,
    `diem_quydoi` DECIMAL(4,2) DEFAULT 0.0,
    `diem_cong` DECIMAL(4,2) DEFAULT 0.0,
    PRIMARY KEY (`cccd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Bảng lưu điểm cộng học sinh giỏi/giải thưởng (Từ file: Uu tien xet tuyen.xlsx - ds nguyen vong)
DROP TABLE IF EXISTS `xt_uutien_xettuyen`;

-- 2. Tạo lại bảng với cấu trúc lưu Mã môn học đạt giải
CREATE TABLE `xt_uutien_xettuyen` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `cccd` VARCHAR(20) NOT NULL,
  `cap` VARCHAR(50) DEFAULT NULL,          -- Quốc gia / Tỉnh
  `doi_tuong` VARCHAR(100) DEFAULT NULL,    -- Học sinh giỏi
  `ma_mon` VARCHAR(10) NOT NULL,           -- TO, VA, SU, SI, DI, N1...
  `loai_giai` VARCHAR(50) DEFAULT NULL,     -- Giải Nhất, Nhì, Ba, Khuyến khích
  `diem_cong_trung_mon` DECIMAL(4,2) DEFAULT '0.00', -- Điểm cộng nếu tổ hợp xét tuyển chứa môn này
  `diem_cong_khac_mon` DECIMAL(4,2) DEFAULT '0.00',  -- Điểm cộng nếu tổ hợp không chứa môn này
  INDEX `idx_cccd_priority` (`cccd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;