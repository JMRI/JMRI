/**********************************************************************************************
* 
* JMRI JSON WebSockets and Ajax communication javascript 
* File version 1.0 (old): script 'ThrottleMonitor.py' must be loaded for throttle support
* File version 2.0: native JSON throttle support - doesn't need script 'ThrottleMonitor.py' anymore
* 
* >>> This file version: 2.1 - from original 'jquery.jmri.js' modified by Oscar Moutinho (oscar.moutinho@gmail.com)
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
			jmri.ready = function(jsonVersion, jmriVersion, railroadName) {};
			jmri.throttle = function(name, address, speed, forward, fs) {};
			jmri.light = function(name, userName, comment, state) {};
			jmri.reporter = function(name, userName, state, comment, report, lastReport) {};
			jmri.sensor = function(name, userName, comment, inverted, state) {};
			jmri.turnout = function(name, userName, comment, inverted, state) {};
			jmri.signalHead = function(name, userName, comment, lit, appearance, held, state, appearanceName) {};
			jmri.signalMast = function(name, userName, aspect, lit, held, state) {};
			jmri.route = function(name, userName, comment, state) {};
			jmri.memory = function(name, userName, comment, value) {};
			jmri.power = function(state) {};
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
								name: $(this).attr('id'),
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
			jmri.getRosterItem = function(name) {
				if (!roster) roster = loadRoster();
				var loco = null;
				if (roster) {
					roster.find('roster-config roster locomotive').each(function() { 
						if ($(this).attr('id') == name) {
							loco = {
								name: $(this).attr('id'),
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
					url: '/roster?format=xml',
					async: false,
					cache: false,
					type: 'GET',
					dataType: 'xml',
					error: function(jqXHR, textStatus, errorThrown) {
						if(jqXHR.status == 404) jmri.error(jqXHR.status, 'Roster empty.\nNo locos defined in JMRI.');
						else jmri.error(jqXHR.status, 'Response:\n' + jqXHR.responseText + '\n\nError:\n' + errorThrown);
					},
					success: function(xmlReturned, status, jqXHR) {
                                            roster = $(xmlReturned);
                                            if (xmlReturned === null) {
                                                jmri.error(200, 'Roster empty.\nNo locos defined in JMRI.');
                                            }
                                        }
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
			var heartbeatDelay;	// Heartbeat timer delay
			var heartbeat = null;	// Heartbeat timer
			jmri.closeSocket = function() {jmri.socket.close();};
			jmri.getJMRI = function(type, name) {
				if (!heartbeat) {jmri.error(0, 'The JMRI WebSocket service is not ready.\nSolve the problem and refresh web page.'); return;}
				var lp = (name) ? {"name":name} : {};
				jmri.toSend(JSON.stringify({"type":type,"data":lp}));
				jmri.socket.send(type, lp);
			};
			jmri.setJMRI = function(type, name, args) {
				if (!heartbeat) {jmri.error(0, 'The JMRI WebSocket service is not ready.\nSolve the problem and refresh web page.'); return;}
				var lp;
				if (type == 'throttle') lp = (name) ? {"throttle":name} : {};
				else lp = (name) ? {"name":name} : {};
				jmri.toSend(JSON.stringify({"type":type,"data":jmri.jsonConcat(lp, args)}));
				jmri.socket.send(type, jmri.jsonConcat(lp, args));
			};
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
					}
				},
				message: function(e) {jmri.fullData(e.originalEvent.data);},
				events: {
					error: function(e) {jmri.error(e.data.code, e.data.message);},
					pong: function(e) {},
					goodbye: function(e) {
						jmri.socket.close();
						jmri.end();
					},
					hello: function(e) {	// Handle the initial handshake response from the server
						heartbeatDelay = e.data.heartbeat;
						heartbeat = setInterval(function() {jmri.toSend(JSON.stringify({"type":"ping"})); jmri.socket.send('ping');}, heartbeatDelay);
						jmri.ready(e.data.json, e.data.JMRI, e.data.railroad);
					},
					throttle: function(e) {
						var functions = new Array(29);
						for (var i = 0; i < 29; i++) functions[i] = e.data['F' + i];
						jmri.throttle(e.data.throttle, e.data.address, e.data.speed, e.data.forward, functions);
					},
					light: function(e) {jmri.light(e.data.name, e.data.userName, e.data.comment, e.data.state);},
					reporter: function(e) {jmri.reporter(e.data.name, e.data.userName, e.data.state, e.data.comment, e.data.report, e.data.lastReport);},
					sensor: function(e) {jmri.sensor(e.data.name, e.data.userName, e.data.comment, e.data.inverted, e.data.state);},
					turnout: function(e) {jmri.turnout(e.data.name, e.data.userName, e.data.comment, e.data.inverted, e.data.state);},
					signalHead: function(e) {jmri.signalHead(e.data.name, e.data.userName, e.data.comment, e.data.lit, e.data.appearance, e.data.held, e.data.state, e.data.appearanceName);},
					signalMast: function(e) {jmri.signalMast(e.data.name, e.data.userName, e.data.aspect, e.data.lit, e.data.held, e.data.state);},
					route: function(e) {jmri.route(e.data.name, e.data.userName, e.data.comment, e.data.state);},
					memory: function(e) {jmri.memory(e.data.name, e.data.userName, e.data.comment, e.data.value);},
					power: function(e) {jmri.power(e.data.state);}
				}
			});
			return jmri;
		}
	});
})(jQuery);

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ End of file
