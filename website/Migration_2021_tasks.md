# JMRI Responsive
This file is /Migration_2021_Tasks.md  Written by Egbert Broerse 21-01-2021, updated 01-03-2021.
It describes the necessary steps to migrate jmr.org Help etc files to the CSS3 responsive format and new style.

## Early tasks
- [ ] replace any left-over local Style definitions by an include:`<!--#include virtual="/Style.shtml" -->`
- [x] Check new SVG graphic is present in `/images/jmri-small.svg`
- [x] Check `/js` contains 4 scripts: `here.js`, `main.js`, `side.js` and `header.js`
- [ ] Change all multi-line `<pre><code>` blocks to `<code class="block">` tags
- [ ] Clean up Sidebars: change all occurrences of `<dt><h3>` into`<dt>`(removing `<h3>` and `</h3>`, this was all markup for layout)
- [ ] Change `class="nomenu"` to `class="no-sidebar"`
## Migration tasks
- [x] No changes required to `Style.shtml`
- [x] Replace file `/css/defaults.css` by file `/css/defaults2020.css`
- [ ] In all _Sidebar_ files:
    - [ ] replace `<div id="side"> <!-- Block of text on left side of page -->`
      `<div style="text-align: center">JMRI&reg; is...</div>`    
      `<dl class="doc">` with `<div class="card-1" id="side" > <!-- Block of text on left side of page -->`  
      `<div id="close-side" class="floatRight button fa fa-close" style="display:none;" onclick="side_close()"></div>`
      `<div style="text-align: center;">JMRI&reg; is...</div>`  
      `<dl>`
    - [ ] To the end add: `<!-- button is in /Header -->`
  `<script src=“/js/side.js"></script>`
- [x] Completely replace file _Header.shtml_ with _Header2020.shtml_
- [ ] On all pages:
    - [ ] Replace line 1  
      `<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">`  
      by `<!DOCTYPE html>`
    - [ ] Just before the </body> end tag, add: `<script src="js/main.js"></script>`
- [ ] Place `<img src=` tags inside either a 100%, 70%, 50% or 30% flex-item, like  
  ```<div class="flex-container"><!-- flex is a set for responsive positioning on different screen sizes -->
  <div id="icon-large" class="flex-item-30">
  <!-- Logo -->
  <img src="images/logo-jmri.gif" alt="JMRI Logo" style="object-fit: scale-down;">
  <!-- /Logo -->
  </div>  
  
  <div class="flex-item-70">
- [ ] Remove all `<p>` tags used as spacers. If they are used inside `<li>`, replace them by `<br`
- [ ] convert all PRAGMA redirects to html5 replacement
    
