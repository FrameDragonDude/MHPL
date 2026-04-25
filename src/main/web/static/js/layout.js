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

	if (!dgnlForm || !dgnlResultCard) {
		return;
	}

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
});