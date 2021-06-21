/* script type="text/javascript" */

function closeSearch() {
  document.getElementById("searchform").style.display = "none";
}

function open_search(elmnt) {
  var a = document.getElementById("searchform");
  if (a.style.display == "") {
    document.getElementById("hor-nav").style.visibility = "visible";
    document.getElementById("drop-nav").style.visibility = "visible";
    a.style.display = "none";
    elmnt.innerHTML = "<i class='fa fa-search'></i>";
  } else {
      document.getElementById("hor-nav").style.visibility = "hidden";
      document.getElementById("drop-nav").style.visibility = "hidden";
    a.style.display = "";
    if (window.innerWidth > 1000) {
      a.style.width = "40%";
    } else if (window.innerWidth > 768) {
      a.style.width = "60%";
    } else {
      a.style.width = "60%";
    }
    <!-- set cursor in search field (opens keyboard on mobile) -->
    if (document.getElementById("q")) {document.getElementById("q").focus(); }

    elmnt.innerHTML = "<i class='fa fa-close'></i>";
  }
}

function open_more(elmnt) {
  var a = document.getElementById("more");
  var navbar = document.getElementById("mainNav");
  if (a.style.display == "") {
    document.getElementById("hor-nav").style.visibility = "visible";
    a.style.display = "none";
    elmnt.innerHTML = "Help &amp; More <i class='fa fa-caret-down'></i>";
  } else {
      document.getElementById("hor-nav").style.visibility = "hidden";
    a.style.display = "";
    if (window.innerWidth > 1000) {
      a.style.width = "40%";
    } else if (window.innerWidth > 768) {
      a.style.width = "60%";
    } else {
      a.style.width = "100%";
    }
    elmnt.innerHTML = "Help &amp; More <i class='fa fa-caret-up'></i>";
  }
}

// When the user scrolls the page, execute stickyFunction
window.onscroll = function() {
    stickyFunction()
};

// Get the navbar
var navbar = document.getElementById("mainNav");
// Get the offset position of the navbar
var sticky = navbar.offsetTop;
// Add the "sticky" class to the navbar when you reach its scroll position. Remove "sticky" when you leave the scroll position
function stickyFunction() {
  if (window.pageYOffset >= sticky) {
    mainNav.classList.add("sticky")
  } else {
    mainNav.classList.remove("sticky");
  }
}
