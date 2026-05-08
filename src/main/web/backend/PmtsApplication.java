package backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"backend", "webthisinh"})
public class PmtsApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(PmtsApplication.class, args);
	}
}