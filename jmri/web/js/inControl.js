/**********************************************************************************************
*  
*  For 'inControl.html'
*  
*  Some settings are defined as constants in 'Vars' section below.
*  These settings can be changed (timeouts, image URLs, size ratios, ...).
*  
*  Using jQuery
*  
**********************************************************************************************/

//============================================================= Specific code

//+++++++++++++++++++++++++++++++++++++++++++++++++++ Global Vars and Functions

//----------------------------------------- Vars
var $imagePowerUndefined = "/resources/icons/throttles/power_yellow.png";	// images/PowerGrey.png
var $imagePowerOff = "/resources/icons/throttles/power_red.png";			// images/PowerRed.png
var $imagePowerOn = "/resources/icons/throttles/power_green.png";			// images/PowerGreen.png
var $imageSliderCursor = "images/SpeedCursor1.png";
var $imagePlusPlus = "images/PlusPlus.png";
var $imagePlus = "images/Plus.png";
var $imageMinus = "images/Minus.png";
var $imageMinusMinus = "images/MinusMinus.png";
var $imageReverseEnabled = "/resources/icons/throttles/down-green.png";		// images/LeftGreen.png
var $imageReverseDisabled = "/resources/icons/throttles/down-red.png";		// images/LeftRed.png
var $imageStop = "/resources/icons/throttles/estop.png";					// images/Stop.png
var $imageForwardEnabled = "/resources/icons/throttles/up-green.png";		// images/RightGreen.png
var $imageForwardDisabled = "/resources/icons/throttles/up-red.png";		// images/RightRed.png
var $imageSettings = "images/Settings.png";
var $imageClose = "images/Close.png";
var $imageTrash = "images/Trash.png";
var $xmlRosterPath = "/prefs/roster.xml";
var $xmlioPath = "/xmlio/";
var $imagesPath = "/web/images/";
var $userImagesPath = "/prefs/resources/";
var allUrlParameters;
var $selectAux;
var $imagesFileList = [];
var $ResizeCheckInterval = 500;												//ms
var $RetrieveFrameInterval = 3000;											//ms
var $SizeReference;
var $SizeReferenceMin = 30;													//px minimum
var $SizeReferencePercent = 7;												//%
var $TextSizeRatioS = 0.7;													//small
var $TextSizeRatioN = 1;													//normal
var $TextSizeRatioL = 1.3;													//large
var $LocoImageLayoutHeightPercent = 18;										//%
var $TurnoutRouteWidthRef = 400;											//px
var $viewportWidth = 0;
var $viewportHeight = 0;
var $frameInitHeight = -1;
var $frameInitWidth = -1;
var $isMultiPage;
var $insideMulti;
var $iframeId = "";
var $changeBothSizes;
var $portrait;
var $inSettings = false;
var $inSettingsLoco = false;
var $inChooseImage = false;
var $inQueryString = false;
var $inHelp = false;
var $powerStatusBlock = false;			//If true, cannot control loco if not Power ON
var $powerStatus = -1;
var $speedValue = 0;
var $forward = true;
var $shunt;
var $LocoNameList = [];
var $FnPressed = [];
var $SettingsLocoFunctionSelected = 0;
var $VerticalSlider;
var $paramRemoveFromLocalStorage;
var $paramFrameName;
var $listTurnoutsRoutes = [];
var $paramTurnouts;
var $paramRoutes;
var $paramThrottles = [];
var $paramTextSizeRatio;
var $paramPower;
var $paramSpeedCtrlRight;
var $paramSpeedCtrlButtons;
var $paramLocoName;
var $paramLocoAddress;
var $paramLocoImage;
var $paramLocoNoSpeedCtrl;
var $paramFnActive = [];
var $paramFnToggle = [];
var $paramFnAutoUnpress = [];
var $paramFnLabel = [];
var $paramFnImage = [];
var $paramFnImagePressed = [];
var $paramFnAddress = [];
var $paramFnShunt = [];
var $paramXmlioDebug;
var $QueryStringText =
	"- RemoveFromLocalStorage &nbsp;&nbsp;(all parameters)" +
	"<br />- FrameName (display a frame - case sensitive name - instead of a loco throttle)" +
	"<br />- Turnouts &nbsp;&nbsp;(display the turnouts list instead of a loco throttle)" +
	"<br />- Routes &nbsp;&nbsp;(display the routes list instead of a loco throttle)" +
	"<br />- LocoName&lt;i&gt; &nbsp;&nbsp;(multiple loco throttles - case sensitive name)" +
	"<br />- TextSize &nbsp;&nbsp;(s[mall] n[ormal] l[arge])" +
	"<br />- Power &nbsp;&nbsp;(power button)" +
	"<br />- SpeedCtrlRight &nbsp;&nbsp;(speed control on right)" +
	"<br />- SpeedCtrlButtons &nbsp;&nbsp;(speed control with buttons)" +
	"<br />- LocoName (loco or function only decoder - case sensitive name)" +
	"<br />- LocoAddress (loco or function only decoder)" +
	"<br />- LocoImage &nbsp;&nbsp;(URL)" +
	"<br />- LocoNoSpeedCtrl &nbsp;&nbsp;(no speed control)" +
	"<br />- F&lt;i&gt;Active" +					//also true if any param exists					i: de 0 a 28
	"<br />- F&lt;i&gt;Toggle" +					//also true if param 'f<i>imagepressed' exists
	"<br />- F&lt;i&gt;AutoUnpress" +
	"<br />- F&lt;i&gt;Label" +
	"<br />- F&lt;i&gt;Image &nbsp;&nbsp;(URL)" +
	"<br />- F&lt;i&gt;ImagePressed &nbsp;&nbsp;(URL)" +
	"<br />- F&lt;i&gt;Address &nbsp;&nbsp;(alternative device address)" +
	"<br />- F&lt;i&gt;Shunt &nbsp;&nbsp;(activate half speed with bidirectional control)" +
	"<br />- xmlioDebug &nbsp;&nbsp;(display XML in/out)" +
	"<br />[all parameters are optional and their names are not case sensitive]" +
	"<br />[boolean parameters: empty (no) / anything (yes)]" +
	"<br />[already saved parameters are not changed - they are temporarily replaced]" +
	"<br />Examples:" +
	"<br />http://localhost:12080/web/inControl.html?loconame7=loco1" +
	"<br />http://localhost:12080/web/inControl.html?POWER=x&LocoName=loco1" +
	"<br />http://localhost:12080/web/inControl.html?Framename=myFrame" +
	"";
var $HelpText =
	"<br />Version 2.7 - by Oscar Moutinho" +
	"<br />" +
	"<br />All browsers: set zoom to 100%." +
	"<br />Google Chrome: deactivate 'instant' functionality." +
	"<br />This page may be opened in iFrames of any size - you may create sets of throttle/frame controls in HTML pages." +
	"<br />" +
	"<br />The Locos in the roster file are always loaded." +
	"<br />" +
	"<br />To best display full JMRI Frames, you should resize the frame window in 'Panel editor' to show the full diagram and select 'No scrollbars'." +
	"<br />" +
	"<br />For Loco or Function Keys, you may select any image from the internet. Just write the correct URL." +
	"<br />Example: http://www.anysite.org/images/Loco3.jpg" +
	"<br />You may also select images from JMRI '/web/images/' directory (directly from the list)." +
	"<br />The images already stored in JMRI user resources '/prefs/resources/' must be written without directory info (ex.: MyHorn.png)." +
	"";

//----------------------------------------- Generic onError
window.onerror = function(errMsg, errUrl, errLineNumber) {
	alert("Error running javascript:\n" + errMsg + "\n\nURL:\n" + errUrl + "\n\nLine Number: " + errLineNumber);
	return true;
}

//----------------------------------------- Vertical Slider (uses function $errorLoadingImage)
function $verticalSlider(_parent, sliderTop, sliderLeft, sliderHeight, sliderWidth, levelInit, _middleZero, callbackFunction){	// Top and Left relative to parent
	var $parent;
	var sliderId = "verticalSlider" + (Math.random() + "").split(".")[1];	//Math.random(): for uniqueness of several instances
	var sliderButtonId = sliderId + "Button";
	var $sliderId;
	var $sliderButtonId;
	var w = false;
	var enable;
	var level;
	var middleZero;
	var slider_t;
	var slider_l;
	var slider_h;
	var slider_w;
	var sb_hw;
	var sb_pos_t;
	var sb_pos_l;
	this.toString = function() {
		return  sliderId + " (inside " + $($parent)[0].nodeName + " id: " + $($parent).attr("id") + ")" +
		"; sliderTop: " + sliderTop + ", sliderLeft: " + sliderLeft + 
		", sliderHeight: " + sliderHeight + ", sliderWidth: " + sliderWidth +
		", enable: " + enable + ", level: " + level;
	};
	this.setLevel = function(_level, _middleZero) {
		level = Math.round(_level);
		if(level > 100) level = 100;
		if(level < 0) level = 0;
		middleZero = _middleZero;
		buttonPositionFromLevel();
	};
	this.setEnable = function(_enable) {
		enable = _enable;
		$("#" + sliderId).css("background", enable ? ((level == (middleZero ? 50 : 0)) ? "red" : "navy") : "grey");
	};
	this.mouse = function(e) {
		e.preventDefault();
    	e.stopImmediatePropagation();
		if(!enable) return;
		if(e.type == "mousedown"){w = true; setNewPositionFromPointer(e.clientY);}
		if(e.type == "mouseup" && w){w = false; callbackFunction(level);}
		if(e.type == "mouseout" && w){
			if(null == e.relatedTarget || !((e.target.id == sliderId && e.relatedTarget.id == sliderButtonId) || (e.target.id == sliderButtonId && e.relatedTarget.id == sliderId))){w = false; callbackFunction(level);}
		}
		if(e.type == "mousemove" && w) setNewPositionFromPointer(e.clientY);
	};
	this.touch = function(_e) {
		var e = _e.originalEvent;
		_e.preventDefault();
    	_e.stopImmediatePropagation();
		if(!enable) return;
		if(e.type == "touchstart"){w = true; setNewPositionFromPointer(e.changedTouches[0].clientY);}
		if((e.type == "touchend" || e.type == "touchcancel") && w){w = false; callbackFunction(level);}
		if(e.type == "touchmove" && w) setNewPositionFromPointer(e.changedTouches[0].clientY);
	};
	var buttonPositionFromLevel = function(){
		$("#" + sliderId).css("background", enable ? ((level == (middleZero ? 50 : 0)) ? "red" : "navy") : "grey");
		sb_pos_t = (slider_h - (sb_hw / 2) - level * slider_h / 100);
		$("#" + sliderButtonId).css("top",sb_pos_t);
	};
	var levelFromButtonPosition = function(){
		level = Math.round((slider_h - sb_pos_t - (sb_hw / 2)) * 100 / slider_h);
		if(level > 100) level = 100;
		if(level < 0) level = 0;
	};
	var setNewPositionFromPointer = function(y){
		sb_pos_t = y - $("#" + sliderId).offset().top - sb_hw / 2;
		if(sb_pos_t > slider_h - sb_hw / 2) sb_pos_t = slider_h - sb_hw / 2;
		if(sb_pos_t < -sb_hw / 2) sb_pos_t = -sb_hw / 2;
		$("#" + sliderButtonId).css("top",sb_pos_t);
		levelFromButtonPosition();
		$("#" + sliderId).css("background", (level == (middleZero ? 50 : 0)) ? "red" : "navy");
	};
	$parent = ((typeof(_parent) == "string") ? $parent = $("#" + _parent) : ((typeof(_parent) == "object" && null != _parent) ? $parent = _parent : $parent = $("body")));
	this.setLevel(levelInit, _middleZero);
	this.setEnable(false);
	sb_hw = sliderWidth;
	slider_t = sliderTop;
	slider_h = sliderHeight - sb_hw;
	slider_w = sliderWidth / 4;
	slider_l = sliderLeft;
	sb_pos_l = (-sb_hw + slider_w) / 2;
	$parent.append("<div id='" + sliderId + "'>");
	$sliderId = $("#" + sliderId);
	$sliderId.css("position", "absolute");
	$sliderId.css("cursor", "pointer");
	$sliderId.css("-moz-border-radius", "10px");
	$sliderId.css("-webkit-border-radius", "10px");
	$sliderId.css("border-radius", "10px");
	$sliderId.css("z-index", 100);
	$sliderId.css("top", slider_t);
	$sliderId.css("left", slider_l);
	$sliderId.height(slider_h);
	$sliderId.width(slider_w);
	$sliderId.css("margin-top", sb_hw / 2);
	$sliderId.css("margin-bottom", sb_hw / 2);
	$sliderId.css("margin-left", (sliderWidth - slider_w) / 2);
	$sliderId.css("margin-right", (sliderWidth - slider_w) / 2);
	$sliderId.bind("mousedown mousemove mouseup mouseout", this.mouse);
	$sliderId.bind("touchstart touchmove touchend touchcancel", this.touch);
	$sliderId.html("<img id='" + sliderButtonId +"' />");
	$sliderButtonId = $("#" + sliderButtonId);
	$sliderButtonId.height(sb_hw);
	$sliderButtonId.width(sb_hw);
	$sliderButtonId.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imageSliderCursor + "?MaxHeight=" + $sliderButtonId.height() + "&MaxWidth=" + $sliderButtonId.width()));
	$sliderButtonId.css("position", "absolute");
	$sliderButtonId.css("cursor", "pointer");
	$sliderButtonId.css("z-index", 101);
	$sliderButtonId.css("left", sb_pos_l);
	$sliderButtonId.bind("mouseout", this.mouse);
	$sliderButtonId.bind("touchend touchcancel", this.touch);
	buttonPositionFromLevel();
}

//----------------------------------------- AJAX errors
$(document).ajaxError(function(event, jqXHR, ajaxSettings, thrownError){
	if ($paramXmlioDebug) alert((($iframeId.length > 0) ? "[" + $iframeId + "] " : "") + "AJAX error.\n\nURL:\n" + ajaxSettings.url + "\n\nStatus:\n" + jqXHR.status + "\n\nResponse:\n" + jqXHR.responseText + "\n\nError:\n" + thrownError);
});

//----------------------------------------- Send XMLIO Set Turnout
var $xmlioSendSetTurnout = function(turnoutName, turnoutStatus){
	$xmlioSend("<xmlio>\n  <item>\n    <type>turnout</type>\n    <name>" + turnoutName + "</name>\n    <set>" + turnoutStatus + "</set>\n  </item>\n</xmlio>");
}

//----------------------------------------- Send XMLIO Get Turnouts list
var $xmlioSendGetTurnouts = function(){
	$xmlioSend("<xmlio>\n  <list>\n    <type>turnout</type>\n  </list>\n</xmlio>");
}

//----------------------------------------- Send XMLIO Set Route
var $xmlioSendSetRoute = function(routeName, routeStatus){
	$xmlioSend("<xmlio>\n  <item>\n    <type>route</type>\n    <name>" + routeName + "</name>\n    <set>" + routeStatus + "</set>\n  </item>\n</xmlio>");
}

//----------------------------------------- Send XMLIO Get Routes list
var $xmlioSendGetRoutes = function(){
	$xmlioSend("<xmlio>\n  <list>\n    <type>route</type>\n  </list>\n</xmlio>");
}

//----------------------------------------- Send XMLIO Set Power
var $xmlioSendSetPower = function(){
	$xmlioSend("<xmlio>\n  <item>\n    <type>power</type>\n    <name>power</name>\n    <set>" + $powerStatus + "</set>\n  </item>\n</xmlio>");
}

//----------------------------------------- Send XMLIO Get Power
var $xmlioSendGetPower = function(){
	$xmlioSend("<xmlio>\n  <list>\n    <type>power</type>\n  </list>\n</xmlio>");
}

//----------------------------------------- Send XMLIO Set Loco speed
var $xmlioSendSetLocoSpeed = function(_speed){
	var speed;
	var forward;
	if($shunt && _speed >= 0){
		forward = ((_speed >= 50) ? $forward : !$forward);
		speed = Math.abs(_speed - 50);
		$xmlioSend("<xmlio>\n  <throttle>\n    <address>" + $paramLocoAddress + "</address>\n    <speed>" + (speed / 100) + "</speed>\n    <forward>" + (forward ? "true": "false") + "</forward>\n  </throttle>\n</xmlio>");
	} else {
		$xmlioSend("<xmlio>\n  <throttle>\n    <address>" + $paramLocoAddress + "</address>\n    <speed>" + (_speed / 100) + "</speed>\n  </throttle>\n</xmlio>");
	}
}

//----------------------------------------- Send XMLIO Set Loco direction
var $xmlioSendSetLocoDirection = function(){
	$xmlioSend("<xmlio>\n  <throttle>\n    <address>" + $paramLocoAddress + "</address>\n    <forward>" + ($forward ? "true": "false") + "</forward>\n  </throttle>\n</xmlio>");
}

//----------------------------------------- Send XMLIO Set Loco Function
var $xmlioSendSetLocoFunction = function(addr, f, ft){
	$xmlioSend("<xmlio>\n  <throttle>\n    <address>" + addr + "</address>\n    <F" + f + ">" + (ft ? "true": "false") + "</F" + f + ">\n  </throttle>\n</xmlio>");
}

//----------------------------------------- Send XMLIO Get Loco data
var $xmlioSendGetLoco = function(){
	var auxAddress = [];
	var f;
	auxAddress.push($paramLocoAddress);
	for(var i = 0; i < 29; i++){
		if($paramFnActive[i] && typeof($paramFnAddress[i]) == "number"){
			f = false;
			for(var j = 0; j < auxAddress.length; j++){
				if(auxAddress[j] === $paramFnAddress[i]){f = true; break;}
			}
			if(!f) auxAddress.push($paramFnAddress[i]);
		}
	}
	for(var i = 0; i < auxAddress.length; i++) $xmlioSendGetDevice(auxAddress[i]);
}

//----------------------------------------- Send XMLIO Get Device data
var $xmlioSendGetDevice = function(address){
	$xmlioSend("<xmlio>\n  <throttle>\n    <address>" + address + "</address>\n  </throttle>\n</xmlio>");
}

//----------------------------------------- Send XMLIO message
var $xmlioSend = function(xml){
	if ($paramXmlioDebug) alert((($iframeId.length > 0) ? "[" + $iframeId + "] " : "") + "XML to send:\n\n" + xml);
	$.ajax({
		data: xml,
		success: function(xmlReturned, status, jqXHR){
			if ($paramXmlioDebug) alert((($iframeId.length > 0) ? "[" + $iframeId + "] " : "") + "XML received:\n\n" + jqXHR.responseText);
			var $xmlReturned = $(xmlReturned);
			var $responseForThrottle = ($xmlReturned.find("throttle").text().length > 0);
			var $xmlNode;
			var address;
			var int;
			var frw;
			var s;
			var isTurnoutOrRoute = false;
			var addedTurnoutRoute = false;
			if($responseForThrottle){
				$xmlNode = $xmlReturned.find("throttle");
				address = parseInt($xmlNode.find("address").text(), 10);				
				if($xmlNode.find("speed").text().length == 0){
					$xmlioSend(jqXHR.responseText);
					return;
				}
				for(var i = 0; i < 29; i++){
					if($paramFnActive[i] && (((typeof($paramFnAddress[i]) != "number") && address == $paramLocoAddress) || address == $paramFnAddress[i])){
						s = $xmlNode.find("F" + i).text();
						if(s.length > 0){
							$FnPressed[i] = ("true" == s);
							if($paramFnShunt[i]) $shunt = $FnPressed[i];
							$updateFn(i);
							if($paramFnAutoUnpress[i] && $FnPressed[i]) $xmlioSendSetLocoFunction(typeof($paramFnAddress[i]) != "number" ? $paramLocoAddress : $paramFnAddress[i], i, false);
						}
					}
				}
				if(address == $paramLocoAddress){
					int = Math.round(parseFloat($xmlNode.find("speed").text()) * 100);
					if(int > 100) int = 100;
					if(int < 0) int = 0;
					frw = ("true" == $xmlNode.find("forward").text());
					if($shunt){
						if(frw == $forward) int+= 50; else int = 50 - int;
						frw = $forward;
					}
					$speedValue = int;
					$forward = frw;
					$updateSpeed();
				}
			} else {
				$xmlReturned.find("item").each(function(){
					$xmlNode = $(this);
					if("power" == $xmlNode.find("type").text() && "power" == $xmlNode.find("name").text()){
						$powerStatus = parseInt($xmlNode.find("value").text(), 10);
						$updatePower();
					}
					isTurnoutOrRoute = ("turnout" == $xmlNode.find("type").text() || "route" == $xmlNode.find("type").text());
					if(isTurnoutOrRoute){
						if($updateTurnoutRoute("turnout" == $xmlNode.find("type").text(), $xmlNode.find("name").text(), $xmlNode.find("userName").text(), $xmlNode.find("value").text())) addedTurnoutRoute = true;
					}
				});
				if(addedTurnoutRoute) $buildLayoutTurnoutRoute(); else if(isTurnoutOrRoute) for(var i = 0; i < $listTurnoutsRoutes.length; i++) $updateLayoutTurnoutRoute(i);
				$xmlioSend(jqXHR.responseText);
			}
		}
	});
}

//----------------------------------------- Update Turnout or Route in list
var $updateTurnoutRoute = function(turnout, name, description, status){
	var f = -1;
	for(var i = 0; i < $listTurnoutsRoutes.length; i++){
		if($listTurnoutsRoutes[i].name == name){
			f = i;
			break;
		}
	}
	if(f > -1){
			$listTurnoutsRoutes[f].status = status;
	} else {
		$listTurnoutsRoutes.push({turnout: turnout, name: name, description: description, status: status});
		$listTurnoutsRoutes.sort(function(a, b){
 			var nameA=a.name.toLowerCase(), nameB=b.name.toLowerCase();
 			if (nameA < nameB) return -1;
			if (nameA > nameB) return 1;
			return 0;
		});
	}
	return (f == -1);
}

//----------------------------------------- Build Layout for Turnouts or Routes
var $buildLayoutTurnoutRoute = function(){
	var $TurnoutsRoutes = $("#TurnoutsRoutes");
	var $TurnoutsRoutesI;
	var $divTR;
	var s = "";
	var s1 =  "<div id='divTR";
	var s2 =  "' class='TurnoutsRoutes' onclick='$changeTurnoutRoute(";
	var s3 =  ")'><label id='lblTR";
	var s4 =  "' class='lblTurnoutsRoutes'>x</label><label id='lblTRStatus";
	var s5 =  "' class='TurnoutsRoutesStatus'>x</label></div>";
	var hTR;
	var wTR;
	var h_mp;
	var w_mp;
	var n;
	var t = 0;
	var l = 0;
	$TurnoutsRoutes.height($viewportHeight);
	$TurnoutsRoutes.width($viewportWidth);
	if($changeBothSizes){
		n = ($TurnoutsRoutes.height() * $SizeReferencePercent / 100);
		$SizeReference = Math.round((n > $SizeReferenceMin) ? n : $SizeReferenceMin);
	}
	$TurnoutsRoutes.offset({top: 0, left: 0});
	for(var i = 0; i < $listTurnoutsRoutes.length; i++) s+= s1 + i + s2 + i + s3 + i + s4 + i + s5;
	$TurnoutsRoutes.html(s);
	$TurnoutsRoutesI = $(".TurnoutsRoutes");
	n = Math.floor($TurnoutsRoutes.width() / $TurnoutRouteWidthRef) + 1;
	wTR = Math.floor($viewportWidth / n);
	$(".lblTurnoutsRoutes").css("font-size", $SizeReference * 0.4);
	$(".TurnoutsRoutesStatus").css("font-size", $SizeReference * 0.4);
	hTR = $TurnoutsRoutesI.height();
	h_mp = $.calcTotalMBP($TurnoutsRoutesI, false);
	w_mp = $.calcTotalMBP($TurnoutsRoutesI, true);
	$TurnoutsRoutesI.width(wTR - w_mp);
	for(var i = 0; i < $listTurnoutsRoutes.length; i++){
		$divTR = $("#divTR" + i);
		$divTR.css("top", t);
		$divTR.css("left", l);
		if(l + wTR * 2 <= $TurnoutsRoutes.width()) l+= wTR; else {l = 0; t+= hTR + h_mp;}
		$("#lblTR" + i).text($listTurnoutsRoutes[i].name + " - " + $listTurnoutsRoutes[i].description);
	}
	for(var i = 0; i < $listTurnoutsRoutes.length; i++) $updateLayoutTurnoutRoute(i);
	$TurnoutsRoutesI.height(hTR);
}

//----------------------------------------- Update Layout for Turnouts or Routes
var $updateLayoutTurnoutRoute = function(index){
	var st = $listTurnoutsRoutes[index].status;
	var $TurnoutRouteStatus = $("#lblTRStatus" + index);
	if($TurnoutRouteStatus.hasClass("TRon")) $TurnoutRouteStatus.removeClass("TRon");
	if($TurnoutRouteStatus.hasClass("TRoff")) $TurnoutRouteStatus.removeClass("TRoff");
	if($TurnoutRouteStatus.hasClass("TRdisabled")) $TurnoutRouteStatus.removeClass("TRdisabled");
	if($TurnoutRouteStatus.hasClass("TRundefined")) $TurnoutRouteStatus.removeClass("TRundefined");
	if($listTurnoutsRoutes[index].turnout){
		switch (st) {
			case "2": {		//Closed
				$TurnoutRouteStatus.text("C");
				$TurnoutRouteStatus.addClass("TRon");
				break;
			}
			case "4": {		//Thrown
				$TurnoutRouteStatus.text("T");
				$TurnoutRouteStatus.addClass("TRoff");
				break;
			}
			default: {		//undefined
				$TurnoutRouteStatus.text("?");
				$TurnoutRouteStatus.addClass("TRundefined");
				break;
			}
		}
	} else {
		switch (st) {
			case "2": {		//Active
				$TurnoutRouteStatus.text("On");
				$TurnoutRouteStatus.addClass("TRon");
				break;
			}
			case "4": {		//Inactive
				$TurnoutRouteStatus.text("Off");
				$TurnoutRouteStatus.addClass("TRoff");
				break;
			}
			case "0": {		//Disabled
				$TurnoutRouteStatus.text("X");
				$TurnoutRouteStatus.addClass("TRdisabled");
				break;
			}
			default: {		//undefined
				$TurnoutRouteStatus.text("?");
				$TurnoutRouteStatus.addClass("TRundefined");
				break;
			}
		}
	}
}

//----------------------------------------- Click Turnout or Route
var $changeTurnoutRoute = function(index){
	var st = $listTurnoutsRoutes[index].status;
	if($listTurnoutsRoutes[index].turnout){		// 1(undefined) 2(Closed) 4(Thrown)
		if("2" == st) $listTurnoutsRoutes[index].status = "4"; else $listTurnoutsRoutes[index].status = "2";
		$updateLayoutTurnoutRoute(index);
		$xmlioSendSetTurnout($listTurnoutsRoutes[index].name, $listTurnoutsRoutes[index].status);
	} else {									// 0(disable - cannot change) 1(undefined) 2(Active) 4(Inactive) - Can only activate
		if("1" == st || "4" == st){
			$listTurnoutsRoutes[index].status = "2";
			$updateLayoutTurnoutRoute(index);
			$xmlioSendSetRoute($listTurnoutsRoutes[index].name, $listTurnoutsRoutes[index].status);
		}
	}
}

//----------------------------------------- Retrieve images file list from dir
var $imagesInDir = function(dir){
	$.ajax({
		url: dir,
		async: false,
		type: "GET",
		dataType: "html",
		error: function(jqXHR, textStatus, errorThrown){
			if(jqXHR.status == 404) alert("Error calling '" + dir + "' page."); else alert("AJAX error.\n\nStatus:\n" + jqXHR.status + "\n\nResponse:\n" + jqXHR.responseText + "\n\nError:\n" + errorThrown);
		},
		success: function(htmlReturned, status, jqXHR){
			var $htmlReturned = $(htmlReturned);
			$htmlReturned.find("td a").each(function(){ 
				var f = $(this).attr("href");
				var e = f.slice(f.lastIndexOf(".") + 1).toLowerCase();
				if("ico" == e || "gif" == e || "png" == e || "jpg" == e || "jpeg" == e) $imagesFileList.push(f);
			});
			$imagesFileList.sort();
		}
	});
}

//----------------------------------------- Error loading image
var $errorLoadingImage = function(obj){
	//alert("Error loading image: " + obj.attr("src").split("?")[0] + "\n\nPlease check if file exists.\nIf it exists, please refresh the page.");
        $(obj).remove();
}

//----------------------------------------- Host resize helper
var $correctForSomeImageTypes = function(url){
	var s = url.split("?")[0];
	var e = s.slice(s.lastIndexOf(".") + 1).toLowerCase();
	if("ico" != e && "gif" != e && "png" != e) s = url; 		//Do not resize at the host: ICO (to show), GIF (animated), PNG (animated)
	return s;
}

//----------------------------------------- Resize function image
var $resizeFunctionImage = function(thisObj){
	var op = thisObj.parent();
	if (thisObj.height() / thisObj.width() > op.height() / op.width()){
		thisObj.height(op.height());
		thisObj.css("top", parseInt(op.css("margin-top").replace("px",""), 10) + parseInt(op.css("border-top-width").replace("px",""), 10) + parseInt(op.css("padding-top").replace("px",""), 10));
		thisObj.css("left", (op.width() - thisObj.width()) / 2);
	} else {
		thisObj.width(op.width());
		thisObj.css("top", (op.height() - thisObj.height()) / 2);
		thisObj.css("left", parseInt(op.css("margin-left").replace("px",""), 10) + parseInt(op.css("border-left-width").replace("px",""), 10) + parseInt(op.css("padding-left").replace("px",""), 10));
	}
}

//----------------------------------------- Run at start up
$(document).ready(function(){
	$readParametersFromQueryString();
	if(!$paramRemoveFromLocalStorage && $paramFrameName.length > 0){
		$manageFrameDisplay();
		return;
	};
	if(!$paramRemoveFromLocalStorage && ($paramTurnouts || $paramRoutes)){
		$.ajaxSetup({url: $xmlioPath, async: true, cache: false, type: "POST", dataType: "xml"});
		$changeBothSizes = (($viewportWidth != $(window).width()) && ($viewportHeight != $(window).height()));
		$viewportWidth = $(window).width();
		$viewportHeight = $(window).height();
		$portrait = ($viewportWidth <= $viewportHeight);
		$("body").html("<div id='TurnoutsRoutes'></div>");
		window.setInterval(
			"if(($viewportHeight != $(window).height()) || ($viewportWidth != $(window).width())){" +
			"	$changeBothSizes = (($viewportWidth != $(window).width()) && ($viewportHeight != $(window).height()));" +
			"	$viewportHeight = $(window).height();" +
			"	$viewportWidth = $(window).width();" +
			"	$portrait = ($viewportWidth <= $viewportHeight);" +
			"	$buildLayoutTurnoutRoute();" +
			"}"
			, $ResizeCheckInterval
		);
		if($paramTurnouts){
			$xmlioSendGetTurnouts();
			return;
		};
		if($paramRoutes){
			$xmlioSendGetRoutes();
			return;
		};
	};
	$("body").html(
		"<div id='throttle'>" +
			"<div id='throttleMain'>" +
				"<div id='powerButton'></div>" +
				"<div id='locoInfo'></div>" +
				"<div id='settingsButton'></div>" +
				"<div id='locoImage'></div>" +
				"<div id='speed'></div>" +
				"<div id='functions'></div>" +
			"</div>" +
			"<div id='settings'></div>" +
			"<div id='settingsLoco'></div>" +
			"<div id='queryString'></div>" +
			"<div id='help'></div>" +
		"</div>" +
		"<div id='throttles'></div>"
	);
//$DisplayAllParameters();														//For debug
	$isMultiPage = (!$paramRemoveFromLocalStorage && $paramThrottles.length > 0);
	$insideMulti = (window.top != window.self && parent.document.getElementById("throttle") && parent.document.getElementById("throttles"));
	if($isMultiPage){
		$("#throttle").hide();
		$onStart_inControlMulti();
	} else {
		$("#throttles").hide();
		$onStart_inControl();
	}
});

//----------------------------------------- Manage frame display
var $manageFrameDisplay = function(){
	$viewportHeight = $(window).height();
	$viewportWidth = $(window).width();
	$("body").css("overflow", "hidden");		//to fix possible margins overflow
	$("body").html("<img id='imgFrame' alt='" + $paramFrameName + "' title='" + $paramFrameName + "' border='0' ismap='ismap' style='margin: -8px -8px -8px -8px;' />");	//to fix iframe margins (top, right, bottom, left)
	var $imgFrame = $("#imgFrame");
	$imgFrame.css("cursor", "pointer");
	$imgFrame.load(function(){
		if($frameInitHeight >= 0 && $frameInitWidth >= 0) return;
		$frameInitHeight = $imgFrame.height();
		$frameInitWidth = $imgFrame.width();
		$imgFrame.height($viewportHeight);
		$imgFrame.width($viewportWidth);
		window.setInterval(
			"$('#imgFrame').attr('src', '/frame/' + encodeURIComponent($paramFrameName) + '.png?loop=' + Math.floor(Math.random() * 9999));"	//URL must be different to force retrieve
			, $RetrieveFrameInterval
		);
		window.setInterval(
			"if(($viewportHeight != $(window).height()) || ($viewportWidth != $(window).width())){" +
			"	$viewportHeight = $(window).height();" +
			"	$viewportWidth = $(window).width();" +
			"	var $imgFrame = $('#imgFrame');" +
			"	$imgFrame.height($viewportHeight);" +
			"	$imgFrame.width($viewportWidth);" +
			"}"
			, $ResizeCheckInterval
		);
	});
	$imgFrame.error(function(){
		$frameInitHeight = $viewportHeight;
		$frameInitWidth = $viewportWidth;
		alert("Could not load frame '" + $paramFrameName + "'." + "\n\nPlease check if it is opened.\nIf yes, please refresh the page.");
	});
	$imgFrame.click(function(e){
		var $imgFrame = $("#imgFrame");
		$imgFrame.css("cursor", "crosshair");
   		window.setTimeout(
        	"$('#imgFrame').css('cursor', 'pointer');"
			, 200
		); 
		var $Y = Math.round(e.clientY * $frameInitHeight / $imgFrame.height());
		var $X = Math.round(e.clientX * $frameInitWidth / $imgFrame.width());
		$.get("/frame/" + encodeURIComponent($paramFrameName) + ".html?" + $X + "," + $Y, function(data) {
			$("#imgFrame").attr("src", "/frame/" + encodeURIComponent($paramFrameName) + ".png?click=" + Math.floor(Math.random() * 9999));		//URL must be different to force retrieve
		});
		e.preventDefault();
	});
	$imgFrame.attr("src", "/frame/" + encodeURIComponent($paramFrameName) + ".png");
}

//----------------------------------------- Run at start (inControlMulti)
var $onStart_inControlMulti = function(){
	$buildLayoutMulti();
	window.setInterval(
		"if(($viewportHeight != $(window).height()) || ($viewportWidth != $(window).width())){" +
		"	$viewportHeight = $(window).height();" +
		"	$viewportWidth = $(window).width();" +
		"	$rebuildLayoutMulti();" +	
		"}"
		, $ResizeCheckInterval
	);
}

//----------------------------------------- Buid multi layout
var $buildLayoutMulti = function(){
	var $throttles = $("#throttles");
	var s = "";
	var s1 =  "<div id='divT";
	var s2 =  "' class='throttles'></div>";
	$throttles.offset({top: 0, left: 0});
	for(var i = 0; i < $paramThrottles.length; i++) s+= s1 + i + s2;
	$throttles.html(s);
	if($paramThrottles.length == 0) return;			//Never happens
	for(var i = 0; i < $paramThrottles.length; i++){
		$("#divT" + i).html("<iframe id='throttle-" + (i + 1) + "' scrolling='no' frameborder='0' class='ifrm1'></iframe>");
		$("#throttle-" + (i + 1)).attr("src", "inControl.html?" + ($paramXmlioDebug ? "xmliodebug=t" : "xmliodebug=") + ((i == 0) ? "&power=t" : "&power=") + "&loconame=" + encodeURIComponent($paramThrottles[i]));
	}
}

//----------------------------------------- Rebuid multi layout
var $rebuildLayoutMulti = function(){
	var $throttles = $("#throttles");
	var $divT;
	var t = 0;
	var l = 0;
	var tc;
	var lc;
	var h;
	var w;
	var hi;
	var wi;
	var h_mp;
	var w_mp;
	$throttles.height($viewportHeight);
	$throttles.width($viewportWidth);
	if($paramThrottles.length == 0) return;			//Never happens
	h_mp = $.calcTotalMBP($(".throttles"), false);
	w_mp = $.calcTotalMBP($(".throttles"), true);
	h = Math.round(Math.sqrt($throttles.height() / $throttles.width() * $paramThrottles.length));
	w = Math.round($paramThrottles.length / h);
	if(h * w < $paramThrottles.length){
		if($throttles.height() / $throttles.width() > h / w) h++; else w++;
	}
	if((h * w - $paramThrottles.length) >= ((h > w) ? w : h)){
		if($throttles.height() / $throttles.width() > h / w) h--; else w--;
	}
	hi = Math.floor($throttles.height() / h);					//Each rectangle height (h: Number of rectangles per column)
	wi = Math.floor($throttles.width() / w);					//Each rectangle width (w: Number of rectangles per row)
	tc = Math.floor(($throttles.height() - (h * hi)) / 2);
	lc = Math.floor(($throttles.width() - (w * wi)) / 2);
	for(var i = 0; i < $paramThrottles.length; i++){
		$divT = $("#divT" + i);
		$divT.css("top", t + tc);
		$divT.css("left", l + lc);
		$divT.height(hi - h_mp);
		$divT.width(wi - w_mp);
		$(".ifrm1").height($divT.height());
		$(".ifrm1").width($divT.width());
		$("#throttle-" + (i + 1)).attr("speedctrlright", ((l + lc + wi / 2 > $throttles.width() / 2) ? "t" : ""));
		if(l + lc + wi * 2 <= $throttles.width()) l+= wi; else {l = 0; t+= hi;}
	}
}

//----------------------------------------- Run at start (inControl)
var $onStart_inControl = function(){
	var f;
	$.ajaxSetup({url: $xmlioPath, async: true, cache: false, type: "POST", dataType: "xml"});
	$("#settings").hide();
	$loadApplicationParameters();
	if($paramRemoveFromLocalStorage){
		$removeApplicationParameters();
		alert("Parameters removed from Local Storage.");
		self.close();
		return;
	}
	$imagesInDir($imagesPath);
	$loadRoster();
	if($paramLocoName == "" && $LocoNameList.length > 0) $paramLocoName = $LocoNameList[0];
	$loadParametersByLocoName($paramLocoName);
//$DisplayAllParameters();														//For debug
	f = false;
	for(var i = 0; i < $LocoNameList.length; i++){
		if($LocoNameList[i] == $paramLocoName){
			f = true;
			break;
		}
	}
	if(!f){
		$LocoNameList.push($paramLocoName);
		$LocoNameList.sort();
		$saveLocoNameList();
		$saveParametersByLocoName($paramLocoName);
	}
	for(var i = 0; i < 29; i++) $FnPressed[i] = false;
	$shunt = false;
	$sizeAndBuild();
	$xmlioSendGetPower();
	$xmlioSendGetLoco();
	window.setInterval(
		"if(($viewportHeight != $(window).height()) || ($viewportWidth != $(window).width())) $sizeAndBuild();"
		, $ResizeCheckInterval
	);
};

//----------------------------------------- Retrieve roster
var $loadRoster = function(){
	$.ajax({
		url: $xmlRosterPath,
		async: false,
		type: "GET",
		error: function(jqXHR, textStatus, errorThrown){
			if(jqXHR.status == 404) alert("Roster empty.\nNo locos defined in JMRI."); else alert("AJAX error.\n\nStatus:\n" + jqXHR.status + "\n\nResponse:\n" + jqXHR.responseText + "\n\nError:\n" + errorThrown);
		},
		success: function(xmlReturned, status, jqXHR){
			var $locoName = $paramLocoName;
			var $locoAddress = $paramLocoAddress;
			var $locoImage = $paramLocoImage;
			var $locoNoSpeedCtrl = $paramLocoNoSpeedCtrl;
			var $locoFnActive = [];
			var $locoFnToggle = [];
			var $locoFnAutoUnpress = [];
			var $locoFnLabel = [];
			var $locoFnImage = [];
			var $locoFnImagePressed = [];
			var $locoFnAddress = [];
			var $locoFnShunt = [];
			for(var i = 0; i < 29; i++){
				$locoFnActive[i] = $paramFnActive[i];
				$locoFnToggle[i] = $paramFnToggle[i];
				$locoFnAutoUnpress[i] = $paramFnAutoUnpress[i];
				$locoFnLabel[i] = $paramFnLabel[i];
				$locoFnImage[i] = $paramFnImage[i];
				$locoFnImagePressed[i] = $paramFnImagePressed[i];
				$locoFnAddress[i] = $paramFnAddress[i];
				$locoFnShunt[i] = $paramFnShunt[i];
			}
			var $xmlReturned = $(xmlReturned);
			var $locos = [];
			$xmlReturned.find("roster-config roster locomotive").each(function(){ 
				var $loco = {
					name: $.trim($(this).attr("id")),
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
			var $auxImg;
			var $auxInt;
			var f;
			for(var i = 0; i < $locos.length; i++){
				$paramLocoName = $locos[i].name;
				$cleanParametersByLocoName($paramLocoName);
				$loadParametersByLocoName($paramLocoName);
				$paramLocoAddress = $locos[i].address;
				$auxImg = $auxImage($locos[i].image, $locos[i].icon);
				if($auxImg.length > 0) if($auxImg.indexOf("/") != 0) $auxImg = $userImagesPath + $auxImg;
				if($auxImg.length > 0) $paramLocoImage = $auxImg;
				for(var j = 0; j < $locos[i].functions.length; j++){
					$paramFnActive[$locos[i].functions[j].number] = true;
					$paramFnToggle[$locos[i].functions[j].number] = $locos[i].functions[j].toggle;
					if($locos[i].functions[j].label.length > 0) $paramFnLabel[$locos[i].functions[j].number] = $locos[i].functions[j].label;
					$auxImg = $auxImage($locos[i].functions[j].image);
					if($auxImg.length > 0) if($auxImg.indexOf("/") != 0) $auxImg = $userImagesPath + $auxImg;
					if($auxImg.length > 0) $paramFnImage[$locos[i].functions[j].number] = $auxImg;
					$auxImg = $auxImage($locos[i].functions[j].imagePressed);
					if($auxImg.length > 0) if($auxImg.indexOf("/") != 0) $auxImg = $userImagesPath + $auxImg;
					if($auxImg.length > 0) $paramFnImagePressed[$locos[i].functions[j].number] = $auxImg;
				}
				if($locos[i].shunt.length > 1){
					$auxInt = parseInt($locos[i].shunt.substr(1), 10);
					$paramFnActive[$auxInt] = true;
					$paramFnToggle[$auxInt] = true;
					$paramFnShunt[$auxInt] = true;
				}
				f = false;
				for(var l = 0; l < $LocoNameList.length; l++){
					if($LocoNameList[l] == $paramLocoName){
						f = true;
						break;
					}
				}
				if(!f) $LocoNameList.push($paramLocoName);
				$saveParametersByLocoName($paramLocoName);
			}
			$LocoNameList.sort();
			$saveLocoNameList();
			$paramLocoName = $locoName;
			$paramLocoAddress = $locoAddress;
			$paramLocoImage = $locoImage;
			$locoNoSpeedCtrl = $paramLocoNoSpeedCtrl;
			for(var i = 0; i < 29; i++){
				$paramFnActive[i] = $locoFnActive[i];
				$paramFnToggle[i] = $locoFnToggle[i];
				$paramFnAutoUnpress[i] = $locoFnAutoUnpress[i];
				$paramFnLabel[i] = $locoFnLabel[i];
				$paramFnImage[i] = $locoFnImage[i];
				$paramFnImagePressed[i] = $locoFnImagePressed[i];
				$paramFnAddress[i] = $locoFnAddress[i];
				$paramFnShunt[i] = $locoFnShunt[i];
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

//----------------------------------------- Standard size and build repeat
var $sizeAndBuild = function(){
	$changeBothSizes = (($viewportWidth != $(window).width()) && ($viewportHeight != $(window).height()));
	$viewportWidth = $(window).width();
	$viewportHeight = $(window).height();
	$portrait = ($viewportWidth <= $viewportHeight);
	$buildLayout();
}

//----------------------------------------- Display all parameters	(for debug)
var $DisplayAllParameters = function(){
	var s = "";
	s+= ">>> Number of Throttles: " + $paramThrottles.length;
	for(var i = 0; i < $paramThrottles.length; i++) s+= "\n. Throttle(" + i + ") [" + $paramThrottles[i] + "]";
	s+= "\nTextSizeRatio [" + $paramTextSizeRatio + "]";
	s+= "\nPower [" + $paramPower + "]";
	s+= "\nSpeedCtrlRight [" + $paramSpeedCtrlRight + "]";
	s+= "\nSpeedCtrlButtons [" + $paramSpeedCtrlButtons + "]";
	s+= "\nFrameName [" + $paramFrameName + "]";
	alert(s);
	$DisplayLocoNameList();
	$DisplayCurrentLocoInfo();
}

//----------------------------------------- Display Locos list	(for debug)
var $DisplayLocoNameList = function(){
	var s = "";
	s+= ">>> Number of Locos/Devices: " + $LocoNameList.length;
	for(var i = 0; i < $LocoNameList.length; i++) s+= "\n. Loco/Device(" + i + ") [" + $LocoNameList[i] + "]";
	alert(s);
}

//----------------------------------------- Display current Loco info	(for debug)
var $DisplayCurrentLocoInfo = function(){
	var s = "Loco/Device:\n";
	s+= "LocoName [" + $paramLocoName + "]\n";
	s+= "LocoAddress [" + $paramLocoAddress + "]\n";
	s+= "LocoImage [" + $paramLocoImage + "]\n";
	s+= "LocoNoSpeedCtrl [" + $paramLocoNoSpeedCtrl + "]";
	for(var i = 0; i < 29; i++) {
		s+= "\nF" + i + "Active [" + $paramFnActive[i] + "] ... ";
		s+= "F" + i + "Toggle [" + $paramFnToggle[i] + "] ... ";
		s+= "F" + i + "AutoUnpress [" + $paramFnAutoUnpress[i] + "] ... ";
		s+= "F" + i + "Label [" + $paramFnLabel[i] + "] ... ";
		s+= "F" + i + "Image [" + $paramFnImage[i] + "] ... ";
		s+= "F" + i + "ImagePressed [" + $paramFnImagePressed[i] + "] ... ";
		s+= "F" + i + "Address [" + $paramFnAddress[i] + "] ... ";
		s+= "F" + i + "Shunt [" + $paramFnShunt[i] + "] ... ";
	}
	alert(s);
}

//----------------------------------------- Read parameters from query string (case sensitive)
var $readParametersFromQueryString = function(){
	var s;
	var n;
	var nThrottles = 0;
	allUrlParameters = $.getUrlParameters();
	$paramRemoveFromLocalStorage = (typeof(allUrlParameters["removefromlocalstorage"]) != "undefined" && $.trim(allUrlParameters["removefromlocalstorage"]).length > 0);
	if(typeof(allUrlParameters["framename"]) == "undefined") allUrlParameters["framename"] = "";
	$paramFrameName = $.trim(allUrlParameters["framename"]);
	$paramTurnouts = (typeof(allUrlParameters["turnouts"]) != "undefined" && $.trim(allUrlParameters["turnouts"]).length > 0);
	$paramRoutes = (typeof(allUrlParameters["routes"]) != "undefined" && $.trim(allUrlParameters["routes"]).length > 0);
	for(var i = 0; i < allUrlParameters.length; i++){
		if(allUrlParameters[i].length > 8 && allUrlParameters[i].substr(0, 8) == "loconame"){
			n = parseInt(allUrlParameters[i].substr(8), 10);
			if(isNaN(n)) n = 0;
			n = Math.abs(n);
			if((n + "") != allUrlParameters[i].substr(8)) n = 0;
			if(n > nThrottles) nThrottles = n;
		}
	}
	for(var i = 1; i <= nThrottles; i++){
		if(typeof(allUrlParameters["loconame" + i]) == "undefined") allUrlParameters["loconame" + i] = "";
		$paramThrottles.push($.trim(allUrlParameters["loconame" + i]));
	}
	$paramTextSizeRatio = "";
	if(typeof(allUrlParameters["textsize"]) != "undefined"){
		s = $.trim(allUrlParameters["textsize"]);
		if(s != "") {
			if(s.charAt(0).toLowerCase() == "s") $paramTextSizeRatio = $TextSizeRatioS * 1;
			if(s.charAt(0).toLowerCase() == "n") $paramTextSizeRatio = $TextSizeRatioN * 1;
			if(s.charAt(0).toLowerCase() == "l") $paramTextSizeRatio = $TextSizeRatioL * 1;
		}
	}
	if(typeof(allUrlParameters["power"]) == "undefined") $paramPower = ""; else $paramPower = ($.trim(allUrlParameters["power"]).length > 0);
	if(typeof(allUrlParameters["speedctrlright"]) == "undefined") $paramSpeedCtrlRight = ""; else $paramSpeedCtrlRight = ($.trim(allUrlParameters["speedctrlright"]).length > 0);
	if(typeof(allUrlParameters["speedctrlbuttons"]) == "undefined") $paramSpeedCtrlButtons = ""; else $paramSpeedCtrlButtons = ($.trim(allUrlParameters["speedctrlbuttons"]).length > 0);
	if(typeof(allUrlParameters["loconame"]) == "undefined"){
		$paramLocoName = "";
	} else {
		$paramLocoName = $.trim(allUrlParameters["loconame"]);
		if($paramThrottles.length >= 1){
			$paramThrottles.push($paramLocoName);
			$paramLocoName = "";
		}
	}
	if($paramThrottles.length == 1) $paramLocoName = $paramThrottles.pop();
	if(typeof(allUrlParameters["locoaddress"]) == "undefined") $paramLocoAddress = ""; else {
		$paramLocoAddress = parseInt($.trim(allUrlParameters["locoaddress"]), 10);
		if(isNaN($paramLocoAddress)) $paramLocoAddress = ""; else {
			$paramLocoAddress = Math.abs($paramLocoAddress) + "";
			$paramLocoAddress = parseInt($paramLocoAddress.slice(-4), 10);
		}
	}
	if(typeof(allUrlParameters["locoimage"]) == "undefined") allUrlParameters["locoimage"] = "";
	$paramLocoImage = $.trim(allUrlParameters["locoimage"]);
	if($paramLocoImage.length > 0) if($paramLocoImage.indexOf("/") != 0) $paramLocoImage = $userImagesPath + $paramLocoImage;
	if(typeof(allUrlParameters["loconospeedctrl"]) == "undefined") $paramLocoNoSpeedCtrl = ""; else $paramLocoNoSpeedCtrl = ($.trim(allUrlParameters["loconospeedctrl"]).length > 0);
	for(var i = 0; i < 29; i++){
		if(typeof(allUrlParameters["f" + i + "active"]) == "undefined") $paramFnActive[i] = ""; else $paramFnActive[i] = ($.trim(allUrlParameters["f" + i + "active"]).length > 0);
		if(typeof(allUrlParameters["f" + i + "toggle"]) == "undefined") $paramFnToggle[i] = ""; else $paramFnToggle[i] = ($.trim(allUrlParameters["f" + i + "toggle"]).length > 0);
		if(typeof(allUrlParameters["f" + i + "autounpress"]) == "undefined") $paramFnAutoUnpress[i] = ""; else $paramFnAutoUnpress[i] = ($.trim(allUrlParameters["f" + i + "autounpress"]).length > 0);
		if(typeof(allUrlParameters["f" + i + "label"]) == "undefined") allUrlParameters["f" + i + "label"] = "";
		$paramFnLabel[i] = $.trim(allUrlParameters["f" + i + "label"]);
		if(typeof(allUrlParameters["f" + i + "image"]) == "undefined") allUrlParameters["f" + i + "image"] = "";
		$paramFnImage[i] = $.trim(allUrlParameters["f" + i + "image"]);
		if($paramFnImage[i].length > 0) if($paramFnImage[i].indexOf("/") != 0) $paramFnImage[i] = $userImagesPath + $paramFnImage[i];
		if(typeof(allUrlParameters["f" + i + "imagepressed"]) == "undefined") allUrlParameters["f" + i + "imagepressed"] = "";
		$paramFnImagePressed[i] = $.trim(allUrlParameters["f" + i + "imagepressed"]);
		if($paramFnImagePressed[i].length > 0) if($paramFnImagePressed[i].indexOf("/") != 0) $paramFnImagePressed[i] = $userImagesPath + $paramFnImagePressed[i];
		if(typeof(allUrlParameters["f" + i + "address"]) == "undefined") $paramFnAddress[i] = ""; else {
			$paramFnAddress[i] = parseInt($.trim(allUrlParameters["f" + i + "address"]), 10);
			if(isNaN($paramFnAddress[i])) $paramFnAddress[i] = ""; else {
				$paramFnAddress[i] = Math.abs($paramFnAddress[i]) + "";
				$paramFnAddress[i] = parseInt($paramFnAddress[i].slice(-4), 10);
			}
		}
		if(typeof(allUrlParameters["f" + i + "shunt"]) == "undefined") $paramFnShunt[i] = ""; else $paramFnShunt[i] = ($.trim(allUrlParameters["f" + i + "shunt"]).length > 0);
		if(typeof($paramFnActive[i]) != "boolean" && (typeof($paramFnToggle[i]) == "boolean" || typeof($paramFnAutoUnpress[i]) == "boolean" || $paramFnLabel[i] != "" || $paramFnImage[i] != "" || $paramFnImagePressed[i] != "" || typeof($paramFnShunt[i]) == "boolean")) $paramFnActive[i] = true;
		if(typeof($paramFnToggle[i]) != "boolean" && ($paramFnImagePressed[i] != "" || typeof($paramFnShunt[i]) == "boolean")) $paramFnToggle[i] = true;
	}
	$paramXmlioDebug = (typeof(allUrlParameters["xmliodebug"]) != "undefined" && $.trim(allUrlParameters["xmliodebug"]).length > 0);
}

//----------------------------------------- Save Locos list
var $saveLocoNameList = function(){
	$.saveLocalInfo("inControl.locoListSize", $LocoNameList.length);
	for(var i = 0; i < $LocoNameList.length; i++) $.saveLocalInfo("inControl.locoList[" + i + "]", $LocoNameList[i]);
}

//----------------------------------------- Load application parameters
var $loadApplicationParameters = function(){
	var s;
	var l;
//Load only if 'typeof() != number'
	if(typeof($paramTextSizeRatio) != "number"){
		s = $.loadLocalInfo("inControl.TextSizeRatio");
		if(typeof(s) == "undefined") s = $TextSizeRatioN + "";
		s = parseFloat($.trim(s));
		if(isNaN(s)) s = $TextSizeRatioN; else s = Math.abs(s);
		if(s < $TextSizeRatioS) s = $TextSizeRatioS;
		if(s > $TextSizeRatioL) s = $TextSizeRatioL;
		if(s > $TextSizeRatioS && s < $TextSizeRatioL) s = $TextSizeRatioN;
		$paramTextSizeRatio = s;
	}
//Load only if 'typeof() != boolean'
	if(typeof($paramPower) != "boolean"){
		s = $.loadLocalInfo("inControl.Power");
		$paramPower = ((typeof(s) != "undefined") && s.length > 0);
	}
//Load only if 'typeof() != boolean'
	if(typeof($paramSpeedCtrlRight) != "boolean"){
		s = $.loadLocalInfo("inControl.SpeedCtrlRight");
		$paramSpeedCtrlRight = ((typeof(s) != "undefined") && s.length > 0);
	}
//Load only if 'typeof() != boolean'
	if(typeof($paramSpeedCtrlButtons) != "boolean"){
		s = $.loadLocalInfo("inControl.SpeedCtrlButtons");
		$paramSpeedCtrlButtons = ((typeof(s) != "undefined") && s.length > 0);
	}
	l = $.loadLocalInfo("inControl.locoListSize");
	if(typeof(l) == "undefined") l = "0";
	l = parseInt($.trim(l), 10);
	if(isNaN(l)) l = 0; else l = Math.abs(l);
	for(var i = 0; i < l; i++) $LocoNameList[i] = $.loadLocalInfo("inControl.locoList[" + i + "]");
}

//----------------------------------------- Remove all application parameters
var $removeApplicationParameters = function(){
	$.removeLocalInfo("inControl.TextSizeRatio");
	$.removeLocalInfo("inControl.Power");
	$.removeLocalInfo("inControl.SpeedCtrlRight");
	$.removeLocalInfo("inControl.SpeedCtrlButtons");
	$.removeLocalInfo("inControl.locoListSize");
	for(var i = 0; i < $LocoNameList.length; i++){
		$removeParametersByLocoName($LocoNameList[i]);
		$.removeLocalInfo("inControl.locoList[" + i + "]");
	}
}

//----------------------------------------- Clean parameters by loco name
var $cleanParametersByLocoName = function(loconame){
	$paramLocoAddress = "";
	$paramLocoImage = "";
	$paramLocoNoSpeedCtrl = "";
	for(var i = 0; i < 29; i++) {
		$paramFnActive[i] = "";
		$paramFnToggle[i] = "";
		$paramFnAutoUnpress[i] = "";
		$paramFnLabel[i] = "";
		$paramFnImage[i] = "";
		$paramFnImagePressed[i] = "";
		$paramFnAddress[i] = "";
		$paramFnShunt[i] = "";
		$FnPressed[i] = false;
	}
	$shunt = false;
}

//----------------------------------------- Save parameters by loco name
var $saveParametersByLocoName = function(loconame){
	$.saveLocalInfo("inControl.loco(" + loconame + ")LocoAddress", $paramLocoAddress);
	$.saveLocalInfo("inControl.loco(" + loconame + ")LocoImage", $paramLocoImage);
	$.saveLocalInfo("inControl.loco(" + loconame + ")LocoNoSpeedCtrl", ($paramLocoNoSpeedCtrl ? "true" : ""));
	for(var i = 0; i < 29; i++) {
		$.saveLocalInfo("inControl.loco(" + loconame + ")FnActive[" + i + "]", ($paramFnActive[i] ? "true" : ""));
		$.saveLocalInfo("inControl.loco(" + loconame + ")FnToggle[" + i + "]", ($paramFnToggle[i] ? "true" : ""));
		$.saveLocalInfo("inControl.loco(" + loconame + ")FnAutoUnpress[" + i + "]", ($paramFnAutoUnpress[i] ? "true" : ""));
		$.saveLocalInfo("inControl.loco(" + loconame + ")FnLabel[" + i + "]", $paramFnLabel[i]);
		$.saveLocalInfo("inControl.loco(" + loconame + ")FnImage[" + i + "]", $paramFnImage[i]);
		$.saveLocalInfo("inControl.loco(" + loconame + ")FnImagePressed[" + i + "]", $paramFnImagePressed[i]);
		$.saveLocalInfo("inControl.loco(" + loconame + ")FnAddress[" + i + "]", $paramFnAddress[i]);
		$.saveLocalInfo("inControl.loco(" + loconame + ")FnShunt[" + i + "]", ($paramFnShunt[i] ? "true" : ""));
	}
}

//----------------------------------------- Load parameters by loco name
var $loadParametersByLocoName = function(loconame){
	var s;
	var auxShunt = false;
//Load only if 'string empty'
	if($paramLocoAddress == ""){
		$paramLocoAddress = $loadLocoAddressByLocoName(loconame);
	}
//Load only if 'string empty'
	if($paramLocoImage == ""){
		$paramLocoImage = $.loadLocalInfo("inControl.loco(" + loconame + ")LocoImage");
		if(typeof($paramLocoImage) == "undefined") $paramLocoImage = "";
	}
//Load only if 'typeof() != boolean'
	if(typeof($paramLocoNoSpeedCtrl) != "boolean"){
		s = $.loadLocalInfo("inControl.loco(" + loconame + ")LocoNoSpeedCtrl");
		$paramLocoNoSpeedCtrl = ((typeof(s) != "undefined") && s.length > 0);
	}
	for(var i = 0; i < 29; i++) {
//Load only if 'typeof() != boolean'
		if(typeof($paramFnActive[i]) != "boolean"){
			s = $.loadLocalInfo("inControl.loco(" + loconame + ")FnActive[" + i + "]");
			$paramFnActive[i] = ((typeof(s) != "undefined") && s.length > 0);
		}
//Load only if 'typeof() != boolean'
		if(typeof($paramFnToggle[i]) != "boolean"){
			s = $.loadLocalInfo("inControl.loco(" + loconame + ")FnToggle[" + i + "]");
			$paramFnToggle[i] = ((typeof(s) != "undefined") && s.length > 0);
		}
//Load only if 'typeof() != boolean'
		if(typeof($paramFnAutoUnpress[i]) != "boolean"){
			s = $.loadLocalInfo("inControl.loco(" + loconame + ")FnAutoUnpress[" + i + "]");
			$paramFnAutoUnpress[i] = ((typeof(s) != "undefined") && s.length > 0);
		}
//Load only if 'string empty'
		if($paramFnLabel[i] == ""){
			$paramFnLabel[i] = $.loadLocalInfo("inControl.loco(" + loconame + ")FnLabel[" + i + "]");
			if(typeof($paramFnLabel[i]) == "undefined") $paramFnLabel[i] = "";
		}
//Load only if 'string empty'
		if($paramFnImage[i] == ""){
			$paramFnImage[i] = $.loadLocalInfo("inControl.loco(" + loconame + ")FnImage[" + i + "]");
			if(typeof($paramFnImage[i]) == "undefined") $paramFnImage[i] = "";
		}
//Load only if 'string empty'
		if($paramFnImagePressed[i] == ""){
			$paramFnImagePressed[i] = $.loadLocalInfo("inControl.loco(" + loconame + ")FnImagePressed[" + i + "]");
			if(typeof($paramFnImagePressed[i]) == "undefined") $paramFnImagePressed[i] = "";
		}
//Load only if 'string empty'
		if($paramFnAddress[i] == ""){
			$paramFnAddress[i] = $loadAddressByLocoNameAndFn(loconame, i);
		}
//Load only if 'typeof() != boolean'
		if(typeof($paramFnShunt[i]) != "boolean"){
			s = $.loadLocalInfo("inControl.loco(" + loconame + ")FnShunt[" + i + "]");
			$paramFnShunt[i] = ((typeof(s) != "undefined") && s.length > 0);
		}
		if(auxShunt || typeof($paramFnAddress[i]) == "number") $paramFnShunt[i] = false;
		if($paramFnShunt[i]) auxShunt = true;
	}
}

//----------------------------------------- Load loco address by loco name
var $loadLocoAddressByLocoName = function(loconame){
	var n;
	n = $.loadLocalInfo("inControl.loco(" + loconame + ")LocoAddress");
	if(typeof(n) == "undefined") n = "3";
	n = parseInt($.trim(n), 10);
	if(isNaN(n)) n = "3";
	n = Math.abs(n) + "";
	n = parseInt(n.slice(-4), 10);
	return n;
}

//----------------------------------------- Load address by loco name and function
var $loadAddressByLocoNameAndFn = function(loconame, func){
	var n;
	n = $.loadLocalInfo("inControl.loco(" + loconame + ")FnAddress[" + func + "]");
	if(typeof(n) == "undefined") n = "";
	n = parseInt($.trim(n), 10);
	if(isNaN(n)) n = ""; else {
		n = Math.abs(n) + "";
		n = parseInt(n.slice(-4), 10);
	}
	return n;
}

//----------------------------------------- Remove parameters by loco name
var $removeParametersByLocoName = function(loconame){
	$.removeLocalInfo("inControl.loco(" + loconame + ")LocoAddress");
	$.removeLocalInfo("inControl.loco(" + loconame + ")LocoImage");
	$.removeLocalInfo("inControl.loco(" + loconame + ")LocoNoSpeedCtrl");
	for(var i = 0; i < 29; i++){
		$.removeLocalInfo("inControl.loco(" + loconame + ")FnActive[" + i + "]");
		$.removeLocalInfo("inControl.loco(" + loconame + ")FnToggle[" + i + "]");
		$.removeLocalInfo("inControl.loco(" + loconame + ")FnAutoUnpress[" + i + "]");
		$.removeLocalInfo("inControl.loco(" + loconame + ")FnLabel[" + i + "]");
		$.removeLocalInfo("inControl.loco(" + loconame + ")FnImage[" + i + "]");
		$.removeLocalInfo("inControl.loco(" + loconame + ")FnImagePressed[" + i + "]");
		$.removeLocalInfo("inControl.loco(" + loconame + ")FnAddress[" + i + "]");
		$.removeLocalInfo("inControl.loco(" + loconame + ")FnShunt[" + i + "]");
	}
}

//----------------------------------------- Buid layout
var $buildLayout = function(){
	var n;
	if ($insideMulti){									//If running inside Multi
		parent.$("iframe").each(function(iel, el){		//If running in an iframe, force Speed Control position
			if(el.contentWindow === window){
				$iframeId = el.id;
				if(typeof(parent.$("#" + $iframeId).attr("speedctrlright")) != "undefined") $paramSpeedCtrlRight = (parent.$("#" + $iframeId).attr("speedctrlright").length > 0);
			}
		});
	}
	if($changeBothSizes){
		n = ($viewportHeight * $SizeReferencePercent / 100);
		$SizeReference = Math.round((n > $SizeReferenceMin) ? n : $SizeReferenceMin);
	}
	if(!$inSettings){
		$buildPowerButtonLayout();
		$buildSettingsButtonLayout();
		$buildLocoInfoLayout();
		$buildLocoImageLayout();
		$buildSpeedLayout();
		$updatePower();
		$buildFunctionsLayout();
	} else {
		if(!$inSettingsLoco && !$inQueryString && !$inHelp) $buildSettingsLayout();
		if($inSettingsLoco) if($inChooseImage) $buildChooseImageLayout(); else $buildSettingsLocoLayout();
		if($inQueryString) $buildQueryStringLayout();
		if($inHelp) $buildHelpLayout();
	}
}

//----------------------------------------- Buid power button layout
var $buildPowerButtonLayout = function(){
	var $powerButton = $("#powerButton");
	var $imgPower;
	$powerButton.offset({top: 0, left: 0});
	if(!$paramPower){
		$powerButton.height(0);
		$powerButton.width(0);
		$powerButton.html("");
		return;
	}
	$powerButton.height($SizeReference);
	$powerButton.width($SizeReference);
	$powerButton.html("<img id='imgPower' alt='Power' title='Power' class='buttons' onclick='$Power()' />");
	$imgPower = $("#imgPower");
	$imgPower.height($powerButton.height() - $.calcTotalMBP($imgPower, false));
	$imgPower.width($powerButton.width() - $.calcTotalMBP($imgPower, true));
}

//----------------------------------------- Buid settings button layout
var $buildSettingsButtonLayout = function(){
	var $settingsButton = $("#settingsButton");
	var $imgSettings;
	$settingsButton.offset({top: 0, left: $viewportWidth - $SizeReference});
	$settingsButton.height($SizeReference);
	$settingsButton.width($SizeReference);
	$settingsButton.html("<img id='imgSettings' alt='Settings' title='Settings' class='buttons' onclick='$Settings()' />");
	$imgSettings = $("#imgSettings");
	$imgSettings.height($settingsButton.height() - $.calcTotalMBP($imgSettings, false));
	$imgSettings.width($settingsButton.width() - $.calcTotalMBP($imgSettings, true));
	$imgSettings.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imageSettings + "?MaxHeight=" + $imgSettings.height() + "&MaxWidth=" + $imgSettings.width()));
}

//----------------------------------------- Buid loco info layout
var $buildLocoInfoLayout = function(){
	var $powerButton = $("#powerButton");
	var $locoInfo = $("#locoInfo");
	var $selLocoList;
	var s;
	var f;
	$locoInfo.offset({top: 0, left: ($paramPower ? $powerButton.width() : 0)});
	$locoInfo.height($SizeReference);
	$locoInfo.width($viewportWidth - ($paramPower ? $powerButton.width() : 0) - $("#settingsButton").width());
	f = false;
	s = "<select id='selLocoList' size='1' onchange='$LocoListSelect()'>";
	for(var i = 0; i < $LocoNameList.length; i++){
		if($LocoNameList[i] == $paramLocoName) f = true;
		s+= "<option value='" + $LocoNameList[i] + "'" + (($LocoNameList[i] == $paramLocoName) ? " selected='selected'" : "") +">" + ("0000" + $loadLocoAddressByLocoName($LocoNameList[i])).slice(-4) + " - " + $("<div />").text($LocoNameList[i]).html() + "</option>";	// HTML encode
	}
	s+=	"</select>";
	$locoInfo.html(s);
	$selLocoList = $("#selLocoList");
	$selLocoList.width($locoInfo.width() - $.calcTotalMBP($selLocoList, true));
	$selLocoList.css("font-size", $SizeReference / 2 * $paramTextSizeRatio);
	$selLocoList.css("top", ($locoInfo.height() - $selLocoList.height() - $.calcTotalMBP($selLocoList, false)) / 2);
	$selLocoList.css("left", ($locoInfo.width() - $selLocoList.width() - $.calcTotalMBP($selLocoList, true)) / 2);
}

//----------------------------------------- Buid loco image layout
var $buildLocoImageLayout = function(){
	var $locoImage = $("#locoImage");
	var $imgLocoImage;
	$locoImage.offset({top: $SizeReference, left: ($paramSpeedCtrlRight ? 0 : ($portrait ? 0 : ($viewportWidth / 5)))});
	if($paramLocoImage == ""){
		$locoImage.width(0);
		$locoImage.html("");
		return;
	}
	$locoImage.width($viewportWidth - ($portrait ? 0 : ($viewportWidth / 5)));
	$locoImage.height(Math.floor($viewportHeight * $LocoImageLayoutHeightPercent / 100));
	$locoImage.html("<img id='imgLocoImage' />");
	$imgLocoImage = $("#imgLocoImage");
	$imgLocoImage.height($locoImage.height());
	$imgLocoImage.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($paramLocoImage + "?MaxHeight=" + $imgLocoImage.height() + "&MaxWidth=" + $imgLocoImage.width()));
}

//----------------------------------------- Buid speed control layout
var $buildSpeedLayout = function(){
	var $speed = $("#speed");
	var $speedCtrl;
	var $divRSF;
	var $divSpeed;
	var $imgPlusPlus;
	var $imgPlus;
	var $imgMinus;
	var $imgMinusMinus;
	var $imgRev;
	var $imgRevR;
	var $imgSTOP;
	var $imgFrw;
	var $imgFrwR;
	var top = $SizeReference + ((($paramLocoImage == "") || !$portrait) ? 0 : $("#locoImage").height());
	var height =  $viewportHeight - top;
	if($paramLocoNoSpeedCtrl){
		$speed.height(0);
		$speed.width(0);
		$speed.offset({top: 0, left: 0});
		$speed.html("");
		return;
	}
	$speed.height(height);
	$speed.width(Math.floor($viewportWidth / ($portrait ? 3 : 5)));
	$speed.offset({top: top, left: ($paramSpeedCtrlRight ? ($viewportWidth - $speed.width()) : 0)});
	$speed.html("<div id='speedCtrl'></div><div id='divRSF'></div>");
	var $speedCtrl = $("#speedCtrl");
	var $divRSF = $("#divRSF");
	$divRSF.html(
		"<img id='imgRev' alt='Reverse' title='Reverse' class='buttons' onclick='$Rev()' />" +
		"<img id='imgRevR' alt='Reverse' title='Reverse' class='buttons' onclick='$Rev()' />" +
		"<img id='imgSTOP' alt='STOP' title='Emergency STOP' class='buttons' onclick='$STOP()' />" +
		"<img id='imgFrw' alt='Forward' title='Forward' class='buttons' onclick='$Frw()' />" +
		"<img id='imgFrwR' alt='Forward' title='Forward' class='buttons' onclick='$Frw()' />"
	);
	$imgRev = $("#imgRev");
	$imgRevR = $("#imgRevR");
	$imgSTOP = $("#imgSTOP");
	$imgFrw = $("#imgFrw");
	$imgFrwR = $("#imgFrwR");
	$imgRev.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imageReverseEnabled + "?MaxHeight=" + $imgRev.height() + "&MaxWidth=" + $imgRev.width())).load(function(){$(this).width(Math.round($speed.width() / 3) - $.calcTotalMBP($(this), true)); $(this).height($(this).width())});
	$imgRevR.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imageReverseDisabled + "?MaxHeight=" + $imgRev.height() + "&MaxWidth=" + $imgRev.width())).load(function(){$(this).width(Math.round($speed.width() / 3) - $.calcTotalMBP($(this), true)); $(this).height($(this).width())});
	$imgSTOP.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes( $imageStop + "?MaxHeight=" + $imgSTOP.height() + "&MaxWidth=" + $imgSTOP.width())).load(function(){$(this).width(Math.round($speed.width() / 3) - $.calcTotalMBP($(this), true)); $(this).height($(this).width())});
	$imgFrw.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imageForwardEnabled + "?MaxHeight=" + $imgFrw.height() + "&MaxWidth=" + $imgFrw.width())).load(function(){$(this).width(Math.round($speed.width() / 3) - $.calcTotalMBP($(this), true)); $(this).height($(this).width())});
	$imgFrwR.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imageForwardDisabled + "?MaxHeight=" + $imgFrw.height() + "&MaxWidth=" + $imgFrw.width())).load(function(){$(this).width(Math.round($speed.width() / 3) - $.calcTotalMBP($(this), true)); $(this).height($(this).width())});
	height-= Math.round($speed.width() / 3) + $.calcTotalMBP($imgSTOP, false);
	if($paramSpeedCtrlButtons){													//Buttons Speed Control
		$speedCtrl.html(
			"<div id='divSpeed' title='Speed (0-100)'><label id='lblSpeed'></label> %</div>" +
			"<img id='imgPlusPlus' alt='++' title='Accelerate quickly' class='buttons' onclick='$PlusPlus()' />" +
			"<img id='imgPlus' alt='+' title='Accelerate' class='buttons' onclick='$Plus()' />" +
			"<img id='imgMinus' alt='-' title='Decelerate' class='buttons' onclick='$Minus()' />" +
			"<img id='imgMinusMinus' alt='--' title='Decelerate quickly' class='buttons' onclick='$MinusMinus()' />"
		);
		$divSpeed = $("#divSpeed");
		$imgPlusPlus = $("#imgPlusPlus");
		$imgPlus = $("#imgPlus");
		$imgMinus = $("#imgMinus");
		$imgMinusMinus = $("#imgMinusMinus");
		$divSpeed.css("font-size", $SizeReference / 2 * $paramTextSizeRatio);
		$divSpeed.width($speed.width());
		$divSpeed.height(Math.floor($SizeReference * $paramTextSizeRatio));
		height = Math.floor((height - $divSpeed.height()) / 4);
		$imgPlusPlus.css("top", $divSpeed.height());
		$imgPlusPlus.height(height - $.calcTotalMBP($imgPlusPlus, false));
		$imgPlusPlus.width($speed.width() - $.calcTotalMBP($imgPlusPlus, true));
		$imgPlusPlus.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imagePlusPlus + "?MaxHeight=" + $imgPlusPlus.height() + "&MaxWidth=" + $imgPlusPlus.width()));
		$imgPlus.css("top", $divSpeed.height() + height);
		$imgPlus.height(height - $.calcTotalMBP($imgPlus, false));
		$imgPlus.width($speed.width() - $.calcTotalMBP($imgPlus, true));
		$imgPlus.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imagePlus + "?MaxHeight=" + $imgPlus.height() + "&MaxWidth=" + $imgPlus.width()));
		$imgMinus.css("top", $divSpeed.height() + height * 2);
		$imgMinus.height(height - $.calcTotalMBP($imgMinus, false));
		$imgMinus.width($speed.width() - $.calcTotalMBP($imgMinus, true));
		$imgMinus.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imageMinus + "?MaxHeight=" + $imgMinus.height() + "&MaxWidth=" + $imgMinus.width()));
		$imgMinusMinus.css("top", $divSpeed.height() + height * 3);
		$imgMinusMinus.height(height - $.calcTotalMBP($imgMinusMinus, false));
		$imgMinusMinus.width($speed.width() - $.calcTotalMBP($imgMinusMinus, true));
		$imgMinusMinus.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imageMinusMinus + "?MaxHeight=" + $imgMinusMinus.height() + "&MaxWidth=" + $imgMinusMinus.width()));
		$speedCtrl.height($divSpeed.height() + height * 4);
	} else {																//Slider Speed Control
		$speedCtrl.width($speed.width());
		$speedCtrl.height(height);
		$VerticalSlider = new $verticalSlider($speedCtrl, 0, Math.floor(($speedCtrl.width() - $speedCtrl.width() / 1.5) / 2), $speedCtrl.height(), Math.floor($speedCtrl.width() / 1.5), $speedValue, $shunt, $LevelChange);
	}
	$divRSF.css("top", $speedCtrl.position().top + $speedCtrl.height());
	$updateSpeed();
}

//----------------------------------------- Buid function buttons layout
var $buildFunctionsLayout = function(){
	var $functions = $("#functions");
	var $divF;
	var $lblF;
	var top = $SizeReference + (($paramLocoImage == "") ? 0 : $("#locoImage").height());
	var left = ($paramSpeedCtrlRight ? 0 : $("#speed").width());
	var s = "";
	var s1 =  "<div id='divF";
	var s2 =  "' class='functions' onclick='$Fn(";
	var s3 =  ")'></div>";
	var text;
	var FnCount = 0;
	var t = 0;
	var l = 0;
	var tc;
	var lc;
	var h;
	var w;
	var hi;
	var wi;
	var h_mp;
	var w_mp;
	$functions.offset({top: top, left: left});
	$functions.height($viewportHeight - top);
	$functions.width($viewportWidth - $("#speed").width());
	for(var i = 0; i < 29; i++){
		if($paramFnActive[i]){
			s+= s1 + i + s2 + i + s3;
			FnCount++;
		}
	}
	$functions.html(s);
	if(FnCount == 0) return;
	h_mp = $.calcTotalMBP($(".functions"), false);
	w_mp = $.calcTotalMBP($(".functions"), true);
	w = Math.round(Math.sqrt($functions.width() / $functions.height() * FnCount));
	h = Math.round(FnCount / w);
	if(h * w < FnCount){
		if($functions.height() / $functions.width() > h / w) h++; else w++;
	}
	if((h * w - FnCount) >= ((h > w) ? w : h)){
		if($functions.height() / $functions.width() > h / w) h--; else w--;
	}
	hi = Math.floor($functions.height() / h);					//Each rectangle height (h: Number of rectangles per column)
	wi = Math.floor($functions.width() / w);					//Each rectangle width (w: Number of rectangles per row)
	tc = Math.floor(($functions.height() - (h * hi)) / 2);
	lc = Math.floor(($functions.width() - (w * wi)) / 2);
	for(var i = 0; i < 29; i++){
		if($paramFnActive[i]){
			$divF = $("#divF" + i);
			$divF.css("top", t + tc);
			$divF.css("left", l + lc);
			$divF.height(hi - h_mp);
			$divF.width(wi - w_mp);
			if(l + lc + wi * 2 <= $functions.width()) l+= wi; else {l = 0; t+= hi;}
			text = (($paramFnLabel[i] == "") ? "F" + i : $("<div />").text($paramFnLabel[i]).html());	// HTML encode
			if($paramFnImage[i] == ""){														// Label
				$divF.html("<label id='lblF" + i + "' class='lblFunctions' title='F" + i + "'>" + text + "</label>");
				$lblF = $("#lblF" + i);
				$lblF.height($divF.height() - $.calcTotalMBP($lblF, false));
				$lblF.width($divF.width() - $.calcTotalMBP($lblF, true));
				$lblF.css("font-size", $SizeReference * (($paramFnLabel[i] == "") ? 0.6 : 0.4) * $paramTextSizeRatio);
			} else {																		// Image
				$divF.html("<img id='imgF" + i + "Image' class='imgFunctions' alt='" + text + "' title='" + text + "' /><img id='imgF" + i + "ImagePressed' class='imgFunctions' alt='" + text + "' title='" + text + "' />");
				$("#imgF" + i + "Image").error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($paramFnImage[i] + "?MaxHeight=" + $divF.height() + "&MaxWidth=" + $divF.width())).load(function(){$resizeFunctionImage($(this))});
				if($paramFnToggle[i]) $("#imgF" + i + "ImagePressed").error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($paramFnImagePressed[i] + "?MaxHeight=" + $divF.height() + "&MaxWidth=" + $divF.width())).load(function(){$resizeFunctionImage($(this))});
				$("#imgF" + i + "Image").show();
				$("#imgF" + i + "ImagePressed").hide();
			}
			$updateFn(i);
		}
	}
}

//----------------------------------------- Buid settings layout
var $buildSettingsLayout = function(){
	var $settings = $("#settings");
	var $SettingsTitle;
	var $lblSettingsTitle;
	var $imgSettingsClose;
	var $divSettingsParams;
	var s;
	$settings.offset({top: 0, left: 0});
	$settings.width($viewportWidth);
	$settings.height($viewportHeight);
	$settings.html(
		"<div id='SettingsTitle'>" +
		"<label id='lblSettingsTitle'>Settings</label>" +
		"<img id='imgSettingsClose' alt='Close' title='Close' class='buttons' onclick='$SettingsClose()' />" +
		"</div>" +
		"<div id='divSettingsParams'></div>"
	);
	$SettingsTitle = $("#SettingsTitle");
	$lblSettingsTitle = $("#lblSettingsTitle");
	$imgSettingsClose = $("#imgSettingsClose");
	$divSettingsParams = $("#divSettingsParams");
	$imgSettingsClose.height($SizeReference - $.calcTotalMBP($imgSettingsClose, false));
	$imgSettingsClose.width($SizeReference - $.calcTotalMBP($imgSettingsClose, true));
	$imgSettingsClose.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imageClose + "?MaxHeight=" + $imgSettingsClose.height() + "&MaxWidth=" + $imgSettingsClose.width()));
	$SettingsTitle.height($SizeReference);
	$lblSettingsTitle.css("font-size", $SizeReference * 0.7 * $paramTextSizeRatio);
	$lblSettingsTitle.css("top", ($SettingsTitle.height() - $lblSettingsTitle.height()) / 2);
	$divSettingsParams.css("top", $SizeReference + $.calcTotalMBP($SettingsTitle, false) + parseInt($divSettingsParams.css("padding-top").replace("px",""), 10));
	$divSettingsParams.width($settings.width() - $.calcTotalMBP($divSettingsParams, true));
	$divSettingsParams.height($settings.height() -  $SettingsTitle.height() - $.calcTotalMBP($SettingsTitle, false) - $.calcTotalMBP($divSettingsParams, false));
	s = "<div class='pointer divSettingsParams' onclick='$SettingsTextSizeChange()'>" +
		"<label id='lblSettingsTextSize' class='pointer'>Text size</label>" +
		"<label id='chkSettingsTextSize' class='pointer settingsCheckboxes settingsOptions'>";
	if($paramTextSizeRatio < 1) s+= "[S]"; else if($paramTextSizeRatio == 1) s+= "[N]"; else s+= "[L]";
	s+= "</label>" +
		"</div>" +
		"<div class='pointer divSettingsParams' onclick='$SettingsPowerChange()'>" +
		"<label id='lblSettingsPower' class='pointer'>Power button</label>" +
		"<label id='chkSettingsPower' class='pointer settingsCheckboxes settingsOptions'>" + ($paramPower ? "[x]" : "[&nbsp;]") + "</label>" +
		"</div>" +
		"<div class='pointer divSettingsParams' onclick='$SettingsSpeedCtrlRightChange()'>" +
		"<label id='lblSettingsSpeedCtrlRight' class='pointer'>Speed Control on right side</label>" +
		"<label id='chkSettingsSpeedCtrlRight' class='pointer settingsCheckboxes settingsOptions'>" + ($paramSpeedCtrlRight ? "[x]" : "[&nbsp;]") + "</label>" +
		"</div>" +
		"<div class='pointer divSettingsParams' onclick='$SettingsSpeedCtrlButtonsChange()'>" +
		"<label id='lblSettingsSpeedCtrlButtons' class='pointer'>Speed Control with buttons</label>" +
		"<label id='chkSettingsSpeedCtrlButtons' class='pointer settingsCheckboxes settingsOptions'>" + ($paramSpeedCtrlButtons ? "[x]" : "[&nbsp;]") + "</label>" +
		"</div>" +
		"<div class='divSettingsParamsSpace'></div>" +
		"<div class='divSettingsParamsClick'>" +
		"<label id='lblLoco' class='pointer' onclick='$SettingsLoco()'>Loco/Device parameters ...</label>" +
		"</div>" +
		"<div class='divSettingsParamsSpace'></div>" +
		"<div class='divSettingsParamsClick'>" +
		"<label id='lblQueryString' class='pointer' onclick='$QueryString()'>Query String help ...</label>" +
		"</div>" +
		"<div class='divSettingsParamsSpace'></div>" +
		"<div class='divSettingsParamsClick'>" +
		"<label id='lblHelp' class='pointer' onclick='$Help()'>More help ...</label>" +
		"</div>";
	$divSettingsParams.html("<div>" + s + "</div>");
	$(".divSettingsParams").css("font-size", $SizeReference * 0.4 * $paramTextSizeRatio);
	$(".divSettingsParamsClick").css("font-size", $SizeReference * 0.6 * $paramTextSizeRatio);
	$divSettingsParams.jScrollPane();
}

//----------------------------------------- Buid settings loco layout
var $buildSettingsLocoLayout = function(){
	var $settingsLoco = $("#settingsLoco");
	var $SettingsLocoTitle;
	var $lblSettingsLocoTitle;
	var $imgSettingsLocoTrash;
	var $imgSettingsLocoClose;
	var $divSettingsLocoParams;
	var s;
	$settingsLoco.offset({top: 0, left: 0});
	$settingsLoco.width($viewportWidth);
	$settingsLoco.height($viewportHeight);
	$settingsLoco.html(
		"<div id='SettingsLocoTitle'>" +
		"<label id='lblSettingsLocoTitle'>Loco/Device</label>" +
		"<img id='imgSettingsLocoTrash' alt='Remove' title='Remove' class='buttons' onclick='$SettingsLocoRemove()' />" +
		"<img id='imgSettingsLocoClose' alt='Close' title='Close' class='buttons' onclick='$SettingsLocoClose()' />" +
		"</div>" +
		"<div id='divSettingsLocoParams'></div>"
	);
	$SettingsLocoTitle = $("#SettingsLocoTitle");
	$lblSettingsLocoTitle = $("#lblSettingsLocoTitle");
	$imgSettingsLocoTrash = $("#imgSettingsLocoTrash");
	$imgSettingsLocoClose = $("#imgSettingsLocoClose");
	$divSettingsLocoParams = $("#divSettingsLocoParams");
	$imgSettingsLocoClose.height($SizeReference - $.calcTotalMBP($imgSettingsLocoClose, false));
	$imgSettingsLocoClose.width($SizeReference - $.calcTotalMBP($imgSettingsLocoClose, true));
	$imgSettingsLocoClose.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imageClose + "?MaxHeight=" + $imgSettingsLocoClose.height() + "&MaxWidth=" + $imgSettingsLocoClose.width()));
	$SettingsLocoTitle.height($SizeReference);
	$lblSettingsLocoTitle.css("font-size", $SizeReference * 0.7 * $paramTextSizeRatio);
	$lblSettingsLocoTitle.css("top", ($SettingsLocoTitle.height() - $lblSettingsLocoTitle.height()) / 2);
	$imgSettingsLocoTrash.css("left", $lblSettingsLocoTitle.width());
	$imgSettingsLocoTrash.height($SizeReference - $.calcTotalMBP($imgSettingsLocoTrash, false));
	$imgSettingsLocoTrash.width($SizeReference - $.calcTotalMBP($imgSettingsLocoTrash, true));
	$divSettingsLocoParams.css("top", $SizeReference + $.calcTotalMBP($SettingsLocoTitle, false) + parseInt($divSettingsLocoParams.css("padding-top").replace("px",""), 10));
	$divSettingsLocoParams.width($settingsLoco.width() - $.calcTotalMBP($divSettingsLocoParams, true));
	$divSettingsLocoParams.height($settingsLoco.height() -  $SettingsLocoTitle.height() - $.calcTotalMBP($SettingsLocoTitle, false) - $.calcTotalMBP($divSettingsLocoParams, false));
	s = "<div class='pointer divSettingsParams' onclick='$SettingsLocoNameChange()'>" +
		"<label id='lblSettingsLocoName' class='pointer'>Name</label>" +
		"<label id='txtSettingsLocoName' class='pointer settingsInputText settingsOptions'>" + $("<div />").text($paramLocoName).html() + "</label>" +	// HTML encode
		"</div>" +
		"<div class='pointer divSettingsParams' onclick='$SettingsLocoAddressChange()'>" +
		"<label id='lblSettingsLocoAddress' class='pointer'>Address</label>" +
		"<label id='txtSettingsLocoAddress' class='pointer settingsInputText settingsOptions'>" + $paramLocoAddress + "</label>" +
		"</div>" +
		"<div class='pointer divSettingsParams' onclick='$SettingsLocoImageChange()'>" +
		"<label id='lblSettingsLocoImage' class='pointer'>Image</label>" +
		"<label id='txtSettingsLocoImage' class='pointer settingsInputText settingsOptions'>" + $("<div />").text($paramLocoImage).html() + "</label>" +	// HTML encode
		"</div>" +
		"<div class='pointer divSettingsParams' onclick='$SettingsLocoNoSpeedCtrlClick()'>" +
		"<label id='lblSettingsLocoNoSpeedCtrl' class='pointer'>No speed control</label>" +
		"<label id='chkSettingsLocoNoSpeedCtrl' class='pointer settingsCheckboxes settingsOptions'>" + ($paramLocoNoSpeedCtrl ? "[x]" : "[&nbsp;]") + "</label>" +
		"</div>" +
		"<div class='divSettingsParams'>" +
		"<label id='lblSettingsLocoFunctions'>Function to manage</label>" +
		"<span class='settingsOptions'>" +
		"<label id='lblSettingsLocoFunctionsMinus' onclick='$SettingsLocoFunctionClick(-1)'>&nbsp;-&nbsp;</label>" +
		"<label id='lblSettingsLocoFunctionsValue'>" + $SettingsLocoFunctionSelected + "</label>" +
		"<label id='lblSettingsLocoFunctionsPlus' onclick='$SettingsLocoFunctionClick(+1)'>&nbsp;+&nbsp;</label>" +
		"</span>" +
		"</div>" +
		"<div class='pointer divSettingsParams' onclick='$SettingsLocoFunctionActiveChange()'>" +
		"<label id='lblSettingsLocoFunctionActive' class='pointer settingsLabelsFi'>- Active</label>" +
		"<label id='chkSettingsLocoFunctionActive' class='pointer settingsCheckboxes settingsOptions'>" + ($paramFnActive[$SettingsLocoFunctionSelected] ? "[x]" : "[&nbsp;]") + "</label>" +
		"</div>" +
		"<div class='pointer divSettingsParams' onclick='$SettingsLocoFunctionToggleChange()'>" +
		"<label id='lblSettingsLocoFunctionToggle' class='pointer settingsLabelsFi'>- Toggle button (on/off)</label>" +
		"<label id='chkSettingsLocoFunctionToggle' class='pointer settingsCheckboxes settingsOptions'>" + ($paramFnToggle[$SettingsLocoFunctionSelected] ? "[x]" : "[&nbsp;]") + "</label>" +
		"</div>" +
		"<div class='pointer divSettingsParams' onclick='$SettingsLocoFunctionAutoUnpressChange()'>" +
		"<label id='lblSettingsLocoFunctionAutoUnpress' class='pointer settingsLabelsFi'>- Auto unpress (turn off)</label>" +
		"<label id='chkSettingsLocoFunctionAutoUnpress' class='pointer settingsCheckboxes settingsOptions'>" + ($paramFnAutoUnpress[$SettingsLocoFunctionSelected] ? "[x]" : "[&nbsp;]") + "</label>" +
		"</div>" +
		"<div class='pointer divSettingsParams' onclick='$SettingsLocoFunctionLabelChange()'>" +
		"<label id='lblSettingsLocoFunctionLabel' class='pointer settingsLabelsFi'>- Button label</label>" +
		"<label id='txtSettingsLocoFunctionLabel' class='pointer settingsInputText settingsOptions'>" + $("<div />").text($paramFnLabel[$SettingsLocoFunctionSelected]).html() + "</label>" +	// HTML encode
		"</div>" +
		"<div class='pointer divSettingsParams' onclick='$SettingsLocoFunctionImageChange()'>" +
		"<label id='lblSettingsLocoFunctionImage' class='pointer settingsLabelsFi'>- Button image</label>" +
		"<label id='txtSettingsLocoFunctionImage' class='pointer settingsInputText settingsOptions'>" + $("<div />").text($paramFnImage[$SettingsLocoFunctionSelected]).html() + "</label>" +	// HTML encode
		"</div>" +
		"<div class='pointer divSettingsParams' onclick='$SettingsLocoFunctionImagePressedChange()'>" +
		"<label id='lblSettingsLocoFunctionImagePressed' class='pointer settingsLabelsFi'>- Button pressed image</label>" +
		"<label id='txtSettingsLocoFunctionImagePressed' class='pointer settingsInputText settingsOptions'>" + $("<div />").text($paramFnImagePressed[$SettingsLocoFunctionSelected]).html() + "</label>" +	// HTML encode
		"</div>" +
		"<div class='pointer divSettingsParams' onclick='$SettingsLocoFunctionAddressChange()'>" +
		"<label id='lblSettingsLocoFunctionAddress' class='pointer settingsLabelsFi'>- Alternative device address</label>" +
		"<label id='txtSettingsLocoFunctionAddress' class='pointer settingsInputText settingsOptions'>" + $paramFnAddress[$SettingsLocoFunctionSelected] + "</label>" +
		"</div>" +
		"<div class='pointer divSettingsParams' onclick='$SettingsLocoFunctionShuntChange()'>" +
		"<label id='lblSettingsLocoFunctionShunt' class='pointer settingsLabelsFi'>- Shunt</label>" +
		"<label id='chkSettingsLocoFunctionShunt' class='pointer settingsCheckboxes settingsOptions'>" + ($paramFnShunt[$SettingsLocoFunctionSelected] ? "[x]" : "[&nbsp;]") + "</label>" +
		"</div>";
	$divSettingsLocoParams.html("<div>" + s + "</div>");
	$imgSettingsLocoTrash.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imageTrash + "?MaxHeight=" + $imgSettingsLocoTrash.height() + "&MaxWidth=" + $imgSettingsLocoTrash.width()));
	$(".divSettingsParams").css("font-size", $SizeReference * 0.4 * $paramTextSizeRatio);
	$divSettingsLocoParams.jScrollPane();
	$SettingsLocoFunctionClick(0);
}

//----------------------------------------- Buid query string help layout
var $buildQueryStringLayout = function(){
	var $queryString = $("#queryString");
	var $QueryStringTitle;
	var $lblQueryStringTitle;
	var $imgQueryStringClose;
	var $divQueryStringText;
	$queryString.offset({top: 0, left: 0});
	$queryString.width($viewportWidth);
	$queryString.height($viewportHeight);
	$queryString.html(
		"<div id='QueryStringTitle'>" +
		"<label id='lblQueryStringTitle'>Query String</label>" +
		"<img id='imgQueryStringClose' alt='Close' title='Close' class='buttons' onclick='$QueryStringClose()' />" +
		"</div>" +
		"<div id='divQueryStringText'></div>"
	);
	$QueryStringTitle = $("#QueryStringTitle");
	$lblQueryStringTitle = $("#lblQueryStringTitle");
	$imgQueryStringClose = $("#imgQueryStringClose");
	$divQueryStringText = $("#divQueryStringText");
	$imgQueryStringClose.height($SizeReference - $.calcTotalMBP($imgQueryStringClose, false));
	$imgQueryStringClose.width($SizeReference - $.calcTotalMBP($imgQueryStringClose, true));
	$imgQueryStringClose.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imageClose + "?MaxHeight=" + $imgQueryStringClose.height() + "&MaxWidth=" + $imgQueryStringClose.width()));
	$QueryStringTitle.height($SizeReference);
	$lblQueryStringTitle.css("font-size", $SizeReference * 0.7 * $paramTextSizeRatio);
	$lblQueryStringTitle.css("top", ($QueryStringTitle.height() - $lblQueryStringTitle.height()) / 2);
	$divQueryStringText.css("top", $SizeReference + $.calcTotalMBP($QueryStringTitle, false) + parseInt($divQueryStringText.css("padding-top").replace("px",""), 10));
	$divQueryStringText.width($queryString.width() - $.calcTotalMBP($divQueryStringText, true));
	$divQueryStringText.height($queryString.height() -  $QueryStringTitle.height() - $.calcTotalMBP($QueryStringTitle, false) - $.calcTotalMBP($divQueryStringText, false));
	$divQueryStringText.html("<div>" + $QueryStringText + "</div>");
	$divQueryStringText.css("font-size", $SizeReference * 0.4 * $paramTextSizeRatio);
	$divQueryStringText.jScrollPane();
}

//----------------------------------------- Buid help layout
var $buildHelpLayout = function(){
	var $help = $("#help");
	var $HelpTitle;
	var $lblHelpTitle;
	var $imgHelpClose;
	var $divHelpText;
	$help.offset({top: 0, left: 0});
	$help.width($viewportWidth);
	$help.height($viewportHeight);
	$help.html(
		"<div id='HelpTitle'>" +
		"<label id='lblHelpTitle'>Help</label>" +
		"<img id='imgHelpClose' alt='Close' title='Close' class='buttons' onclick='$HelpClose()' />" +
		"</div>" +
		"<div id='divHelpText'></div>"
	);
	$HelpTitle = $("#HelpTitle");
	$lblHelpTitle = $("#lblHelpTitle");
	$imgHelpClose = $("#imgHelpClose");
	$divHelpText = $("#divHelpText");
	$imgHelpClose.height($SizeReference - $.calcTotalMBP($imgHelpClose, false));
	$imgHelpClose.width($SizeReference - $.calcTotalMBP($imgHelpClose, true));
	$imgHelpClose.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imageClose + "?MaxHeight=" + $imgHelpClose.height() + "&MaxWidth=" + $imgHelpClose.width()));
	$HelpTitle.height($SizeReference);
	$lblHelpTitle.css("font-size", $SizeReference * 0.7 * $paramTextSizeRatio);
	$lblHelpTitle.css("top", ($HelpTitle.height() - $lblHelpTitle.height()) / 2);
	$divHelpText.css("top", $SizeReference + $.calcTotalMBP($HelpTitle, false) + parseInt($divHelpText.css("padding-top").replace("px",""), 10));
	$divHelpText.width($help.width() - $.calcTotalMBP($divHelpText, true));
	$divHelpText.height($help.height() -  $HelpTitle.height() - $.calcTotalMBP($HelpTitle, false) - $.calcTotalMBP($divHelpText, false));
	$divHelpText.html("<div>" + $HelpText + "</div>");
	$divHelpText.css("font-size", $SizeReference * 0.4 * $paramTextSizeRatio);
	$divHelpText.jScrollPane();
}

//----------------------------------------- LocoList select
var $LocoListSelect = function(){
	var o = $("#selLocoList");
	$paramLocoName = o.val();
	$cleanParametersByLocoName($paramLocoName);
	$loadParametersByLocoName($paramLocoName);
	$xmlioSendGetLoco();
	$buildLocoImageLayout();
	$buildSpeedLayout();
	$updatePower();
	$buildFunctionsLayout();
}

//----------------------------------------- Click Settings
var $Settings = function(){
	$inSettings = true;
	$("#throttleMain").hide();
	$("#settings").show();
	$buildLayout();
}

//----------------------------------------- Settings close
var $SettingsClose = function(){
	$inSettings = false;
	$("#throttleMain").show();
	$("#settings").hide();
	$buildLayout();
}

//----------------------------------------- Click Settings loco
var $SettingsLoco = function(){
	$inSettingsLoco = true;
	$("#settings").hide();
	$("#settingsLoco").show();
	$buildLayout();
}

//----------------------------------------- Choose Image close
var $ChooseImageClose = function(){
	$inChooseImage = false;
	$("#settingsLoco").show();
	$("#chooseImage").remove();
	$buildLayout();
}

//----------------------------------------- Settings Loco close
var $SettingsLocoClose = function(){
	$inSettingsLoco = false;
	$("#settings").show();
	$("#settingsLoco").hide();
	$buildLayout();
}

//----------------------------------------- Click Query String
var $QueryString = function(){
	$inQueryString = true;
	$("#settings").hide();
	$("#queryString").show();
	$buildLayout();
}

//----------------------------------------- Query String close
var $QueryStringClose = function(){
	$inQueryString = false;
	$("#settings").show();
	$("#queryString").hide();
	$buildLayout();
}

//----------------------------------------- Click Help
var $Help = function(){
	$inHelp = true;
	$("#settings").hide();
	$("#help").show();
	$buildLayout();
}

//----------------------------------------- Help close
var $HelpClose = function(){
	$inHelp = false;
	$("#settings").show();
	$("#help").hide();
	$buildLayout();
}

//----------------------------------------- Choose image
var $ChooseImage = function(promptText, defaultValue){
	var s = "";
	if(confirm("Select from a list ?\n(Press [Cancel] to write the file location URL)")){
		$buildChooseImageLayout();
		return null;
	} else {
		s = prompt(promptText, defaultValue);
		if(!s) return null;
		s = $.trim(s);
		$paramLocoImage = s;
		return s;
	}
}

//----------------------------------------- Build choose image layout
var $buildChooseImageLayout = function(){
	var s = "";
	var $chooseImage;
	var $ChooseImageTitle;
	var $lblChooseImageTitle;
	var $imgChooseImageClose;
	var $divChooseImageList;
	$inChooseImage = true;
	$("#chooseImage").remove();
	$("body").append("<div id='chooseImage'></div>");
	$chooseImage = $("#chooseImage");
	$chooseImage.offset({top: 0, left: 0});
	$chooseImage.width($viewportWidth);
	$chooseImage.height($viewportHeight);
	s+= "<div id='ChooseImageTitle'>";
	s+= "<label id='lblChooseImageTitle'>Images list</label>";
	s+= "<img id='imgChooseImageClose' alt='Close' title='Close' class='buttons' onclick='$ChooseImageClose()' />";
	s+= "</div>";
	s+= "<div id='divChooseImageList'><div>";
	for(var i = 0; i < $imagesFileList.length; i++){
		if(i > 0) s+= " &nbsp;&nbsp; ";
		s+= "<label id='lblImageItem' class='pointer' onclick='$ChooseImageClick(\"" + $imagesFileList[i] +"\")'>" + $("<div />").text($imagesFileList[i]).html() + "</label>";	// HTML encode
	}
	s+= "</div></div>";
	$chooseImage.html(s);
	$ChooseImageTitle = $("#ChooseImageTitle");
	$lblChooseImageTitle = $("#lblChooseImageTitle");
	$imgChooseImageClose = $("#imgChooseImageClose");
	$divChooseImageList = $("#divChooseImageList");
	$imgChooseImageClose.height($SizeReference - $.calcTotalMBP($imgChooseImageClose, false));
	$imgChooseImageClose.width($SizeReference - $.calcTotalMBP($imgChooseImageClose, true));
	$imgChooseImageClose.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imageClose + "?MaxHeight=" + $imgChooseImageClose.height() + "&MaxWidth=" + $imgChooseImageClose.width()));
	$ChooseImageTitle.height($SizeReference);
	$lblChooseImageTitle.css("font-size", $SizeReference * 0.7 * $paramTextSizeRatio);
	$lblChooseImageTitle.css("top", ($ChooseImageTitle.height() - $lblChooseImageTitle.height()) / 2);
	$divChooseImageList.css("top", $SizeReference + $.calcTotalMBP($ChooseImageTitle, false) + parseInt($divChooseImageList.css("padding-top").replace("px",""), 10));
	$divChooseImageList.width($chooseImage.width() - $.calcTotalMBP($divChooseImageList, true));
	$divChooseImageList.height($chooseImage.height() -  $ChooseImageTitle.height() - $.calcTotalMBP($ChooseImageTitle, false) - $.calcTotalMBP($divChooseImageList, false));
	$divChooseImageList.css("font-size", $SizeReference * 0.4 * $paramTextSizeRatio);
	$divChooseImageList.jScrollPane();
	$("#settingsLoco").hide();
}

//----------------------------------------- Choose image click
var $ChooseImageClick = function(filename){
	var s = $imagesPath + filename;
	switch($selectAux) {
		case 0: {		//Loco image
			$paramLocoImage = s;
			$("#txtSettingsLocoImage").text($paramLocoImage);
			$.saveLocalInfo("inControl.loco(" + $paramLocoName + ")LocoImage", $paramLocoImage);
			break;
		}
		case 1: {		//Function image
			$paramFnImage[$SettingsLocoFunctionSelected] = s;
			$("#txtSettingsLocoFunctionImage").text($paramFnImage[$SettingsLocoFunctionSelected]);
			$.saveLocalInfo("inControl.loco(" + $paramLocoName + ")FnImage[" + $SettingsLocoFunctionSelected + "]", $paramFnImage[$SettingsLocoFunctionSelected]);
			break;
		}
		case 2: {		//Function Pressed image
			$paramFnImagePressed[$SettingsLocoFunctionSelected] = s;
			$("#txtSettingsLocoFunctionImagePressed").text($paramFnImagePressed[$SettingsLocoFunctionSelected]);
			$.saveLocalInfo("inControl.loco(" + $paramLocoName + ")FnImagePressed[" + $SettingsLocoFunctionSelected + "]", $paramFnImagePressed[$SettingsLocoFunctionSelected]);
			break;
		}
	}
	$inChooseImage = false;
	$("#settingsLoco").show();
	$("#chooseImage").remove();
	$buildLayout();
}

//----------------------------------------- Settings Text size change
var $SettingsTextSizeChange = function(){
	var s;
	if($paramTextSizeRatio < $TextSizeRatioN) $paramTextSizeRatio = $TextSizeRatioN; else if($paramTextSizeRatio == $TextSizeRatioN) $paramTextSizeRatio = $TextSizeRatioL; else $paramTextSizeRatio = $TextSizeRatioS;
	if($paramTextSizeRatio < $TextSizeRatioN) s = "[S]"; else if($paramTextSizeRatio == $TextSizeRatioN) s = "[N]"; else s = "[L]";
	$("#chkSettingsTextSize").html(s);
	$.saveLocalInfo("inControl.TextSizeRatio", $paramTextSizeRatio);
	$buildLayout();
}

//----------------------------------------- Settings Power change
var $SettingsPowerChange = function(){
	$paramPower = !$paramPower;
	$("#chkSettingsPower").html($paramPower ? "[x]" : "[&nbsp;]");
	$.saveLocalInfo("inControl.Power", ($paramPower ? "true" : ""));
}

//----------------------------------------- Settings Speed Control on right change
var $SettingsSpeedCtrlRightChange = function(){
	$paramSpeedCtrlRight = !$paramSpeedCtrlRight;
	$("#chkSettingsSpeedCtrlRight").html($paramSpeedCtrlRight ? "[x]" : "[&nbsp;]");
	$.saveLocalInfo("inControl.SpeedCtrlRight", ($paramSpeedCtrlRight ? "true" : ""));
}

//----------------------------------------- Settings Speed Control with buttons
var $SettingsSpeedCtrlButtonsChange = function(){
	$paramSpeedCtrlButtons = !$paramSpeedCtrlButtons;
	$("#chkSettingsSpeedCtrlButtons").html($paramSpeedCtrlButtons ? "[x]" : "[&nbsp;]");
	$.saveLocalInfo("inControl.SpeedCtrlButtons", ($paramSpeedCtrlButtons ? "true" : ""));
}

//----------------------------------------- Settings Loco remove
var $SettingsLocoRemove = function(){
	if($LocoNameList.length == 1){
		if(confirm("Cannot remove the last Loco/Device.\nErase all parameters from this Loco/Device ?")){
			$cleanParametersByLocoName($paramLocoName);
			$paramLocoAddress = 3;
			$saveParametersByLocoName($paramLocoName);
			$xmlioSendGetLoco();
			$SettingsLocoClose();
		}
	} else {
		if(confirm("Remove this Loco/Device from the stored list ?")){
			for(var i = 0; i < $LocoNameList.length; i++){
				if($LocoNameList[i] == $paramLocoName){
					$LocoNameList[i] = "";
					break;
				}
			}
			$LocoNameList.sort();
			$LocoNameList.shift();
			$saveLocoNameList();
			$.removeLocalInfo("inControl.locoList[" + $LocoNameList.length + "]");
			$removeParametersByLocoName($paramLocoName);
			$paramLocoName = $LocoNameList[0];
			$("#txtSettingsLocoName").text($paramLocoName);
			$cleanParametersByLocoName($paramLocoName);
			$loadParametersByLocoName($paramLocoName);
			$xmlioSendGetLoco();
			$SettingsLocoClose();
		}
	}
}

//----------------------------------------- Settings LocoName change
var $SettingsLocoNameChange = function(){
	var ch = true;
	var s = prompt("New name (duplicate Loco/Device):", " " + $("#txtSettingsLocoName").text());
	if(!s) return;
	s = $.trim(s);
	for(var i = 0; i < $LocoNameList.length; i++){
		if($LocoNameList[i] == s){
			alert("This Loco/Device already exists.");
			ch = false;
			break;
		}
	}
	if(!ch) return;
	$LocoNameList.push(s);
	$LocoNameList.sort();
	$saveLocoNameList();
	$paramLocoName = s;
	$("#txtSettingsLocoName").text($paramLocoName);
	$saveParametersByLocoName($paramLocoName);
}

//----------------------------------------- Settings LocoAddress change
var $SettingsLocoAddressChange = function(){
	var ok;
	var s = prompt("New address:", $("#txtSettingsLocoAddress").text());
	if(!s) return;
	s = $.trim(s);
	if(!s) return;
	s = parseInt(s, 10);
	ok = !isNaN(s);
	if(ok){
		s = Math.abs(s);
		ok = (s < 10000);
	}
	if(!ok){
		alert("Invalid Loco/Device address.");
		return;
	}
	$paramLocoAddress = s;
	$("#txtSettingsLocoAddress").text($paramLocoAddress);
	$.saveLocalInfo("inControl.loco(" + $paramLocoName + ")LocoAddress", $paramLocoAddress);
	$xmlioSendGetDevice($paramLocoAddress);
}

//----------------------------------------- Settings LocoImage change
var $SettingsLocoImageChange = function(){
	$selectAux = 0;
	var s = $ChooseImage("New image URL (space to remove):", " " + $("#txtSettingsLocoImage").text());
	if(null == s) return;
	$paramLocoImage = s;
	$("#txtSettingsLocoImage").text($paramLocoImage);
	$.saveLocalInfo("inControl.loco(" + $paramLocoName + ")LocoImage", $paramLocoImage);
}

//----------------------------------------- Settings No Speed Control change
var $SettingsLocoNoSpeedCtrlClick = function(){
	$paramLocoNoSpeedCtrl = !$paramLocoNoSpeedCtrl;
	$("#chkSettingsLocoNoSpeedCtrl").html($paramLocoNoSpeedCtrl ? "[x]" : "[&nbsp;]");
	$.saveLocalInfo("inControl.loco(" + $paramLocoName + ")LocoNoSpeedCtrl", ($paramLocoNoSpeedCtrl ? "true" : ""));
}

//----------------------------------------- Settings LocoFunction change
var $SettingsLocoFunctionClick = function(value){
	var o = $("#lblSettingsLocoFunctionsValue");
	var v = parseInt(o.text(), 10) + value;
	if(v < 0) v = 0;
	if(v > 28) v = 28;
	o.text(v);
	$SettingsLocoFunctionSelected = v;
	$("#chkSettingsLocoFunctionActive").html($paramFnActive[$SettingsLocoFunctionSelected] ? "[x]" : "[&nbsp;]");
	$("#chkSettingsLocoFunctionToggle").html($paramFnToggle[$SettingsLocoFunctionSelected] ? "[x]" : "[&nbsp;]");
	$("#chkSettingsLocoFunctionAutoUnpress").html($paramFnAutoUnpress[$SettingsLocoFunctionSelected] ? "[x]" : "[&nbsp;]");
	$("#txtSettingsLocoFunctionLabel").text($paramFnLabel[$SettingsLocoFunctionSelected]);
	$("#txtSettingsLocoFunctionImage").text($paramFnImage[$SettingsLocoFunctionSelected]);
	$("#txtSettingsLocoFunctionImagePressed").text($paramFnImagePressed[$SettingsLocoFunctionSelected]);
	$("#txtSettingsLocoFunctionAddress").text($paramFnAddress[$SettingsLocoFunctionSelected]);
	$("#chkSettingsLocoFunctionShunt").html($paramFnShunt[$SettingsLocoFunctionSelected] ? "[x]" : "[&nbsp;]");
}

//----------------------------------------- Settings LocoFunctionActive change
var $SettingsLocoFunctionActiveChange = function(){
	$paramFnActive[$SettingsLocoFunctionSelected] = !$paramFnActive[$SettingsLocoFunctionSelected];
	$("#chkSettingsLocoFunctionActive").html($paramFnActive[$SettingsLocoFunctionSelected] ? "[x]" : "[&nbsp;]");
	$.saveLocalInfo("inControl.loco(" + $paramLocoName + ")FnActive[" + $SettingsLocoFunctionSelected + "]", ($paramFnActive[$SettingsLocoFunctionSelected] ? "true" : ""));
	if($paramFnActive[$SettingsLocoFunctionSelected]) if(typeof($paramFnAddress[$SettingsLocoFunctionSelected]) == "number") $xmlioSendGetDevice($paramFnAddress[$SettingsLocoFunctionSelected]); else $xmlioSendGetDevice($paramLocoAddress);
}

//----------------------------------------- Settings LocoFunctionToggle change
var $SettingsLocoFunctionToggleChange = function(){
	$paramFnToggle[$SettingsLocoFunctionSelected] = !$paramFnToggle[$SettingsLocoFunctionSelected];
	$("#chkSettingsLocoFunctionToggle").html($paramFnToggle[$SettingsLocoFunctionSelected] ? "[x]" : "[&nbsp;]");
	$.saveLocalInfo("inControl.loco(" + $paramLocoName + ")FnToggle[" + $SettingsLocoFunctionSelected + "]", ($paramFnToggle[$SettingsLocoFunctionSelected] ? "true" : ""));
}

//----------------------------------------- Settings LocoFunctionAutoUnpress change
var $SettingsLocoFunctionAutoUnpressChange = function(){
	$paramFnAutoUnpress[$SettingsLocoFunctionSelected] = !$paramFnAutoUnpress[$SettingsLocoFunctionSelected];
	$("#chkSettingsLocoFunctionAutoUnpress").html($paramFnAutoUnpress[$SettingsLocoFunctionSelected] ? "[x]" : "[&nbsp;]");
	$.saveLocalInfo("inControl.loco(" + $paramLocoName + ")FnAutoUnpress[" + $SettingsLocoFunctionSelected + "]", ($paramFnAutoUnpress[$SettingsLocoFunctionSelected] ? "true" : ""));
}

//----------------------------------------- Settings LocoFunctionLabel change
var $SettingsLocoFunctionLabelChange = function(){
	var s = prompt("New F" + $SettingsLocoFunctionSelected + " button label (space to remove):", " " + $("#txtSettingsLocoFunctionLabel").text());
	if(!s) return;
	s = $.trim(s);
	$paramFnLabel[$SettingsLocoFunctionSelected] = s;
	$("#txtSettingsLocoFunctionLabel").text($paramFnLabel[$SettingsLocoFunctionSelected]);
	$.saveLocalInfo("inControl.loco(" + $paramLocoName + ")FnLabel[" + $SettingsLocoFunctionSelected + "]", $paramFnLabel[$SettingsLocoFunctionSelected]);
}

//----------------------------------------- Settings LocoFunctionImage change
var $SettingsLocoFunctionImageChange = function(){
	$selectAux = 1;
	var s = $ChooseImage("New F" + $SettingsLocoFunctionSelected + " button image URL (space to remove):", " " + $("#txtSettingsLocoFunctionImage").text());
	if(null == s) return;
	$paramFnImage[$SettingsLocoFunctionSelected] = s;
	$("#txtSettingsLocoFunctionImage").text($paramFnImage[$SettingsLocoFunctionSelected]);
	$.saveLocalInfo("inControl.loco(" + $paramLocoName + ")FnImage[" + $SettingsLocoFunctionSelected + "]", $paramFnImage[$SettingsLocoFunctionSelected]);
}

//----------------------------------------- Settings LocoFunctionImagePressed change
var $SettingsLocoFunctionImagePressedChange = function(){
	$selectAux = 2;
	var s = $ChooseImage("New F" + $SettingsLocoFunctionSelected + " button pressed image URL (space to remove):", " " + $("#txtSettingsLocoFunctionImagePressed").text());
	if(null == s) return;
	$paramFnImagePressed[$SettingsLocoFunctionSelected] = s;
	$("#txtSettingsLocoFunctionImagePressed").text($paramFnImagePressed[$SettingsLocoFunctionSelected]);
	$.saveLocalInfo("inControl.loco(" + $paramLocoName + ")FnImagePressed[" + $SettingsLocoFunctionSelected + "]", $paramFnImagePressed[$SettingsLocoFunctionSelected]);
}

//----------------------------------------- Settings LocoFunctionAddress change
var $SettingsLocoFunctionAddressChange = function(){
	var ok;
	var s = prompt("New function address (space to remove):", " " + $("#txtSettingsLocoFunctionAddress").text());
	if(!s) return;
	s = $.trim(s);
	if(!s){
		$paramFnAddress[$SettingsLocoFunctionSelected] = "";
		$("#txtSettingsLocoFunctionAddress").text("");
		$.saveLocalInfo("inControl.loco(" + $paramLocoName + ")FnAddress[" + $SettingsLocoFunctionSelected + "]", "");
		$xmlioSendGetDevice($paramLocoAddress);
		return;
	}
	if($paramFnShunt[$SettingsLocoFunctionSelected]){
		alert("Cannot apply an Alternative Device Address when Shunting is enabled.");
		return;
	}
	s = parseInt(s, 10);
	ok = !isNaN(s);
	if(ok){
		s = Math.abs(s);
		ok = (s < 10000);
	}
	if(!ok){
		alert("Invalid Device address.");
		return;
	}
	$paramFnAddress[$SettingsLocoFunctionSelected] = s;
	$("#txtSettingsLocoFunctionAddress").text($paramFnAddress[$SettingsLocoFunctionSelected]);
	$.saveLocalInfo("inControl.loco(" + $paramLocoName + ")FnAddress[" + $SettingsLocoFunctionSelected + "]", $paramFnAddress[$SettingsLocoFunctionSelected]);
	$xmlioSendGetDevice($paramFnAddress[$SettingsLocoFunctionSelected]);
}

//----------------------------------------- Settings LocoFunctionShunt change
var $SettingsLocoFunctionShuntChange = function(){
	var auxShunt = false;
	for(var i = 0; i < 29; i++) if($paramFnShunt[i] && i != $SettingsLocoFunctionSelected) auxShunt = true;
	if(auxShunt && !$paramFnShunt[$SettingsLocoFunctionSelected]){
		alert("Another Function Key already defined for Shunting.");
		return;
	}
	if(!$paramFnShunt[$SettingsLocoFunctionSelected] && typeof($paramFnAddress[$SettingsLocoFunctionSelected]) == "number"){
		alert("Cannot apply Shunting when there is an Alternative Device Address.");
		return;
	}
	if($paramFnShunt[$SettingsLocoFunctionSelected]) $shunt = false;
	$paramFnShunt[$SettingsLocoFunctionSelected] = !$paramFnShunt[$SettingsLocoFunctionSelected];
	$("#chkSettingsLocoFunctionShunt").html($paramFnShunt[$SettingsLocoFunctionSelected] ? "[x]" : "[&nbsp;]");
	$.saveLocalInfo("inControl.loco(" + $paramLocoName + ")FnShunt[" + $SettingsLocoFunctionSelected + "]", ($paramFnShunt[$SettingsLocoFunctionSelected] ? "true" : ""));
}

//----------------------------------------- Click Power (2 - ON, 4 - OFF, other - undefined)
var $Power = function(){
	$powerStatus = (($powerStatus != 2) ? 2 : 4);
	$xmlioSendSetPower();
	$updatePower();
}

//----------------------------------------- Click ++
var $PlusPlus = function(){
	if ($powerStatusBlock && $powerStatus != 2) return;
	$speedValue+= 10;
	if($speedValue > 100) $speedValue = 100;
	$xmlioSendSetLocoSpeed($speedValue);
	$updateSpeed();
}

//----------------------------------------- Click +
var $Plus = function(){
	if ($powerStatusBlock && $powerStatus != 2) return;
	$speedValue++;
	if($speedValue > 100) $speedValue = 100;
	$xmlioSendSetLocoSpeed($speedValue);
	$updateSpeed();
}

//----------------------------------------- Click -
var $Minus = function(){
	if ($powerStatusBlock && $powerStatus != 2) return;
	$speedValue--;
	if($speedValue < 0) $speedValue = 0;
	$xmlioSendSetLocoSpeed($speedValue);
	$updateSpeed();
}

//----------------------------------------- Click --
var $MinusMinus = function(){
	if ($powerStatusBlock && $powerStatus != 2) return;
	$speedValue -= 10;
	if($speedValue < 0) $speedValue = 0;
	$xmlioSendSetLocoSpeed($speedValue);
	$updateSpeed();
}

//----------------------------------------- Level change
var $LevelChange = function(level){
	$speedValue = level;
	$xmlioSendSetLocoSpeed($speedValue);
}

//----------------------------------------- Click Rev
var $Rev = function(){
	if ($powerStatusBlock && ($powerStatus != 2 || !$forward)) return;
	$forward = false;
	$xmlioSendSetLocoDirection();
	$updateSpeed();
}

//----------------------------------------- Click STOP
var $STOP = function(){
	$speedValue = ($shunt ? 50 : 0);
	$xmlioSendSetLocoSpeed(-100);
	$updateSpeed();
}

//----------------------------------------- Click Frw
var $Frw = function(){
	if ($powerStatusBlock && ($powerStatus != 2 || $forward)) return;
	$forward = true;
	$xmlioSendSetLocoDirection();
	$updateSpeed();
}

//----------------------------------------- Click Function
var $Fn = function(func){
	if ($powerStatusBlock && $powerStatus != 2) return;
	$FnPressed[func] = !$FnPressed[func];
	if($paramFnShunt[func]){
		$shunt = $FnPressed[func];
		$updateSpeed();
	}
	$xmlioSendSetLocoFunction((typeof($paramFnAddress[func]) == "number") ? $paramFnAddress[func] : $paramLocoAddress, func, $FnPressed[func]);
	$updateFn(func);
}

//----------------------------------------- Power updade
var $updatePower = function(){
	var $html = $("html");
	var $body = $("body");
	var $imgPower = $("#imgPower");
	if(!$paramSpeedCtrlButtons && !$paramLocoNoSpeedCtrl) $VerticalSlider.setEnable(!$powerStatusBlock || $powerStatus == 2);	//Slider Speed Control
	if($html.hasClass("powerUndefined")) $html.removeClass("powerUndefined");
	if($html.hasClass("powerOff")) $html.removeClass("powerOff");
	if($html.hasClass("powerOn")) $html.removeClass("powerOn");
	if($body.hasClass("powerUndefined")) $body.removeClass("powerUndefined");
	if($body.hasClass("powerOff")) $body.removeClass("powerOff");
	if($body.hasClass("powerOn")) $body.removeClass("powerOn");
	switch($powerStatus) {
		case 2: {		//power on
			$html.addClass("powerOn");
			$body.addClass("powerOn");
			$imgPower.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imagePowerOff + "?MaxHeight=" + $imgPower.height() + "&MaxWidth=" + $imgPower.width()));
			break;
		}
		case 4: {		//power off
			$html.addClass("powerOff");
			$body.addClass("powerOff");
			$imgPower.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imagePowerOn + "?MaxHeight=" + $imgPower.height() + "&MaxWidth=" + $imgPower.width()));
			break;
		}
		default: {		//undefined
			$powerStatus = 0;
			$html.addClass("powerUndefined");
			$body.addClass("powerUndefined");
			$imgPower.error(function(){$errorLoadingImage($(this))}).attr("src", $correctForSomeImageTypes($imagePowerUndefined + "?MaxHeight=" + $imgPower.height() + "&MaxWidth=" + $imgPower.width()));
			break;
		}
	}
}

//----------------------------------------- Speed updade
var $updateSpeed = function(){
	if($paramLocoNoSpeedCtrl) return;
	var $imgRev = $("#imgRev");
	var $imgRevR = $("#imgRevR");
	var $imgFrw = $("#imgFrw");
	var $imgFrwR = $("#imgFrwR");
	if($paramSpeedCtrlButtons) $("#lblSpeed").text($shunt ? $speedValue - 50 : $speedValue); else $VerticalSlider.setLevel($speedValue, $shunt);	//Buttons Speed Control or Slider Speed Control
	if($forward){
		$imgRev.hide();
		$imgRevR.show();
		$imgFrw.show();
		$imgFrwR.hide();
	} else {
		$imgRev.show();
		$imgRevR.hide();
		$imgFrw.hide();
		$imgFrwR.show();
	}
}

//----------------------------------------- Function updade
var $updateFn = function(func){
	if(!$paramFnToggle[func]) return;
	var $lblF = $("#lblF" + func);
	var $imgF = $("#imgF" + func + "Image");
	var $imgFP = $("#imgF" + func + "ImagePressed");
	if($paramFnImage[func] == ""){															// Label
		if($FnPressed[func]){
			if($lblF.hasClass("lblFunctionsUnPressed")) $lblF.removeClass("lblFunctionsUnPressed");
			if(!$lblF.hasClass("lblFunctionsPressed")) $lblF.addClass("lblFunctionsPressed");
		} else {  
			if($lblF.hasClass("lblFunctionsPressed")) $lblF.removeClass("lblFunctionsPressed");
			if(!$lblF.hasClass("lblFunctionsUnPressed")) $lblF.addClass("lblFunctionsUnPressed");
		}
	} else {																				//Image
		if ($FnPressed[func]){
			$imgF.hide();
			$imgFP.show();
		} else {
			$imgF.show();
			$imgFP.hide();
		}
	}
}

//============================================================= Generic code

//+++++++++++++++++++++++++++++++++++++++++++++++++++ Extensions

//----------------------------------------- Get URL parameters (parameters are not case sensitive - must compare 'key' with lowercase)
$.extend({
	getUrlParameters: function(){							//Item 'undefined' if index not found
		var vars = [], hash, key;
		var hashes;
		var auxInt = window.location.href.indexOf("?");
		if(auxInt < 0) return vars;
		hashes = window.location.href.slice(auxInt + 1).split("&");
		for(var i = 0; i < hashes.length; i++){
			if (hashes[i].indexOf("=") == -1) hashes[i]+= "=";
			hash = hashes[i].split("=");
			key = hash[0].toLowerCase();
			vars.push(decodeURIComponent(key));
			vars[key] = decodeURIComponent(hash[1]);
		}
		return vars;
	}
});

//----------------------------------------- Manage local info (case sensitive)
$.extend({
	saveLocalInfo: function(key, value){
		var _key = encodeURIComponent(key);
		var _value = encodeURIComponent(value);
		if(typeof(window.localStorage) == "undefined"){
			var dt = new Date();
			dt.setTime(dt.getTime() + 315360000000);	//~10 years
			document.cookie = _key + "=" + _value + "; expires=" + dt.toGMTString();
		} else localStorage[_key] = _value;
//localStorage.setItem("_key", "_value");
	},
	loadLocalInfo: function(key){						//Return 'undefined' if not found
		var _key = encodeURIComponent(key);
		var value;
		if(typeof(window.localStorage) == "undefined"){
			var aux, cookieArray, cookie;
			aux = _key + "=";
			cookieArray = document.cookie.split(";");
			for(var i = 0; i < cookieArray.length; i++){
				cookie = cookieArray[i];
				while(cookie.charAt(0) == " ") cookie=cookie.substring(1);
				if(cookie.indexOf(aux) == 0){
					value = cookie.substring(aux.length);
					break;
				}
			}
		} else value = localStorage[_key];
//localStorage.getItem("_key");
		return ((typeof(value) == "undefined" || null == value) ? (function(){})() : decodeURIComponent(value));
	},
	removeLocalInfo: function(key){
		var _key = encodeURIComponent(key);
		if(typeof(window.localStorage) == "undefined"){
			var dt = new Date();
			dt.setTime(dt.getTime() - 1);
			document.cookie = _key + "=; expires=" + dt.toGMTString();
		} else localStorage.removeItem(_key);
//localStorage.clear(); //all
	}
});

//----------------------------------------- Calculate total margin + border + padding (horizontal or vertical)
$.extend({
	calcTotalMBP: function(obj, h){
		var t = 0;
		if(h){
			t+= parseInt(obj.css("margin-left").replace("px",""), 10) + parseInt(obj.css("border-left-width").replace("px",""), 10) + parseInt(obj.css("padding-left").replace("px",""), 10);
			t+= parseInt(obj.css("margin-right").replace("px",""), 10) + parseInt(obj.css("border-right-width").replace("px",""), 10) + parseInt(obj.css("padding-right").replace("px",""), 10);
		} else {
			t+= parseInt(obj.css("margin-top").replace("px",""), 10) + parseInt(obj.css("border-top-width").replace("px",""), 10) + parseInt(obj.css("padding-top").replace("px",""), 10);
			t+= parseInt(obj.css("margin-bottom").replace("px",""), 10) + parseInt(obj.css("border-bottom-width").replace("px",""), 10) + parseInt(obj.css("padding-bottom").replace("px",""), 10);
		}
		return t;
	}
});

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ End of file
