/**********************************************************************************************
* 
* Javascript for 'webThrottle.html'
* 
* This script defines the web throttle behaviour.
* 
* >>> This file version: 2.4 - by Oscar Moutinho (oscar.moutinho@gmail.com)
* 
* This script relies on 'jquery.jmriConnect.js v2.1' (read its header for dependencies).
* 
* URL parameters: (roster/panels list if no parameters)
* - 'loconame' (to open a throttle for a loco)
* - 'turnouts' (to list turnouts)
* - 'routes' (to list routes)
* - 'panelname' (to open a panel)
* - 'reset' (to remove local configuration - restore defaults)
* - 'debug' (=true -> turn it ON, =false -> turn it OFF, other values -> nothing change)
* 
**********************************************************************************************/

//+++++++++++++++++++++++++++++++++++++++++++++++++++ Global Vars and Functions

//----------------------------------------- Global vars

var log = new Logger();
var $debug = true;
var $vScrollbarWidth;
var $showScrollBar = false;
var $zIndexForSmoothAlert = 999999999;										// Max z-index = 2147483647
var $jmri = null;
var $throttleType = '';
var $rosterGroup = '';
var $isRoster;
var $frameList = [];
var $toFrame = false;
var $inFrame = false;
var $pageInFrame;
var $speedPosition = true;
var $inputParameters;
var $paramLocoName;
var $paramPanels;
var $paramPanelName;
var $rosterGroups;
var $locoList;
var $panelLoaded = false;
var $resizeCheckTimer = null;
var $resizeCheckInterval = 500;												// ms
var $viewportHeight = 0;													// Initial value to force resize
var $viewportWidth = 0;														// Initial value to force resize
var $changeBothSizes;
var $heightChanged;
var $widthChanged;
var $portrait;
var $fontSizeMin = 8;														// px
var $fontSizeMax = 48;														// px
var $defaultFontSize = 16;													// px - default
var $fontSize = $defaultFontSize;
var $fontDelta;
var $fontChanged;
var $sizeCtrlPercent;
var $nextBlockTop;
var $headerHeightRef = 40;													// px
var $buttonsHeightRef = 30;													// px
var $cellHeightRef = 100;													// px
var $cellWidthRef = 500;													// px
var $speedWidthRef = 80;													// px
var $functionHeightRef = 80;												// px
var $functionWidthRef = 250;												// px
var $buttonDelayTimeout = 1000;												// ms
var $powerDelayTimer = null;
var $removeDelayTimer = null;
var $speedTimer = null;
var $speedInterval = 250;													// ms
var $speedStep = 0.10;
var $speedFeedback = true;
var $speedAux = 0;
var $hasTouch = ('ontouchstart' in window);
var $hasMovement = (window.orientation !== null) && $hasTouch;
var $orientation = null;
var $movementTilt = null;
var $movementActive = false;
var $movementOn = false;
var $movementCtrl = 0;
var $locoAddress = "none";
var $help = [];

//----------------------------------------- Generic onError
window.onerror = function(errMsg, errUrl, errLineNumber) {
	if ($jmri) $jmri.closeSocket();
	if ($resizeCheckTimer) {
		clearInterval($resizeCheckTimer);
		$resizeCheckTimer = null;
	}
	if ($speedTimer) {
		clearInterval($speedTimer);
		$speedTimer = null;
	}
	if (errMsg.indexOf('private~') >= 0) alert(errMsg.split('~')[1]);
	else {
		if (errMsg == 'Uncaught ReferenceError: stopme is not defined') location.reload(true);	// I don't know what this is !?!?!? Just reload.
		else alert('\nError running javascript:\n' + errMsg + '\n\nURL:\n' + errUrl + '\n\nLine Number: ' + errLineNumber);
	}
	document.body.innerHTML = '';
	return true;
};

//----------------------------------------- Page exit cleanup 1
window.onbeforeunload = function() {
};

//----------------------------------------- Page exit cleanup 2
window.onunload = function() {
	if ($jmri) {
		if ($('body').attr('locoReady') == 'true') $jmri.setJMRI('throttle', $locoAddress, {"release":null});
		$jmri.closeSocket();
	}
	if ($resizeCheckTimer) {
		clearInterval($resizeCheckTimer);
		$resizeCheckTimer = null;
	}
	if ($speedTimer) {
		clearInterval($speedTimer);
		$speedTimer = null;
	}
};

//----------------------------------------- Add trim method to string
String.prototype.trim = function () {return this.replace(/^\s+|\s+$/g,'');};

//----------------------------------------- Immediate execution
try {
	if (jQuery === undefined) throw new Error('private~jQuery not loaded.\nHTML5 and WebSockets needed.\nCheck browser compatibility.');
} catch(error) {
	throw new Error('private~jQuery not loaded.\nHTML5 and WebSockets needed.\nCheck browser compatibility.');
}

try {
	localStorage['webThrottle.test'] = '1';
	localStorage.removeItem('webThrottle.test');
} catch(error) {
	if (error.code === DOMException.QUOTA_EXCEEDED_ERR && localStorage.length === 0) throw new Error('private~Turn off Private Browsing.');
	else throw new Error('private~Local Storage not available.\nHTML5 and WebSockets needed.\nCheck browser compatibility.');
}

//----------------------------------------- Run at start up
$(document).ready(function() {
	/******* Constants available in '$jmri' object
	*	$jmri.powerUNKNOWN = 0;
	*	$jmri.powerON = 2;
	*	$jmri.powerOFF = 4;
	*	$jmri.turnoutUNDEFINED = 0;
	*	$jmri.turnoutUNKNOWN = 1;
	*	$jmri.turnoutCLOSED = 2;
	*	$jmri.turnoutTHROWN = 4;
	*	$jmri.routeDISABLED = 0;
	*	$jmri.routeUNDEFINED = 1;
	*	$jmri.routeACTIVE = 2;
	*	$jmri.routeINACTIVE = 4;
	*	$jmri.TRUE = true;
	*	$jmri.FALSE = false;
	*	$jmri.YES = 1;
	*	$jmri.NO = 0;
	*	$jmri.EMERGENCY_STOP = '-1.0';
	*	$jmri.STOP = '0.0';
	*	$jmri.FULL_SPEED = '1.0';
	******** Functions available in '$jmri' object
	* $jmri.getRoster(group)
	* . To list all: group = null or undefined
	* . Returns array of object: id, roadNumber, roadName, mfg, owner, model, dccAddress, imageFilePath, iconFilePath
	* $jmri.getRosterItem(id)
	* . Returns object: id, fileName, roadNumber, roadName, mfg, owner, model, dccAddress, comment, maxSpeed, imageFilePath, iconFilePath, URL, IsShuntingOn, f[i].lockable, f[i].functionlabel, f[i].functionImage, f[i].functionImageSelected
	* . If a function is not defined: f[i] = null 
	* $jmri.getRosterGroups()
	* . Returns array of strings: rosterGroup
	* $jmri.getObjectList(listType) {
	* . Possible values for string 'listType' (get): roster, panels, lights, reporters, sensors, turnouts, signalHeads, signalMasts, routes, memories
	* . Returns array of objects: list
	* $jmri.closeSocket()
	* . To stop communication with JMRI (usually, before exit and before blocking code: alert(), ...)
	* $jmri.getJMRI(type, name)
	* . Possible values for string 'type' (get): light, reporter, sensor, turnout, signalHead, signalMast, route, memory, power, rosterEntry
	* $jmri.setJMRI(type, name, args)
	* . Possible values for string 'type' (set): light, reporter, sensor, turnout, signalHead, signalMast, route, memory, power
	* Special case for 'type' = 'power' > string 'name' should be null
	* Possible 'args' for 'throttle': {"throttle":throttleName,"address":dccAddress,"speed":speed,"forward":forward,"Fn":active} (0 <= n <= 28)
	* Possible 'args' for 'light': {"userName":userName,"comment":comment,"state":state}
	* Possible 'args' for 'reporter': {"userName":userName,"state":state,"comment":comment,"report":report,"lastReport":lastReport}
	* Possible 'args' for 'sensor': {"userName":userName,"comment":comment,"inverted":inverted,"state":state}
	* Possible 'args' for 'turnout': {"userName":userName,"comment":comment,"inverted":inverted,"state":state}
	* Possible 'args' for 'signalHead': {"userName":userName,"comment":comment,"lit":lit,"appearance":appearance,"held":held,"state":state,"appearanceName":appearanceName}
	* Possible 'args' for 'signalMast': {"userName":userName,"":aspect,"lit":lit,"held":held,"state":state}
	* Possible 'args' for 'route': {"userName":userName,"comment":comment,"state":state}
	* Possible 'args' for 'memory': {"userName":userName,"comment":comment,"value":value}
	* Possible 'args' for 'power': {"state":state}
	* >>> Other values for 'type' and new 'args' may be available
	********************************************/
	var debug = loadLocalInfo('webThrottle.debug');
	if (debug == 'true' || debug == 'false') $debug = (debug == 'true'); else saveLocalInfo('webThrottle.debug', $debug = false);
	$fontDelta = 0.14;	// hardcoded from testing
	var fS = loadLocalInfo('webThrottle.fontSize');
	if (fS && !isNaN(fS) && Number(fS) >= $fontSizeMin && Number(fS) <= $fontSizeMax) $fontSize = Number(fS); else saveLocalInfo('webThrottle.fontSize', $fontSize);
	$vScrollbarWidth = getVerticalScrollbarWidth();
	loadFrameList();
	for (var i = 0; i < $frameList.length; i++) {	// Check if this page is flagged to be displayed in a frame
		if ($frameList[i] == document.URL) {
			$inFrame = true;
			break;
		}
	}
	var speedPosition = loadLocalInfo('webThrottle.speedPosition');
	if (speedPosition == 'true' || speedPosition == 'false') $speedPosition = (speedPosition == 'true'); else saveLocalInfo('webThrottle.speedPosition', $speedPosition);
	var movementActive = loadLocalInfo('webThrottle.movementActive');
	if (movementActive == 'true' || movementActive == 'false') $movementActive = (movementActive == 'true'); else saveLocalInfo('webThrottle.movementActive', $movementActive);
	$inputParameters = getUrlParameters();
	debug = $inputParameters.debug;
	if (debug) debug = debug.toLowerCase();
	if (debug == 'true' || debug == 'false') {
		$debug = (debug == 'true');
		saveLocalInfo('webThrottle.debug', $debug);
		alert('Debug mode ' + ($debug ? 'started.' : 'stopped.'));
		window.open(document.URL.split('?')[0], '_top');	// Stop building throttle and restart without parameters
		return;
	}
	if ($inputParameters.reset !== undefined) {	// Remove all application parameters
		removeLocalInfo("webThrottle.debug");
		removeLocalInfo("webThrottle.fontSize");
		removeLocalInfo("webThrottle.speedPosition");
		removeLocalInfo("webThrottle.movementActive");
		removeLocalInfo("webThrottle.rosterGroup");
		removeLocalInfo("webThrottle.frameListSize");
		for(var i = 0; i < $frameList.length; i++) removeLocalInfo("webThrottle.frameList[" + i + "]");
		alert('Default configuration restored.');
		window.open(document.URL.split('?')[0], '_top');	// Stop building throttle and restart without parameters
		return;
	}
	if ($debug) smoothAlert('In debug mode ...', 3);
	$paramLocoName = $inputParameters.loconame;
	$paramPanelName = $inputParameters.panelname;
	if ($paramLocoName) $throttleType = 'loco';
	if (!$throttleType) if ($inputParameters.turnouts !== undefined) $throttleType = 'turnouts';
	if (!$throttleType) if ($inputParameters.routes !== undefined) $throttleType = 'routes';
	if (!$throttleType) if ($paramPanelName) $throttleType = 'panel';
	if (!$throttleType) $throttleType = 'roster';
	$pageInFrame = (window.parent.$('iframe').length > 0);
	if ($frameList.length && $throttleType == 'roster' && !$pageInFrame) {	// Has info for frames, is the master and it is the top window (build frameset)
		var body = $('body').attr('id', 'bodyFrames');
		var frame = $('<iframe>').attr('src', document.URL + '?frameFF').attr('id', encodeId(document.URL + '?frameFF')).addClass('frame');	// Added 'frameFF' to solve FF bug
		body.append(frame);
		for (var j = 0; j < $frameList.length; j++) {
			frame = $('<iframe>').attr('src', $frameList[j]).attr('id', encodeId($frameList[j])).addClass('frame');
			body.append(frame);
		}
		$resizeCheckTimer = setInterval(function() {
			checkSmoothAlertEnd();
			var h = $(window).height();
			var w = $(window).width();
			if ($viewportHeight != h) {
				$heightChanged = true;
				$viewportHeight = h;
			} else $heightChanged = false;
			if ($viewportWidth != w) {
				$widthChanged = true;
				$viewportWidth = w;
			} else $widthChanged = false;
			if ($heightChanged || $widthChanged) {
				$portrait = (w <= h);
				var totalFrames = $frameList.length + 1;
				/* Explanation for the next lines
					h / w = vRects / hRects
					totalFrames = vRects * hRects
					.
					vRects = h / w * hRects
					hRects = totalFrames / vRects
					.
					vRects = h / w * totalFrames / vRects
					.
					vRects^2 = h / w * totalFrames
				*/
				var ratio = h / w * 1.2;									// Ratio with correction to choose rectangularity
				var vRects = Math.round(Math.sqrt(ratio * totalFrames));	// Number of rectangles per column
				var hRects = Math.round(totalFrames / vRects);				// Number of rectangles per row
				if (vRects * hRects < totalFrames) {
					if (ratio > vRects / hRects) vRects++; else hRects++;
				}
				if ((vRects * hRects - totalFrames) >= ((vRects > hRects) ? hRects : vRects)) {
					if (ratio > vRects / hRects) vRects--; else hRects--;
				}
				var master = Math.floor((hRects - 1) / 2);
				var rectHeight = Math.floor(h / vRects);					//Each rectangle height
				var rectWidth = Math.floor(w / hRects);						//Each rectangle width
				var t = 0;
				var l = 0;
				var frames = $('.frame');
				var o;
				for (var i = 1; i <= master; i++) {
					o = frames.eq(i);
					o.attr('rightSide', (l + 1 > hRects / 2));
					o.css('top', t).css('left', l);
					setOuterHeight(o, rectHeight, true);
					setOuterWidth(o, rectWidth, true);
					if(l + rectWidth * 1.5 < w) l+= rectWidth; else {l = 0; t+= rectHeight;}
				}
				o = frames.eq(0);
				o.attr('rightSide', (l + 1 > hRects / 2));
				o.css('top', t).css('left', l);
				setOuterHeight(o, rectHeight, true);
				setOuterWidth(o, rectWidth, true);
				if(l + rectWidth * 1.5 < w) l+= rectWidth; else {l = 0; t+= rectHeight;}
				for (var i = master + 1; i < totalFrames; i++) {
					o = frames.eq(i);
					o.attr('rightSide', (l + 1 > hRects / 2));
					o.css('top', t).css('left', l);
					setOuterHeight(o, rectHeight, true);
					if (i < totalFrames - 1) {
						setOuterWidth(o, rectWidth, true);
						if(l + rectWidth * 1.5 < w) l+= rectWidth; else {l = 0; t+= rectHeight;}						
					} else {	// Last frame
						setOuterWidth(o, w - l, true);
					}
				}
				resizeSmoothAlerts();
			}
		}, $resizeCheckInterval);
		return;	// Stop building throttle if frameset
	}
	// Continue building throttle if not frameset
	startJMRI();
});

//----------------------------------------- Start JMRI
var startJMRI = function() {
	$jmri = $.JMRI({
		//*** Callback Functions available in '$jmri' object
		toSend: function(data) {$debug && log.log(new Date() + ' - ' + document.title + '\n' + 'JSONtoSend: ' + data);},	//Nothing to do
		fullData: function(data) {$debug && log.log(new Date() + ' - ' + document.title + '\n' + 'JSONreceived: ' + data);},	//Nothing to do
                error: function (code, message) {
                    if (code === 0)
                        jmriLostComm(message);
                    else if (code === 200)
                        smoothAlert(message, 10);
                    else
                        smoothAlert('Error: ' + code + ' - ' + message);
                },
		end: function() {jmriLostComm('The JMRI WebSocket service was turned off.\nSolve the problem and refresh web page.');},
		ready: function(jsonVersion, jmriVersion, railroadName) {jmriReady(jsonVersion, jmriVersion, railroadName);},	//When WebSocket connection established - continue next steps
		throttle: function(name, address, speed, forward, fs) {throttleState(name, address, speed, forward, fs);},
		light: function(name, userName, comment, state) {},	//Nothing to do
		reporter: function(name, userName, state, comment, report, lastReport) {},	//Nothing to do
		sensor: function(name, userName, comment, inverted, state) {},	//Nothing to do
		turnout: function(name, userName, comment, inverted, state) {layoutTurnoutState(name, userName, comment, inverted, state);},
		signalHead: function(name, userName, comment, lit, appearance, held, state, appearanceName) {},	//Nothing to do
		signalMast: function(name, userName, aspect, lit, held, state) {},	//Nothing to do
		route: function(name, userName, comment, state) {layoutRouteState(name, userName, comment, state);},
        memory: function(name, userName, comment, value) {},	//Nothing to do
        power: function(state) {layoutPowerState(state);},
    });
	if (!$jmri) throw new Error('private~Could not open JMRI WebSocket.');
};

//----------------------------------------- Lost communication with JMRI
var jmriLostComm = function(message) {
	var timer = loadLocalInfo('webThrottle.timerReload');
	if (timer && !isNaN(timer) && Number(timer) >= 0 && Number(timer) == Math.abs(timer)) timer = Number(timer); else saveLocalInfo('webThrottle.timerReload', timer = new Date().getTime());	// Miliseconds
	if (new Date().getTime() - timer < 30000) {	// Reload if less than 30s after communication lost
		smoothAlert('Communication lost.\nRestarting ...');
		location.reload(true);
	} else {
		throw new Error('private~' + message);
	}
};

//----------------------------------------- JMRI ready
var jmriReady = function(jsonVersion, jmriVersion, railroadName) {
	removeLocalInfo('webThrottle.timerReload');	// Communication OK -> Restart count for communication lost
	var body = $('body');
	var bodyFrameOuter = $('<div>');
	bodyFrameOuter.attr('id', 'bodyFrameOuter');
	body.append(bodyFrameOuter);
	var bodyFrameInner = $('<div>');
	bodyFrameInner.attr('id', 'bodyFrameInner');
	bodyFrameOuter.append(bodyFrameInner);
	if ($hasTouch) {	// to prevent 'ghosts', ...
//		body.on('touchstart', function(event) {event.preventDefault(); event.stopImmediatePropagation();});	// This blocks every touch !?
	} else {
		body.on('mousedown', function(event) {event.preventDefault(); event.stopImmediatePropagation();});
	}
	if ($hasMovement && window.DeviceOrientationEvent) window.addEventListener('deviceorientation', function(event) {deviceOrientation(event);});
	var header = $('<div>');
	header.attr('id', 'header');
	bodyFrameInner.append(header);
	if ($throttleType == 'roster' || !$pageInFrame) {	// Is master or isn't in a frame
		var power = $('<div>').addClass('button').attr('id', 'power').attr('state', -1);
		power.append($('<div>').text('o').addClass('buttonText'));
		if ($hasTouch) {
			power.on('touchstart', function(event) {powerButtonPressedTouch(event.originalEvent);});
			power.on('touchend', function(event) {powerButtonReleasedTouch(event.originalEvent);});
		} else {
			power.on('mousedown', function(event) {powerButtonPressedMouse(event);});
			power.on('mouseup', function(event) {powerButtonReleasedMouse(event);});
			power.on('mouseout', function(event) {powerButtonCanceledMouse(event);});
		}
		header.append(power);
	}
	if ($throttleType == 'roster') {	// Button to define new throttles in frame or not
		var toFrame;
		toFrame = $('<div>').addClass('button').attr('id', 'toFrame');
		toFrame.append($('<div>').text('single').addClass('buttonText'));
		toFrame.on('click', function(event) {manageNewThrottles(event)});
		header.append(toFrame);
	} else {
		if ($inFrame) {	// Button to remove itself from frame
			$help.push(
				'Press [X] button for 2s' +
				'\nto remove this frame.' +
				''
			);
			var removeFrame = $('<div>').addClass('button').attr('id', 'removeFrame');
			removeFrame.append($('<div>').text('X').addClass('buttonText'));
			if ($hasTouch) {
				removeFrame.on('touchstart', function(event) {removeButtonPressedTouch(event.originalEvent);});
				removeFrame.on('touchend', function(event) {removeButtonReleasedTouch(event.originalEvent);});
			} else {
				removeFrame.on('mousedown', function(event) {removeButtonPressedMouse(event);});
				removeFrame.on('mouseup', function(event) {removeButtonReleasedMouse(event);});
				removeFrame.on('mouseout', function(event) {removeButtonCanceledMouse(event);});
			}
			header.append(removeFrame);
		}
	}
	if ($throttleType == 'loco' && !$pageInFrame) {	// Button to select speed control position
		var speedPosition;
		speedPosition = $('<div>').addClass('button').attr('id', 'speedPosition');
		speedPosition.append($('<div>').text('xxx').addClass('buttonText'));
		speedPosition.on('click', function(event) {manageSpeedPosition(event)});
		header.append(speedPosition);
		changeSpeedPosition($speedPosition);
	}
	if ($throttleType == 'roster' || !$pageInFrame) {	// Is master or isn't in a frame
		var fontSmaller = $('<div>').addClass('button').attr('id', 'fontSmaller');
		fontSmaller.append($('<div>').text('A').addClass('buttonText'));
		fontSmaller.on('click', function(event) {fontSizeChange(event, -2);});
		header.append(fontSmaller);
		var fontLarger = $('<div>').addClass('button').attr('id', 'fontLarger');
		fontLarger.append($('<div>').text('A').addClass('buttonText'));
		fontLarger.on('click', function(event) {fontSizeChange(event, +2);});
		header.append(fontLarger);
	}
	var help = $('<div>').addClass('button').attr('id', 'help');
	help.append($('<div>').text('?').addClass('buttonText'));
	help.on('click', function(event) {helpShow(event);});
	header.append(help);
	switch ($throttleType) {
		case 'roster':
			var rg = loadLocalInfo('webThrottle.rosterGroup');
			if (rg || rg == '') $rosterGroup = rg; else saveLocalInfo('webThrottle.rosterGroup', $rosterGroup);
			$help.push(
				'Web Throttle for JMRI ' + jmriVersion + ' controlling \'' + railroadName + '\'' +
				'\n' +
				'\n URL parameters: (roster/panels list if no parameters)' +
				'\n- \'loconame\' (to open a throttle for a loco)' +
				'\n- \'turnouts\' (to list turnouts)' +
				'\n- \'routes\' (to list routes)' +
				'\n- \'panelname\' (to open a panel)' +
				'\n- \'reset\' (to restore defaults)' +
				'\n- \'debug\' (true or false -> turn debug ON/OFF)' +
				'\nEx.: .../web/webThrottle.html?loconame=A%20325' +
				'\n                  (for loco ID = \'A 325\')' +
				'\nEx.: .../web/webThrottle.html?turnouts' +
				'\n' +
				'\nCheck the header of \'webThrottle.html\' for' +
				'\ndependencies and versions.' +
				'\n' +
				'\n... by Oscar Moutinho (oscar.moutinho@gmail.com)' +
				''
			);
			$help.push(
				'Press [o] button for 2s to turn power ON/OFF.' +
				'\n' +
				'\nClick [single] / [multi] button to open\npages alone or grouped.' +
				'\n' +
				'\nAlert messages:' +
				'\n- Older messages are on top of the others;' +
				'\n- They can be moved - just drag them.' +
				'\n' +
				'\nAdvices:' +
				'\n- Prevent device sleep when running.' +
				'\n- You may want to prevent rotation on your tablet.' +
				''
			);
			$isRoster = ($rosterGroup != '[Panels]');
			var buttons = $('<div>');
			buttons.attr('id', 'buttons');
			bodyFrameInner.append(buttons);
			$rosterGroups = $jmri.getRosterGroups();
			var groups = $('<div>').addClass('button').attr('id', 'groups');
			groups.append($('<div>').text('R. Groups / Panels').addClass('buttonText'));
			groups.on('click', function(event) {chooseGroup(event);});
			buttons.append(groups);
			var turnouts = $('<div>').addClass('button').attr('id', 'turnouts');
			turnouts.append($('<div>').text('Turnouts').addClass('buttonText'));
			turnouts.on('click', function(event) {turnoutsShow(event);});
			buttons.append(turnouts);
			var routes = $('<div>').addClass('button').attr('id', 'routes');
			routes.append($('<div>').text('Routes').addClass('buttonText'));
			routes.on('click', function(event) {routesShow(event);});
			buttons.append(routes);
			if ($isRoster) {	// Roster
				var roster = $jmri.getRoster($rosterGroup);
				if (roster.length) {
					roster.forEach(function(loco) {
						var rosterCell = $('<div>');
						rosterCell.on('click', function(event) {throttleShow(event, loco.name);});
						rosterCell.addClass('rosterCell');
						bodyFrameInner.append(rosterCell);
						rosterCell.append($('<div>').text(loco.name + ' (' + loco.dccAddress + ')').addClass('locoName'));
						rosterCell.append($('<div>').text(loco.roadName + ((loco.roadNumber !== '') ? ' (' + loco.roadNumber + ')' : '')).addClass('locoRoad'));
						rosterCell.append($('<div>').text(loco.mfg + ((loco.model !== '') ? ' (' + loco.model + ')' : '') + ((loco.owner !== '') ? ' [' + loco.owner + ']' : '')).addClass('locoMfg'));
						var icon = loco.iconFilePath.length > 0;
						var image = loco.imageFilePath.length > 0;
						if (icon || image) {
							rosterCell.append($('<div>').addClass('imageContainer'));
							var img = $('<img>').addClass('imageInitial');
							rosterCell.children('.imageContainer').append(img); 
							img[0].onload = function() {
								var o = $(this);
								o.attr('originalHeight', o.height());
								o.attr('originalWidth', o.width());
								resizeImage(o);
							};
							img.attr('src', '/roster/' + encodeURIComponent(loco.name) + '/' + (icon ? 'icon' : 'image') + '?maxHeight=' + $cellHeightRef);
						}
					});
				}
			} else {	// Panels
				var panels = $jmri.getObjectList('panels');
				panels.forEach(function(item) {
					var panelCell = $('<div>');
					panelCell.on('click', function(event) {openPanel(event, decodeURIComponent(item.name), item.URL);});	// 'decode' because it arrives encoded
					panelCell.addClass('panelCell');
					bodyFrameInner.append(panelCell);
					panelCell.append($('<div>').text(decodeURIComponent(item.name) + ((item.userName) ? ' - ' + item.userName : '')).addClass('panelName'));	// 'decode' because it arrives encoded
					panelCell.append($('<div>').addClass('imageContainer'));
					var img = $('<img>').addClass('imageInitial');
					panelCell.children('.imageContainer').append(img); 
					img[0].onload = function() {
						var o = $(this);
						o.attr('originalHeight', o.height());
						o.attr('originalWidth', o.width());
						resizeImage(o);
					};
					img.attr('src', '/frame/' + decodeURIComponent(item.name) + '.png');
				});
			}
			break;
		case 'loco':
			body.attr('locoReady', 'false');
			$help.push(
				(!$inFrame ? 'Press [|::] / [::|] button to\nposition speed control left/right.\n\n' : '') +
				($hasMovement ? 'Click [tilt] / [normal] button to turn\nON/OFF device movement sensor.' +
				'\nIf [tilt] active, press speed control and\nmove your phone/tablet to change speed.\n\n' : '') +
				'Click on loco name to select another one.' +
				''
			);
			if (!$hasMovement) $help.push(
				'This device or browser doesn\'t\nsupport movement detection.' +
				''
			);
			document.title+= ' (loco: ' + $paramLocoName + ')';
			var loco = $jmri.getRosterItem($paramLocoName);
			if (loco) {
				var header = $('#header');
				var emergencyStop = $('<div>').addClass('button').attr('id', 'emergencyStop');
				emergencyStop.append($('<div>').text('STOP').addClass('buttonText'));
				emergencyStop.on('click', function(event) {immediateStop(event);});
				header.append(emergencyStop);
				if ($hasMovement) {
					var movementActive = $('<div>').addClass('button').attr('id', 'movementActive');
					movementActive.append($('<div>').text('xxx').addClass('buttonText'));
					movementActive.on('click', function(event) {manageMovementActive(event);});
					header.append(movementActive);
				}
				var speed = $('<div>');
				speed.addClass('speed');
				speed.attr('speed', 0);
				bodyFrameInner.append(speed);
				speed.append($('<div>').attr('id', 'speedGrid'));
				var speedGrid = $('#speedGrid');
				speedGrid.append($('<div>').attr('id', 'speedLimits'));
				speedGrid.append($('<div>').attr('id', 'speedBar'));
				var speedTouch = $('<div>');
				speedGrid.append(speedTouch.attr('id', 'speedTouch'));
				if ($hasMovement) changeMovementActive($movementActive);
				else {
					if ($hasTouch) {
						speedTouch.on('touchstart', function(event) {speedPressedTouch(event.originalEvent);});
						speedTouch.on('touchmove', function(event) {speedMovingTouch(event.originalEvent);});
						speedTouch.on('touchend', function(event) {speedReleasedTouch(event.originalEvent);});
					} else {
						speedTouch.on('mousedown', function(event) {speedPressedMouse(event);});
						speedTouch.on('mousemove', function(event) {speedMovingMouse(event);});
						speedTouch.on('mouseup', function(event) {speedReleasedMouse(event);});
						speedTouch.on('mouseout', function(event) {speedCanceledMouse(event);});
					}
				}
				var reverse = $('<div>').addClass('button').addClass('direction').attr('id', 'reverse');
				reverse.append($('<div>').text('>').addClass('buttonText'));	// Will rotare 90º right
				reverse.on('click', function(event) {setDirection(event, false);});
				speed.append(reverse);
				var forward = $('<div>').addClass('button').addClass('direction').attr('id', 'forward');
				forward.append($('<div>').text('<').addClass('buttonText'));	// Will rotare 90º right
				forward.on('click', function(event) {setDirection(event, true);});
				speed.append(forward);
				var functionsOuter = $('<div>');
				functionsOuter.addClass('functionsOuter');
				bodyFrameInner.append(functionsOuter);
				var functionsInner = $('<div>');
				functionsInner.addClass('functionsInner');
				functionsOuter.append(functionsInner);
				functionsInner.append($('<div>').text(loco.name + ' (' + loco.dccAddress + ')').addClass('locoName').attr('id', 'locoName').on('click', function(event) {changeLoco(event);}));
				var noFs = true;
				for (var i = 0; i < 29; i++) if (loco.f[i]) noFs = false;
				for (var i = 0; i < 29; i++) {
					if (loco.f[i] || noFs) {
						var func = $('<div>');
						if (noFs || loco.f[i].lockable) {
							func.on('click', function(event) {functionClick(event);});
						} else {
							if ($hasTouch) {
								func.on('touchstart', function(event) {functionPressedTouch(event.originalEvent, $(this));});
								func.on('touchend', function(event) {functionReleasedTouch(event.originalEvent, $(this));});
							} else {
								func.on('mousedown', function(event) {functionPressedMouse(event);});
								func.on('mouseup', function(event) {functionReleasedMouse(event);});
								func.on('mouseout', function(event) {functionCanceledMouse(event);});
							}
						}
						func.addClass('functionButton').attr('id', 'locoFunction' + i);
						func.attr('state', $jmri.NO);
						functionsInner.append(func);
						var funcLabel = $('<div>');
						funcLabel.text((loco.f[i] && loco.f[i].functionlabel.length) ? loco.f[i].functionlabel : 'F' + i);
						funcLabel.addClass('funcLabel');
						func.append(funcLabel);
						var funcState = $('<div>');
						funcState.addClass('funcState');
						func.append(funcState);
					}
				}
				var icon = loco.iconFilePath.length > 0;
				var image = loco.imageFilePath.length > 0;
				if (icon || image) {
					var locoCell = $('<div>');
					locoCell.addClass('locoCell');
					functionsInner.append(locoCell);
					var img = $('<img>').addClass('imageInitial');
					locoCell.append(img); 
					img[0].onload = function() {
						var o = $(this);
						o.attr('originalHeight', o.height());
						o.attr('originalWidth', o.width());
						resizeImage(o);
					};
					img.attr('src', '/roster/' + encodeURIComponent(loco.name) + '/' + (icon ? 'icon' : 'image') + '?maxHeight=' + $cellHeightRef);
				}
				$locoAddress = '' + loco.dccAddress;
				$jmri.setJMRI('throttle', $locoAddress, {"address":loco.dccAddress});
			} else smoothAlert('Loco \'' + $paramLocoName + '\' doesn\'t exist.\nReopen the web page with a valid loco name.');
			break;
		case 'turnouts':
			$help.push(
				'C - closed' +
				'\nT - thrown' +
				'\n? - undefined' +
				''
			);
			document.title+= ' (turnouts)';
			defineTurnoutsRoutes($jmri.getObjectList('turnouts'));
			break;
		case 'routes':
			$help.push(
				'On - on' +
				'\nOff - off' +
				'\nX - disabled' +
				'\n? - undefined' +
				''
			);
			document.title+= ' (routes)';
			defineTurnoutsRoutes($jmri.getObjectList('routes'));
			break;
		case 'panel':
			$help.push(
				'This shows an' +
				'\ninteractive panel.' +
				''
			);
			document.title+= ' (panel: ' + $paramPanelName + ')';
			var iframeAux = $('<div>').attr('id', 'iframeAux');
			var panel = $('<iframe>').attr('src', '/panel?name=' + $paramPanelName).addClass('panel');
			panel.load(function() {
				var bodyFrameOuter = $('#bodyFrameOuter');
				var bodyFrameInner = $('#bodyFrameInner');
				var panel = $('.panel');
				var panelBody = panel.contents().find('body');
				bodyFrameInner.css('top', 0).css('left', 0);
				panel.css('top', 0).css('left', 0);
				panelBody.css('padding-top', 0).css('padding-bottom', 0);
				panelBody.children('footer').remove();
				panelBody.children('#wrap').children('#panel-area').appendTo(panelBody);
				panelBody.children('#wrap').remove();
				panelBody.children('#panel-area').css('border', 'none').css('position', 'absolute');
				panelBody.css('background-color', $('#iframeAux').css('background-color'))
				setTimeout(function() {$panelLoaded = true; $viewportHeight = 0;}, $resizeCheckInterval * 5);	// Force resize some miliseconds after loading
			});
			iframeAux.css('position', 'absolute').css('background-color', $('body').css('background-color')).css('z-index', '+199');
			iframeAux.css('top', 0).css('left', 0);
			bodyFrameInner.append(iframeAux);
			panel.css('z-index', '+200');
			bodyFrameInner.append(panel);
			break;
	}
	checkLayoutSizeChange();
	$resizeCheckTimer = setInterval(function() {checkLayoutSizeChange();}, $resizeCheckInterval);
	$jmri.getJMRI('power', null);
};

//----------------------------------------- Definition of turnouts or routes
var defineTurnoutsRoutes = function(listTurnoutsRoutes) {
	var bodyFrameInner = $('#bodyFrameInner');
	listTurnoutsRoutes.forEach(function(item) {
		var trCell = $('<div>');
		trCell.addClass('trCell');
		bodyFrameInner.append(trCell);
		trCell.append($('<div>').text(item.data.name + ((item.data.userName) ? ' - ' + item.data.userName : '')).addClass('trName'));
		trCell.append($('<div>').text('').addClass('trStatus').attr('id',encodeId(item.data.name)).attr('state', -1).on('click', function(event) {trChangeStatus(event, item.type, item.data.name);}));
		$jmri.getJMRI(item.type == 'turnout' ? 'turnout' : 'route', item.data.name);
	});
};

//===================================================================================== Resize functions ==============================

//----------------------------------------- Check screen change size and position (control also 'smoothAlert' messages timeout)
var checkLayoutSizeChange = function() {
	checkSmoothAlertEnd();
	var h = $(window).height();
	var w = $(window).width();
	var speedPosition = loadLocalInfo('webThrottle.speedPosition');
	if (speedPosition == 'true' || speedPosition == 'false') if ($speedPosition != (speedPosition == 'true')) changeSpeedPosition(!$speedPosition);
	var movementActive = loadLocalInfo('webThrottle.movementActive');
	if (movementActive == 'true' || movementActive == 'false') if ($movementActive != (movementActive == 'true')) changeMovementActive(!$movementActive);
	var fS = loadLocalInfo('webThrottle.fontSize');
	if ($fontSize != Number(fS)) {
		$fontChanged = true;
		$fontSize = Number(fS);
	} else $fontChanged = false;
	if ($viewportHeight != h) {
		$heightChanged = true;
		$viewportHeight = h;
	} else $heightChanged = false;
	if ($viewportWidth != w) {
		$widthChanged = true;
		$viewportWidth = w;
	} else $widthChanged = false;
	if ($heightChanged || $widthChanged || $fontChanged) {
		$portrait = (w <= h);
		$sizeCtrlPercent = $fontSize / $defaultFontSize;
		$('body').css('font-size', $fontSize);
		resizeHeader();
		switch ($throttleType) {
			case 'roster':
				resizeRosterLayout();
				break;
			case 'loco':
				resizeThrottleLayout();
				break;
			case 'turnouts':
				resizeTurnoutsRoutesLayout();
				break;
			case 'routes':
				resizeTurnoutsRoutesLayout();
				break;
			case 'panel':
				resizePanelLayout();
				break;
		}
		resizeSelectionList();
		resizeSmoothAlerts();
	}
};

//----------------------------------------- [after image loaded and from 'checkLayoutSizeChange()'] Image resize (related to parent)
var resizeImage = function(img) {
	if (!img.length) return;
	img.css('margin', 'auto');
	img.height(img.attr('originalHeight'));
	img.width(img.attr('originalWidth'));
	if (img.parent().height() / img.parent().width() <= img.outerHeight(false) / img.outerWidth(false))  {
		setOuterHeight(img, img.parent().height(), false);
		img.width('auto');
	} else {
		setOuterWidth(img, img.parent().width(), false);
		img.height('auto');
		img.css('margin-top', (img.parent().height() - img.outerHeight(false)) / 2);
	}
	if (img.hasClass('imageInitial')) img.removeClass('imageInitial').addClass('image');
};

//----------------------------------------- [from 'checkLayoutSizeChange()'] Header layout resize when screen or font changes size
var resizeHeader = function() {
	$nextBlockTop = 0;
	var headerWidth = $(window).width();
	var headerHeight = $sizeCtrlPercent * $headerHeightRef;
	var header = $('#header');
//header.on('mousedown mouseup click mouseout mousemove', function(event) {mouse(event);});
//header.on('touchstart touchend touchcancel touchmove', function(event) {touch(event, $(this));});
	header.css('top', $nextBlockTop).css('left', 0);
	setOuterHeight(header, headerHeight, true);
	setOuterWidth(header, headerWidth - (($showScrollBar && $throttleType != 'loco') ? $vScrollbarWidth : 0), true);
	var power = $('#power');
	if (power.length) {
		setOuterHeight(power, header.height(), true);
		setOuterWidth(power, header.height(), true);
		setTopFromParentContent(power, 0);
		setLeftFromParentContent(power, 0);
		var powerText = power.children('.buttonText');
		powerText.css('top', (power.outerHeight(true) - powerText.outerHeight(true)) / 2 - powerText.outerHeight(true) * $fontDelta);
		setLeftFromParentContent(powerText, 0);
		setOuterWidth(powerText, power.width(), true);
	}
	if ($throttleType == 'roster' || $inFrame) {	// Button to define new throttles in frame or not OR Button to remove itself from frame
		var btFrame = (!$inFrame ? $('#toFrame') : $('#removeFrame'));
		setOuterHeight(btFrame, header.height(), true);
		setOuterWidth(btFrame, header.height() * (($throttleType == 'roster') ? 2 : 1), true);
		setTopFromParentContent(btFrame, 0);
		setLeftFromParentContent(btFrame, power.length ? power.outerWidth(true) + header.height() * 0.2 : 0);
		var btFrameText = btFrame.children('.buttonText');
		btFrameText.css('top', (btFrame.outerHeight(true) - btFrameText.outerHeight(true)) / 2 - btFrameText.outerHeight(true) * $fontDelta);
		setLeftFromParentContent(btFrameText, 0);
		setOuterWidth(btFrameText, btFrame.width(), true);
	}
	if ($throttleType == 'loco' && !$pageInFrame) {	// Button to select speed control position
		var speedPosition = $('#speedPosition');
		setOuterHeight(speedPosition, header.height(), true);
		setOuterWidth(speedPosition, header.height(), true);
		setTopFromParentContent(speedPosition, 0);
		setLeftFromParentContent(speedPosition, power.outerWidth(true) + header.height() * 0.2);
		var speedPositionText = speedPosition.children('.buttonText');
		speedPositionText.css('top', (speedPosition.outerHeight(true) - speedPositionText.outerHeight(true)) / 2 - speedPositionText.outerHeight(true) * $fontDelta);
		setLeftFromParentContent(speedPositionText, 0);
		setOuterWidth(speedPositionText, speedPosition.width(), true);
	}
	var help = $('#help');
	setOuterHeight(help, header.height(), true);
	setOuterWidth(help, header.height() * 0.6, true);
	setTopFromParentContent(help, 0);
	setLeftFromParentContent(help, header.width() - help.outerWidth(true));
	var helpText = help.children('.buttonText');
	helpText.css('top', (help.outerHeight(true) - helpText.outerHeight(true)) / 2 - helpText.outerHeight(true) * $fontDelta);
	setLeftFromParentContent(helpText, 0);
	setOuterWidth(helpText, help.width(), true);
	var fontLarger = $('#fontLarger');
	if (fontLarger.length) {
		setOuterHeight(fontLarger, header.height(), true);
		setOuterWidth(fontLarger, header.height() * 0.6, true);
		setTopFromParentContent(fontLarger, 0);
		setLeftFromParentContent(fontLarger, header.width() - fontLarger.outerWidth(true) - help.outerWidth(true));
		var fontLargerText = fontLarger.children('.buttonText');
		fontLargerText.css('top', (fontLarger.outerHeight(true) - fontLargerText.outerHeight(true)) / 2 - fontLargerText.outerHeight(true) * $fontDelta);
		setLeftFromParentContent(fontLargerText, 0);
		setOuterWidth(fontLargerText, fontLarger.width(), true);
	}
	var fontSmaller = $('#fontSmaller');
	if (fontSmaller.length) {
		setOuterHeight(fontSmaller, header.height(), true);
		setOuterWidth(fontSmaller, header.height() * 0.6, true);
		setTopFromParentContent(fontSmaller, 0);
		setLeftFromParentContent(fontSmaller, header.width() - fontSmaller.outerWidth(true) - (fontLarger.length ? fontLarger.outerWidth(true) : 0) - help.outerWidth(true));
		var fontSmallerText = fontSmaller.children('.buttonText');
		fontSmallerText.css('top', (fontSmaller.outerHeight(true) - fontSmallerText.outerHeight(true)) / 2 - fontSmallerText.outerHeight(true) * $fontDelta);
		setLeftFromParentContent(fontSmallerText, 0);
		setOuterWidth(fontSmallerText, fontSmaller.width(), true);
	}
	$nextBlockTop+= header.outerHeight(true);
};

//----------------------------------------- [from 'checkLayoutSizeChange()'] Roster layout resize when screen or font changes size
var resizeRosterLayout = function() {
	var h = $(window).height();
	var w = $(window).width();
	var bodyFrameOuter = $('#bodyFrameOuter');
	var bodyFrameInner = $('#bodyFrameInner');
	var cellWidthCtrl;
	var horizontalCells;
	var cellWidth;
	var cellHeight;
	bodyFrameOuter.css('top', 0).css('left', 0);
	setOuterHeight(bodyFrameOuter, h, true);
	setOuterWidth(bodyFrameOuter, w, true);
	bodyFrameInner.css('top', 0).css('left', 0);
	setOuterWidth(bodyFrameInner, bodyFrameOuter.width() - ($showScrollBar ? $vScrollbarWidth : 0), true);
	var buttonsWidth = bodyFrameInner.width();
	var buttonsHeight = $sizeCtrlPercent * $buttonsHeightRef;
	var buttons = $('#buttons');
	buttons.css('top', $nextBlockTop).css('left', 0);
	setOuterHeight(buttons, buttonsHeight, true);
	setOuterWidth(buttons, buttonsWidth, true);
	var groups = $('#groups');
	setOuterHeight(groups, buttons.height(), true);
	setOuterWidth(groups, buttons.width() / 2, true);
	setTopFromParentContent(groups, 0);
	setLeftFromParentContent(groups, 0);
	var groupsText = groups.children('.buttonText');
	groupsText.css('top', (groups.outerHeight(true) - groupsText.outerHeight(true)) / 2 - groupsText.outerHeight(true) * $fontDelta);
	setLeftFromParentContent(groupsText, 0);
	setOuterWidth(groupsText, groups.width(), true);
	var turnouts = $('#turnouts');
	setOuterHeight(turnouts, buttons.height(), true);
	setOuterWidth(turnouts, buttons.width() / 4, true);
	setTopFromParentContent(turnouts, 0);
	setLeftFromParentContent(turnouts, groups.outerWidth(true));
	var turnoutsText = turnouts.children('.buttonText');
	turnoutsText.css('top', (turnouts.outerHeight(true) - turnoutsText.outerHeight(true)) / 2 - turnoutsText.outerHeight(true) * $fontDelta);
	setLeftFromParentContent(turnoutsText, 0);
	setOuterWidth(turnoutsText, turnouts.width(), true);
	var routes = $('#routes');
	setOuterHeight(routes, buttons.height(), true);
	setOuterWidth(routes, buttons.width() / 4, true);
	setTopFromParentContent(routes, 0);
	setLeftFromParentContent(routes, buttons.width() - routes.outerWidth(true));
	var routesText = routes.children('.buttonText');
	routesText.css('top', (routes.outerHeight(true) - routesText.outerHeight(true)) / 2 - routesText.outerHeight(true) * $fontDelta);
	setLeftFromParentContent(routesText, 0);
	setOuterWidth(routesText, routes.width(), true);
	var t = $nextBlockTop + buttons.outerHeight(true);
	var l = 0;
	if ($isRoster) {	// Roster
		var rosterCell = $('.rosterCell');
		cellWidthCtrl = $sizeCtrlPercent * $cellWidthRef;
		horizontalCells = Math.floor(bodyFrameInner.width() / cellWidthCtrl);
		cellWidth = (horizontalCells == 0) ? bodyFrameInner.width() : bodyFrameInner.width() / horizontalCells;
		var cellHeightIni = $sizeCtrlPercent * $cellHeightRef;
		rosterCell.each(function(index) {
			var o = $(this);
			var locoImageContainer = o.children('.imageContainer');
			if (horizontalCells == 0 && locoImageContainer.length) cellHeight = cellHeightIni * 2; else cellHeight = cellHeightIni;
			o.css('top', t).css('left', l);
			setOuterHeight(o, cellHeight, true);
			setOuterWidth(o, cellWidth, true);
			if (horizontalCells == 0) {
				t+= cellHeight;
				l = 0;
			} else {
				if ((l + cellWidth * 2) > bodyFrameInner.width()) {
					t+= cellHeight;
					l = 0;
				} else l+= cellWidth;
			}
			var locoName = o.children('.locoName');
			var locoRoad = o.children('.locoRoad');
			var locoMfg = o.children('.locoMfg');
			if (locoImageContainer.length) {
				setTopFromParentContent(locoImageContainer, 0);
				setLeftFromParentContent(locoImageContainer, 0);
				if (horizontalCells == 0) {
					setOuterHeight(locoImageContainer, o.height() / 2, true);
					setOuterWidth(locoImageContainer, o.width(), true);
					locoName.css('top', (cellHeight / 2 + (cellHeight / 2 - locoName.outerHeight(true)) / 2 - locoName.outerHeight(true) * $fontDelta) - o.height() / 2 * 0.25);
					locoRoad.css('top', (cellHeight / 2 + (cellHeight / 2 - locoRoad.outerHeight(true)) / 2 - locoRoad.outerHeight(true) * $fontDelta));
					locoMfg.css('top', (cellHeight / 2 + (cellHeight / 2 - locoMfg.outerHeight(true)) / 2 - locoMfg.outerHeight(true) * $fontDelta) + o.height() / 2 * 0.25);
					setLeftFromParentContent(locoName, 0);
					setLeftFromParentContent(locoRoad, 0);
					setLeftFromParentContent(locoMfg, 0);
					setOuterWidth(locoName, o.width(), true);
					setOuterWidth(locoRoad, o.width(), true);
					setOuterWidth(locoMfg, o.width(), true);
				} else {
					setOuterHeight(locoImageContainer, o.height(), true);
					setOuterWidth(locoImageContainer, o.width() / 2, true);
					locoName.css('top', ((cellHeight - locoName.outerHeight(true)) / 2 - locoName.outerHeight(true) * $fontDelta) - o.height() * 0.30);
					locoRoad.css('top', ((cellHeight - locoRoad.outerHeight(true)) / 2 - locoRoad.outerHeight(true) * $fontDelta));
					locoMfg.css('top', ((cellHeight - locoMfg.outerHeight(true)) / 2 - locoMfg.outerHeight(true) * $fontDelta) + o.height() * 0.30);
					setLeftFromParentContent(locoName, locoImageContainer.outerWidth(true));
					setLeftFromParentContent(locoRoad, locoImageContainer.outerWidth(true));
					setLeftFromParentContent(locoMfg, locoImageContainer.outerWidth(true));
					setOuterWidth(locoName, o.width() / 2, true);
					setOuterWidth(locoRoad, o.width() / 2, true);
					setOuterWidth(locoMfg, o.width() / 2, true);
				} 
				resizeImage(locoImageContainer.children('.image'));
			} else {
				locoName.css('top', ((cellHeight - locoName.outerHeight(true)) / 2 - locoName.outerHeight(true) * $fontDelta) - o.height() * 0.25);
				locoRoad.css('top', ((cellHeight - locoRoad.outerHeight(true)) / 2 - locoRoad.outerHeight(true) * $fontDelta));
				locoMfg.css('top', ((cellHeight - locoMfg.outerHeight(true)) / 2 - locoMfg.outerHeight(true) * $fontDelta) + o.height() * 0.25);
				setLeftFromParentContent(locoName, 0);
				setLeftFromParentContent(locoRoad, 0);
				setLeftFromParentContent(locoMfg, 0);
				setOuterWidth(locoName, o.width(), true);
				setOuterWidth(locoRoad, o.width(), true);
				setOuterWidth(locoMfg, o.width(), true);
			}
		});
	} else {	// Panels
		var panelCell = $('.panelCell');
		cellWidthCtrl = $sizeCtrlPercent * $cellWidthRef * 2;
		horizontalCells = Math.floor(bodyFrameInner.width() / cellWidthCtrl) + 1;
		cellWidth = bodyFrameInner.width() / horizontalCells;
		cellHeight = $sizeCtrlPercent * $cellHeightRef * 0.5;
		cellHeight*= 4;
		panelCell.each(function(index) {
			var o = $(this);
			o.css('top', t).css('left', l);
			setOuterHeight(o, cellHeight, true);
			setOuterWidth(o, cellWidth, true);
			if ((l + cellWidth * 2) > bodyFrameInner.width()) {
				t+= cellHeight;
				l = 0;
			} else l+= cellWidth;
			var panelImageContainer = o.children('.imageContainer');
			var panelName = o.children('.panelName');
			setTopFromParentContent(panelImageContainer, 0);
			setLeftFromParentContent(panelImageContainer, 0);
			setOuterHeight(panelImageContainer, o.height() / 4 * 3, true);
			setOuterWidth(panelImageContainer, o.width(), true);
			panelName.css('top', cellHeight / 4 * 3.2);
			setLeftFromParentContent(panelName, 0);
			setOuterWidth(panelName, o.width(), true);
			resizeImage(panelImageContainer.children('.image'));
		});
	}
	if (l != 0) t+= cellHeight;
	setOuterHeight(bodyFrameInner, t, true);
	if (bodyFrameOuter.height() < bodyFrameInner.outerHeight(true) && !$showScrollBar) {
		$showScrollBar = true;
		resizeHeader();
		resizeRosterLayout();
	}
	if (bodyFrameOuter.height() >= bodyFrameInner.outerHeight(true) && $showScrollBar) {
		$showScrollBar = false;
		resizeHeader();
		resizeRosterLayout();
	}
};

//----------------------------------------- [from 'checkLayoutSizeChange()'] Throttle layout resize when screen or font changes size
var resizeThrottleLayout = function() {
	var h = $(window).height();
	var w = $(window).width();
	var bodyFrameOuter = $('#bodyFrameOuter');
	var bodyFrameInner = $('#bodyFrameInner');
	bodyFrameOuter.css('top', 0).css('left', 0);
	setOuterHeight(bodyFrameOuter, h, true);
	setOuterWidth(bodyFrameOuter, w, true);
	bodyFrameInner.css('top', 0).css('left', 0);
	setOuterHeight(bodyFrameInner, bodyFrameOuter.height(), true);
	setOuterWidth(bodyFrameInner, bodyFrameOuter.width(), true);
	var emergencyStop = $('#emergencyStop');
	if (!emergencyStop.length) return;
	if ($pageInFrame) $speedPosition = window.parent.$('#' + encodeId(document.URL)).attr('rightSide');
	if ($speedPosition == 'true' || $speedPosition == 'false') $speedPosition = ($speedPosition == 'true');
	var headerHeight = $sizeCtrlPercent * $headerHeightRef;
	var header = $('#header');
	var fontSmaller = $('#fontSmaller');
	var fontLarger = $('#fontLarger');
	var help = $('#help');
	setOuterHeight(emergencyStop, header.height(), true);
	setOuterWidth(emergencyStop, header.height() * 2, true);
	setTopFromParentContent(emergencyStop, 0);
	setLeftFromParentContent(emergencyStop, header.width() - emergencyStop.outerWidth(true) - (fontSmaller.length ? fontSmaller.outerWidth(true) : 0) - (fontLarger.length ? fontLarger.outerWidth(true) : 0) - help.outerWidth(true));
	var emergencyStopText = emergencyStop.children('.buttonText');
	emergencyStopText.css('top', (emergencyStop.outerHeight(true) - emergencyStopText.outerHeight(true)) / 2 - emergencyStopText.outerHeight(true) * $fontDelta);
	setLeftFromParentContent(emergencyStopText, 0);
	setOuterWidth(emergencyStopText, emergencyStop.width(), true);
	var movementActive = $('#movementActive');
	if (movementActive.length) {
		setOuterHeight(movementActive, header.height(), true);
		setOuterWidth(movementActive, header.height() * 2, true);
		setTopFromParentContent(movementActive, 0);
		setLeftFromParentContent(movementActive, $pageInFrame ? $('#removeFrame').outerWidth(true) + header.height() * 0.2 : $('#power').outerWidth(true) + header.height() * 0.2 + $('#speedPosition').outerWidth(true) + header.height() * 0.2);
		var movementActiveText = movementActive.children('.buttonText');
		movementActiveText.css('top', (movementActive.outerHeight(true) - movementActiveText.outerHeight(true)) / 2 - movementActiveText.outerHeight(true) * $fontDelta);
		setLeftFromParentContent(movementActiveText, 0);
		setOuterWidth(movementActiveText, movementActive.width(), true);
	}
	var speed = $('.speed');
	var speedWidth = $sizeCtrlPercent * $speedWidthRef;
	if (speedWidth < bodyFrameInner.width() / 8) speedWidth = bodyFrameInner.width() / 8;
	speed.css('top', $nextBlockTop).css('left', $speedPosition ? bodyFrameInner.width() - speedWidth : 0);
	setOuterHeight(speed, bodyFrameInner.height() - $nextBlockTop, true);
	setOuterWidth(speed, speedWidth, true);
	var buttonsSize = speed.width() / 2;
	var speedGrid = $('#speedGrid');
	setOuterHeight(speedGrid, speed.height() - buttonsSize, true);
	setOuterWidth(speedGrid, speedWidth, true);
	setTopFromParentContent(speedGrid, 0);
	setLeftFromParentContent(speedGrid, 0);
	var speedLimits = $('#speedLimits');
	setOuterHeight(speedLimits, speedGrid.height() - buttonsSize / 2, true);
	setOuterWidth(speedLimits, speedGrid.width(), true);
	setTopFromParentContent(speedLimits, 0);
	setLeftFromParentContent(speedLimits, 0);
	showSpeed();
	var speedTouch = $('#speedTouch');
	setOuterHeight(speedTouch, speedGrid.height(), true);
	setOuterWidth(speedTouch, speedGrid.width(), true);
	setTopFromParentContent(speedTouch, 0);
	setLeftFromParentContent(speedTouch, 0);
	var reverse = $('#reverse');
	setOuterHeight(reverse, buttonsSize, true);
	setOuterWidth(reverse, buttonsSize, true);
	setTopFromParentContent(reverse, speed.height() - reverse.outerHeight(true));
	setLeftFromParentContent(reverse, 0);
	var reverseText = reverse.children('.buttonText');
	reverseText.css('top', (reverse.outerHeight(true) - reverseText.outerHeight(true)) / 2 - reverseText.outerHeight(true) * $fontDelta);
	setLeftFromParentContent(reverseText, 0);
	setOuterWidth(reverseText, reverse.width(), true);
	var forward = $('#forward');
	setOuterHeight(forward, reverse.outerHeight(true), true);
	setOuterWidth(forward, reverse.outerWidth(true), true);
	setTopFromParentContent(forward, speed.height() - forward.outerHeight(true));
	setLeftFromParentContent(forward, speed.width() - forward.outerWidth(true));
	var forwardText = forward.children('.buttonText');
	forwardText.css('top', (forward.outerHeight(true) - forwardText.outerHeight(true)) / 2 - forwardText.outerHeight(true) * $fontDelta);
	setLeftFromParentContent(forwardText, 0);
	setOuterWidth(forwardText, forward.width(), true);
	var functionsOuter = $('.functionsOuter');
	var functionsInner = $('.functionsInner');
	functionsOuter.css('top', $nextBlockTop).css('left', $speedPosition ? 0 : speedWidth);
	setOuterHeight(functionsOuter, bodyFrameInner.height() - $nextBlockTop, true);
	setOuterWidth(functionsOuter, bodyFrameInner.width() - speedWidth, true);
	functionsInner.css('top', 0).css('left', 0);
	setOuterWidth(functionsInner, functionsOuter.width() - ($showScrollBar ? $vScrollbarWidth : 0), true);
	var cellWidthCtrl = $sizeCtrlPercent * $functionWidthRef;
	var horizontalCells = Math.floor(functionsInner.width() / cellWidthCtrl) + 1;
	var cellWidth = functionsInner.width() / horizontalCells;
	var cellHeight = $sizeCtrlPercent * $functionHeightRef;
	var t = 0;
	var l = 0;
	var locoName = $('.locoName');
	locoName.css('top', t).css('left', l);
	setOuterWidth(locoName, functionsInner.width(), true);
	t+= locoName.outerHeight(true);
	for (var i = 0; i < 29; i++) {
		var func = $('#locoFunction' + i);
		if (func.length) {
			func.css('top', t).css('left', l);
			setOuterHeight(func, cellHeight, true);
			setOuterWidth(func, cellWidth, true);
			if ((l + cellWidth * 2) > functionsInner.width()) {
				t+= cellHeight;
				l = 0;
			} else l+= cellWidth;
			var funcLabel = func.children('.funcLabel');
			funcLabel.css('top', ((cellHeight - funcLabel.outerHeight(true)) / 2 - funcLabel.outerHeight(true) * $fontDelta)).css('left', 0);
			setOuterWidth(funcLabel, func.width(), true);
			var funcState = func.children('.funcState');
			setOuterHeight(funcState, func.height() / 10, true);
			funcState.css('top', func.height() - funcState.outerHeight(true)).css('left', 0);
			setOuterWidth(funcState, func.width(), true);
		}
	}
	if (l != 0) t+= cellHeight;
	var locoHeight = $sizeCtrlPercent * $cellHeightRef;
	var locoCell = $('.locoCell');
	if (locoCell.length) {
		locoCell.css('top', t).css('left', 0);
		setOuterHeight(locoCell, locoHeight, true);
		setOuterWidth(locoCell, functionsInner.width(), true);
		t+= locoCell.outerHeight(true);
		resizeImage(locoCell.children('.image'));
	}
	setOuterHeight(functionsInner, t, true);
	if (functionsOuter.height() < functionsInner.outerHeight(true) && !$showScrollBar) {
		$showScrollBar = true;
		resizeThrottleLayout();
	}
	if (functionsOuter.height() >= functionsInner.outerHeight(true) && $showScrollBar) {
		$showScrollBar = false;
		resizeThrottleLayout();
	}
};

//----------------------------------------- [from 'checkLayoutSizeChange()'] Turnouts and Routes layout resize when screen or font changes size
var resizeTurnoutsRoutesLayout = function() {
	var h = $(window).height();
	var w = $(window).width();
	var bodyFrameOuter = $('#bodyFrameOuter');
	var bodyFrameInner = $('#bodyFrameInner');
	bodyFrameOuter.css('top', 0).css('left', 0);
	setOuterHeight(bodyFrameOuter, h, true);
	setOuterWidth(bodyFrameOuter, w, true);
	bodyFrameInner.css('top', 0).css('left', 0);
	setOuterWidth(bodyFrameInner, bodyFrameOuter.width() - ($showScrollBar ? $vScrollbarWidth : 0), true);
	var trCell = $('.trCell');
	var cellWidthCtrl = $sizeCtrlPercent * $cellWidthRef * 1.5;
	var horizontalCells = Math.floor(bodyFrameInner.width() / cellWidthCtrl) + 1;
	var cellWidth = bodyFrameInner.width() / horizontalCells;
	var cellHeight = $sizeCtrlPercent * $cellHeightRef * 0.5;
	var t = $nextBlockTop;
	var l = 0;
	trCell.each(function(index) {
		var o = $(this);
		o.css('top', t).css('left', l);
		setOuterHeight(o, cellHeight, true);
		setOuterWidth(o, cellWidth, true);
		if ((l + cellWidth * 2) > bodyFrameInner.width()) {
			t+= cellHeight;
			l = 0;
		} else l+= cellWidth;
		var trName = o.children('.trName');
		var trStatus = o.children('.trStatus');
		trName.css('top', ((cellHeight - trName.outerHeight(true)) / 2 - trName.outerHeight(true) * $fontDelta));
		trStatus.css('top', trName.css('top'));
		setOuterWidth(trName, o.width() * 0.8, true);
		setOuterWidth(trStatus, o.width() - trName.outerWidth(true), true);
		setLeftFromParentContent(trName, 0);
		setLeftFromParentContent(trStatus, trName.outerWidth(true));
	});
	if (l != 0) t+= cellHeight;
	setOuterHeight(bodyFrameInner, t, true);
	if (bodyFrameOuter.height() < bodyFrameInner.outerHeight(true) && !$showScrollBar) {
		$showScrollBar = true;
		resizeHeader();
		resizeTurnoutsRoutesLayout();
	}
	if (bodyFrameOuter.height() >= bodyFrameInner.outerHeight(true) && $showScrollBar) {
		$showScrollBar = false;
		resizeHeader();
		resizeTurnoutsRoutesLayout();
	}
};

//----------------------------------------- [from 'checkLayoutSizeChange()'] Panel layout resize when screen or font changes size
var resizePanelLayout = function() {
	if (!$panelLoaded) return;	// If loading not complete, give up! (onload event will force resize)
	$nextBlockTop-= $('#header').outerHeight(true);
	var h = $(window).height();
	var w = $(window).width();
	var bodyFrameOuter = $('#bodyFrameOuter');
	var bodyFrameInner = $('#bodyFrameInner');
	var iframeAux = $('#iframeAux');
	var panel = $('.panel');
	var panelBody = panel.contents().find('body');
	var panelArea = panelBody.children('#panel-area');
	var offsetV = 0;
	var scrollbarV = 0;
	var offsetH = 0;
	var scrollbarH = 0;
	setOuterHeight(bodyFrameOuter, h, true);
	setOuterWidth(bodyFrameOuter, w, true);
	setOuterHeight(bodyFrameInner, bodyFrameOuter.height(), true);
	setOuterWidth(bodyFrameInner, bodyFrameOuter.width(), true);
	setOuterHeight(iframeAux, bodyFrameInner.height(), true);
	setOuterWidth(iframeAux, bodyFrameInner.width(), true);
	setOuterHeight(panel, bodyFrameInner.height(), true);
	setOuterWidth(panel, bodyFrameInner.width(), true);
	if (panel.height() >= panelArea.height()) offsetV = (panel.height() - panelArea.height()) / 2;
	else scrollbarV = $vScrollbarWidth;
	if (panel.width() >= panelArea.width()) offsetH = (panel.width() - panelArea.width()) / 2;
	else scrollbarH = $vScrollbarWidth;
	if (scrollbarV > 0 && scrollbarH > 0) {
		scrollbarV = 0;
		scrollbarH = 0;
	}
	panelArea.css('top', offsetV + scrollbarH);
	panelArea.css('left', offsetH + scrollbarV);
};

//----------------------------------------- [from 'checkLayoutSizeChange()' and 'selectionList'] Selection list layout resize when screen or font changes size
var resizeSelectionList = function() {
	var o = $('.selectionList');
	if (!o.length) return;
	var h = $(window).height();
	var w = o.parent().width();
	o.css('top', (h - o.outerHeight(true)) / 2);
	o.css('left', (w - o.outerWidth(true)) / 2);
};

//----------------------------------------- [from 'checkLayoutSizeChange()' and 'smoothAlert'] Smooth Alerts layout resize when screen or font changes size
var resizeSmoothAlerts = function() {
	var os = $('.smoothAlert');
	var h = $(window).height();
	var w = os.eq(0).parent().width();
	var auxPosition = (os.length - 1) / 2;
	os.each(function(index) {
		var o = $(this);
		o.css('top', (h - o.outerHeight(true)) / 2 - 1.5 * (index - auxPosition) * Number(o.css('border-top-width').split('px')[0]));
		o.css('left', (w - o.outerWidth(true)) / 2 + 1.5 * (index - auxPosition) * Number(o.css('border-left-width').split('px')[0]));
	});
};

//===================================================================================== Actions and Auxiliary functions ===============

//----------------------------------------- Check smooth alert end
var checkSmoothAlertEnd = function() {
	$('.smoothAlert').each(function(index) {
		var o = $(this);
		var sec = o.attr('seconds');
		if (!isNaN(sec)) {
			if (sec <= -1) o.remove();
			else {
				if (sec < 1.5 && !o.hasClass('fadeOut')) o.addClass('fadeOut');
				o.attr('seconds', sec - $resizeCheckInterval / 1000);
			}
		}
	});
};

//----------------------------------------- Change power state
var changePowerState = function() {
	var lastState = $('#power').attr('state');
	switch (Number(lastState)) {
		case $jmri.powerOFF:
			$jmri.setJMRI('power', null, {"state":$jmri.powerON});
			break;
		case $jmri.powerON:
			$jmri.setJMRI('power', null, {"state":$jmri.powerOFF});
			break;
		default:
			$jmri.setJMRI('power', null, {"state":$jmri.powerON});
			break;
	}
};

//----------------------------------------- Remove frame
var removeFrame = function() {
	removeFromFrameList(document.URL);
	window.open(document.URL.split('?')[0], '_top');
	return;
};

//----------------------------------------- Load frames list {$frameList[]}
var loadFrameList = function() {
	var l = loadLocalInfo('webThrottle.frameListSize');
	if (l && !isNaN(l) && Number(l) >= 0 && Number(l) == Math.abs(l)) l = Number(l); else saveLocalInfo('webThrottle.frameListSize', l = 0);
	for (var i = 0; i < l; i++) $frameList[i] = loadLocalInfo('webThrottle.frameList[' + i + ']');
};

//----------------------------------------- Save frames list {$frameList[]}
var saveFrameList = function() {
	saveLocalInfo('webThrottle.frameListSize', $frameList.length);
	for (var i = 0; i < $frameList.length; i++) saveLocalInfo('webThrottle.frameList[' + i + ']', $frameList[i]);
};

//----------------------------------------- Add URL to frames list {$frameList[]}
var addToFrameList = function(url) {
	loadFrameList();
	var find = false;
	for (var i = 0; i < $frameList.length; i++) {
		if ($frameList[i] == url) {
			find = true;
			break;
		}
	}
	if (!find) {
		$frameList.push(url);
		saveFrameList();
	}
	return !find;
};

//----------------------------------------- Replace URL in frames list {$frameList[]}
var replaceInFrameList = function(urlOld, urlNew) {
	loadFrameList();
	for (var i = 0; i < $frameList.length; i++) if ($frameList[i] == urlNew) return false;
	for (var i = 0; i < $frameList.length; i++) {
		if ($frameList[i] == urlOld) {
			$frameList[i] = urlNew;
			break;
		}
	}
	saveFrameList();
	return true;
};

//----------------------------------------- Remove URL from frames list {$frameList[]}
var removeFromFrameList = function(url) {
	loadFrameList();
	var item = -1;
	for (var i = 0; i < $frameList.length; i++) {
		if ($frameList[i] == url) {
			item = i;
			break;
		}
	}
	if (item >= 0) {
		$frameList.splice(item, 1);
		saveFrameList();
	}
};

//----------------------------------------- Show speed bar
var showSpeed = function() {
	var speed = $('.speed');
	var speedLimits = $('#speedLimits');
	var speedBar = $('#speedBar');
	var speedValue = Number(speed.attr('speed'));
	if (speedValue < 0) speedValue = 0;
	var speedOffset = speedLimits.height() * speedValue;
	if (speedValue > 0 && speedOffset < 1) speedOffset = 1;
	speedBar.height(speedOffset);
	speedBar.width(speedLimits.width());
	setTopFromParentContent(speedBar, speedLimits.height() - speedOffset);
	setLeftFromParentContent(speedBar, 0);
};

//===================================================================================== Responses from server =========================

//----------------------------------------- [from server] Layout power state
var layoutPowerState = function(state) {
	var power = $('#power');
	power.attr('state', state);
	power.removeClass('powerUnknown powerOff powerOn');
	switch (state) {
		case $jmri.powerOFF:
			power.addClass('powerOff');
			break;
		case $jmri.powerON:
			power.addClass('powerOn');
			break;
		default:
			power.addClass('powerUnknown');
			break;
	}
};

//----------------------------------------- [from server] Last selected throttle address
var throttleState = function(name, address, speed, forward, fs) {
	if (name != $locoAddress) return;
	$('body').attr('locoReady', 'true');
	if (forward != undefined) throttleDirection(name, forward);
	if (speed != undefined) throttleSpeed(name, speed);
	for (var i = 0; i < 29; i++) if (fs[i] != undefined) throttleFunctionState(name, i, fs[i]);
};

//----------------------------------------- Throttle direction
var throttleDirection = function(name, forward) {
	if (name != $locoAddress) return;
	var _reverse = $('#reverse');
	var _forward = $('#forward');
	_reverse.removeClass('directionActive directionInactive');
	_forward.removeClass('directionActive directionInactive');
	if (forward == $jmri.TRUE) {
		_reverse.addClass('directionInactive');
		_forward.addClass('directionActive');
	} else {
		_reverse.addClass('directionActive');
		_forward.addClass('directionInactive');
	}
};

//----------------------------------------- Throttle speed
var throttleSpeed = function(name, speed) {
	if (name != $locoAddress) return;
	$speedAux = speed;
	if (!$speedFeedback) return;
	$('.speed').attr('speed', speed);
	showSpeed();
};

//----------------------------------------- Throttle function state
var throttleFunctionState = function(name, functionNumber, active) {
	if (name != $locoAddress) return;
	var func = $('#locoFunction' + functionNumber);
	if (func.length) {
		func.attr('state', active);
		var funcState = func.children('.funcState');
		funcState.removeClass('funcOff funcOn');
		if (active == $jmri.TRUE) funcState.addClass('funcOn');
		else funcState.addClass('funcOff');
	}
};

//----------------------------------------- [from server] Layout turnout state
var layoutTurnoutState = function(name, userName, comment, inverted, state) {
	var o = $('#' + encodeId(name));
	o.attr('state', state);
	o.removeClass('turnoutUndefined turnoutThrown turnoutClosed');
	switch (state) {
		case $jmri.turnoutTHROWN:
			o.text('T').addClass('turnoutThrown');
			break;
		case $jmri.turnoutCLOSED:
			o.text('C').addClass('turnoutClosed');
			break;
		default:
			o.text('?').addClass('turnoutUndefined');
			break;
	}
};

//----------------------------------------- [from server] Layout route state
var layoutRouteState = function(name, userName, comment, state) {
	var o = $('#' + encodeId(name));
	o.attr('state', state);
	o.removeClass('routeUndefined routeDisabled routeInactive routeActive');
	switch (state) {
		case $jmri.routeINACTIVE:
			o.text('off').addClass('routeInactive');
			break;
		case $jmri.routeACTIVE:
			o.text('on').addClass('routeActive');
			break;
		case $jmri.routeDISABLED:
			o.text('X').addClass('routeDisabled');
			break;
		default:
			o.text('?').addClass('routeUndefined');
			break;
	}
};

//===================================================================================== Touch and Mouse ===============================

//----------------------------------------- Test event (touch)
var touch = function(_e, o) {
	var e = _e.originalEvent;
	if (e.type != 'touchmove') smoothAlert(
		'*** Touch ***' +
		'\nObject: ' + o.attr('id') + 
		'\nEvent type: ' + e.type + 
		'\nX: ' + e.changedTouches[0].pageX + 
		'\nY: ' + e.changedTouches[0].pageY
	);
	e.stopPropagation();
	e.preventDefault();
};

//----------------------------------------- Test event (mouse)
var mouse = function(e) {
	var o = $(e.currentTarget);
	if (e.type != 'mousemove') smoothAlert(
		'*** Mouse ***' +
		'\nObject: ' + o.attr('id') + 
		'\nEvent type: ' + e.type + 
		'\nButton: ' + ((e.which) ? e.which : e.button) + 
		'\nX: ' + e.pageX + 
		'\nY: ' + e.pageY
	);
	e.stopPropagation();
	e.preventDefault();
};

//----------------------------------------- Left button pressed ? (mouse)
var isLeftButton = function(e) {
	var r = false;
	if (e.which) if (e.which == 1) r = true;
	if (e.button) if (e.button == 0) r = true;
	return r;
};

//----------------------------------------- Power button pressed (touch)
var powerButtonPressedTouch = function(e) {powerButtonPressed(e);};

//----------------------------------------- Power button pressed (mouse)
var powerButtonPressedMouse = function(e) {if (isLeftButton(e)) powerButtonPressed(e);};

//----------------------------------------- Power button pressed
var powerButtonPressed = function(e) {
	e.preventDefault();
	e.stopImmediatePropagation();
	$powerDelayTimer = setTimeout(function() {changePowerState();}, $buttonDelayTimeout);
};

//----------------------------------------- Power button released (touch)
var powerButtonReleasedTouch = function(e) {powerButtonReleased(e);};

//----------------------------------------- Power button released (mouse)
var powerButtonReleasedMouse = function(e) {if (isLeftButton(e)) powerButtonReleased(e);};

//----------------------------------------- Power button released
var powerButtonReleased = function(e) {
	e.preventDefault();
	e.stopImmediatePropagation();
	clearInterval($powerDelayTimer);
};

//----------------------------------------- Power button released or canceled (mouse)
var powerButtonCanceledMouse = function(e) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	var o = $(e.currentTarget);
	var mouseY = e.pageY;
	var mouseX = e.pageX;
	var oTop = o.offset().top;
	var oLeft = o.offset().left;
	var oBottom = oTop + o.outerHeight(false);
	var oRight = oLeft + o.outerWidth(false);
	if (mouseY > oTop && mouseY < oBottom && mouseX > oLeft && mouseX < oRight) return;
	else clearInterval($powerDelayTimer);
};

//----------------------------------------- Remove button pressed (touch)
var removeButtonPressedTouch = function(e) {removeButtonPressed(e);};

//----------------------------------------- Remove button pressed (mouse)
var removeButtonPressedMouse = function(e) {if (isLeftButton(e)) removeButtonPressed(e);};

//----------------------------------------- Remove button pressed
var removeButtonPressed = function(e) {
	e.preventDefault();
	e.stopImmediatePropagation();
	$removeDelayTimer = setTimeout(function() {removeFrame();}, $buttonDelayTimeout);
};

//----------------------------------------- Remove button released (touch)
var removeButtonReleasedTouch = function(e) {removeButtonReleased(e);};

//----------------------------------------- Remove button released (mouse)
var removeButtonReleasedMouse = function(e) {if (isLeftButton(e)) removeButtonReleased(e);};

//----------------------------------------- Remove button released
var removeButtonReleased = function(e) {
	e.preventDefault();
	e.stopImmediatePropagation();
	clearInterval($removeDelayTimer);
};

//----------------------------------------- Remove button released or canceled (mouse)
var removeButtonCanceledMouse = function(e) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	var o = $(e.currentTarget);
	var mouseY = e.pageY;
	var mouseX = e.pageX;
	var oTop = o.offset().top;
	var oLeft = o.offset().left;
	var oBottom = oTop + o.outerHeight(false);
	var oRight = oLeft + o.outerWidth(false);
	if (mouseY > oTop && mouseY < oBottom && mouseX > oLeft && mouseX < oRight) return;
	else clearInterval($removeDelayTimer);
};

//----------------------------------------- Change font size - Click (mouse and touch with simulation)
var fontSizeChange = function(e, increment) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	var fontSize = $fontSize;
	fontSize+= increment;
	if (fontSize < $fontSizeMin) fontSize = $fontSizeMin;
	if (fontSize > $fontSizeMax) fontSize = $fontSizeMax;
	saveLocalInfo('webThrottle.fontSize', fontSize);
	smoothAlert('text size: ' + fontSize + 'px', 2);
};

//----------------------------------------- Show help - Click (mouse and touch with simulation)
var helpShow = function(e) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	$help.forEach(function(entry) {smoothAlert(entry);});
};

//----------------------------------------- Choose roster group - Click (mouse and touch with simulation)
var chooseGroup = function(e) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	if (!$rosterGroups.length || $rosterGroups[0] != 'Complete roster') $rosterGroups.unshift('Complete roster');
	if ($rosterGroups[$rosterGroups.length - 1] != '[Panels]') {
		$rosterGroups.push('');
		$rosterGroups.push('[Panels]');
	}
	var f = function(i) {
		if (($rosterGroups[i] != $rosterGroup && i != 0) ||  (i == 0 && $rosterGroup != '')) {
			if (i == 0) saveLocalInfo('webThrottle.rosterGroup', '');
			else saveLocalInfo('webThrottle.rosterGroup', $rosterGroups[i]);
			window.open(document.URL.split('?')[0], '_self');
		}
	};
	var ai = 0;
	for (var i = 0; i < $rosterGroups.length; i++) if ($rosterGroups[i] != '' && $rosterGroups[i] == $rosterGroup) ai = i;
	selectionList($rosterGroups, 'Roster groups', ai, f);
};

//----------------------------------------- Show turnouts - Click (mouse and touch with simulation)
var turnoutsShow = function(e) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	var url = document.URL.split('?')[0] + '?turnouts';
	if ($toFrame) {
		if (addToFrameList(url + '&inframe')) window.open(document.URL.split('?')[0], $pageInFrame ? '_top' : '_self');
	} else window.open(url, url);
};

//----------------------------------------- Show routes - Click (mouse and touch with simulation)
var routesShow = function(e) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	var url = document.URL.split('?')[0] + '?routes';
	if ($toFrame) {
		if (addToFrameList(url + '&inframe')) window.open(document.URL.split('?')[0], $pageInFrame ? '_top' : '_self');
	} else window.open(url, url);
};

//----------------------------------------- Show loco throttle - Click (mouse and touch with simulation)
var throttleShow = function(e, locoId) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	var url = document.URL.split('?')[0] + '?loconame=' + encodeURIComponent(locoId);
	if ($toFrame) {
		if (addToFrameList(url + '&inframe')) window.open(document.URL.split('?')[0], $pageInFrame ? '_top' : '_self');
	} else window.open(url, url);
};

//----------------------------------------- Change turnout or route status - Click (mouse and touch with simulation)
var trChangeStatus = function(e, type, name) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	var lastState = Number($('#' + encodeId(name)).attr('state'));
	if (type == 'turnout') {	// 0(undefined) 1(unknown) 2(Closed) 4(Thrown)
		switch (lastState) {
			case $jmri.turnoutTHROWN:
				$jmri.setJMRI('turnout', name, {"state":$jmri.turnoutCLOSED});
				break;
			case $jmri.turnoutCLOSED:
				$jmri.setJMRI('turnout', name, {"state":$jmri.turnoutTHROWN});
				break;
			default:
				$jmri.setJMRI('turnout', name, {"state":$jmri.turnoutCLOSED});
				break;
		}
	} else {	// 0(unknown) 2(Active) 4(Inactive) 8(inconsistent) - Can only activate
		$jmri.setJMRI('route', name, {"state":$jmri.routeACTIVE});
	}
};

//----------------------------------------- Open panel - Click (mouse and touch with simulation)
var openPanel = function(e, name, URL) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	// URL not used for now
	var url = document.URL.split('?')[0] + '?panelname=' + encodeURIComponent(name);
	if ($toFrame) {
		if (addToFrameList(url + '&inframe')) window.open(document.URL.split('?')[0], $pageInFrame ? '_top' : '_self');
	} else window.open(url, url);
};

//----------------------------------------- Select item from list - Click (mouse and touch with simulation)
var itemSelected = function(e, f) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	var o = $(e.currentTarget);
	var i = o.attr('seq');
	o.parent().remove();
	if (i >= 0) f(i);
};

//----------------------------------------- Remove alert message - Click (mouse and touch with simulation)
var removeSmoothAlertMouse = function(e) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	var o = $(e.currentTarget);
	if (o.attr('preventClick') !== undefined) return;
	o.remove();
};

//----------------------------------------- Initial position for moving object (touch)
var startMoveTouch = function(e, o) {startMove(e, o, e.targetTouches[0]);};

//----------------------------------------- Initial position for moving object (mouse)
var startMoveMouse = function(e) {if (isLeftButton(e)) startMove(e, $(e.currentTarget), e);};

//----------------------------------------- Initial position for moving object
var startMove = function(e, o, t) {
	e.preventDefault();
	e.stopImmediatePropagation();
	o.removeAttr('preventClick');
	o.attr('y', t.pageY);
	o.attr('x', t.pageX);
	o.attr('cursor', o.css('cursor'));
	o.css('cursor', 'move');
};

//----------------------------------------- Several steps in moving object (touch)
var movingTouch = function(e, o) {moving(e, o, e.targetTouches[0]);};

//----------------------------------------- Several steps in moving object (mouse)
var movingMouse = function(e) {if (isLeftButton(e)) moving(e, $(e.currentTarget), e);};

//----------------------------------------- Several steps in moving object
var moving = function(e, o, t) {
	e.preventDefault();
	e.stopImmediatePropagation();
	if (o.attr('y') === undefined || o.attr('x') === undefined || o.attr('cursor') === undefined) return;
	o.attr('preventClick', '');
	var deltaY = t.pageY - o.attr('y');
	var deltaX = t.pageX - o.attr('x');
	o.css('top', Number(o.css('top').split('px')[0]) + deltaY);
	o.css('left', Number(o.css('left').split('px')[0]) + deltaX);
	o.attr('y', t.pageY);
	o.attr('x', t.pageX);
};

//----------------------------------------- Final position for moving object (touch)
var stopMoveTouch = function(e, o) {stopMove(e, o);};

//----------------------------------------- Final position for moving object (mouse)
var stopMoveMouse = function(e) {if (isLeftButton(e)) stopMove(e, $(e.currentTarget));};

//----------------------------------------- Final position for moving object
var stopMove = function(e, o) {
	e.preventDefault();
	e.stopImmediatePropagation();
	if (o.attr('preventClick') !== undefined) {
		o.css('cursor', o.attr('cursor'));
		o.removeAttr('y');
		o.removeAttr('x');
		o.removeAttr('cursor');
	} else o.remove();
};

//----------------------------------------- Final position for moving object, if out of parent (mouse)
var stopMoveConditionallyMouse = function(e) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	var o = $(e.currentTarget);
	var mouseY = e.pageY;
	var mouseX = e.pageX;
	var oTop = o.offset().top;
	var oLeft = o.offset().left;
	var oBottom = oTop + o.outerHeight(false);
	var oRight = oLeft + o.outerWidth(false);
	if (mouseY > oTop && mouseY < oBottom && mouseX > oLeft && mouseX < oRight) return;
	o.css('cursor', o.attr('cursor'));
	o.removeAttr('y');
	o.removeAttr('x');
	o.removeAttr('cursor');
};

//----------------------------------------- Manage new throttles - Click (mouse and touch with simulation)
var manageNewThrottles = function(e) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	var toFrame = $('#toFrame');
	var toFrameText = toFrame.children('.buttonText');
	if ($toFrame) {
		$toFrame = false;
		toFrameText.text('single');
	} else {
		$toFrame = true;
		toFrameText.text('multi');
	}
	var bc = toFrame.css('background-color');
	toFrame.css('background-color', toFrame.css('color'));
	toFrame.css('color', bc);
};

//----------------------------------------- Manage speed control position - Click (mouse and touch with simulation)
var manageSpeedPosition = function(e) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	changeSpeedPosition(!$speedPosition);
	saveLocalInfo('webThrottle.speedPosition', $speedPosition);
};

//----------------------------------------- Change speed control position
var changeSpeedPosition = function(speedPos) {
	var speedPosition = $('#speedPosition');
	if (!speedPosition.length) return;
	var speedPositionText = speedPosition.children('.buttonText');
	if (!speedPos) {
		$speedPosition = false;
		speedPositionText.text('|::');
	} else {
		$speedPosition = true;
		speedPositionText.text('::|');
	}
	$viewportHeight = 0;	// Force rezise
};

//----------------------------------------- Send immediate STOP - Click (mouse and touch with simulation)
var immediateStop = function(e) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	if ($('body').attr('locoReady') != 'true') return;
	$jmri.setJMRI('throttle', $locoAddress, {"speed":$jmri.EMERGENCY_STOP});
};

//----------------------------------------- Select another loco - Click (mouse and touch with simulation)
var changeLoco = function(e) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	var o = $(e.currentTarget);
	$locoList = [];
	var rg = loadLocalInfo('webThrottle.rosterGroup');
	if (rg || rg == '') $rosterGroup = rg; else saveLocalInfo('webThrottle.rosterGroup', $rosterGroup);
	if ($rosterGroup == '[Panels]') $rosterGroup = '';
	var roster = $jmri.getRoster($rosterGroup);
	if (roster.length) roster.forEach(function(loco) {$locoList.push(loco.name + ' (' + loco.dccAddress + ')');});
	var f = function(i) {
		var url = document.URL.split('?')[0] + '?loconame=' + encodeURIComponent($locoList[i].substring(0, $locoList[i].lastIndexOf(' ')));
		if ($inFrame) {
			if (replaceInFrameList(document.URL, url + '&inframe')) window.parent.$('#' + encodeId(document.URL)).attr('src', url + '&inframe').attr('id', encodeId(url + '&inframe'));
		}
		else if (document.URL != url) window.location = url;
	};
	var ai = 0;
	for (var i = 0; i < $locoList.length; i++) if ($locoList[i] == $('#locoName').text()) ai = i;
	selectionList($locoList, 'Locos', ai, f);
};

//----------------------------------------- Set direction - Click (mouse and touch with simulation)
var setDirection = function(e, forward) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	if ($('body').attr('locoReady') != 'true') return;
	$jmri.setJMRI('throttle', $locoAddress, {"forward": forward ? $jmri.TRUE : $jmri.FALSE});
};

//----------------------------------------- Manage tilt to control speed or not - Click (mouse and touch with simulation)
var manageMovementActive = function(e) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	changeMovementActive(!$movementActive);
	saveLocalInfo('webThrottle.movementActive', $movementActive);
};

//----------------------------------------- Change tilt to control speed or not
var changeMovementActive = function(movActive) {
	var movementActive = $('#movementActive');
	if (!movementActive.length) return;
	var movementActiveText = movementActive.children('.buttonText');
	var speedTouch = $('#speedTouch');
	speedTouch.off('touchstart');
	speedTouch.off('touchmove');
	speedTouch.off('touchend');
	speedTouch.off('mousedown');
	speedTouch.off('mousemove');
	speedTouch.off('mouseup');
	speedTouch.off('mouseout');
	if (!movActive) {
		$movementActive = false;
		movementActiveText.text('normal');
		if ($hasTouch) {
			speedTouch.on('touchstart', function(event) {speedPressedTouch(event.originalEvent);});
			speedTouch.on('touchmove', function(event) {speedMovingTouch(event.originalEvent);});
			speedTouch.on('touchend', function(event) {speedReleasedTouch(event.originalEvent);});
		} else {
			speedTouch.on('mousedown', function(event) {speedPressedMouse(event);});
			speedTouch.on('mousemove', function(event) {speedMovingMouse(event);});
			speedTouch.on('mouseup', function(event) {speedReleasedMouse(event);});
			speedTouch.on('mouseout', function(event) {speedCanceledMouse(event);});
		}
	} else {
		$movementActive = true;
		movementActiveText.text('tilt');
		speedTouch.on('touchstart', function(event) {speedPressedTiltTouch(event.originalEvent);});
		speedTouch.on('touchend', function(event) {speedReleasedTiltTouch(event.originalEvent);});
	}
};

//----------------------------------------- Orientation changed (device)
var deviceOrientation = function(e) {
	if ($('body').attr('locoReady') != 'true') return;
	if ($movementActive && $movementOn) {
		if ($orientation != window.top.orientation) $movementTilt = null;
		$orientation = window.top.orientation;
		var m;
		switch ($orientation) {
			case 0:
				m = e.beta;
				break;
			case 90:
				m = -e.gamma;
				break;
			case -90:
				m = e.gamma;
				break;
			default:
				m = -e.beta;
				break;
		}
		m = Math.round(m * $speedStep * 2);	// Sensitivity (for $speedStep=10%): 5º
		if ($movementTilt != m) {
			if ($movementTilt != null) {
				$movementCtrl = m - $movementTilt;
				var speed = $('.speed');
				var speedValue = Number(speed.attr('speed'));
				if (speedValue < 0) speedValue = 0;
				speedValue+= ($movementCtrl * $speedStep / 2);	// 100º <=> full cursor displacement
				if (speedValue < 0) speedValue = 0;
				if (speedValue > 1) speedValue = 1;
				speed.attr('speed', speedValue);
				showSpeed();
				var speedValueFormated = '' + speedValue;
				if (speedValue == 0) speedValueFormated = $jmri.STOP;
				if (speedValue == 1) speedValueFormated = $jmri.FULL_SPEED;
				$jmri.setJMRI('throttle', $locoAddress, {"speed":speedValueFormated});
			}
			$movementTilt = m;
		}
	}
}

//----------------------------------------- Speed zone pressed - tilt (touch)
var speedPressedTiltTouch = function(e) {
	e.preventDefault();
	e.stopImmediatePropagation();
	if ($('body').attr('locoReady') != 'true') return;
	$movementTilt = null;
	$movementOn = true;
	$speedFeedback = false;
};

//----------------------------------------- Speed zone released - tilt (touch)
var speedReleasedTiltTouch = function(e) {
	e.preventDefault();
	e.stopImmediatePropagation();
	if ($('body').attr('locoReady') != 'true') return;
	$movementOn = false;
	$speedFeedback = true;
	throttleSpeed($locoAddress, $speedAux);
};

//----------------------------------------- Speed zone pressed (touch)
var speedPressedTouch = function(e) {speedPressed(e, e.targetTouches[0]);};

//----------------------------------------- Speed zone pressed (mouse)
var speedPressedMouse = function(e) {if (isLeftButton(e)) speedPressed(e, e);};

//----------------------------------------- Speed zone pressed
var speedPressed = function(e, t) {
	e.preventDefault();
	e.stopImmediatePropagation();
	if ($('body').attr('locoReady') != 'true') return;
	$movementCtrl = t.pageY;
	functionForSpeedCtrl();
	$speedTimer = setInterval(function() {functionForSpeedCtrl();}, $speedInterval);
	$speedFeedback = false;
};

//----------------------------------------- Function for speed control
var functionForSpeedCtrl = function() {
	var speed = $('.speed');
	var speedValue = Number(speed.attr('speed'));
	if (speedValue < 0) speedValue = 0;
	var speedLimitsHeight = $('#speedLimits').height();
	var speedBarValue = $('#speedBar').offset().top;
	var increment = (speedBarValue - $movementCtrl) / speedLimitsHeight;
	var maxIncrement = speedLimitsHeight * $speedStep;
	speedValue+= (increment > $speedStep ? $speedStep : (increment < $speedStep * -1 ? $speedStep * -1 : increment));
	if (speedValue < 0) speedValue = 0;
	if (speedValue > 1) speedValue = 1;
	speed.attr('speed', speedValue);
	showSpeed();
	var speedValueFormated = '' + speedValue;
	if (speedValue == 0) speedValueFormated = $jmri.STOP;
	if (speedValue == 1) speedValueFormated = $jmri.FULL_SPEED;
	$jmri.setJMRI('throttle', $locoAddress, {"speed":speedValueFormated});
};

//----------------------------------------- Speed zone moving (touch)
var speedMovingTouch = function(e) {speedMoving(e, e.targetTouches[0]);};

//----------------------------------------- Speed zone moving (mouse)
var speedMovingMouse = function(e) {if (isLeftButton(e)) speedMoving(e, e);};

//----------------------------------------- Speed zone moving
var speedMoving = function(e, t) {
	e.preventDefault();
	e.stopImmediatePropagation();
	if ($('body').attr('locoReady') != 'true') return;
	$movementCtrl = t.pageY;
};

//----------------------------------------- Speed zone released (touch)
var speedReleasedTouch = function(e) {speedReleased(e);};

//----------------------------------------- Speed zone released (mouse)
var speedReleasedMouse = function(e) {if (isLeftButton(e)) speedReleased(e);};

//----------------------------------------- Speed zone canceled (mouse)
var speedCanceledMouse = function(e) {if (isLeftButton(e)) speedReleased(e);};

//----------------------------------------- Speed zone released or canceled
var speedReleased = function(e) {
	e.preventDefault();
	e.stopImmediatePropagation();
	if ($('body').attr('locoReady') != 'true') return;
	if ($speedTimer) {
		clearInterval($speedTimer);
		$speedTimer = null;
	}
	$speedFeedback = true;
	throttleSpeed($locoAddress, $speedAux);
};

//----------------------------------------- Function button click - Click (mouse and touch with simulation)
var functionClick = function(e) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	if ($('body').attr('locoReady') != 'true') return;
	var o = $(e.currentTarget);
	var lastState = o.attr('state');
	var propName = 'F' + o.attr('id').substr(12);	// id=locoFunction<n>
	var obj  = {};
	obj[propName] = (lastState == '' + $jmri.TRUE) ? $jmri.FALSE : $jmri.TRUE;
	$jmri.setJMRI('throttle', $locoAddress, obj);
};

//----------------------------------------- Function button pressed (touch)
var functionPressedTouch = function(e, o) {functionPressed(e, o);};

//----------------------------------------- Function button pressed (mouse)
var functionPressedMouse = function(e) {if (isLeftButton(e)) functionPressed(e, $(e.currentTarget));};

//----------------------------------------- Function button pressed
var functionPressed = function(e, o) {
	e.preventDefault();
	e.stopImmediatePropagation();
	if ($('body').attr('locoReady') != 'true') return;
	var propName = 'F' + o.attr('id').substr(12);	// id=locoFunction<n>
	var obj  = {};
	obj[propName] = $jmri.TRUE;
	$jmri.setJMRI('throttle', $locoAddress, obj);
};

//----------------------------------------- Function button released (touch)
var functionReleasedTouch = function(e, o) {functionReleased(e, o);};

//----------------------------------------- Function button released (mouse)
var functionReleasedMouse = function(e) {if (isLeftButton(e)) functionReleased(e, $(e.currentTarget));};

//----------------------------------------- Function button released
var functionReleased = function(e, o) {
	e.preventDefault();
	e.stopImmediatePropagation();
	if ($('body').attr('locoReady') != 'true') return;
	var propName = 'F' + o.attr('id').substr(12);	// id=locoFunction<n>
	var obj  = {};
	obj[propName] = $jmri.FALSE;
	$jmri.setJMRI('throttle', $locoAddress, obj);
};

//----------------------------------------- Function button released or canceled (mouse)
var functionCanceledMouse = function(e) {
	if (!isLeftButton(e)) return;
	e.preventDefault();
	e.stopImmediatePropagation();
	var o = $(e.currentTarget);
	var mouseY = e.pageY;
	var mouseX = e.pageX;
	var oTop = o.offset().top;
	var oLeft = o.offset().left;
	var oBottom = oTop + o.outerHeight(false);
	var oRight = oLeft + o.outerWidth(false);
	if (mouseY > oTop && mouseY < oBottom && mouseX > oLeft && mouseX < oRight) return;
	if ($('body').attr('locoReady') != 'true') return;
	var propName = 'F' + o.attr('id').substr(12);	// id=locoFunction<n>
	var obj  = {};
	obj[propName] = $jmri.FALSE;
	$jmri.setJMRI('throttle', $locoAddress, obj);
};

//===================================================================================== Generic functions =============================

//----------------------------------------- Debug print
// Use the following lines anywhere logs or alerts are needed:
// $debug && window.console && console.log(anything);
// $debug && window.console && console.log(new Date() + ' - ' + document.title + '\n' + text);
// $debug && alert(text);
// $debug && alert(new Date() + '\n\n' + document.title + '\n\n' + text);

//----------------------------------------- Get URL parameters (parameters are not case sensitive so, 'key' must be compared in lowercase)
var getUrlParameters = function() {			//Item 'undefined' if index not found
	var vars = [], hash, key;
	var hashes;
	var auxInt = window.location.href.indexOf('?');
	if (auxInt < 0) return vars;
	hashes = window.location.href.slice(auxInt + 1).split('&');
	for (var i = 0; i < hashes.length; i++) {
		if (hashes[i].indexOf('=') == -1) hashes[i]+= '=';
		hash = hashes[i].split('=');
		key = hash[0].toLowerCase();
		vars.push(decodeURIComponent(key));
		vars[key] = decodeURIComponent(hash[1]).replace(/\+/g, " "); //undo server query string space replacements
	}
	return vars;
};

//----------------------------------------- Save local info ('key' is case sensitive)
var	saveLocalInfo = function(key, value) {
	var _key = encodeURIComponent(key);
	var _value = encodeURIComponent(value);
	localStorage[_key] = _value;
};

//----------------------------------------- Load local info ('key' is case sensitive)
var	loadLocalInfo = function(key) {			//Return 'undefined' if not found
	var _key = encodeURIComponent(key);
	var value;
	value = localStorage[_key];
	return ((value === undefined || value === null) ? (function() {})() : decodeURIComponent(value));
};

//----------------------------------------- Remove local info ('key' is case sensitive)
var	removeLocalInfo = function(key) {
		var _key = encodeURIComponent(key);
		localStorage.removeItem(_key);
};

//----------------------------------------- Set top related to parent content
var setTopFromParentContent = function(o, px) {
	var p = Number(o.parent().css('padding-top').split('px')[0]);
	o.css('top', px + p);
};

//----------------------------------------- Set left related to parent content
var setLeftFromParentContent = function(o, px) {
	var p = Number(o.parent().css('padding-left').split('px')[0]);
	o.css('left', px + p);
};

//----------------------------------------- Set outer height
var setOuterHeight = function(o, px, includeMargin) {
	var p = includeMargin ? (Number(o.css('margin-top').split('px')[0]) + Number(o.css('margin-bottom').split('px')[0])) : 0;
	p+= Number(o.css('border-top-width').split('px')[0]) + Number(o.css('border-bottom-width').split('px')[0]);
	p+= Number(o.css('padding-top').split('px')[0]) + Number(o.css('padding-bottom').split('px')[0]);
	o.height(px - p);
};

//----------------------------------------- Set outer width
var setOuterWidth = function(o, px, includeMargin) {
	var p = includeMargin ? (Number(o.css('margin-left').split('px')[0]) + Number(o.css('margin-right').split('px')[0])) : 0;
	p+= Number(o.css('border-left-width').split('px')[0]) + Number(o.css('border-right-width').split('px')[0]);
	p+= Number(o.css('padding-left').split('px')[0]) + Number(o.css('padding-right').split('px')[0]);
	o.width(px - p);
};

//----------------------------------------- Show a list to select an item (-1 if canceled)
var selectionList = function(a, t, ai, f) {
	if ($('.selectionList').length) return;	// Nothing if a list is already active
	var o = $('<div>');
	var item;
	o.addClass('selectionList');
	$('body').append(o);
	item = $('<p>');
	item.addClass('selectionListItem');
	item.attr('id', 'selectionListTitle');
	o.append(item);
	item.text(t);
	item.html(item.html().replace(/ /g, '&nbsp;'));
	for (var i = 0; i < a.length; i++) {
		item = $('<p>');
		item.attr('seq', i);
		item.addClass('selectionListItem');
		o.append(item);
		if (a[i] != '') {
			item.on('click', function(event) {itemSelected(event, f);});
			item.text(a[i]);
		} else {
			item.attr('id', 'selectionListItemBlank');
			item.text(' ');
		}
		item.html(item.html().replace(/ /g, '&nbsp;'));
	}
	if (ai >= 0) $('.selectionListItem[seq = ' + ai + ']').addClass('activeListItem');
	item = $('<p>');
	item.addClass('selectionListItem');
	item.attr('id', 'selectionListItemBlank');
	o.append(item);
	item.html('&nbsp;');
	item = $('<p>');
	item.attr('seq', -1);
	item.on('click', function(event) {itemSelected(event, f);});
	item.addClass('selectionListItem');
	item.attr('id', 'selectionListCancel');
	o.append(item);
	item.text('Cancel');
	resizeSelectionList();
};

//----------------------------------------- Show alert message (no blocking) - timeout parameter 'seconds' is optional
var smoothAlert = function(message, seconds) {
	var lines = ('' + message).split('\n');
	var o = $('<div>');
	if ($hasTouch) {
		o.on('touchstart', function(event) {startMoveTouch(event.originalEvent, $(this));});
		o.on('touchmove', function(event) {movingTouch(event.originalEvent, $(this));});
		o.on('touchend', function(event) {stopMoveTouch(event.originalEvent, $(this));});
	} else {
		o.on('click', function(event) {removeSmoothAlertMouse(event);});
		o.on('mousedown', function(event) {startMoveMouse(event);});
		o.on('mousemove', function(event) {movingMouse(event);});
		o.on('mouseup', function(event) {stopMoveMouse(event);});
		o.on('mouseout', function(event) {stopMoveConditionallyMouse(event);});
	}
	o.addClass('smoothAlert');
	$('body').append(o);
	lines.forEach(function(line) {
		var l = $('<p>');
		l.addClass('smoothAlertLine');
		o.append(l);
		l.text(line + ((line.length) ? '' : ' '));
		l.html(l.html().replace(/ /g, '&nbsp;'));
	});
	o.css('z-index', $zIndexForSmoothAlert--);
	o.addClass('fadeIn');
	if (!isNaN(seconds) && seconds > 0) o.attr('seconds', seconds);
	resizeSmoothAlerts();
};

//----------------------------------------- Encode text to be used in attribute 'id'
var	encodeId = function(text) {
	var s = escape(text);	// will insert: %		The exceptions: * @ - _ + . /
	s = s.replace(/\%/g, '_-0-_');
	s = s.replace(/\*/g, '_-1-_');
	s = s.replace(/\@/g, '_-2-_');
	s = s.replace(/\+/g, '_-3-_');
	s = s.replace(/\./g, '_-4-_');
	s = s.replace(/\//g, '_-5-_');
	return s;
};

//----------------------------------------- Get width of vertical scrollbar
var getVerticalScrollbarWidth = function() {
	var p, c, w;
	p = $('<div style="width:50px;height:50px;overflow:auto"><div/></div>').appendTo('body');
	c = p.children();
	w = c.innerWidth() - c.height(99).innerWidth();
	p.remove();
	return w;
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ End of file
