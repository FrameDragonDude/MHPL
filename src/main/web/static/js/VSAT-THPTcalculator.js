document.addEventListener('DOMContentLoaded', () => {
	const pills = document.querySelectorAll('.mode-pill');

	pills.forEach((pill) => {
		pill.addEventListener('click', () => {
			pills.forEach((item) => item.classList.remove('active'));
			pill.classList.add('active');
		});
	});
});
