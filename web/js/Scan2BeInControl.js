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
var $locos = [];
var $selectedLocos = [];
var $help =
	"Version 1.6 - by Oscar Moutinho" +
	"\n" +
	"\nThis application lists the roster (locos) and frames defined in JMRI." +
	"\n" +
	"\nTo clean stored info in your device or to open a web throttle to control locos, turnouts, routes or frames using 'inControl', users may:" +
	"\n- scan a graph image using a smartphone (with qrCode reader application)" +
	"\n- click or touch an image or link" +
	"\n" +
	"\nFor locos, the parameters from JMRI are merged with the ones already stored in your device (computer, tablet or smartphone)." +
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
	$("body").html("<div id='help' class='noPrint pointer' title='Help' onclick='alert($help)'>?</div><div id='first'></div><div id='roster'></div><div id='frames'></div>");
	$.ajaxSetup({async: false, cache: false, type: "GET"});
	$showFirst();
	$loadRoster();
	$loadFrames();
});

//----------------------------------------- Show 'RemoveFromLocalStorage', 'Selected Locos', 'Turnouts' and 'Routes'
var $showFirst = function(){
	var s = "";
	var $queryString;
	var $title;
	var $lnk;
	var $prt;
	var $qrCode;
	s+= "<div id='first1' class='divFirst'>";
	s+= "<table><tr width='100%'><td width='50%'>";
	s+= "<div id='titleClean' class='titleFirst'></div>";
	s+= "</td><td width='50%'>";
	s+= "<div id='titleMulti' class='titleFirst'></div>";
	s+= "</td></tr><tr><td>";
	s+= "<a id='lnkClean' class='noPrint lnkFirst'></a>";
	s+= "<span id='prtClean' class='noScreen lnkFirst'></span>";
	s+= "</td><td>";
	s+= "<a id='lnkMulti' class='noPrint lnkFirst'></a>";
	s+= "<span id='prtMulti' class='noScreen lnkFirst'></span>";
	s+= "</td></tr><tr><td align='center'>";
	s+= "<div id='qrCodeClean' class='imgFirst'></div>";
	s+= "</td><td id='TDqrCodeMulti' align='center'>";
	s+= "<div id='qrCodeMulti' class='imgFirst'></div>";
	s+= "</td></tr></table>";
	s+= "<hr />";
	s+= "</div>";
	s+= "<div id='first2' class='divFirst'>";
	s+= "<table><tr width='100%'><td width='50%'>";
	s+= "<div id='titleTurnouts' class='titleFirst'></div>";
	s+= "</td><td>";
	s+= "<div id='titleRoutes' class='titleFirst'></div>";
	s+= "</td></tr><tr><td>";
	s+= "<a id='lnkTurnouts' class='noPrint lnkFirst'></a>";
	s+= "<span id='prtTurnouts' class='noScreen lnkFirst'></span>";
	s+= "</td><td>";
	s+= "<a id='lnkRoutes' class='noPrint lnkFirst'></a>";
	s+= "<span id='prtRoutes' class='noScreen lnkFirst'></span>";
	s+= "</td></tr><tr><td align='center'>";
	s+= "<div id='qrCodeTurnouts' class='imgFirst'></div>";
	s+= "</td><td align='center'>";
	s+= "<div id='qrCodeRoutes' class='imgFirst'></div>";
	s+= "</td></tr></table>";
	s+= "<hr />";
	s+= "</div>";
	$("#first").html(s);
	$("#first2").css("page-break-before", "always");
	$queryString = "RemoveFromLocalStorage=x";
	$title = $("#titleClean");
	$lnk = $("#lnkClean");
	$prt = $("#prtClean");
	$qrCode = $("#qrCodeClean");
	$title.html(($isLocalhost ? "" : "Point your smartphone qrCode scanner to the square graph below to clean stored info in your device."));
	$lnk.attr("href", $inControlURL + "?" + $queryString);
	$lnk.html("Click here to clean stored info in this computer.");
	$lnk.attr("target", "RemoveFromLocalStorage");
	$prt.html("<b>Link to clean stored info in this computer:</b><br />" + $("<div />").text($inControlURL).html() + "?<wbr>" + $("<div />").text($queryString).html().replace(/&amp;/g, "&amp;<wbr>"));
	if(!$isLocalhost) $qrCode.qrcode($inControlURL + "?" + $queryString);
	$manageSelectedLocos();
	$queryString = "Turnouts=x";
	$title = $("#titleTurnouts");
	$lnk = $("#lnkTurnouts");
	$prt = $("#prtTurnouts");
	$qrCode = $("#qrCodeTurnouts");
	$title.html(($isLocalhost ? "" : "Point your smartphone qrCode scanner to the square graph below to open a throttle in your device to control Turnouts."));
	$lnk.attr("href", $inControlURL + "?" + $queryString);
	$lnk.html("Click here to open a throttle in this computer to control Turnouts.");
	$lnk.attr("target", "Turnouts");
	$prt.html("<b>Link to open a throttle in this computer to control Turnouts:</b><br />" + $("<div />").text($inControlURL).html() + "?<wbr>" + $("<div />").text($queryString).html().replace(/&amp;/g, "&amp;<wbr>"));
	if(!$isLocalhost) $qrCode.qrcode($inControlURL + "?" + $queryString);
	$queryString = "Routes=x";
	$title = $("#titleRoutes");
	$lnk = $("#lnkRoutes");
	$prt = $("#prtRoutes");
	$qrCode = $("#qrCodeRoutes");
	$title.html(($isLocalhost ? "" : "Point your smartphone qrCode scanner to the square graph below to open a throttle in your device to control Routes."));
	$lnk.attr("href", $inControlURL + "?" + $queryString);
	$lnk.html("Click here to open a throttle in this computer to control Routes.");
	$lnk.attr("target", "Routes");
	$prt.html("<b>Link to open a throttle in this computer to control Routes:</b><br />" + $("<div />").text($inControlURL).html() + "?<wbr>" + $("<div />").text($queryString).html().replace(/&amp;/g, "&amp;<wbr>"));
	if(!$isLocalhost) $qrCode.qrcode($inControlURL + "?" + $queryString);
}

//----------------------------------------- Select loco
var $selectLoco = function(locoIndex){
	var locoName = $locos[locoIndex].name;
	var l = -1;
	var $selLocoChecked = $("#selLoco" + locoIndex).is(":checked");
	for(var i = 0; i < $selectedLocos.length; i++){
		if($selectedLocos[i] == locoName){
			l = i;
			break;
		}
	}
	if($selLocoChecked && l == -1){
		$selectedLocos.push(locoName);
		$selectedLocos.sort();
	}
	if(!$selLocoChecked && l > -1){
		$selectedLocos[l] = "";
		$selectedLocos.sort();
		$selectedLocos.shift();
	}
	$manageSelectedLocos();
}

//----------------------------------------- Manage selected locos
var $manageSelectedLocos = function(){
	var $queryString = "";
	var $title;
	var $lnk;
	var $prt;
	for(var i = 0; i < $selectedLocos.length; i++){
		$queryString+= "LocoName" + (i + 1) + "=" + encodeURIComponent($selectedLocos[i]);
		if(i < ($selectedLocos.length - 1)) $queryString+= "&";
	}
	$title = $("#titleMulti");
	$lnk = $("#lnkMulti");
	$prt = $("#prtMulti");
	if($selectedLocos.length > 1){
		$title.html(($isLocalhost ? "" : "Point your smartphone qrCode scanner to the square graph below to open a throttle of multiple selected Locos in your device."));
		$lnk.attr("href", $inControlURL + "?" + $queryString);
		$lnk.html("Click here to open a throttle of multiple selected Locos in this computer.");
		$lnk.attr("target", "Multi");
		$prt.html("<b>Link to open a throttle of multiple selected Locos in this computer:</b><br />" + $("<div />").text($inControlURL).html() + "?<wbr>" + $("<div />").text($queryString).html().replace(/&amp;/g, "&amp;<wbr>"));
		$("#qrCodeMulti").empty();
		$("#TDqrCodeMulti").add("<div id='qrCodeMulti' class='imgFirst'></div>");
		if(!$isLocalhost) $("#qrCodeMulti").qrcode($inControlURL + "?" + $queryString);
	} else {
		$title.html("To open a throttle of multiple selected Locos, you need to select first the desired Locos.");
		$lnk.attr("href", "");
		$lnk.html("");
		$lnk.attr("target", "nothing");
		$prt.html("");
		$("#qrCodeMulti").empty();
	}
}

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
			var $titlePrtLoco;
			var $lnkLoco;
			var $prtLoco;
			var $infoLoco;
			var $auxLocoImg;
			for(var i = 0; i < $locos.length; i++){
				s+= "<div id='loco" + i + "' class='divLoco'>";
				s+= "<table><tr><td>";
				s+= "<div id='qrCode" + i + "' class='imgLoco'></div>";
				s+= "</td><td>";
				s+= "<table><tr><td>";
				s+= "<div id='titleLoco" + i + "' class='noPrint titleLoco'></div>";
				s+= "<div id='titlePrtLoco" + i + "' class='noScreen titleLoco'></div>";
				s+= "</td></tr><tr><td>";
				s+= "<a id='lnkLoco" + i + "' class='noPrint lnkLoco'></a>";
				s+= "<span id='prtLoco" + i + "' class='noScreen lnkLoco'></span>";
				s+= "</td></tr><tr><td>";
				s+= "<label id='infoLoco" + i + "' class='infoLoco'></label>";
				s+= "<div class='noPrint selLoco'>&nbsp;&nbsp;&nbsp;<input id='selLoco" + i + "' type='checkbox' onclick='$selectLoco(" + i + ")' />Select for multiple locos throttle</div>";
				s+= "</td></tr></table>";
				s+= "</td></tr></table>";
				s+= "<hr />";
				s+= "</div>";
			}
			$("#roster").html(s);
			for(var i = 0; i < $locos.length; i++){
				$("#loco" + i).css("page-break-before", "always");
				$auxLocoImg = $auxImage($locos[i].image, $locos[i].icon);
				$queryString = "LocoName=" + encodeURIComponent($locos[i].name);
				$qrCode = $("#qrCode" + i);
				$titleLoco = $("#titleLoco" + i);
				$titlePrtLoco = $("#titlePrtLoco" + i);
				$lnkLoco = $("#lnkLoco" + i);
				$prtLoco = $("#prtLoco" + i);
				$infoLoco = $("#infoLoco" + i);
				if(!$isLocalhost) $qrCode.qrcode($inControlURL + "?" + $queryString);
				$titleLoco.html(($isLocalhost ? "" : "Point your smartphone qrCode scanner to the square graph to open the throttle in your device.<br />") + "To open the throttle in this computer, click at the " +  ($auxLocoImg.length > 0 ? "Loco image." : "link."));
				$titlePrtLoco.html($isLocalhost ? "<b>Link to open the throttle:</b>" : "Point your smartphone qrCode scanner to the square graph to open the throttle in your device.");
				$lnkLoco.attr("href", $inControlURL + "?" + $queryString);
				$lnkLoco.attr("target", $locos[i].name);
				if($auxLocoImg.length > 0) $lnkLoco.html("<img src='/prefs/resources/" + $auxLocoImg + "?MaxHeight=120' title='Open a throttle to control this Loco' />"); else $lnkLoco.html($("<div />").text($inControlURL).html() + "?<wbr>" + $("<div />").text($queryString).html().replace(/&amp;/g, "&amp;<wbr>"));				// HTML encode
				$prtLoco.html((($auxLocoImg.length > 0) ? "<img src='/prefs/resources/" + $auxLocoImg + "?MaxHeight=120' />" : "") + "<br />" + $("<div />").text($inControlURL).html() + "?<wbr>" + $("<div />").text($queryString).html().replace(/&amp;/g, "&amp;<wbr>"));
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
			var $titlePrtFrame;
			var $lnkFrame;
			var $prtFrame;
			for(var i = 0; i < $frames.length; i++){
				s+= "<div id='frame" + i + "' class='divFrame'>";
				s+= "<table><tr><td>";
				s+= "<div id='qrCodeF" + i + "' class='imgFrame'></div>";
				s+= "</td><td>";
				s+= "<table><tr><td>";
				s+= "<div id='titleFrame" + i + "' class='noPrint titleFrame'></div>";
				s+= "<div id='titlePrtFrame" + i + "' class='noScreen titleFrame'></div>";
				s+= "</td></tr><tr><td>";
				s+= "<a id='lnkFrame" + i + "' class='noPrint lnkFrame'></a>";
				s+= "<span id='prtFrame" + i + "' class='noScreen lnkFrame'></span>";
				s+= "</td></tr></table>";
				s+= "</td></tr></table>";
				s+= "<hr />";
				s+= "</div>";
			}
			$("#frames").html(s);
			for(var i = 0; i < $frames.length; i++){
				$("#frame" + i).css("page-break-before", "always");
				$queryString = "FrameName=" + $frames[i];
				$qrCode = $("#qrCodeF" + i);
				$titleFrame = $("#titleFrame" + i);
				$titlePrtFrame = $("#titlePrtFrame" + i);
				$lnkFrame = $("#lnkFrame" + i);
				$prtFrame = $("#prtFrame" + i);
				if(!$isLocalhost) $qrCode.qrcode($inControlURL + "?" + $queryString);
				$titleFrame.html(($isLocalhost ? "" : "Point your smartphone qrCode scanner to the square graph to open the frame in your device.<br />") + "To open the frame in this computer, click at the Frame image.");
				$titlePrtFrame.html($isLocalhost ? "<b>Link to open the frame:</b>" : "Point your smartphone qrCode scanner to the square graph to open the frame in your device.");
				$lnkFrame.attr("href", $inControlURL + "?" + $queryString);
				$lnkFrame.attr("target", $frames[i]);
				$lnkFrame.html("<img src='/frame/" + $frames[i] + ".png?MaxHeight=120' title='Open this Frame to control it' />");
				$prtFrame.html("<img src='/frame/" + $frames[i] + ".png?MaxHeight=120' />" + "<br />" + $("<div />").text($inControlURL).html() + "?<wbr>" + $("<div />").text($queryString).html().replace(/&amp;/g, "&amp;<wbr>"));
			}
		}
	});
}
//----------------------------------------- Aux image URL
var $auxImage = function(Url1, Url2){
	var Url = "";
	if(Url1) if(Url1.length > 0 && Url1 != "__noImage.jpg") Url = Url1;
	if(Url.length == 0) if(Url2) if(Url2.length > 0 && Url2 != "__noImage.jpg") Url = Url2;
	return Url;
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ End of file
