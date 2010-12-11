function openThrottle(address, id, imageURL) {
    winref = window.open("/web/inControl.html?locoaddress="+address+"&loconame="+id+"&locoimage="+imageURL, address);
    winref.focus();
}

function loadXMLDoc(dname) {
	if (window.XMLHttpRequest) {
		xhttp = new XMLHttpRequest();
	} else {
		xhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	xhttp.open("GET", dname, false);
	xhttp.send("");
	return xhttp.responseXML;
}

function displayRosterUsing(xsltfile, inElement) {
	xml = loadXMLDoc("/prefs/roster.xml");
	xsl = loadXMLDoc(xsltfile);
	// code for IE
	if (window.ActiveXObject) {
		ex = xml.transformNode(xsl);
		inElement.innerHTML = ex;
	}
	// code for Mozilla, Firefox, Opera, etc.
	else if (document.implementation
			&& document.implementation.createDocument) {
		xsltProcessor = new XSLTProcessor();
		xsltProcessor.importStylesheet(xsl);
		resultDocument = xsltProcessor.transformToFragment(xml, document);
		inElement.appendChild(resultDocument);
	}
}