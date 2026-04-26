document.addEventListener('DOMContentLoaded', () => {
	const tabs = document.querySelectorAll('.portal-tab-btn');
	const panels = document.querySelectorAll('[data-tab-panel]');

	const activateTab = (tabName) => {
		tabs.forEach((tab) => {
			const isActive = tab.dataset.tab === tabName;
			tab.classList.toggle('active', isActive);
			tab.setAttribute('aria-selected', String(isActive));
		});

		panels.forEach((panel) => {
			const isActive = panel.dataset.tabPanel === tabName;
			panel.hidden = !isActive;
			panel.classList.toggle('is-active', isActive);
		});
	};

	tabs.forEach((tab) => {
		tab.addEventListener('click', () => activateTab(tab.dataset.tab));
	});

	const hashTab = window.location.hash.replace('#', '');
	activateTab(['lookup', 'dgnl', 'vsat'].includes(hashTab) ? hashTab : 'lookup');

	const dgnlForm = document.getElementById('dgnlForm');
	const dgnlResultCard = document.getElementById('dgnlResultCard');
	const dgnlReset = document.getElementById('dgnlReset');

	const getNumber = (value) => {
		const n = Number.parseFloat(value);
		return Number.isFinite(n) ? n : 0;
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

	if (dgnlForm && dgnlResultCard) {
		dgnlForm.addEventListener('submit', (event) => {
			event.preventDefault();

		const majorSelect = document.getElementById('dgnlMajor');
		const scoreInput = document.getElementById('dgnlScore');
		const bonusInput = document.getElementById('dgnlBonus');
		const areaSelect = document.getElementById('dgnlArea');
		const objectSelect = document.getElementById('dgnlObject');

		if (!majorSelect.value || scoreInput.value === '') {
			dgnlResultCard.hidden = true;
			return;
		}

		const selectedOption = majorSelect.options[majorSelect.selectedIndex];
		const majorName = selectedOption.value;
		const majorCode = selectedOption.dataset.code || '--';
		const combo = selectedOption.dataset.combo || 'D01';
		const threshold = getNumber(selectedOption.dataset.threshold);
		const admission = getNumber(selectedOption.dataset.admission);

		const score = getNumber(scoreInput.value);
		const bonus = getNumber(bonusInput.value);
		const areaPriority = getNumber(areaSelect.value);
		const objectPriority = getNumber(objectSelect.value);
		const priority = areaPriority + objectPriority;

		const converted = (score * 30) / 1200;
		const total = converted + bonus + priority;

		document.getElementById('dgnlResultTitle').textContent = `Tính quy đổi điểm xét tuyển DGNL: ${majorName} (${majorCode})`;
		document.getElementById('dgnlCombo').textContent = combo;
		document.getElementById('dgnlInputScore').textContent = fmt(score);
		document.getElementById('dgnlFormula').textContent = `${fmt(score)} × 30 / 1200 = ${fmt(converted)}`;
		document.getElementById('dgnlConverted').textContent = fmt(converted);
		document.getElementById('dgnlBonusOut').textContent = fmt(bonus);
		document.getElementById('dgnlPriority').textContent = fmt(priority);
		document.getElementById('dgnlTotal').textContent = fmt(total);

		const thresholdPassed = total >= threshold;
		const admissionPassed = admission > 0 ? total >= admission : null;

		setStatus(
			document.getElementById('dgnlThresholdStatus'),
			thresholdPassed,
			`&#10003; So với điểm ngưỡng (${fmt(threshold)}): <strong>ĐẠT</strong>`,
			`&#10007; So với điểm ngưỡng (${fmt(threshold)}): <strong>CHƯA ĐẠT</strong>`,
			'Không có dữ liệu điểm ngưỡng'
		);

		setStatus(
			document.getElementById('dgnlAdmissionStatus'),
			admissionPassed,
			`&#10003; So với điểm trúng tuyển (${fmt(admission)}): <strong>ĐẠT</strong>`,
			`&#10007; So với điểm trúng tuyển (${fmt(admission)}): <strong>CHƯA ĐẠT</strong>`,
			'! So với điểm trúng tuyển: <strong>CHƯA CÓ DỮ LIỆU</strong>'
		);

			dgnlResultCard.hidden = false;
			dgnlResultCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
		});

		dgnlReset.addEventListener('click', (event) => {
			event.preventDefault();
			dgnlForm.reset();
			document.getElementById('dgnlBonus').value = '0.0';
			dgnlResultCard.hidden = true;
		});
	}

	const vsatForm = document.getElementById('vsatForm');
	const vsatResultCard = document.getElementById('vsatResultCard');
	const vsatReset = document.getElementById('vsatReset');
	const vsatTypeAlert = document.getElementById('vsatTypeAlert');

	if (!vsatForm || !vsatResultCard) {
		return;
	}

	const subjectIds = ['vToan', 'vVan', 'vLy', 'vHoa', 'vSinh', 'vAnh', 'vSu', 'vDia', 'vKhxh', 'vKhtn'];

	const readSubjectValues = (examType) => {
		const map = {};
		subjectIds.forEach((id) => {
			const raw = getNumber(document.getElementById(id).value);
			map[id] = examType === 'VSAT' ? (raw * 10.0 / 150.0) : raw;
		});
		return map;
	};

	const comboSubjects = {
		A00: ['vToan', 'vLy', 'vHoa'],
		A01: ['vToan', 'vLy', 'vAnh'],
		B03: ['vToan', 'vSinh', 'vVan'],
		C01: ['vVan', 'vToan', 'vLy'],
		D01: ['vVan', 'vToan', 'vAnh']
	};

	const calcCombo = (scores, combo) => {
		const subs = comboSubjects[combo] || comboSubjects.B03;
		return getNumber(scores[subs[0]]) + getNumber(scores[subs[1]]) + getNumber(scores[subs[2]]);
	};

	const toComboLabel = (combo) => {
		const text = {
			B03: 'Toán, Sinh học, Ngữ văn',
			C01: 'Ngữ văn, Toán, Vật lý',
			D01: 'Ngữ văn, Toán, Tiếng Anh',
			A01: 'Toán, Vật lý, Tiếng Anh',
			A00: 'Toán, Vật lý, Hóa học'
		};
		return `${combo} - ${text[combo] || text.B03}`;
	};

	const updateExamTypeUi = () => {
		const examType = vsatForm.querySelector('input[name="examType"]:checked').value;
		vsatTypeAlert.textContent = examType === 'VSAT'
			? 'Điểm thi VSAT (thang điểm 150)'
			: 'Điểm thi THPT (thang điểm 10)';
	};

	vsatForm.querySelectorAll('input[name="examType"]').forEach((radio) => {
		radio.addEventListener('change', updateExamTypeUi);
	});
	updateExamTypeUi();

	vsatForm.addEventListener('submit', (event) => {
		event.preventDefault();

		const majorSelect = document.getElementById('vsatMajor');
		if (!majorSelect.value) {
			vsatResultCard.hidden = true;
			return;
		}

		const option = majorSelect.options[majorSelect.selectedIndex];
		const majorName = option.value;
		const majorCode = option.dataset.code || '--';
		const c1 = option.dataset.c1 || 'B03';
		const c2 = option.dataset.c2 || 'C01';
		const threshold = getNumber(option.dataset.threshold);
		const admission = getNumber(option.dataset.admission);

		const examType = vsatForm.querySelector('input[name="examType"]:checked').value;
		const scores = readSubjectValues(examType);
		const area = getNumber(document.getElementById('vsatArea').value);
		const obj = getNumber(document.getElementById('vsatObject').value);
		const bonus = getNumber(document.getElementById('vsatBonus').value);
		const priority = area + obj;

		const c1Raw = calcCombo(scores, c1);
		const c2Raw = calcCombo(scores, c2);
		const c1Dx = c1Raw + priority + bonus;
		const c2Dx = c2Raw + priority + bonus;
		const best = Math.max(c1Dx, c2Dx);
		const bestCombo = c1Dx >= c2Dx ? c1 : c2;

		const convertedAvg = examType === 'VSAT'
			? Object.values(scores).reduce((sum, val) => sum + val, 0) / Object.values(scores).length
			: 0;

		document.getElementById('vsatResultTitle').textContent = `Tính điểm vào ngành xét tuyển: ${majorName} (${majorCode})`;
		document.getElementById('vsatBestCombo').textContent = bestCombo;

		document.getElementById('vsatAreaOut').textContent = fmt(area);
		document.getElementById('vsatObjectOut').textContent = fmt(obj);
		document.getElementById('vsatBonusOut').textContent = fmt(bonus);

		document.getElementById('vsatCombo1Title').textContent = `1) Tổ hợp ${toComboLabel(c1)}`;
		document.getElementById('vsatCombo1Value').textContent = `${fmt(c1Raw)} + ${fmt(priority)} = ${fmt(c1Raw + priority)}`;
		document.getElementById('vsatCombo1Dx').textContent = fmt(c1Dx);

		document.getElementById('vsatCombo2Title').textContent = `2) Tổ hợp ${toComboLabel(c2)}`;
		document.getElementById('vsatCombo2Value').textContent = `${fmt(c2Raw)} + ${fmt(priority)} = ${fmt(c2Raw + priority)}`;
		document.getElementById('vsatCombo2Dx').textContent = fmt(c2Dx);

		document.getElementById('vsatConverted').textContent = examType === 'VSAT'
			? fmt(convertedAvg)
			: 'Không áp dụng';

		setStatus(
			document.getElementById('vsatThresholdStatus'),
			best >= threshold,
			`&#10003; So với điểm ngưỡng (${fmt(threshold)}): <strong>ĐẠT</strong>`,
			`&#10007; So với điểm ngưỡng (${fmt(threshold)}): <strong>CHƯA ĐẠT</strong>`,
			'Không có dữ liệu điểm ngưỡng'
		);

		setStatus(
			document.getElementById('vsatAdmissionStatus'),
			admission > 0 ? best >= admission : null,
			`&#10003; So với điểm trúng tuyển (${fmt(admission)}): <strong>ĐẠT</strong>`,
			`&#10007; So với điểm trúng tuyển (${fmt(admission)}): <strong>CHƯA ĐẠT</strong>`,
			'! So với điểm trúng tuyển: <strong>CHƯA CÓ DỮ LIỆU</strong>'
		);

		vsatResultCard.hidden = false;
		vsatResultCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
	});

	if (vsatReset) {
		vsatReset.addEventListener('click', (event) => {
			event.preventDefault();
			vsatForm.reset();
			updateExamTypeUi();
			vsatResultCard.hidden = true;
		});
	}
});