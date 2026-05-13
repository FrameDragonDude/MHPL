package backend.dto;

public class NguyenVongXetTuyenGenerationResultDTO {

	private boolean replaceExisting;
	private int sourceRows;
	private int generatedRows;
	private int insertedRows;
	private int skippedRows;
	private int deletedExistingRows;
	private String message;

	public boolean isReplaceExisting() {
		return replaceExisting;
	}

	public void setReplaceExisting(boolean replaceExisting) {
		this.replaceExisting = replaceExisting;
	}

	public int getSourceRows() {
		return sourceRows;
	}

	public void setSourceRows(int sourceRows) {
		this.sourceRows = sourceRows;
	}

	public int getGeneratedRows() {
		return generatedRows;
	}

	public void setGeneratedRows(int generatedRows) {
		this.generatedRows = generatedRows;
	}

	public int getInsertedRows() {
		return insertedRows;
	}

	public void setInsertedRows(int insertedRows) {
		this.insertedRows = insertedRows;
	}

	public int getSkippedRows() {
		return skippedRows;
	}

	public void setSkippedRows(int skippedRows) {
		this.skippedRows = skippedRows;
	}

	public int getDeletedExistingRows() {
		return deletedExistingRows;
	}

	public void setDeletedExistingRows(int deletedExistingRows) {
		this.deletedExistingRows = deletedExistingRows;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
