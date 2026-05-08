document.addEventListener('DOMContentLoaded', () => {
	const chips = document.querySelectorAll('.toolbar-chip');
	const majorSelect = document.querySelector('#majorSelect');

	chips.forEach((chip) => {
		chip.addEventListener('click', () => {
			chips.forEach((item) => item.classList.remove('active'));
			chip.classList.add('active');
		});
	});

	if (majorSelect) {
		loadMajors(majorSelect);
	}
});

async function loadMajors(selectElement) {
	const apiUrl = selectElement.dataset.apiUrl || '/api/majors';
	try {
		const response = await fetch(apiUrl, {
			headers: {
				'Accept': 'application/json'
			}
		});
		if (!response.ok) {
			throw new Error(`HTTP ${response.status}`);
		}

		const majors = await response.json();
		selectElement.innerHTML = '<option value="">-- Chọn ngành --</option>';
		majors.forEach((major) => {
			const option = document.createElement('option');
			option.value = major.code || '';
			option.textContent = major.label || [major.code, major.name].filter(Boolean).join(' - ') || major.code || '';
			selectElement.appendChild(option);
		});
	} catch (error) {
		console.error('Không tải được danh sách ngành:', error);
	}
}
