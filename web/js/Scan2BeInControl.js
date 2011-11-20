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
	alert("AJAX error.\n\nURL:\n" + ajaxSettings.url + "\n\nStatus:\n" + jqXHR.status + "\n\nResponse:\n" + jqXHR.responseText + "\n\nError:\n" + thrownError);
});

//----------------------------------------- Vars
var $isLocalhost;
var $xmlRosterPath = "/prefs/roster.xml";
var $htmlFramesPath = "/frame";
var $inControlURL;
var $help =
	"Version 1.4 - by Oscar Moutinho" +
	"\n" +
	"\nThis application lists the roster (locos) and frames defined in JMRI." +
	"\n" +
	"\nTo open a web throttle or frame using 'inControl', users may:" +
	"\n- scan the image using a smartphone (with qrCode reader application)" +
	"\n- click or touch the loco or frame image or link" +
	"\n" +
	"\nFor locos, the parameters from JMRI are merged with the ones already stored in your device (computer, tablet or smartphone)." +
	"\nIf you want to use only the parameters from JMRI, you must delete the loco from your device before scan the qrCode graph or use the link." +
	"";

//----------------------------------------- Run at start up
$(document).ready(function(){
	var $hasLocos = false;
	var $currentHost = window.location.host;
	var $currentPath = window.location.pathname;
	var $aux = $currentHost.split(":")[0];
	$isLocalhost = ($aux == "localhost" || $aux == "127.0.0.1");
	if($isLocalhost) alert("Don't use 'localhost' or '127.0.0.1' to call this page.\nUse the computer name or IP address (must be known by the network) in order to generate qrCode graphs.");
	$aux = $currentPath.slice(0, $currentPath.lastIndexOf("/") + 1);
	$inControlURL = "http://" + $currentHost + $aux + "inControl.html";
	$("body").html("<div id='help' class='pointer' title='Help' onclick='alert($help)'>?</div><div id='roster'></div><div id='frames'></div>");
	$.ajaxSetup({async: false, cache: false, type: "GET"});
	$loadRoster();
	$loadFrames();
});

//----------------------------------------- Retrieve roster
var $loadRoster = function(){
	$.ajax({
		url: $xmlRosterPath,
		dataType: "xml",
		error: function(jqXHR, textStatus, errorThrown){
			if(jqXHR.status == 404) alert("Roster empty.\nNo locos defined in JMRI."); else alert("AJAX error.\n\nStatus:\n" + jqXHR.status + "\n\nResponse:\n" + jqXHR.responseText + "\n\nError:\n" + errorThrown);
		},
		success: function(xmlReturned, status, jqXHR){
			var $xmlReturned = $(xmlReturned);
			var $locos = [];
			$xmlReturned.find("roster-config roster locomotive").each(function(){ 
				var $loco = {
					name: $.trim($(this).attr("id")),
					roadName: $.trim($(this).attr("roadName")),
					roadNumber: $.trim($(this).attr("roadNumber")),
					address: parseInt($(this).attr("dccAddress"), 10),
					icon: $.trim($(this).attr("iconFilePath")),
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
			var s = "";
			var $queryString;
			var $qrCode;
			var $titleLoco;
			var $lnkLoco;
			var $infoLoco;
			var $auxLocoImg;
			for(var i = 0; i < $locos.length; i++){
				$hasLocos = true;
				s+= "<div id='loco" + i + "' class='divLoco'>";
				s+= "<table><tr><td>";
				s+= "<div id='qrCode" + i + "' class='imgLoco'></div>";
				s+= "</td><td>";
				s+= "<table><tr><td>";
				s+= "<div id='titleLoco" + i + "' class='titleLoco'></div>";
				s+= "</td></tr><tr><td>";
				s+= "<a id='lnkLoco" + i + "' class='lnkLoco'></a>";
				s+= "</td></tr><tr><td>";
				s+= "<label id='infoLoco" + i + "' class='infoLoco'></label>";
				s+= "</td></tr></table>";
				s+= "</td></tr></table>";
				s+= "<hr />";
				s+= "</div>";
			}
			$("#roster").html(s);
			for(var i = 0; i < $locos.length; i++){
				if(i > 0) $("#loco" + i).css("page-break-before", "always");
				$auxLocoImg = $auxImage($locos[i].image, $locos[i].icon);
				$queryString = "loconame=" + encodeURIComponent($locos[i].name);
				$qrCode = $("#qrCode" + i);
				$titleLoco = $("#titleLoco" + i);
				$lnkLoco = $("#lnkLoco" + i);
				$infoLoco = $("#infoLoco" + i);
				if(!$isLocalhost) $qrCode.qrcode($inControlURL + "?" + $queryString);
				$titleLoco.html(($isLocalhost ? "" : "Point your smartphone qrCode scanner to the square graph to open the throttle in your device.<br />") + "To open the throttle in this computer, click at the " +  ($auxLocoImg.length > 0 ? "Loco image." : "link."));
				$lnkLoco.attr("href", $inControlURL + "?" + $queryString);
				$lnkLoco.attr("target", $locos[i].name);
				if($auxLocoImg.length > 0) $lnkLoco.html("<img src='/prefs/resources/" + $auxLocoImg + "?MaxHeight=120' title='Open a throttle to control this Loco' />"); else $lnkLoco.html($("<div />").text($inControlURL).html() + "?<wbr>" + $("<div />").text($queryString).html().replace(/&amp;/g, "&amp;<wbr>"));				// HTML encode
				$infoLoco.html(
					"<b>Loco name (DCC address):</b> " + $("<div />").text($locos[i].name).html() + " (" + $("<div />").text($locos[i].address).html() + ")" +				// HTML encode
					"<br /><b>Road name / number:</b> " + $("<div />").text($locos[i].roadName).html() + " / " + $("<div />").text($locos[i].roadNumber).html()	// HTML encode
				);
			}
		}
	});
}

//----------------------------------------- Retrieve frames
var $loadFrames = function(){
	$.ajax({
		url: $htmlFramesPath,
		dataType: "html",
		error: function(jqXHR, textStatus, errorThrown){
			if(jqXHR.status == 404) alert("No frames defined in JMRI."); else alert("AJAX error.\n\nStatus:\n" + jqXHR.status + "\n\nResponse:\n" + jqXHR.responseText + "\n\nError:\n" + errorThrown);
		},
		success: function(htmlReturned, status, jqXHR){
			var $htmlReturned = $(htmlReturned);
			var $frames = [];
			$htmlReturned.find("td a").each(function(){ 
				var f = $(this).attr("href");
				var e = f.slice(f.lastIndexOf(".") + 1).toLowerCase();
				if("html" == e){
					e = f.substr(0, f.lastIndexOf("."));
					e = e.substr(e.indexOf("/frame/") + 7); 
					$frames.push(e);
				}
			});
			var s = "";
			var $queryString;
			var $qrCode;
			var $titleFrame;
			var $lnkFrame;
			for(var i = 0; i < $frames.length; i++){
				s+= "<div id='frame" + i + "' class='divFrame'>";
				s+= "<table><tr><td>";
				s+= "<div id='qrCodeF" + i + "' class='imgFrame'></div>";
				s+= "</td><td>";
				s+= "<table><tr><td>";
				s+= "<div id='titleFrame" + i + "' class='titleFrame'></div>";
				s+= "</td></tr><tr><td>";
				s+= "<a id='lnkFrame" + i + "' class='lnkFrame'></a>";
				s+= "</td></tr></table>";
				s+= "</td></tr></table>";
				s+= "<hr />";
				s+= "</div>";
			}
			$("#frames").html(s);
			for(var i = 0; i < $frames.length; i++){
				if(i > 0 || $hasLocos) $("#frame" + i).css("page-break-before", "always");
				$queryString = "framename=" + $frames[i];
				$qrCode = $("#qrCodeF" + i);
				$titleFrame = $("#titleFrame" + i);
				$lnkFrame = $("#lnkFrame" + i);
				if(!$isLocalhost) $qrCode.qrcode($inControlURL + "?" + $queryString);
				$titleFrame.html(($isLocalhost ? "" : "Point your smartphone qrCode scanner to the square graph to open the frame in your device.<br />") + "To open the frame in this computer, click at the Frame image.");
				$lnkFrame.attr("href", $inControlURL + "?" + $queryString);
				$lnkFrame.attr("target", $frames[i]);
				$lnkFrame.html("<img src='/frame/" + $frames[i] + ".png?MaxHeight=120' title='Open this Frame to control it' />");
			}
		}
	});
}
//----------------------------------------- Aux image URL
var $auxImage = function(Url1, Url2){
	var Url = "";
	if(Url1) if(Url1.length > 0 && Url1.image != "__noImage.jpg") Url = Url1;
	if(Url.length == 0) if(Url2) if(Url2.length > 0 && Url2.image != "__noImage.jpg") Url = Url2;
	return Url;
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ End of file
