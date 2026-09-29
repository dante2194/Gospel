// ==UserScript==
// @name         Spotify Web Declutter
// @namespace    https://github.com/local/spotify-declutter
// @version      1.0
// @description  Replaces the "Get App" nav link with a Liked Songs shortcut and hides promo banners on open.spotify.com
// @author       you
// @match        https://open.spotify.com/*
// @grant        none
// @run-at       document-idle
// ==/UserScript==

(function () {
  'use strict';

  const LIKED_SONGS_URL = 'https://open.spotify.com/collection/tracks';

  // --- Hide promo/banner elements ---------------------------------------
  const style = document.createElement('style');
  style.textContent = `
    div[data-testid="home-banner"],
    .encore-internal-padding-block-end-tighter-2,
    .top-section,
    .bottom-links-container {
      display: none !important;
    }
  `;
  document.head.appendChild(style);

  // --- Replace "Get App" nav link with a Liked Songs shortcut ------------
  // Matched by href rather than the hashed class name, since Spotify's
  // generated class names (e.g. "DeIPd86IX1Q3uT9104WT") can change between
  // deploys, but the /download href is stable.
  const LIBRARY_ICON_SVG = `<svg data-encore-id="icon" role="img" aria-hidden="true" class="e-10860-icon" style="--encore-icon-height:var(--encore-graphic-size-decorative-base);--encore-icon-width:var(--encore-graphic-size-decorative-base)" viewBox="0 0 24 24"><path d="M14.5 2.134a1 1 0 0 1 1 0l6 3.464a1 1 0 0 1 .5.866V21a1 1 0 0 1-1 1h-6a1 1 0 0 1-1-1V3a1 1 0 0 1 .5-.866M16 4.732V20h4V7.041zM3 22a1 1 0 0 1-1-1V3a1 1 0 0 1 2 0v18a1 1 0 0 1-1 1m6 0a1 1 0 0 1-1-1V3a1 1 0 0 1 2 0v18a1 1 0 0 1-1 1"></path></svg>`;

  function convertLink(link) {
    if (!link || link.dataset.declutterDone === '1') return;

    link.removeAttribute('href');
    link.setAttribute('role', 'button');
    link.style.cursor = 'pointer';
    link.innerHTML =
      LIBRARY_ICON_SVG +
      '<span class="e-10860-text encore-text-marginal" data-encore-id="text">Liked Songs</span>';
    link.dataset.declutterDone = '1';

    link.addEventListener('click', (e) => {
      e.preventDefault();
      e.stopPropagation();
      window.open(LIKED_SONGS_URL, '_blank', 'noopener');
    });
  }

  function hideYourLibraryLink() {
    // Matched by href="/" plus visible text rather than the hashed class
    // name, for the same reason as above.
    document.querySelectorAll('a[href="/"]').forEach((el) => {
      if (el.textContent.trim() === 'Your Library') {
        el.style.display = 'none';
      }
    });
  }

  function scan() {
    document.querySelectorAll('a[href="/download"]').forEach(convertLink);
    hideYourLibraryLink();
  }

  scan();

  // Spotify is a single-page app that re-renders the nav on route changes,
  // so keep watching for the link to reappear.
  const observer = new MutationObserver(scan);
  observer.observe(document.body, { childList: true, subtree: true });
})();
