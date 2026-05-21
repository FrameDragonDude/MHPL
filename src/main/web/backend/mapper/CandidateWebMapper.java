package backend.mapper;

import dal.entities.CandidateEntity;
import org.springframework.stereotype.Component;

import backend.dto.CandidateLookupViewModel;
import backend.hibernate.CandidateLookupRepository;

@Component
public class CandidateWebMapper {

	public CandidateLookupViewModel toViewModel(CandidateEntity candidate) {
		CandidateLookupViewModel viewModel = new CandidateLookupViewModel();
		if (candidate == null) {
			return viewModel;
		}
		viewModel.setUsername(candidate.getCccd());
		viewModel.setCccd(candidate.getCccd());
		viewModel.setFullName(candidate.getFullName());
		viewModel.setNgaySinh(candidate.getNgaySinh());
		viewModel.setFound(true);
		viewModel.setAuthenticated(true);
		viewModel.setSearched(true);
		return viewModel;
	}

	public void applyAdmission(CandidateLookupViewModel viewModel, CandidateLookupRepository.AdmissionRow admissionRow) {
		if (viewModel == null || admissionRow == null) {
			return;
		}
		viewModel.setAdmitted(true);
		viewModel.setMajorCode(admissionRow.getMajorCode());
		viewModel.setMajorName(admissionRow.getMajorName());
		viewModel.setScore(admissionRow.getScore());
		viewModel.setCombination(admissionRow.getCombination());
		viewModel.setMethod(admissionRow.getMethod());
		viewModel.setResultLabel(admissionRow.getResultLabel());
		viewModel.setDiemThxt(admissionRow.getDiemThxt());
		viewModel.setDiemUtqd(admissionRow.getDiemUtqd());
		viewModel.setDiemCong(admissionRow.getDiemCong());
		viewModel.setDiemXettuyen(admissionRow.getScore());
		viewModel.setDiemSan(admissionRow.getDiemSan());
	}

	public void markNotAdmitted(CandidateLookupViewModel viewModel) {
		if (viewModel == null) {
			return;
		}

		viewModel.setAdmitted(false);
		viewModel.setResultLabel("Không trúng tuyển");
	}

	public void applyAdmissionsList(CandidateLookupViewModel viewModel, java.util.List<CandidateLookupRepository.AdmissionRow> rows) {
		if (viewModel == null) return;
		java.util.List<backend.dto.AdmissionDto> list = new java.util.ArrayList<>();
		boolean anyPositive = false;
		for (CandidateLookupRepository.AdmissionRow r : rows) {
			backend.dto.AdmissionDto dto = new backend.dto.AdmissionDto();
			dto.setMajorCode(r.getMajorCode());
			dto.setMajorName(r.getMajorName());
			dto.setScore(r.getScore());
			dto.setCombination(r.getCombination());
			dto.setMethod(r.getMethod());
			dto.setResultLabel(r.getResultLabel());
			dto.setPriority(r.getPriority());
			dto.setDiemThxt(r.getDiemThxt());
			dto.setDiemUtqd(r.getDiemUtqd());
			dto.setDiemCong(r.getDiemCong());
			dto.setDiemXettuyen(r.getScore());
			dto.setDiemSan(r.getDiemSan());
			list.add(dto);
			String norm = r.getResultLabel() == null ? "" : r.getResultLabel().toLowerCase(java.util.Locale.ROOT);
			if (norm.contains("khongtrungtuyen") || norm.contains("chuatrungtuyen")) {
				continue;
			}
			if (norm.contains("trungtuyen") || norm.contains("dat") || norm.contains("dau") || norm.contains("pass") || norm.contains("accepted")) {
				anyPositive = true;
			}
		}
		viewModel.setAdmissions(list);
		viewModel.setAdmitted(anyPositive);
	}
}
