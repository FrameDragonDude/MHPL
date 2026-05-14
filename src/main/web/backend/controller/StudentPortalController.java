package backend.controller;

import backend.dto.CandidateLookupRequest;
import backend.dto.CandidateLookupViewModel;
import backend.service.CandidateLookupService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student")
public class StudentPortalController {

	private static final String SESSION_LOOKUP_RESULT = "studentLookupResult";

	private final CandidateLookupService candidateLookupService;

	public StudentPortalController(CandidateLookupService candidateLookupService) {
		this.candidateLookupService = candidateLookupService;
	}

	@GetMapping({"", "/login"})
	public String loginPage(Model model, HttpSession session) {
		if (session.getAttribute(SESSION_LOOKUP_RESULT) instanceof CandidateLookupViewModel) {
			return "redirect:/student/result";
		}
		if (!model.containsAttribute("loginRequest")) {
			model.addAttribute("loginRequest", new CandidateLookupRequest());
		}
		return "student-login";
	}

	@PostMapping("/login")
	public String login(@ModelAttribute("loginRequest") CandidateLookupRequest request,
			HttpSession session,
			RedirectAttributes redirectAttributes) {
		CandidateLookupViewModel result = candidateLookupService.lookup(request);
		if (result == null || !result.isFound()) {
			redirectAttributes.addFlashAttribute("errorMessage",
				result == null || result.getMessage() == null ? "Không tìm thấy thông tin thí sinh." : result.getMessage());
			redirectAttributes.addFlashAttribute("loginRequest", request);
			return "redirect:/student/login";
		}

		session.setAttribute(SESSION_LOOKUP_RESULT, result);
		return "redirect:/student/result";
	}

	@GetMapping("/result")
	public String resultPage(Model model, HttpSession session) {
		Object sessionValue = session.getAttribute(SESSION_LOOKUP_RESULT);
		if (!(sessionValue instanceof CandidateLookupViewModel result)) {
			return "redirect:/student/login";
		}

		model.addAttribute("result", result);
		return "student-result";
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/student/login";
	}
}