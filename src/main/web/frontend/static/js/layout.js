document.addEventListener('DOMContentLoaded', () => {
	// Highlight header navigation based on current path.
	const navLinks = document.querySelectorAll('.portal-tab-btn, .nav-btn');
	const path = window.location.pathname.replace(/\/$/, '') || '/';

	navLinks.forEach((a) => {
		try {
			const href = a.getAttribute('href') || '';
			// Normalize both for comparison
			const normalized = href.replace(/\/$/, '') || '/';
			a.classList.toggle('active', normalized === path);
			a.setAttribute('aria-selected', String(normalized === path));
		} catch (e) { /* ignore malformed links */ }
	});
});