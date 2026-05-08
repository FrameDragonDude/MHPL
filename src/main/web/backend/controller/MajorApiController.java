package backend.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.dto.MajorOptionDTO;
import backend.hibernate.MajorLookupRepository;

@RestController
@RequestMapping("/api/majors")
public class MajorApiController {

	private final MajorLookupRepository majorLookupRepository;

	public MajorApiController(MajorLookupRepository majorLookupRepository) {
		this.majorLookupRepository = majorLookupRepository;
	}

	@GetMapping
	public List<MajorOptionDTO> getMajors() {
		return majorLookupRepository.findAllMajors();
	}
}