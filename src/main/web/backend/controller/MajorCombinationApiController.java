package backend.controller;

import backend.hibernate.MajorCombinationRepository;
import dto.MajorCombinationDTO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/majors")
public class MajorCombinationApiController {

	private final MajorCombinationRepository majorCombinationRepository;

	public MajorCombinationApiController(MajorCombinationRepository majorCombinationRepository) {
		this.majorCombinationRepository = majorCombinationRepository;
	}

	@GetMapping("/{majorCode}/combinations")
	public List<MajorCombinationDTO> getCombinationsByMajor(@PathVariable String majorCode) {
		return majorCombinationRepository.findByMajorCode(majorCode);
	}
}