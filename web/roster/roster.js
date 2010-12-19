function openThrottle(address, id, imageURL, fnlabels) {
	var inParameters="locoaddress="+address;
	if (id)
		inParameters=inParameters+"&loconame="+escape(id);
	if (imageURL && (imageURL!="/prefs/resources/__noIcon.jpg"))
		inParameters=inParameters+"&locoimage="+escape(imageURL);
	if (fnlabels)
		inParameters=inParameters+"&"+fnlabels;
	var winref = window.open("/web/inControl.html?"+inParameters, address);    
	winref.focus();
}

function loadXMLDoc(dname) {
	var xhttp;
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
	var xml = loadXMLDoc("/prefs/roster.xml");
	var xsl = loadXMLDoc(xsltfile);
	// code for IE
	if (window.ActiveXObject) {
		var ex = xml.transformNode(xsl);
		inElement.innerHTML = ex;
	}
	// code for Mozilla, Firefox, Opera, etc.
	else if (document.implementation && document.implementation.createDocument) {
		var xsltProcessor = new XSLTProcessor();
		xsltProcessor.importStylesheet(xsl);
		var resultDocument = xsltProcessor.transformToFragment(xml, document);
		inElement.appendChild(resultDocument);
	}
}