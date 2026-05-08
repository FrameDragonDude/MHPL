-- Rebuild xt_nguyenvongxettuyen from source tables in MySQL
-- Rules:
--   diem_thxt = score of the primary subject chosen by xt_nganh_tohop flags
--   diem_utqd = 0 for now
--   diem_cong = TO + VA + LI
--   diem_xettuyen = diem_cong + diem_thxt + diem_utqd
--   nv_ketqua = Đậu / Dưới sàn based on a cutoff score

SET NAMES utf8mb4;

TRUNCATE TABLE xt_nguyenvongxettuyen;

WITH computed AS (
	SELECT
		nv.cccd AS nn_cccd,
		nv.maxettuyen AS nv_manganh,
		nv.thutunv AS nv_tt,
		CASE
			WHEN nth.`N1` = 1 THEN COALESCE(dxt.`N1_CC`, 0)
			WHEN nth.`TO` = 1 THEN COALESCE(dxt.`TO`, 0)
			WHEN nth.`LI` = 1 THEN COALESCE(dxt.`LI`, 0)
			WHEN nth.`HO` = 1 THEN COALESCE(dxt.`HO`, 0)
			WHEN nth.`SI` = 1 THEN COALESCE(dxt.`SI`, 0)
			WHEN nth.`VA` = 1 THEN COALESCE(dxt.`VA`, 0)
			WHEN nth.`SU` = 1 THEN COALESCE(dxt.`SU`, 0)
			WHEN nth.`DI` = 1 THEN COALESCE(dxt.`DI`, 0)
			WHEN nth.`TI` = 1 THEN COALESCE(dxt.`TI`, 0)
			WHEN nth.`KTPL` = 1 THEN COALESCE(dxt.`KTPL`, 0)
			ELSE 0
		END AS diem_thxt,
		CAST(0 AS DECIMAL(10,5)) AS diem_utqd,
		CAST(
			COALESCE(dxt.`TO`, 0) +
			COALESCE(dxt.`VA`, 0) +
			COALESCE(dxt.`LI`, 0)
			AS DECIMAL(6,2)
		) AS diem_cong,
		CAST(
			COALESCE(dxt.`TO`, 0) +
			COALESCE(dxt.`VA`, 0) +
			COALESCE(dxt.`LI`, 0) +
			CASE
				WHEN nth.`N1` = 1 THEN COALESCE(dxt.`N1_CC`, 0)
				WHEN nth.`TO` = 1 THEN COALESCE(dxt.`TO`, 0)
				WHEN nth.`LI` = 1 THEN COALESCE(dxt.`LI`, 0)
				WHEN nth.`HO` = 1 THEN COALESCE(dxt.`HO`, 0)
				WHEN nth.`SI` = 1 THEN COALESCE(dxt.`SI`, 0)
				WHEN nth.`VA` = 1 THEN COALESCE(dxt.`VA`, 0)
				WHEN nth.`SU` = 1 THEN COALESCE(dxt.`SU`, 0)
				WHEN nth.`DI` = 1 THEN COALESCE(dxt.`DI`, 0)
				WHEN nth.`TI` = 1 THEN COALESCE(dxt.`TI`, 0)
				WHEN nth.`KTPL` = 1 THEN COALESCE(dxt.`KTPL`, 0)
				ELSE 0
			END
			AS DECIMAL(10,5)
		) AS diem_xettuyen,
		CONCAT(nv.cccd, '-', nv.maxettuyen, '-', nv.thutunv) AS nv_keys,
		COALESCE(NULLIF(TRIM(dxt.d_phuongthuc), ''), 'PT2') AS tt_phuongthuc,
		CASE
			WHEN nth.manganh IS NULL THEN 'no'
			ELSE 'yes'
		END AS tt_thm
	FROM xt_nguyen_vong nv
	LEFT JOIN xt_nganh_tohop nth ON nv.maxettuyen = nth.manganh
	LEFT JOIN xt_diemthixettuyen dxt ON dxt.cccd = nv.cccd
)
INSERT INTO xt_nguyenvongxettuyen
(
	nn_cccd,
	nv_manganh,
	nv_tt,
	diem_thxt,
	diem_utqd,
	diem_cong,
	diem_xettuyen,
	nv_ketqua,
	nv_keys,
	tt_phuongthuc,
	tt_thm
)
SELECT
	nn_cccd,
	nv_manganh,
	nv_tt,
	diem_thxt,
	diem_utqd,
	diem_cong,
	diem_xettuyen,
	CASE
		WHEN diem_xettuyen >= 15.00 THEN 'Đậu'
		ELSE 'Dưới sàn'
	END AS nv_ketqua,
	nv_keys,
	tt_phuongthuc,
	tt_thm
FROM computed;
