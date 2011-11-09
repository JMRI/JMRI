/**********************************************************************************************
*  
*  For 'Scan2BeInControl.html'
*  
*  Using jQuery and qrCode
*
**********************************************************************************************/

//============================================================= Specific code

//+++++++++++++++++++++++++++++++++++++++++++++++++++ Global Vars and Functions

//----------------------------------------- Generic onError
window.onerror = function(errMsg, errUrl, errLineNumber) {
	alert("Error running javascript:\n" + errMsg + "\n\nURL:\n" + errUrl + "\n\nLine Number: " + errLineNumber);
	return true;
}

//----------------------------------------- AJAX errors
$(document).ajaxError(function(event, jqXHR, ajaxSettings, thrownError){
	if(jqXHR.status == 404) alert("Roster empty.\nNo locos defined in JMRI."); else alert("AJAX error.\n\nURL:\n" + ajaxSettings.url + "\n\nStatus:\n" + jqXHR.status + "\n\nResponse:\n" + jqXHR.responseText + "\n\nError:\n" + thrownError);
});

//----------------------------------------- Vars
var $isLocalhost;
var $xmlRosterPath = "/prefs/roster.xml";
var $inControlURL;
var $help =
	"Version 1.1 - by Oscar Moutinho" +
	"\n" +
	"\nThis application lists the roster (locos) defined in JMRI." +
	"\n" +
	"\nTo open a web throttle using 'inControl', users may:" +
	"\n- scan the image using a smartphone (with qrCode reader application)" +
	"\n- click or touch the link" +
	"\n- copy and paste the link" +
	"\n" +
	"\nThe parameters from JMRI are merged with the ones already stored in your device (computer, tablet or smartphone)." +
	"\nIf you want to use only the parameters from JMRI, you must delete the loco from your device before scan the qrCode graph or use the link." +
	"";

//----------------------------------------- Run at start up
$(document).ready(function(){
	var $currentHost = window.location.host;
	var $currentPath = window.location.pathname;
	var $aux = $currentHost.split(":")[0];
	$isLocalhost = ($aux == "localhost" || $aux == "127.0.0.1");
	if($isLocalhost) alert("Don't use 'localhost' or '127.0.0.1' to call this page.\nUse the computer name or IP address (must be known by the network) in order to generate qrCode graphs.");
	$aux = $currentPath.slice(0, $currentPath.lastIndexOf("/") + 1);
	$inControlURL = "http://" + $currentHost + $aux + "inControl.html";
	$("body").html("<div id='roster'></div>");
	$.ajaxSetup({url: $xmlRosterPath, cache: true, type: "GET", dataType: "xml"});
	$loadRoster();
});

//----------------------------------------- Retrieve roster
var $loadRoster = function(){
	$.ajax({
		success: function(xmlReturned, status, jqXHR){
			var $xmlReturned = $(xmlReturned);
			var $xmlLocomotive = $(xmlReturned);
			var $locos = [];
			$xmlReturned.find("roster-config roster locomotive").each(function(){ 
				var $loco = {
					name: $.trim($(this).attr("id")),
					roadName: $.trim($(this).attr("roadName")),
					roadNumber: $.trim($(this).attr("roadNumber")),
					address: parseInt($(this).attr("dccAddress"), 10),
					image: $.trim($(this).attr("imageFilePath")),
					shunt: $.trim($(this).attr("IsShuntingOn")),
					functions: []
				};
				var $functions = [];
				$(this).find("functionlabels functionlabel").each(function(){ 
					var $function = {
						number: parseInt($(this).attr("num"), 10),
						toggle: ($(this).attr("lockable") == "true"),
						label: $.trim($(this).text()),
						image: $.trim($(this).attr("functionImage")),
						imagePressed: $.trim($(this).attr("functionImageSelected"))
					};
					$functions.push($function);
				});
				$loco.functions = $functions;
				$locos.push($loco);
			});
			var s = "<div id='help' class='pointer' title='Help' onclick='alert($help)'>?</div>";
			var $queryString;
			var $qrCode;
			var $tittleLoco;
			var $lnkLoco;
			var $infoLoco;
			for(var i = 0; i < $locos.length; i++){
				s+= "<p><div id='loco" + i + "' class='divLoco'>";
				s+= "<table><tr><td>";
				s+= "<div id='qrCode" + i + "' class='imgLoco'></div>";
				s+= "</td><td>";
				s+= "<table><tr><td>";
				s+= "<div id='tittleLoco" + i + "' class='tittleLoco'></div>";
				s+= "</td></tr><tr><td>";
				s+= "<a id='lnkLoco" + i + "' class='lnkLoco'></a>";
				s+= "</td></tr><tr><td>";
				s+= "<label id='infoLoco" + i + "' class='infoLoco'></label>";
				s+= "</td></tr></table>";
				s+= "</td></tr></table>";
				s+= "<hr />";
				s+= "</div></p>";
			}
			$("#roster").html(s);
			for(var i = 0; i < $locos.length; i++){
				$queryString = "loconame=" + encodeURIComponent($locos[i].name);
				$queryString+= "&locoaddress=" + encodeURIComponent($locos[i].address);
				if($locos[i].image.length > 0) $queryString+= "&locoimage=" + encodeURIComponent($locos[i].image);
				$queryString+= "&" + encodeURIComponent($locos[i].shunt) + "shunt=x";
				for(var j = 0; j < $locos[i].functions.length; j++){
					$queryString+= "&f" + $locos[i].functions[j].number + "active=x";
					if($locos[i].functions[j].toggle) $queryString+= "&f" + $locos[i].functions[j].number + "toggle=x";
					if($locos[i].functions[j].label.length > 0) $queryString+= "&f" + $locos[i].functions[j].number + "label=" + encodeURIComponent($locos[i].functions[j].label);
					if($locos[i].functions[j].image.length > 0) $queryString+= "&f" + $locos[i].functions[j].number + "image=" + encodeURIComponent($locos[i].functions[j].image);
					if($locos[i].functions[j].imagePressed.length > 0) $queryString+= "&f" + $locos[i].functions[j].number + "imagepressed=" + encodeURIComponent($locos[i].functions[j].imagePressed);
				}
				$qrCode = $("#qrCode" + i);
				$tittleLoco = $("#tittleLoco" + i);
				$lnkLoco = $("#lnkLoco" + i);
				$infoLoco = $("#infoLoco" + i);
				if(!$isLocalhost) $qrCode.qrcode($inControlURL + "?" + $queryString);
				$tittleLoco.html(($isLocalhost ? "" : "Point your smartphone qrCode scanner to the square graph to open the throttle in your device.<br />") + "To open the throttle in this computer, click at the link.");
				$lnkLoco.attr("href", $inControlURL + "?" + $queryString);
				$lnkLoco.attr("target", $locos[i].name);
				$lnkLoco.html($("<div />").text($inControlURL).html() + "?<wbr>" + $("<div />").text($queryString).html().replace(/&amp;/g, "&amp;<wbr>"));				// HTML encode
				$infoLoco.html(
					"<b>Loco name (DCC address):</b> " + $("<div />").text($locos[i].name).html() + " (" + $("<div />").text($locos[i].address).html() + ")" +				// HTML encode
					"<br /><b>Road name / number:</b> " + $("<div />").text($locos[i].roadName).html() + " / " + $("<div />").text($locos[i].roadNumber).html()	// HTML encode
				);
			}
		}
	});
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ End of file
