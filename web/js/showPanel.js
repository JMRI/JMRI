/**********************************************************************************************
 *  showPanel - Draw JMRI panels on browser screen
 *    Retrieves panel xml and attempts to build panel client-side from the xml, including
 *    click functions.  Sends and listens for changes to panel elements using the xmlio server.
 *    If no parm passed, will list links to available panels.
 *  
 *  TODO: rearrange/refactor code to handle various widget types better (maybe a basetype like image|text|drawing?)
 *  TODO: add heartbeat with error message (with retry button)
 *  TODO: handle text-based and overlay elements
 *  TODO: handle "drawn" elements (for layouteditor)
 *  TODO: handle multisensors, other widgets correctly
 *  TODO: find correct font or adjust size for text labels
 *  TODO: diagnose and correct the small position issues visible with footscray
 *  TODO: if no elements found, don't send list to xmlio server
 *  TODO: figure out what "hidden=yes" is supposed to do
 *  TODO: verify that assuming same rotation and scale for all icons in a "set" is OK
 *  TODO: deal with mouseleave, mouseout, touchout, etc.
 *   
 **********************************************************************************************/

//persistent (global) variables
var $gWidgets = {};  //array of all widget objects, key=CSSId
var $gPanel = {}; 	//store overall panel info
var $gPoints = {}; 	//array of all points, key=PointName (used for layoutEditor panels)
var $gXHR;  	//persistent variable to allow aborting of superseded connections
var DOWNEVENT;  //either mousedown or touchstart, based on device
var UPEVENT;    //either mouseup or touchend, based on device

var UNKNOWN = 		'1';  //constants to match JMRI state names
var ACTIVE =  		'2';
var CLOSED =  		'2';
var INACTIVE= 		'4';
var THROWN =  		'4';
var INCONSISTENT = 	'8';

//process the response returned for the requestPanelXML command
var $processResponse = function($returnedData, $success, $xhr) {

	var $xml = $($returnedData);  //jQuery-ize returned data for easier access

	//remove whitespace
	$xml.xmlClean();

	var $panel = $xml.find('panel');
	$($panel[0].attributes).each(function() {
		$gPanel[this.name] = this.value;
	});
	
	$('div#panelArea').width($gPanel.width);
	$('div#panelArea').height($gPanel.height);

	$panel.contents().each( //process all widgets in the panel xml
			function() {
				//convert attributes to an object array
				var $widget = new Array();
				$widget['widgetType'] = this.nodeName;
				$(this.attributes).each(function() {
					$widget[this.name] = this.value;
				});
				//default various css attributes to not-set, then set in later code as needed
				var $clickable = "";  
				var $hoverText = "";  
				var $elementName = "";  
				var $momentary = "";  
				var $hidden = "";

				var $rotation = 0;
				
				var $drawThis = false; //flag to continue into drawing portion of code
				
				if ($widget.momentary == "true") {
					$momentary = "momentary";
				}
				
				//add and normalize the various type-unique values, from the various spots they are stored
				//  icons named based on states returned from xmlio server, 
				//    1=unknown, 2=active/closed, 4=inactive/thrown, 8=inconsistent
				$widget['state'] = UNKNOWN; //initial state is unknown
				$widget['element']  =	""; //default to no xmlio type (avoid undefined)
				$widget['scale']  =	"1.0"; //default to no scale
				$widget['id'] = "spWidget_" + $gUnique();//set id to a unique value (since same element can be in multiple widgets)
				if ($widget.text == undefined) {  //add image icon to panel
					switch ($widget.widgetType) {
					case "positionablelabel" :
						$widget['icon1'] = 		$(this).find('icon').attr('url');
						$rotation = 			$(this).find('icon').find('rotation').text();
						$widget['degrees'] = 	($(this).find('icon').attr('degrees') * 1) + ($rotation * 90);
						$widget['scale'] = 		$(this).find('icon').attr('scale');
						$drawThis =		 		true;
						break;
					case "turnouticon" :
						$widget['name']  =		$widget.turnout; //normalize name
						$widget['element']  =	"turnout"; //what JMRI calls this
						$widget['icon1'] = 		$(this).find('icons').find('unknown').attr('url');
						$widget['icon2'] =  	$(this).find('icons').find('closed').attr('url');
						$widget['icon4'] =  	$(this).find('icons').find('thrown').attr('url');
						$widget['icon8'] =		$(this).find('icons').find('inconsistent').attr('url');
						$rotation = 			$(this).find('icons').find('unknown').find('rotation').text();
						$widget['degrees'] = 	($(this).find('icons').find('unknown').attr('degrees') * 1) + ($rotation * 90);
						$widget['scale'] = 		$(this).find('icons').find('unknown').attr('scale');
						$clickable =			"clickable";
						$drawThis =		 		true;
						break;
					case "sensoricon" :
					case "multisensoricon" :
						$widget['name']  =		$widget.sensor; //normalize name
						$widget['element']  =	"sensor"; //what JMRI calls this
						$widget['icon1'] = 		$(this).find('unknown').attr('url');
						$widget['icon2'] =  	$(this).find('active').attr('url');
						$widget['icon4'] =  	$(this).find('inactive').attr('url');
						$widget['icon8'] =		$(this).find('inconsistent').attr('url');
						$rotation = 			$(this).find('unknown').find('rotation').text();
						$widget['degrees'] = 	($(this).find('unknown').attr('degrees') * 1) + ($rotation * 90);
						$widget['scale'] = 		$(this).find('unknown').attr('scale');
						$clickable =			"clickable";
						$drawThis =		 		true;
						break;
					case "positionablepoint" :
						//just store these points in persistent variable
						$gPoints[$widget.ident] = $widget;
						break;
					case "tracksegment" :
						//draw the line
						
						break;
					default:
						//list unsupported widgets
						$("div#logArea").append("<br />Unsupported: " + $widget.widgetType + ":"); 
						$(this.attributes).each(function(){
							$("div#logArea").append(" " + this.name);
						});
						$("div#logArea").append(" | ");
						$(this.childNodes).each(function(){
							$("div#logArea").append(" " + this.nodeName);
						});
					}
					if ($drawThis) {

						//if "Disable" checked, remove clickable class from widget
						if ($widget.forcecontroloff == "true") {
							$clickable = "";
						}

						$widget['safeName'] = $safeName($widget.name);  //add a "safe" version of name for use as class

						if ($widget.name) { //if name available, use it as hover text
							$hoverText = " title='"+$widget.name+"' alt='"+$widget.name+"'";
						}

						//add the image to the panel area, with appropriate css classes and id (skip any unsupported)
						if ($widget.icon1 != undefined) {
							$("div#panelArea").append("<img id=" + $widget.id +
									" class='" + $widget.widgetType + " rotatable " + $clickable + " " + $widget.element + " " + $momentary +
									"' src='" + $widget["icon"+$widget['state']] + "' " + $hoverText + "/>");
						}
					} //drawThis
				} else { //add text icon to panel
					$widget['element']  =	""; //no xmlio type for this
					$widget['safeName'] = $safeName($widget.name);
					$("div#panelArea").append("<div id=" + $widget.id + " class='"+$widget.widgetType+"'>" + $widget.text + "</div>");
					var $color = "rgb(" + $widget.red + "," + $widget.blue + "," + $widget.green + ")";
					$("div#panelArea>#"+$widget.id).css({color:$color,fontSize:$widget.size}); //set text color and font-size
					$drawThis =		 		true;
				}

				if ($drawThis) {

					//if widget is known to jmri, store it in persistent array for later use
//					if ($widget.name != undefined) {
						$gWidgets[$widget.id] = $widget;
//					}

					$preloadWidgetImages($widget);
					
					$setWidgetPosition($("div#panelArea>#"+$widget.id));

				} //end of drawThis
				
			}  //end of function
	);  //end of each

	//hook up mousedown state toggle function to non-momentary clickable widgets
	$('.clickable:not(.momentary)').bind(DOWNEVENT, function(e) {
	    var $newState = $getNextState($gWidgets[this.id].state);  //determine next state from current state
		$setWidgetState(this.id, $newState);
		$sendElementChange($gWidgets[this.id].element, $gWidgets[this.id].name, $newState);  //send new value to xmlio server
	});
	
	//momentary widgets always go active on mousedown, and inactive on mouseup
	$('.clickable.momentary').bind(DOWNEVENT, function(e) {
	    var $newState = ACTIVE; 
		$setWidgetState(this.id, $newState);
		$sendElementChange($gWidgets[this.id].element, $gWidgets[this.id].name, $newState);  //send new value to xmlio server
	}).bind(UPEVENT, function(e) {
	    var $newState = INACTIVE; 
		$setWidgetState(this.id, $newState);
		$sendElementChange($gWidgets[this.id].element, $gWidgets[this.id].name, $newState);  //send new value to xmlio server
	});
	
	//add a dummy sensor widget for use as heartbeat
	$widget = new Array();
	$widget['element'] = "sensor";
	$widget['name'] = "ISXmlIOHeartbeat";  //internal sensor, will be created if not there
	$widget['id'] 	= $widget['name'];
	$widget['state'] = UNKNOWN;
	$gWidgets[$widget.id] = $widget;

	//send initial states to xmlio server
	$sendXMLIOList('<xmlio>' + $getXMLStateList() + '</xmlio>');

};    	

//preload all images referred to by the widget
var $preloadWidgetImages = function($widget) {
	if ($widget['icon1'] != undefined) $("<img src='" + $widget['icon1'] + "'/>");
	if ($widget['icon2'] != undefined) $("<img src='" + $widget['icon2'] + "'/>");
	if ($widget['icon4'] != undefined) $("<img src='" + $widget['icon4'] + "'/>");
	if ($widget['icon8'] != undefined) $("<img src='" + $widget['icon8'] + "'/>");
};    	

//place widget in correct position, rotation, z-index and scale.
var $setWidgetPosition = function(e) {
	
	var $id = e.attr('id');
	var $widget = $gWidgets[$id];  //look up the widget and get its panel properties

	
	//TODO: yeah, I know this won't work right if the image hasn't downloaded yet, hit Refresh for now.....
	var $height = e.height() * $widget.scale;
	var $width =  e.width()  * $widget.scale;

//	console.log("processing " + $widget.id + " height=" + $height + " width=" + $width);
	
	//if image is not loaded yet, set callback to do this again when it is loaded
	if ($height == 0) {
//		console.log("delaying positioning of " + $widget.id);
		e.load(function(){
			$setWidgetPosition($(this));
		});
	} else {
		//calculate x and y adjustment needed to keep upper left of bounding box in the same spot
		//  adapted to match JMRI's NamedIcon.rotate().  Note: transform-origin set in .html file
		var tx = 0.0;
		var ty = 0.0;

		if ($widget.degrees != undefined && $widget.degrees != 0 && $height > 0) {
			var $rad = $widget.degrees*Math.PI/180.0;

			if (0<=$widget.degrees && $widget.degrees<90 || -360<$widget.degrees && $widget.degrees<=-270){
				tx = $height*Math.sin($rad);
				ty = 0.0;
			} else if (90<=$widget.degrees && $widget.degrees<180 || -270<$widget.degrees && $widget.degrees<=-180) {
				tx = $height*Math.sin($rad)-$width*Math.cos($rad);
				ty = -$height*Math.cos($rad);
			} else if (180<=$widget.degrees && $widget.degrees<270 || -180<$widget.degrees && $widget.degrees<=-90) {
				tx = -$width*Math.cos($rad);
				ty = -$width*Math.sin($rad)-$height*Math.cos($rad);
			} else /*if (270<=$widget.degrees && $widget.degrees<360)*/ {
				tx = 0.0;
				ty = -$width*Math.sin($rad);
			}
		}
		//position widget to adjusted position, set z-index, then set rotation
		e.css({position:'absolute',left:(parseInt($widget.x)+tx)+'px',top:(parseInt($widget.y)+ty)+'px',zIndex:$widget.level});
		if ($widget.degrees != undefined && $widget.degrees != 0){
			var $rot = "rotate("+$widget.degrees+"deg)";
			e.css({MozTransform:$rot,WebkitTransform:$rot,msTransform:$rot});
		}
		//set new height and width if scale specified 
		if ($widget.scale != 1 && $height > 0){
			e.css({height:$height+'px',width:$width+'px'});
		}
	} //if height == 0
};

//set new value for all widgets having specified type and element name
var $setElementState = function($element, $name, $newState) {
	jQuery.each($gWidgets, function($id, $widget) {
		if ($widget.element == $element && $widget.safeName == $name) {
			$setWidgetState($id, $newState);
		}
	});
};

//set new value for widget, showing proper icon
var $setWidgetState = function($id, $newState) {
	if ($gWidgets[$id].state != $newState) {  //don't bother if already this value
		console.log( "setting " + $id + " for " + $gWidgets[$id].element + " " + $gWidgets[$id].name + " --> " + $newState);
		$('img#'+$id).attr('src', $gWidgets[$id]["icon"+$newState]);  //set image src to next state's image
		$gWidgets[$id].state = $newState;  //update the changed widget
	}
};

//return a unique ID # when called
var $gUnique = function () {
    if (typeof $gUnique.id === 'undefined') {
    	$gUnique.id = 0;
    }
    $gUnique.id++;
    return $gUnique.id;
};

//clean up a name, for example to use as an id  
var $safeName = function($name){
  if ($name == undefined) {
	  return "unique-" + $gUnique();
  } else {
	  return $name.replace(/:/g, "_").replace(/ /g, "_").replace(/%20/g, "_");
  }
};

//send request for state change 
var $sendElementChange = function($element, $name, $nextState){
	console.log("sending " + $element + " " + $name + " --> " + $nextState);
	var $commandstr = "<xmlio><" + $element + " name='" + $name + "' set='" + $nextState +"'/></xmlio>";
	$sendXMLIOChg($commandstr);
};

//build xml for current state values for all jmri elements, to be sent to xmlio server to wait on changes 
var $getXMLStateList = function(){
	var $retXml = "";
	jQuery.each($gWidgets, function($id, $widget) {
		if ($widget.name != undefined) { 
			$retXml += "<" + $widget.element + " name='" + $widget.name + "' value='" + $widget.state +"'/>";
		}
	});
    return $retXml;
};

//send a command to the server, and setup callback to process the response (used for lists)
var $sendXMLIOList = function($commandstr){
	console.log( "sending a list");
	if ($gXHR) {
		$gXHR.abort();
	}
	$gXHR = $.ajax({ 
		type: 'POST',
		url:  '/xmlio/',
		data: $commandstr,
		success: function($r, $s, $x){
			console.log( "receiving a list");
			var $r = $($r);
			$r.xmlClean();
			$r.find("xmlio").children().each(function(){
//				console.log("set type="+this.nodeName+" name="+$(this).attr('name')+" to value="+$(this).attr('value'));
				$setElementState(this.nodeName, $safeName($(this).attr('name')), $(this).attr('value'));
			});
			//send current xml states to xmlio server, will "stall" and wait for changes if matched
			$sendXMLIOList('<xmlio>' + $getXMLStateList() + '</xmlio>');
		},
		async: true,
		timeout: 150000,  //TODO: rethink this, maybe by sending a dummy change?
		dataType: 'xml' //<--dataType
	});
};

//send a command to the server, and ignore the response (used for change commands)
var $sendXMLIOChg = function($commandstr){
//    console.log( "sending a change request");
	if ($gXHR) {
		$gXHR.abort();
	}
	$gXHR = $.ajax({ 
		type: 'POST',
		url:  '/xmlio/',
		data: $commandstr,
		success: function($r, $s, $x){
//		    console.log( "rcving a change request");
			var $r = $($r);
			$r.xmlClean();
			$r.find("xmlio").children().each(function(){
				console.log("rcvd change "+this.nodeName+" "+$(this).attr('name')+" --> "+$(this).attr('value'));
				$setElementState(this.nodeName, $safeName($(this).attr('name')), $(this).attr('value'));
				//send current xml states to xmlio server, will "stall" and wait for changes if matched
				$sendXMLIOList('<xmlio>' + $getXMLStateList() + '</xmlio>');
			});
		},
		async: true,
		timeout: 5000,  //five seconds
		dataType: 'xml' //<--dataType
	});
};

//handle ajax errors, excluding abort, but including timeout, by resending the list
$(document).ajaxError(function(event,xhr,opt, exception){
	if (xhr.statusText !="abort") {
		console.log("AJAX error: " + xhr.statusText + ", retrying....");
		$sendXMLIOList('<xmlio>' + $getXMLStateList() + '</xmlio>');
//	} else {
//		console.log("AJAX Error requesting " + opt.url + ", status= " + xhr.status + " " + xhr.statusText+ " exception=" + exception);
	}
});

//clear out whitespace from xml, function adapted from 
//http://stackoverflow.com/questions/1539367/remove-whitespace-and-line-breaks-between-html-elements-using-jquery/3103269#3103269
jQuery.fn.xmlClean = function() {
	this.contents().filter(function() {
		if (this.nodeType != 3) {
			$(this).xmlClean();
			return false;
		}
		else {
			return !/\S/.test(this.nodeValue);
		}
	}).remove();
}
//handle the toggling of the next state
var $getNextState = function($state){
	var $nextState = ($state == ACTIVE ? INACTIVE : ACTIVE);
	return $nextState;
};

//parse the page's input parameter and return value for name passed in
function getParameterByName(name) {
	var match = RegExp('[?&]' + name + '=([^&]*)')
	.exec(window.location.search);
	return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
}

//request and show a list of available panels from the server (used when no panel passed in)
var $showPanelList = function($panelName){
	$.ajax({
		url:  '/xmlio/list', //request proper url
		data: {type: "panel"},
		success: function($r, $s, $x){
			$(document).attr('title', 'Client-side panels');
			var $h = "<h1>Client-side panels:</h1>";
			$h += "<table><tr><th>View Panel</th><th>Type</th><th>XML</th></tr>";
            $($r).find("panel").each(function(){
            	var $t = $(this).attr("name").split("/");
            	$h += "<tr><td><a href='?name=" + $(this).attr("name") + "'>" + $(this).attr("userName") + "</a></td>";
            	$h += "<td>" + $t[0] + "</td>";
            	$h += "<td><a href='/panel/" + $(this).attr("name") + "' target=_new >XML</a></td></tr>";
            });
            $h += "</table>";
            $('div#panelArea').html($h); //put table on page
		},
		error: function($r, $s, $message){
			$('div#logArea').append("ERROR: " + $message + " sts=" + $s);
		},
		dataType: 'xml' //<--dataType
	});
};

//request the panel xml from the server, and setup callback to process the response
var $requestPanelXML = function($panelName){

	$.ajax({
		type: 'GET',
		url:  '/panel/' + $panelName, //request proper url
		success: function($r, $s, $x){
			$processResponse($r, $s, $x); //handle returned data
		},
		error: function($r, $s, $message){
			$('div#logArea').append("ERROR: " + $message + " sts=" + $s);
		},
		async: true,
		timeout: 5000,  
		dataType: 'xml' //<--dataType
	});
};


//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function() {
	
	
	var $is_touch_device = 'ontouchstart' in document.documentElement;
	if ($is_touch_device) {
		console.log("touch events enabled");
		DOWNEVENT = 'touchstart';
		UPEVENT   = 'touchend';
	} else {
		console.log("mouse events enabled");
		DOWNEVENT = 'mousedown';
		UPEVENT   = 'mouseup';
	}
	
	//if panelname not passed in, show list of available panels
	var $panelName = getParameterByName('name');
	if ($panelName == undefined) {
		$showPanelList();
	} else {
		//include name of panel in page title
		$(document).attr('title', 'Show JMRI Panel: ' + $panelName);
		//add a link to the panel xml
		$("div#panelFooter").append("&nbsp;<a href='/panel/" + $panelName + "' target=_new>[Panel XML]</a>");

		//request actual xml of panel, and process it on return
		$requestPanelXML($panelName);
	}
	
});
