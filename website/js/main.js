/* script type="text/javascript" */

/* for Accordions */
function collapse(id) {
  var x = document.getElementById(id);
  if (x.className.indexOf("block-show") == -1) {
    x.className += " block-show";
  } else {
    x.className = x.className.replace(" block-show", "");
  }
}
