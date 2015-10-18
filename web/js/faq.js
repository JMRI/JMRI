/*
    Turns a DL list into a folded FAQ

    How:
      By surrounding the dt with a link, an empty i element is also added for an icon if required.
      The dd is moved off screen until the dt link is clicked.
      Adding a class="on" to a dt will show the related dd(s).

    Parameters (optional):
      faqClass : "faq",   // class of the dl's to apply folding too, default "faq".
      onClass : "on",     // class applied to show the dd, default "on", also used to switch the dt icon state.
      linkClass : ""      // class added to the dt show/hide link. Default none.

    It is recomended to leave the first question open to provide a hint to the user about the functionality presented.
    Please ensure the link colours used are different to standard links.

    Author: mike foskett mike.foskett@websemantics.co.uk
    http://websemantics.co.uk/resources/simple_faq/
*/

document.documentElement.className="hasJS";

var FAQ = (function () {

  var faqClass,
    onClass,
    lnkClass,
    DLs,
    i,
    ii,
    iii,
    faqDLs = [],
    dts,
    lnkObj,
    ns,
    lnkTxt;

  function hasClass(o,c){return new RegExp('(\\s|^)'+c+'(\\s|$)').test(o.className);}
  function addClass(o,c){if(!hasClass(o,c)){o.className+=' '+c;}}
  function removeClass(o,c){if(hasClass(o,c)){o.className=o.className.replace(new RegExp('(\\s|^)'+c+'(\\s|$)'),' ').replace(/\s+/g,' ').replace(/^\s|\s$/,'');}}

  function getNextSibling(o) {
    var n = o.nextSibling;
    while (n !== null && n.nodeType !== 1) {
      n = n.nextSibling;
    }
    return n;
  }

  function showHideDDs() {
    var dds = this.siblings,
      i = dds.length,
      p = this.parentNode;
    if (hasClass(p, onClass)) { //remove "on" class
      removeClass(p, onClass);
      while (i--) {
        removeClass(dds[i], onClass);
      }
    } else { // add "on" class
      addClass(p, onClass);
      while (i--) {
        addClass(dds[i], onClass);
      }
    }
    return false;
  }

  function init(cfg) {

    // default over-rides
    faqClass = cfg.faqClass || "faq";
    onClass = cfg.onClass || "on";
    lnkClass = cfg.linkClass || "";


    // create a list from all DL's identified by faqClass
    DLs = document.getElementsByTagName("dl");
    i = DLs.length;
    while (i--) {
      if (hasClass(DLs[i], faqClass)) {
        faqDLs.push(DLs[i]);
      }
    }
    i = faqDLs.length;
    while (i--) {

      dts = faqDLs[i].getElementsByTagName('dt');
      ii = dts.length;
      while (ii--) {

        // create an activating link object
        lnkObj = document.createElement('a');

        // get a list of DT siblings and attach to the link object
        lnkObj.siblings = [];
        ns = getNextSibling(dts[ii]);  // the DT's first sibling

        while (ns !== null) {

          // Only interested if it is a DD
          if (ns.tagName === "DD") {
            lnkObj.siblings.push(ns);
            ns = getNextSibling(ns);
          } else {

            // next sibling is not a DD therefore end loop
            ns = null;
          }
        }

        // Only add link object and associated html changes if there is at least one DD
        iii = lnkObj.siblings.length;
        if (iii) {

          // store original text and then empty.
          lnkTxt = dts[ii].innerHTML;
          dts[ii].innerHTML = "";

          // if a class is required for the activating link then add one
          if (lnkClass !== "") {
            lnkObj.className = lnkClass;
          }

          // add click functionality
          lnkObj.href = "#";
          lnkObj.onclick = showHideDDs;

          // add an i element to display a show/hide icon
          lnkObj.appendChild(document.createElement('i'));

          // insert original text back into the new link
          lnkObj.appendChild(document.createTextNode(lnkTxt));

          // add link to HTML
          dts[ii].appendChild(lnkObj);

          // If a DT already has an "on" class, then add the "on" class to its DD's too.
          if (hasClass(dts[ii], onClass)) {
            while (iii--) {
              addClass(lnkObj.siblings[iii], onClass);
            }
          }
        } else {

          // add a B element to reserve space without displaying an icon
          dts[ii].innerHTML = "<b></b>" + dts[ii].innerHTML;
        }
      }
    }
  }

  return {
    init : init
  };

}());

FAQ.init({
  faqClass : "faq", // Default "faq"
  onClass : "on",   // Default "on" used to show dd and switch dt icon state.
  linkClass : ""    // Default none
});

