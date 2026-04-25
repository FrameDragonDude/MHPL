package webthisinh.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/calculator")
public class ScoreCalculatorController {
	@GetMapping("/dgnl")
	public String dgnlPage() {
		return "DGNLcalculator";
	}

	@GetMapping("/vsat-thpt")
	public String vsatThptPage() {
		return "VSAT-THPTcalculator";
	}
}
