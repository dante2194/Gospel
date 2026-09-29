(function () {
  'use strict';

  const selectors = [
    '.col-md-4',
    '.content-box',
    '.footer',
    '#st-2',
    '.sphead-header',
    '.sphead-h2',
    '.head',
    '.sdh-toolbar'
  ];

  function removeTargets() {
    selectors.forEach(function (selector) {
      document.querySelectorAll(selector).forEach(function (el) {
        el.remove();
      });
    });
  }

  function install() {
    removeTargets();
    if (window.__spotifyWebCleanupObserver) return;
    const observer = new MutationObserver(removeTargets);
    observer.observe(document.documentElement || document.body, {
      childList: true,
      subtree: true
    });
    window.__spotifyWebCleanupObserver = observer;
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', install, { once: true });
  } else {
    install();
  }
})();
