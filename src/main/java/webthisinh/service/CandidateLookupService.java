package webthisinh.service;

import dal.entities.CandidateEntity;
import java.time.LocalDate;
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

		List<CandidateLookupRepository.AdmissionRow> admissions = candidateLookupRepository.findAdmissionsByCccd(username);
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

		List<DateTimeFormatter> formatters = List.of(
				DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.getDefault()),
				DateTimeFormatter.ofPattern("d/M/uuuu", Locale.getDefault()),
				DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.getDefault()),
				DateTimeFormatter.ofPattern("ddMMyyyy", Locale.getDefault())
		);

		for (DateTimeFormatter formatter : formatters) {
			try {
				LocalDate date = LocalDate.parse(input, formatter);
				return date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
			} catch (DateTimeParseException ignored) {
			}
		}

		String digits = input.replaceAll("\\D", "");
		if (digits.length() >= 8) {
			return digits.substring(0, 8);
		}
		return digits;
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
