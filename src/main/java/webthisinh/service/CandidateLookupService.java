package webthisinh.service;

import dal.entities.CandidateEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import webthisinh.dto.CandidateLookupRequest;
import webthisinh.dto.CandidateLookupViewModel;
import webthisinh.hibernate.CandidateLookupRepository;
import webthisinh.mapper.CandidateWebMapper;

@Service
public class CandidateLookupService {

	private final CandidateLookupRepository candidateLookupRepository;
	private final CandidateWebMapper candidateWebMapper;

	public CandidateLookupService(CandidateLookupRepository candidateLookupRepository, CandidateWebMapper candidateWebMapper) {
		this.candidateLookupRepository = candidateLookupRepository;
		this.candidateWebMapper = candidateWebMapper;
	}

	public CandidateLookupViewModel lookup(CandidateLookupRequest request) {
		CandidateLookupViewModel viewModel = new CandidateLookupViewModel();
		viewModel.setSearched(true);
		if (request == null) {
			viewModel.setMessage("Vui lòng nhập CCCD và mật khẩu.");
			return viewModel;
		}

		String username = safe(request.getUsername());
		String password = safe(request.getPassword());
		viewModel.setUsername(username);

		if (username.isEmpty() || password.isEmpty()) {
			viewModel.setMessage("Vui lòng nhập CCCD và mật khẩu.");
			return viewModel;
		}

		Optional<CandidateEntity> candidateOpt = candidateLookupRepository.findByCccd(username);
		if (candidateOpt.isEmpty()) {
			viewModel.setMessage("Không tìm thấy.");
			viewModel.setFound(false);
			return viewModel;
		}

		CandidateEntity candidate = candidateOpt.get();
		if (!matchesPassword(candidate.getNgaySinh(), password)) {
			viewModel.setMessage("Không tìm thấy.");
			viewModel.setFound(false);
			return viewModel;
		}

		viewModel = candidateWebMapper.toViewModel(candidate);
		viewModel.setMessage("Tìm thấy.");

		List<CandidateLookupRepository.AdmissionRow> admissions;
		try {
			admissions = candidateLookupRepository.findAdmissionsByCccd(username);
		} catch (RuntimeException ex) {
			candidateWebMapper.markNotAdmitted(viewModel);
			viewModel.setMessage("Tìm thấy thông tin thí sinh, nhưng chưa đọc được dữ liệu nguyện vọng.");
			return viewModel;
		}

		Optional<CandidateLookupRepository.AdmissionRow> winning = admissions.stream()
				.filter(row -> isPositiveResult(row.getResultLabel()))
				.findFirst();

		if (winning.isPresent()) {
			candidateWebMapper.applyAdmission(viewModel, winning.get());
			viewModel.setMessage("Tìm thấy.");
		} else {
			candidateWebMapper.markNotAdmitted(viewModel);
			viewModel.setMessage("Tìm thấy nhưng không trúng tuyển.");
		}

		return viewModel;
	}

	private String safe(String value) {
		return value == null ? "" : value.trim();
	}

	private boolean matchesPassword(String ngaySinh, String password) {
		String expected = normalizeDateToEightDigits(ngaySinh);
		String actual = password.replaceAll("\\D", "");
		return !expected.isEmpty() && expected.equals(actual);
	}

	private String normalizeDateToEightDigits(String value) {
		String input = safe(value);
		if (input.isEmpty()) {
			return "";
		}

		String normalized = tryParseDate(input,
				DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.getDefault()),
				DateTimeFormatter.ofPattern("d/M/uuuu", Locale.getDefault()),
				DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.getDefault()),
				DateTimeFormatter.ofPattern("ddMMyyyy", Locale.getDefault())
		);
		if (!normalized.isEmpty()) {
			return normalized;
		}

		normalized = tryParseDateTime(input,
				DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss", Locale.getDefault()),
				DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm", Locale.getDefault()),
				DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss", Locale.getDefault())
		);
		if (!normalized.isEmpty()) {
			return normalized;
		}

		String digits = input.replaceAll("\\D", "");
		if (digits.length() >= 8) {
			String firstEight = digits.substring(0, 8);

			// Ưu tiên nhận diện dạng yyyyMMdd (thường gặp khi DB lưu kèm time: yyyyMMddHHmmss)
			try {
				LocalDate ymd = LocalDate.parse(firstEight, DateTimeFormatter.ofPattern("uuuuMMdd"));
				return ymd.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
			} catch (DateTimeParseException ignored) {
			}

			// Nếu không parse được như yyyyMMdd thì giữ nguyên 8 số đầu (ddMMyyyy)
			return firstEight;
		}
		return digits;
	}

	private String tryParseDate(String input, DateTimeFormatter... formatters) {
		for (DateTimeFormatter formatter : formatters) {
			try {
				LocalDate date = LocalDate.parse(input, formatter);
				return date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
			} catch (DateTimeParseException ignored) {
			}
		}
		return "";
	}

	private String tryParseDateTime(String input, DateTimeFormatter... formatters) {
		for (DateTimeFormatter formatter : formatters) {
			try {
				LocalDateTime dateTime = LocalDateTime.parse(input, formatter);
				return dateTime.toLocalDate().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
			} catch (DateTimeParseException ignored) {
			}
		}
		return "";
	}

	private boolean isPositiveResult(String resultLabel) {
		if (resultLabel == null) {
			return false;
		}
		String normalized = resultLabel.toLowerCase(Locale.ROOT)
				.replace('đ', 'd')
				.replaceAll("\\p{M}+", "")
				.replaceAll("[^a-z0-9]", "");
		return normalized.contains("trungtuyen")
				|| normalized.contains("dau")
				|| normalized.contains("dat")
				|| normalized.contains("accepted")
				|| normalized.contains("pass");
	}
}
