/* script type="text/javascript" */

    function side_open() {
      var a = document.getElementById("side");
      var b = document.getElementById("show-side");
      var c = document.getElementById("close-side");
      if (a.style.display == "block") {
        a.style.display = "none";
        b.innerHTML = "<i class='fa fa-bars'></i>";
        c.style.display = "none";
      } else {
        a.style.display = "block";
        b.innerHTML = "<i class='fa fa-close'></i>";
        c.style.display = "block";
      }
    }

    function side_close() {
      var a = document.getElementById("side");
      var b = document.getElementById("show-side");
      var c = document.getElementById("close-side");
      b.innerHTML = "<i class='fa fa-bars'></i>";
      a.style.display = "none";
      c.style.display = "none";
    }
