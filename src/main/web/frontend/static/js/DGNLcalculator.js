document.addEventListener('DOMContentLoaded', () => {
	const majorSelect = document.getElementById('majorSelect');
	const form = document.getElementById('dgnlForm');
	const resultsCard = document.getElementById('resultsCard');

	// Load majors from API
	if (majorSelect) {
		loadMajorsFromApi(majorSelect);
	}

	// Handle form submission
	if (form) {
		form.addEventListener('submit', (e) => {
			e.preventDefault();
			calculateScore();
		});
	}
});

async function loadMajorsFromApi(selectElement) {
	const apiUrl = '/api/majors';
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
			option.textContent = major.label || `${major.code} - ${major.name}` || major.code;
			selectElement.appendChild(option);
		});
	} catch (error) {
		console.error('Không tải được danh sách ngành:', error);
	}
}

function calculateScore() {
	const testScore = parseFloat(document.getElementById('testScore').value) || 0;
	const bonusScore = parseFloat(document.getElementById('bonusScore').value) || 0;
	const areaPreference = document.getElementById('areaPreference').value;
	const objectPreference = document.getElementById('objectPreference').value;
	const majorSelect = document.getElementById('majorSelect');
	
	// Validate
	if (!majorSelect.value) {
		alert('Vui lòng chọn ngành!');
		return;
	}
	
	if (testScore < 0 || testScore > 1200) {
		alert('Điểm thi phải từ 0 đến 1200!');
		return;
	}
	
	// Score conversion from 1200 scale to 30 scale
	const convertedScore = (testScore / 1200) * 30;
	
	// Calculate preference bonus
	let preferenceBonus = 0;
	if (areaPreference && areaPreference !== '') {
		preferenceBonus = 1.0; // Example: KV1 = 1.0 point
	}
	if (objectPreference && objectPreference !== '') {
		preferenceBonus += 0.5; // Example: UT1 = +0.5 point
	}
	
	// Final score
	const finalScore = convertedScore + bonusScore + preferenceBonus;
	
	// Display results
	const resultsCard = document.getElementById('resultsCard');
	const selectedOption = majorSelect.options[majorSelect.selectedIndex];
	const selectedMajorLabel = selectedOption?.textContent?.trim() || majorSelect.value;
	document.getElementById('resultTestScore').textContent = testScore.toFixed(2);
	document.getElementById('resultConvertedScore').textContent = convertedScore.toFixed(2);
	document.getElementById('resultBonus').textContent = bonusScore.toFixed(2);
	document.getElementById('resultPreference').textContent = preferenceBonus.toFixed(2);
	document.getElementById('resultFinal').textContent = finalScore.toFixed(2);
	document.getElementById('resultScore').textContent = finalScore.toFixed(2);
	document.getElementById('resultFormula').textContent = `(${testScore.toFixed(2)} / 1200) × 30 = ${convertedScore.toFixed(2)}`;
	document.getElementById('resultsTitle').textContent = `Tính quỹ đối điểm xét tuyển DGNL: ${selectedMajorLabel}`;
	
	// Show results card
	resultsCard.classList.add('show');
	
	// Scroll to results
	setTimeout(() => {
		resultsCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
	}, 100);
}
