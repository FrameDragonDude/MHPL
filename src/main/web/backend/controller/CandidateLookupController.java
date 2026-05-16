package backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import backend.dto.CandidateLookupRequest;
import backend.dto.CandidateLookupViewModel;
import backend.service.CandidateLookupService;

@Controller
@RequestMapping("/lookup")
public class CandidateLookupController {

	private final CandidateLookupService candidateLookupService;

	public CandidateLookupController(CandidateLookupService candidateLookupService) {
		this.candidateLookupService = candidateLookupService;
	}

	@GetMapping
	public String showLookupPage(Model model) {
		model.addAttribute("loginRequest", new CandidateLookupRequest());
		CandidateLookupViewModel result = new CandidateLookupViewModel();
		result.setSearched(false);
		model.addAttribute("result", result);
		return "student-login";
	}

	@PostMapping
	public String lookup(@ModelAttribute("loginRequest") CandidateLookupRequest request, Model model) {
		CandidateLookupViewModel result = candidateLookupService.lookup(request);
		model.addAttribute("loginRequest", request);
		model.addAttribute("result", result);
		return "student-login";
	}
}
