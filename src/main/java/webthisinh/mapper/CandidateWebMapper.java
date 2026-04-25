package webthisinh.mapper;

import dal.entities.CandidateEntity;
import org.springframework.stereotype.Component;
import webthisinh.dto.CandidateLookupViewModel;
import webthisinh.hibernate.CandidateLookupRepository;

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
	}

	public void markNotAdmitted(CandidateLookupViewModel viewModel) {
		if (viewModel == null) {
			return;
		}
		viewModel.setAdmitted(false);
		viewModel.setResultLabel("Không trúng tuyển");
	}
}
