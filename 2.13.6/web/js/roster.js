//
// Part of JMRI - 2010 
// Lionel Jeanson
// Uses inControl web throttles to create web throttles

// Open a inControl throttle for DCC address
function openThrottle(address, id, imageURL) {
	var inParameters="locoaddress="+address;
	if (id)
		inParameters=inParameters+"&loconame="+encodeURIComponent(id);
	if (imageURL && (imageURL!="/prefs/resources/__noIcon.jpg"))
		inParameters=inParameters+"&locoimage="+encodeURIComponent(imageURL);
	var winref = window.open("/web/inControl.html?"+inParameters, address); // We use address as a window id so that we won't open other ones on that client for that address   
	winref.focus();
}

// Load an xml file and returns its root node
function loadXMLDoc(dname) {
	var xhttp;
	if (window.XMLHttpRequest) { // Microsoft IE
		xhttp = new XMLHttpRequest();
	} else { // other browsers
		xhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	xhttp.open("GET", dname, false);
	xhttp.send("");
	return xhttp.responseXML;
}

// Render the roster.xml file using the given in xslt into html element inElement
function displayRosterUsing(xsltfile, inElement) {
	var xml = loadXMLDoc("/prefs/roster.xml");
	var xsl = loadXMLDoc(xsltfile);
	if (window.ActiveXObject) {  // Microsoft IE
		var ex = xml.transformNode(xsl);
		inElement.innerHTML = ex;
	}
	else if (document.implementation && document.implementation.createDocument) { // other browsers
		var xsltProcessor = new XSLTProcessor();
		xsltProcessor.importStylesheet(xsl);
		var resultDocument = xsltProcessor.transformToFragment(xml, document);
		inElement.appendChild(resultDocument);
	}
}