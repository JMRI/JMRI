/*
 * common javascript for help pages
 */

/* for Accordions */
function collapse(id) {
  var x = document.getElementById(id);
  if (x.className.indexOf("block-show") == -1) {
    x.className += " block-show";
  } else {
    x.className = x.className.replace(" block-show", "");
  }
}

// Hide sidebar and search pane when user clicks outside either
window.addEventListener('click', ({target}) => {
    const hit = target.closest('#mainContent');
    if (hit != null) {
        const sidebar = document.getElementById("side");
        const sideburger = document.getElementById("show-side");
        if (sidebar != null && sidebar.style.display == "block" && sideburger.style.display == "") {
            side_close();
        }
        const search = document.getElementById("searchform");
        if (search != null && search.style.display == "") {
          const magni = document.getElementById("magnify");
            open_search(magni); // will close search
        }
    }
});
