document.addEventListener('DOMContentLoaded', () => {
	const chips = document.querySelectorAll('.toolbar-chip');

	chips.forEach((chip) => {
		chip.addEventListener('click', () => {
			chips.forEach((item) => item.classList.remove('active'));
			chip.classList.add('active');
		});
	});
});
