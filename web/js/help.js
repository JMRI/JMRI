/*
 * common javascript for help pages
 * note: requires jQuery be loaded
 */

// execute any server-side includes, since JMRI Web Server doesn't do them server-side
function includeSSI() {
    $("*").contents().filter(function(){
        return (this.nodeType == 8 && this.nodeValue.startsWith("#include")); //find #include comments
    }).each(function(i, e) {
        console.log("including " + e.nodeValue);
        m = e.nodeValue.match('"(.*?)"'); //get the url which is in double-quotes
        if (m) { 
            u = m[1]; //m[1] now has the url to be retrieved and inserted
            if (u.charAt(0) == "/" && u.match(/\//g).length == 1) { //if url is to root, change it to /web/ssi, since root is not allowed for JMRI Web Server
                u = "/help/en/parts" + u; //redirect includes from root to parts folder
            }
            n = "ssi-" + u.replaceAll("/","").replace("\.shtml","").replaceAll("\.","").toLowerCase(); //clean up url for use as a name            
            d = $("<span id='"+n+"'>Loading '"+u+"', please wait...</span>"); //create a span with id to hold the included html
            $(e).replaceWith(d); //put the span where the comment was
            $("span#" + n).load(u, function(responseTxt, statusTxt, xhr) { //request that the html be loaded into the span
                if (statusTxt == "success") {
                    includeSSI(); //recursive call since includes can have includes
                }
            });
        }
    });
}

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

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
// perform tasks that all BootStrap-based servlets need
$(document).ready(function () {
    includeSSI(); //find any unresolved server-side includes and process them
});