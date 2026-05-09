document.addEventListener('DOMContentLoaded', () => {
	const form = document.getElementById('vsatForm');
	const majorSelect = document.getElementById('majorSelect');
	const resultsCard = document.getElementById('resultsCard');
	const resetLink = document.getElementById('resultReset');
	const examTypeSelect = document.getElementById('examType');
	const majorCombinationCache = new Map();

	// Load majors from API
	loadMajorsFromApi(majorSelect);

	const getNumber = (value) => {
		const parsed = Number.parseFloat(value);
		return Number.isFinite(parsed) ? parsed : 0;
	};

	const fmt = (value) => getNumber(value).toFixed(2);

	const setStatus = (element, passed, okText, failText, neutralText) => {
		if (passed === null) {
			element.className = 'dgnl-status muted';
			element.innerHTML = neutralText;
			return;
		}

		element.className = `dgnl-status ${passed ? 'success' : 'danger'}`;
		element.innerHTML = passed ? okText : failText;
	};

	const subjectFields = {
		mathScore: 'Toán',
		literatureScore: 'Ngữ văn',
		physicsScore: 'Vật lý',
		chemistryScore: 'Hóa học',
		biologyScore: 'Sinh học',
		englishScore: 'Tiếng Anh',
		historyScore: 'Lịch sử',
		geographyScore: 'Địa lý',
		khxhScore: 'KHXH',
		khtnScore: 'KHTN'
	};

	const comboSubjects = {
		A00: ['mathScore', 'physicsScore', 'chemistryScore'],
		A01: ['mathScore', 'physicsScore', 'englishScore'],
		B03: ['mathScore', 'biologyScore', 'literatureScore'],
		C01: ['literatureScore', 'mathScore', 'physicsScore'],
		D01: ['literatureScore', 'mathScore', 'englishScore']
	};

	const subjectNameToField = {
		toan: 'mathScore',
		van: 'literatureScore',
		'ngu van': 'literatureScore',
		ly: 'physicsScore',
		'vat ly': 'physicsScore',
		hoa: 'chemistryScore',
		'hoa hoc': 'chemistryScore',
		sinh: 'biologyScore',
		'sinh hoc': 'biologyScore',
		anh: 'englishScore',
		'tieng anh': 'englishScore',
		su: 'historyScore',
		'lich su': 'historyScore',
		dia: 'geographyScore',
		'dia ly': 'geographyScore',
		'khxh': 'khxhScore',
		'khtn': 'khtnScore'
	};

	const normalizeText = (value) => (value || '')
		.toString()
		.trim()
		.toLowerCase()
		.normalize('NFD')
		.replace(/[\u0300-\u036f]/g, '')
		.replace(/đ/g, 'd')
		.replace(/[^a-z0-9]+/g, ' ')
		.replace(/\s+/g, ' ')
		.trim();

	const getSubjectScoreByName = (subjectScores, subjectName) => {
		const field = subjectNameToField[normalizeText(subjectName)];
		return field ? getNumber(subjectScores[field]) : 0;
	};

	const joinSubjects = (combination) => [combination.mon1, combination.mon2, combination.mon3]
		.filter(Boolean)
		.join(', ');

	const formatCombinationBreakdown = (combination, subjectScores) => {
		const subjects = [combination.mon1, combination.mon2, combination.mon3]
			.filter(Boolean)
			.map((subjectName) => ({
				name: subjectName,
				score: getSubjectScoreByName(subjectScores, subjectName)
			}));

		return subjects.map((subject) => `${subject.name} ${fmt(subject.score)}`).join(' + ');
	};

	const readScores = (examType) => {
		const result = {};
		Object.keys(subjectFields).forEach((id) => {
			const raw = getNumber(document.getElementById(id)?.value);
			// VSAT mode accepts both raw 150-scale scores (e.g. 90) and already-converted 10-scale scores (e.g. 6.0).
			result[id] = examType === 'vsat' ? (raw > 10 ? raw / 15 : raw) : raw;
		});
		return result;
	};

	const sumCombination = (scores, combination) => {
		const subjects = [combination.mon1, combination.mon2, combination.mon3].filter(Boolean);
		return subjects.reduce((sum, subjectName) => sum + getSubjectScoreByName(scores, subjectName), 0);
	};

	const fetchMajorCombinations = async (majorCode) => {
		if (!majorCode) {
			return [];
		}

		if (majorCombinationCache.has(majorCode)) {
			return majorCombinationCache.get(majorCode);
		}

		const response = await fetch(`/api/majors/${encodeURIComponent(majorCode)}/combinations`, {
			headers: {
				'Accept': 'application/json'
			}
		});

		if (!response.ok) {
			throw new Error(`HTTP ${response.status}`);
		}

		const data = await response.json();
		const combinations = Array.isArray(data) ? data : [];
		majorCombinationCache.set(majorCode, combinations);
		return combinations;
	};

	const renderCombinationRows = (combinations, subjectScores, priorityScore, bonusScore, bestCode) => {
		const container = document.getElementById('comboResultsBody');
		if (!container) {
			return;
		}

		container.innerHTML = '';

		const sorted = [...combinations]
			.map((combination) => {
				const rawScore = sumCombination(subjectScores, combination);
				const doLechScore = getNumber(combination.doLech);
				const totalScore = rawScore + priorityScore + bonusScore + doLechScore;
				return {
					...combination,
					rawScore,
					doLechScore,
					totalScore
				};
			})
			.sort((left, right) => right.totalScore - left.totalScore);

		sorted.forEach((combination, index) => {
			const row = document.createElement('div');
			row.className = `details-row${combination.maToHop === bestCode ? ' highlight-row' : ''}`;

			const leftCell = document.createElement('span');
			leftCell.textContent = `${index + 1}) ${combination.maToHop || 'Tổ hợp'} - ${joinSubjects(combination)}`;

			const rightCell = document.createElement('span');
				const breakdownText = formatCombinationBreakdown(combination, subjectScores);
				const priorityText = fmt(priorityScore);
				const bonusText = fmt(bonusScore);
				const doLechText = fmt(combination.doLechScore);
				rightCell.innerHTML = `${breakdownText}<br>${fmt(combination.rawScore)} + ${priorityText} + ${bonusText}${combination.doLechScore ? ` + ${doLechText}` : ''} = ${fmt(combination.totalScore)}`;

			row.appendChild(leftCell);
			row.appendChild(rightCell);
			container.appendChild(row);
		});

		return sorted;
	};

	const updateExamTypeUi = () => {
		const alertBox = document.getElementById('vsatTypeAlert');
		const examType = examTypeSelect?.value || 'vsat';

		const subjectInputs = Object.keys(subjectFields)
			.map((id) => document.getElementById(id))
			.filter(Boolean);

		if (examType === 'vsat') {
			alertBox.textContent = 'Điểm thi VSAT: nhập theo thang 150 (ví dụ 90) hoặc thang 10 (ví dụ 6.0)';
			subjectInputs.forEach((input) => {
				input.max = '150';
				input.placeholder = '0 - 150 (hoặc 0 - 10)';
			});
			return;
		}

		alertBox.textContent = 'Điểm thi THPT (thang điểm 10)';
		subjectInputs.forEach((input) => {
			input.max = '10';
			input.placeholder = '0.00';
		});
	};

	if (examTypeSelect) {
		examTypeSelect.addEventListener('change', updateExamTypeUi);
	}
	updateExamTypeUi();

	// Form submission
	form.addEventListener('submit', async (e) => {
		e.preventDefault();

		// Get form values
		const majorCode = majorSelect.value;
		const majorText = majorSelect.options[majorSelect.selectedIndex].text;
		const examType = document.getElementById('examType').value;
		const bonusScore = parseFloat(document.getElementById('bonusScore').value) || 0;
		const areaPreference = document.getElementById('areaPreference').value;
		const objectPreference = document.getElementById('objectPreference').value;

		const subjectScores = readScores(examType);

		if (!majorCode) {
			alert('Vui lòng chọn ngành xét tuyển');
			return;
		}

		const areaPreferenceScore = areaPreference === 'kv1' ? 0.5
			: areaPreference === 'kv2nt' ? 0.75
			: areaPreference === 'kv2' ? 0.25
			: 0;
		const objectPreferenceScore = objectPreference === 'ut1' ? 1.0
			: objectPreference === 'ut2' ? 0.75
			: objectPreference === 'ut3' ? 0.5
			: objectPreference === 'ut6' ? 1.0
			: 0;
		const priorityScore = areaPreferenceScore + objectPreferenceScore;

		const option = majorSelect.options[majorSelect.selectedIndex];
		const threshold = getNumber(option.dataset.threshold) || 19.5;
		const admission = getNumber(option.dataset.admission);

		let combinations;
		try {
			combinations = await fetchMajorCombinations(majorCode);
		} catch (error) {
			console.error('Error loading combinations:', error);
			alert('Không tải được danh sách tổ hợp của ngành đã chọn');
			return;
		}

		if (!combinations.length) {
			alert('Ngành này chưa có tổ hợp nào để xét điểm');
			return;
		}

		const scoredCombinations = combinations.map((combination) => {
			const rawScore = sumCombination(subjectScores, combination);
			const doLechScore = getNumber(combination.doLech);
			const totalScore = rawScore + priorityScore + bonusScore + doLechScore;
			return {
				...combination,
				rawScore,
				doLechScore,
				totalScore
			};
		});

		scoredCombinations.sort((left, right) => right.totalScore - left.totalScore);
		const bestCombination = scoredCombinations[0];
		const bestScore = bestCombination.totalScore;
		const bestCombo = bestCombination.maToHop || '---';

		document.getElementById('resultTitle').textContent = `Tính điểm vào ngành xét tuyển: ${majorText}`;
		document.getElementById('resultBestCombo').textContent = bestCombo;
		document.getElementById('subjectMath').textContent = fmt(subjectScores.mathScore);
		document.getElementById('subjectLiterature').textContent = fmt(subjectScores.literatureScore);
		document.getElementById('subjectPhysics').textContent = fmt(subjectScores.physicsScore);
		document.getElementById('subjectChemistry').textContent = fmt(subjectScores.chemistryScore);
		document.getElementById('subjectBiology').textContent = fmt(subjectScores.biologyScore);
		document.getElementById('subjectEnglish').textContent = fmt(subjectScores.englishScore);
		document.getElementById('subjectHistory').textContent = fmt(subjectScores.historyScore);
		document.getElementById('subjectGeography').textContent = fmt(subjectScores.geographyScore);
		document.getElementById('subjectKhxh').textContent = fmt(subjectScores.khxhScore);
		document.getElementById('subjectKhtn').textContent = fmt(subjectScores.khtnScore);

		document.getElementById('resultAreaPreference').textContent = fmt(areaPreferenceScore);
		document.getElementById('resultObjectPreference').textContent = fmt(objectPreferenceScore);
		document.getElementById('resultBonus').textContent = fmt(bonusScore);
		renderCombinationRows(scoredCombinations, subjectScores, priorityScore, bonusScore, bestCombo);
		document.getElementById('resultConvertedScore').textContent = examType === 'vsat'
			? fmt(Object.values(subjectScores).reduce((sum, value) => sum + value, 0) / Object.keys(subjectScores).length)
			: 'Không áp dụng';
		document.getElementById('resultTotal').textContent = fmt(bestScore);

		setStatus(
			document.getElementById('resultThresholdStatus'),
			bestScore >= threshold,
			`&#10003; So với điểm ngưỡng (${fmt(threshold)}): <strong>ĐẠT</strong>`,
			`&#10007; So với điểm ngưỡng (${fmt(threshold)}): <strong>CHƯA ĐẠT</strong>`,
			'Không có dữ liệu điểm ngưỡng'
		);

		setStatus(
			document.getElementById('resultAdmissionStatus'),
			admission > 0 ? bestScore >= admission : null,
			`&#10003; So với điểm trúng tuyển (${fmt(admission)}): <strong>ĐẠT</strong>`,
			`&#10007; So với điểm trúng tuyển (${fmt(admission)}): <strong>CHƯA ĐẠT</strong>`,
			'! So với điểm trúng tuyển: <strong>CHƯA CÓ DỮ LIỆU</strong>'
		);

		// Show results card
		resultsCard.classList.add('show');
		resultsCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
	});

	// Reset button
	form.addEventListener('reset', () => {
		resultsCard.classList.remove('show');
		updateExamTypeUi();
	});

	if (resetLink) {
		resetLink.addEventListener('click', (event) => {
			event.preventDefault();
			form.reset();
			resultsCard.classList.remove('show');
		});
	}

	// Load majors from API
	async function loadMajorsFromApi(selectElement) {
		if (!selectElement) {
			return;
		}

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

			const data = await response.json();
			selectElement.innerHTML = '<option value="">-- Chọn ngành --</option>';

			if (!Array.isArray(data) || data.length === 0) {
				const emptyOption = document.createElement('option');
				emptyOption.value = '';
				emptyOption.textContent = 'Không có dữ liệu ngành';
				emptyOption.disabled = true;
				selectElement.appendChild(emptyOption);
				return;
			}

			data.forEach((major) => {
				const option = document.createElement('option');
				option.value = major.code || '';
				option.textContent = major.label || [major.code, major.name].filter(Boolean).join(' - ') || major.code || '';
				option.dataset.code = major.code || '';
				option.dataset.c1 = major.c1 || 'B03';
				option.dataset.c2 = major.c2 || 'C01';
				option.dataset.threshold = major.threshold || '19.5';
				option.dataset.admission = major.admission || '0.0';
				selectElement.appendChild(option);
			});
		} catch (error) {
			console.error('Error loading majors:', error);
			selectElement.innerHTML = '<option value="">Không tải được danh sách ngành</option>';
		}
	}
});

