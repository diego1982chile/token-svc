const THEME_STORAGE_KEY = 'identity-theme';

function applyTheme(darkMode) {
  document.documentElement.toggleAttribute('theme', false);
  document.documentElement.setAttribute('theme', darkMode ? 'dark' : '');
}

function isDarkMode() {
  return localStorage.getItem(THEME_STORAGE_KEY) === 'dark';
}

window.IdentityTheme = {
  apply: applyTheme,
  isDark: isDarkMode,
  set(darkMode) {
    localStorage.setItem(THEME_STORAGE_KEY, darkMode ? 'dark' : 'light');
    applyTheme(darkMode);
  }
};

applyTheme(isDarkMode());
