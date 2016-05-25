/**********************************************************************************************
* 
* JMRI JSON WebSockets and Ajax communication javascript (with support for throttles - script 'ThrottleMonitor.py' must be loaded)
* 
* >>> This file version: 1.0 - from original 'jquery.jmri.js' modified by Oscar Moutinho (oscar.moutinho@gmail.com)
* 
* Using HTML5 and jQuery 2...
* Note that this JMRI extension relies on the jquery-websocket v.0.0.4 extension.
* 
**********************************************************************************************/

//----------------------------------------- Manage JMRI WebSockets
(function($) {
	$.extend({
		JMRI: function(url, bindings) {
			var jmri = {};
			if (typeof(url) === 'string') {
				jmri.url = url;
			} else {
				jmri.url = document.URL.split('/')[0] + '//' + document.URL.split('/')[2] + '/json/';
				bindings = url;
			}
			// Default event handlers that do nothing
			jmri.toSend = function(data) {};
			jmri.fullData = function(data) {};
			jmri.error = function(code, message) {};
			jmri.end = function() {};
			jmri.refresh = function() {};
			jmri.ready = function(jsonVersion, jmriVersion, railroadName) {};
			jmri.light = function(name, userName, comment, state) {};
			jmri.reporter = function(name, userName, state, comment, report, lastReport) {};
			jmri.sensor = function(name, userName, comment, inverted, state) {};
			jmri.turnout = function(name, userName, comment, inverted, state) {};
			jmri.signalHead = function(name, userName, comment, lit, appearance, held, state, appearanceName) {};
			jmri.signalMast = function(name, userName, aspect, lit, held, state) {};
			jmri.route = function(name, userName, comment, state) {};
			jmri.memory = function(name, userName, comment, value) {};
			jmri.power = function(state) {};
			jmri.throttleError = function(message) {};
			jmri.throttleAddress = function(address) {};
			jmri.throttleSpeed = function(address, speed) {};
			jmri.throttleDirection = function(address, forward) {};
			jmri.throttleFunctionState = function(address, functionNumber, active) {};
			// Add user-defined handlers to the settings object
			$.extend(jmri, bindings);
			// Constants
			jmri.powerUNKNOWN = 0;
			jmri.powerON = 2;
			jmri.powerOFF = 4;
			jmri.turnoutUNDEFINED = 0;
			jmri.turnoutUNKNOWN = 1;
			jmri.turnoutCLOSED = 2;
			jmri.turnoutTHROWN = 4;
			jmri.routeDISABLED = 0;
			jmri.routeUNDEFINED = 1;
			jmri.routeACTIVE = 2;
			jmri.routeINACTIVE = 4;
			jmri.TRUE = true;
			jmri.FALSE = false;
			jmri.YES = 1;
			jmri.NO = 0;
			jmri.EMERGENCY_STOP = '-1.0';
			jmri.STOP = '0.0';
			jmri.FULL_SPEED = '1.0';
			jmri.strMemVarPrefix = 'IM#THROTTLE:';
			jmri.strMemVar_NewThrottleAddress = 'NEW_THROTTLE_ADDRESS';
			jmri.strMemVar_Speed = '_SPEED';
			jmri.strMemVar_Forward = '_FORWARD';
			jmri.strMemVar_Functions = '_F';
			// Getters and Setters
			var roster = null;
			jmri.getRosterGroups = function() {
				if (!roster) roster = loadRoster();
				var rosterGroupsList = [];
				if (roster) {
					roster.find('roster-config roster locomotive').each(function() { 
						$(this).find('attributepairs keyvaluepair').each(function() { 
							var key = $(this).find('key').text();
							if (key.split(':')[0] == 'RosterGroup') {
								var rosterGroup = key.split(':')[1];
								var value = $(this).find('value').text();
								if (value == 'yes' && rosterGroupsList.indexOf(rosterGroup) == -1) rosterGroupsList.push(rosterGroup);
							}
						});
					});
				}
				return rosterGroupsList.sort();
			};
			jmri.getRoster = function(group) {
				if (!roster) roster = loadRoster();
				var locoList = [];
				if (roster) {
					roster.find('roster-config roster locomotive').each(function() { 
						var inGroup = false;
						if (group) {
							$(this).find('attributepairs keyvaluepair').each(function() { 
								var key = $(this).find('key').text();
								if (key.split(':')[0] == 'RosterGroup') {
									key = key.split(':')[1];
									var value = $(this).find('value').text();
									if (key == group && value == 'yes') inGroup = true;
								}
							});
						} else inGroup = true;
						if (inGroup) {
							var loco = {
								id: $(this).attr('id'),
								roadNumber: $(this).attr('roadNumber'),
								roadName: $(this).attr('roadName'),
								mfg: $(this).attr('mfg'),
								owner: $(this).attr('owner'),
								model: $(this).attr('model'),
								dccAddress: parseInt($(this).attr('dccAddress'), 10),
								imageFilePath: $.trim($(this).attr('imageFilePath')),
								iconFilePath: $.trim($(this).attr('iconFilePath'))
							};
							locoList.push(loco);
						}
					});
				}
				return locoList;
			};
			jmri.getRosterItem = function(id) {
				if (!roster) roster = loadRoster();
				var loco = null;
				if (roster) {
					roster.find('roster-config roster locomotive').each(function() { 
						if ($(this).attr('id') == id) {
							loco = {
								id: $(this).attr('id'),
								fileName: $(this).attr('fileName'),
								roadNumber: $(this).attr('roadNumber'),
								roadName: $(this).attr('roadName'),
								mfg: $(this).attr('mfg'),
								owner: $(this).attr('owner'),
								model: $(this).attr('model'),
								dccAddress: parseInt($(this).attr('dccAddress'), 10),
								comment: $(this).attr('comment'),
								maxSpeed: parseInt($(this).attr('maxSpeed'), 10),
								imageFilePath: $.trim($(this).attr('imageFilePath')),
								iconFilePath: $.trim($(this).attr('iconFilePath')),
								URL: $.trim($(this).attr('URL')),
								IsShuntingOn: $.trim($(this).attr('IsShuntingOn')).toUpperCase(),
								f: new Array(29)
							};
							for (var i = 0; i < 29; i++) loco.f[i] = null;
							$(this).find('functionlabels functionlabel').each(function() { 
								var n = parseInt($(this).attr('num'), 10);
								var f = {
									lockable: ($(this).attr('lockable') == 'true'),
									functionlabel: $(this).text(),
									functionImage: $.trim($(this).attr('functionImage')),
									functionImageSelected: $.trim($(this).attr('functionImageSelected'))
								};
								loco.f[n] = f;
							});
						}
					});
				}
				return loco;
			};
			var loadRoster = function() {	//Retrieve roster
				var roster = null;
				$.ajax({
					url: '/prefs/roster.xml',
					async: false,
					cache: false,
					type: 'GET',
					dataType: 'xml',
					error: function(jqXHR, textStatus, errorThrown) {
						if(jqXHR.status == 404) jmri.error(jqXHR.status, 'Roster empty.\nNo locos defined in JMRI.');
						else jmri.error(jqXHR.status, 'Response:\n' + jqXHR.responseText + '\n\nError:\n' + errorThrown);
					},
					success: function(xmlReturned, status, jqXHR) {roster = $(xmlReturned);}
				});
				return roster;
			};
			jmri.getObjectList = function(listType) {	//Retrieve a JSON list of objects
				var list = [];
				$.ajax({
					url: jmri.url + listType,
					async: false,
					cache: false,
					type: 'GET',
					dataType: 'json',
					error: function(jqXHR, textStatus, errorThrown) {
						jmri.error(jqXHR.status, 'Response:\n' + jqXHR.responseText + '\n\nError:\n' + errorThrown);
					},
					success: function(listReturned, status, jqXHR) {list = listReturned;}
				});
				return list;
			};
			var heartbeat = null;	// Heartbeat timer
			jmri.closeSocket = function() {jmri.socket.close();};
			jmri.getJMRI = function(type, name) {
				if (!heartbeat) {jmri.error(0, 'JMRI not ready.'); return;}
				var lp = (name) ? {"name":name} : {};
				jmri.toSend(JSON.stringify({"type":type,"data":lp}));
				jmri.socket.send(type, lp);
			};
			jmri.setJMRI = function(type, name, args) {
				if (!heartbeat) {jmri.error(0, 'JMRI not ready.'); return;}
				var lp = (name) ? {"name":name} : {};
				jmri.toSend(JSON.stringify({"type":type,"data":jmri.jsonConcat(lp, args)}));
				jmri.socket.send(type, jmri.jsonConcat(lp, args));
			};
			jmri.selectThrottle = function(address) {jmri.setJMRI('memory', jmri.strMemVarPrefix + jmri.strMemVar_NewThrottleAddress, {"value":address});};
			jmri.getThrottleSpeed = function(address) {jmri.getJMRI('memory', jmri.strMemVarPrefix + address + jmri.strMemVar_Speed);};
			jmri.setThrottleSpeed = function(address, speed) {jmri.setJMRI('memory', jmri.strMemVarPrefix + address + jmri.strMemVar_Speed, {"value":speed});};
			jmri.getThrottleDirection = function(address) {jmri.getJMRI('memory', jmri.strMemVarPrefix + address + jmri.strMemVar_Forward);};
			jmri.setThrottleForward = function(address, forward) {jmri.setJMRI('memory', jmri.strMemVarPrefix + address + jmri.strMemVar_Forward, {"value":forward});};
			jmri.getThrottleFunctionState = function(address, functionNumber) {jmri.getJMRI('memory', jmri.strMemVarPrefix + address + jmri.strMemVar_Functions + functionNumber);};
			jmri.setThrottleFunction = function(address, functionNumber, active) {jmri.setJMRI('memory', jmri.strMemVarPrefix + address + jmri.strMemVar_Functions + functionNumber, {"value":active});};
			jmri.jsonConcat = function(o1, o2) {	// Concatenate JSON name-value pair lists
				var o = {};
				var key;
				for (key in o1) o[key] = o1[key];
				for (key in o2) o[key] = o2[key];
				return o;
			};
			// WebSocket
			jmri.socket = $.websocket(jmri.url.replace(/^http/, 'ws'), {
				close: function() {	// Stop the heartbeat when the socket closes
					if (heartbeat) {
						clearInterval(heartbeat);
						heartbeat = null;
						jmri.refresh();
					}
				},
				message: function(e) {jmri.fullData(e.originalEvent.data);},
				events: {
					error: function(e) {
						if (e.data.code == 404 && e.data.message.split(':')[0] === 'Unable to access memory ' + jmri.strMemVarPrefix.split(':')[0]) {
							var s = e.data.message.split(':')[1];
							if (s === jmri.strMemVar_NewThrottleAddress + '.') jmri.throttleError('Please, load the script \'ThrottleMonitor.py\'.\n(it may be loaded at JMRI startup - see Settings)');
							else jmri.throttleError('Memory Variable \'' + jmri.strMemVarPrefix + s.split('.')[0] + '\' doesn\'t exist.\nSelect a throttle with the corresponding address.\nIf error persists, restart JMRI.');
						} else jmri.error(e.data.code, e.data.message);	// Error not related to throttle
					},
					pong: function(e) {},
					goodbye: function(e) {
						clearInterval(heartbeat);
						heartbeat = null;
						jmri.socket.close();
						jmri.end();
					},
					hello: function(e) {	// Handle the initial handshake response from the server
						heartbeat = setInterval(function() {jmri.toSend(JSON.stringify({"type":"ping"})); jmri.socket.send('ping');}, e.data.heartbeat);
						jmri.ready(e.data.json, e.data.JMRI, e.data.railroad);
					},
					light: function(e) {jmri.light(e.data.name, e.data.userName, e.data.comment, e.data.state);},
					reporter: function(e) {jmri.reporter(e.data.name, e.data.userName, e.data.state, e.data.comment, e.data.report, e.data.lastReport);},
					sensor: function(e) {jmri.sensor(e.data.name, e.data.userName, e.data.comment, e.data.inverted, e.data.state);},
					turnout: function(e) {jmri.turnout(e.data.name, e.data.userName, e.data.comment, e.data.inverted, e.data.state);},
					signalHead: function(e) {jmri.signalHead(e.data.name, e.data.userName, e.data.comment, e.data.lit, e.data.appearance, e.data.held, e.data.state, e.data.appearanceName);},
					signalMast: function(e) {jmri.signalMast(e.data.name, e.data.userName, e.data.aspect, e.data.lit, e.data.held, e.data.state);},
					route: function(e) {jmri.route(e.data.name, e.data.userName, e.data.comment, e.data.state);},
					memory: function(e) {
						if (e.data.name.split(':')[0] === jmri.strMemVarPrefix.split(':')[0]) {
							var s = e.data.name.split(':')[1];
							if (s == jmri.strMemVar_NewThrottleAddress) {
								if (e.data.value.split('-')[1] === 'OK') jmri.throttleAddress(e.data.value.split('-')[0]);
							} else {
								var n = s.split('_')[0];
								switch ('_' + s.split('_')[1]) {
									case jmri.strMemVar_Speed:
										jmri.throttleSpeed(n, e.data.value);
										break;
									case jmri.strMemVar_Forward:
										jmri.throttleDirection(n, e.data.value);
										break;
									default:
										var f = s.split('F')[1];
										jmri.throttleFunctionState(n, f, e.data.value);
								}
							}
						} else jmri.memory(e.data.name, e.data.userName, e.data.comment, e.data.value);	// The name does not started with the content of [jmri.strMemVarPrefix]
					},
					power: function(e) {jmri.power(e.data.state);}
				}
			});
			return jmri;
		}
	});
})(jQuery);

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ End of file
