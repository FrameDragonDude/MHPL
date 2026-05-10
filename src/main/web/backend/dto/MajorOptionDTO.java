package backend.dto;

public class MajorOptionDTO {

	private String code;
	private String name;
	private String label;
	private Double threshold;

	public MajorOptionDTO() {
	}

	public MajorOptionDTO(String code, String name) {
		this.code = code;
		this.name = name;
		this.label = buildLabel(code, name);
	}

	public MajorOptionDTO(String code, String name, Double threshold) {
		this.code = code;
		this.name = name;
		this.label = buildLabel(code, name);
		this.threshold = threshold;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
		this.label = buildLabel(code, this.name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		this.label = buildLabel(this.code, name);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Double getThreshold() {
		return threshold;
	}

	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}

	private String buildLabel(String code, String name) {
		String safeCode = code == null ? "" : code.trim();
		String safeName = name == null ? "" : name.trim();
		if (safeCode.isEmpty()) {
			return safeName;
		}
		if (safeName.isEmpty()) {
			return safeCode;
		}
		return safeCode + " - " + safeName;
	}
}