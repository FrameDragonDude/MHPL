package backend.controller;

import backend.dto.NguyenVongXetTuyenGenerationResultDTO;
import backend.service.NguyenVongXetTuyenGenerationService;
import java.sql.SQLException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nguyenvongxettuyen")
public class NguyenVongXetTuyenGenerationController {

	private final NguyenVongXetTuyenGenerationService generationService;

	public NguyenVongXetTuyenGenerationController(NguyenVongXetTuyenGenerationService generationService) {
		this.generationService = generationService;
	}

	@PostMapping("/generate")
	public ResponseEntity<NguyenVongXetTuyenGenerationResultDTO> generate(
			@RequestParam(defaultValue = "false") boolean replaceExisting) {
		try {
			return ResponseEntity.ok(generationService.generate(replaceExisting));
		} catch (SQLException ex) {
			NguyenVongXetTuyenGenerationResultDTO result = new NguyenVongXetTuyenGenerationResultDTO();
			result.setReplaceExisting(replaceExisting);
			result.setMessage(ex.getMessage() == null ? "Lỗi sinh dữ liệu xét tuyển" : ex.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
		}
	}
}
