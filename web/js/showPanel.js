/**********************************************************************************************
 *  showPanel - Draw JMRI panels on browser screen
 *    Retrieves panel xml from JMRI and builds panel client-side from that xml, including
 *    click functions.  Sends and listens for changes to panel elements using the xmlio server.
 *    If no parm passed, page will list links to available panels.
 *  Approach:  Read panel's xml and create widget objects in the browser with all needed attributes.   
 *    There are 3 "widgetFamily"s: text, icon and drawn.  States are handled by storing members 
 *    iconX, textX, cssX where X is the state.  The corresponding members are "shown" whenever the state changes.
 *    CSS classes are used throughout to attach events to correct widgets, as well as control appearance.
 *    The xmlio element name is used to send changes to xmlio server and to listen for changes made elsewhere.
 *    Drawn widgets are handled by drawing directly on the javascript "canvas" layer.
 *    An internal (to JMRI) heartbeat sensor is used to avoid one browser holding multiple server connections (refresh, links, etc.)
 *  Loop: 	1) request panel and process the returned panel xml, placing/drawing widgets on panel, and saving info as needed
 *  		2) send list of current states to server and wait for changes
 *  		3) receive change? set related widget(s) states, redraw widget(s), and go back to 2)
 *  		4) browser user clicks on widget? send "set state" command and go to 3)
 *  		5) error? go back to 2) 
 *  
 *  TODO: "grey-out" screen to indicate loss of server connection
 *  TODO: handle "&" in usernames (see Indicator Demo 00.xml)
 *  TODO: handle drawn ellipse (see LMRC APB)
 *  TODO: research movement of locoicons (will require "promoting" locoicon to system entity)
 *  TODO: finish layoutturntable (draw rays) (see Mtn RR and CnyMod27)
 *  TODO: address color differences between java panel and javascript panel (e.g. lightGray)
 *  TODO: determine proper level (z-index) for canvas layer
 *  TODO: diagnose and correct the small position issues visible with footscray
 *  TODO: diagnose and correct the hover dislocation on rotated images (or turn it off)
 *  TODO: deal with mouseleave, mouseout, touchout, etc. Slide off Stop button on rb1 for example.
 *  TODO: make turnout, levelXing occupancy work like LE panels (more than just checking A)
 *  TODO: draw dashed curves
 *  TODO: finish indicatorXXicon logic, handling occupancy and error states
 *  TODO: handle inputs/selection on various memory widgets
 *  TODO: improve visual of multisensorclick by sending all state changes in one message
 *  TODO: alignment of memoryIcons without fixed width is very different.  Recommended workaround is to use fixed width. 
 *   
 **********************************************************************************************/

//persistent (global) variables
var $gTimeout = 45; //heartbeat timeout in seconds
var $gWidgets = {}; //array of all widget objects, key=CSSId
var $gPanelList = {}; 	//store list of available panels
var $gPanel = {}; 	//store overall panel info
var $gPts = {}; 	//array of all points, key="pointname.pointtype" (used for layoutEditor panels)
var $gBlks = {}; 	//array of all blocks, key="blockname" (used for layoutEditor panels)
var $gXHRList;  	//persistent variable to allow aborting of superseded "list" connections
var $gCtx;  		//persistent context of canvas layer   
var $gDashArray = [12,12]; //on,off of dashed lines
var DOWNEVENT;  	//either mousedown or touchstart, based on device
var UPEVENT;    	//either mouseup or touchend, based on device
var SIZE = 3;  		//default factor for circles
var UNKNOWN = 		'1';  //constants to match JMRI state names
var ACTIVE =  		'2';
var CLOSED =  		'2';
var INACTIVE= 		'4';
var THROWN =  		'4';
var INCONSISTENT = 	'8';
var PT_CEN = ".1";  //named constants for point types
var PT_A = ".2";
var PT_B = ".3";
var PT_C = ".4";
var PT_D = ".5";
var LEVEL_XING_A = ".6";  
var LEVEL_XING_B = ".7";
var LEVEL_XING_C = ".8";
var LEVEL_XING_D = ".9";

var DARK        = 0x00;  //named constants for signalhead states
var RED         = 0x01;
var FLASHRED    = 0x02;
var YELLOW      = 0x04;
var FLASHYELLOW = 0x08;
var GREEN       = 0x10;
var FLASHGREEN  = 0x20;
var LUNAR       = 0x40;
var FLASHLUNAR  = 0x80;
var HELD		= 0x0100;  //additional to deal with "Held" pseudo-state

var RH_TURNOUT = 1; //named constants for turnout types
var LH_TURNOUT = 2;
var WYE_TURNOUT = 3;
var DOUBLE_XOVER = 4;
var RH_XOVER = 5;
var LH_XOVER = 6;
var SINGLE_SLIP = 7;
var DOUBLE_SLIP = 8;

//process the response returned for the requestPanelXML command
var $processPanelXML = function($returnedData, $success, $xhr) {

	$('div#messageText').text("rendering panel from xml, please wait...");
	$('div#workingMessage').show();
	var $xml = $($returnedData);  //jQuery-ize returned data for easier access

	//remove whitespace
	$xml.xmlClean();

	//get the panel-level values from the xml
	var $panel = $xml.find('panel');
	$($panel[0].attributes).each(function() {
		$gPanel[this.name] = this.value;
	});
	$('div#panelArea').width($gPanel.panelwidth);
	$('div#panelArea').height($gPanel.panelheight);
	$('div#workingMessage').width($gPanel.panelwidth);
	$('div#workingMessage').height($gPanel.panelheight);
	
	//insert the canvas layer and set up context used by layouteditor "drawn" objects, set some defaults 
	if ($gPanel.paneltype == "LayoutPanel") {
		$('div#panelArea').before("<canvas id='panelCanvas' width=" + $gPanel.panelwidth + "px height=" + 
				$gPanel.panelheight +"px style='position:absolute;z-index:2;'>");
		var canvas = document.getElementById("panelCanvas");  
		$gCtx = canvas.getContext("2d");  
		$gCtx.strokeStyle = $gPanel.defaulttrackcolor;
		$gCtx.lineWidth = $gPanel.sidetrackwidth;
		
		//set background color from panel attribute
		$('div#panelArea').css({backgroundColor: $gPanel.backgroundcolor});
	}

	//process all widgets in the panel xml, drawing them on screen, and building persistent arrays
	$panel.contents().each( 
			function() {
				//convert attributes to an object array
				var $widget = new Array();
				$widget['widgetType'] = this.nodeName;
				$(this.attributes).each(function() {
					$widget[this.name] = this.value;
				});
				//default various css attributes to not-set, then set in later code as needed
				var $hoverText = "";  

				//add and normalize the various type-unique values, from the various spots they are stored
				//  icons named based on states returned from xmlio server, 
				$widget['state'] = UNKNOWN; //initial state is unknown
				$widget['element']  =	""; //default to no xmlio type (avoid undefined)
				$widget['scale']  =	"1.0"; //default to no scale
				$widget['degrees']  =	0.00; //default to no rotation
				$widget['id'] = "spWidget_" + $gUnique();//set id to a unique value (since same element can be in multiple widgets)
				$widget['widgetFamily'] = $getWidgetFamily($widget);
				var $jc = "";
				if ($widget.class != undefined) {
					var $ta = $widget.class.split('.'); //get last part of java class name for a css class
					$jc = $ta[$ta.length - 1];
				}
				$widget['classes'] = $widget.widgetType + " " + $widget.widgetFamily + " rotatable " + $jc;
				if ($widget.momentary == "true") {
					$widget.classes += " momentary ";
				}
				if ($widget.hidden == "yes") {
					$widget.classes += " hidden ";
				}
				//set additional values in this widget
				switch ($widget.widgetFamily) {
				case "icon" :
					switch ($widget.widgetType) {
					case "positionablelabel" :
						$widget['icon1'] = 		$(this).find('icon').attr('url');
						var $rotation = 		$(this).find('icon').find('rotation').text();
						$widget['degrees'] = 	($(this).find('icon').attr('degrees') * 1) - ($rotation * 90);
						$widget['scale'] = 		$(this).find('icon').attr('scale');
						break;
					case "linkinglabel" :
						$widget['icon1'] = 		$(this).find('icon').attr('url');
						var $rotation = 		$(this).find('icon').find('rotation').text();
						$widget['degrees'] = 	($(this).find('icon').attr('degrees') * 1) - ($rotation * 90);
						$widget['scale'] = 		$(this).find('icon').attr('scale');
						$url = $(this).find('url').text();
						$widget['url'] = $url; //default to using url value as is 		
						if ($widget.forcecontroloff != "true") {
							$widget.classes += 		$widget.element + " clickable ";
						}
						break;
					case "indicatortrackicon" :
						$widget['icon1'] = 		$(this).find('iconmap').find('ClearTrack').attr('url');
						var $rotation = 		$(this).find('iconmap').find('ClearTrack').find('rotation').text();
						$widget['degrees'] = 	($(this).find('iconmap').find('ClearTrack').attr('degrees') * 1) - ($rotation * 90);
						$widget['scale'] = 		$(this).find('iconmap').find('ClearTrack').attr('scale');
						break;
					case "indicatorturnouticon" :
						$widget['name']  =		$(this).find('turnout').text();; //normalize name
						$widget['element']  =	'turnout'; //what xmlio server calls this
						$widget['icon1'] = 		$(this).find('iconmaps').find('ClearTrack').find('BeanStateUnknown').attr('url');
						$widget['icon2'] = 		$(this).find('iconmaps').find('ClearTrack').find('TurnoutStateClosed').attr('url');
						$widget['icon4'] = 		$(this).find('iconmaps').find('ClearTrack').find('TurnoutStateThrown').attr('url');
						$widget['icon8'] = 		$(this).find('iconmaps').find('ClearTrack').find('BeanStateInconsistent').attr('url');
						var $rotation = 		$(this).find('iconmaps').find('ClearTrack').find('BeanStateUnknown').find('rotation').text();
						$widget['degrees'] = 	($(this).find('iconmaps').find('ClearTrack').find('BeanStateUnknown').attr('degrees') * 1) - ($rotation * 90);
						$widget['scale'] = 		$(this).find('iconmaps').find('ClearTrack').find('BeanStateUnknown').attr('scale');
						if ($widget.forcecontroloff != "true") {
							$widget.classes += 		$widget.element + " clickable ";
						}
						break;
					case "turnouticon" :
						$widget['name']  =		$widget.turnout; //normalize name
						$widget['element']  =	"turnout"; //what xmlio server calls this
						$widget['icon1'] = 		$(this).find('icons').find('unknown').attr('url');
						$widget['icon2'] =  	$(this).find('icons').find('closed').attr('url');
						$widget['icon4'] =  	$(this).find('icons').find('thrown').attr('url');
						$widget['icon8'] =		$(this).find('icons').find('inconsistent').attr('url');
						var $rotation = 		$(this).find('icons').find('unknown').find('rotation').text();
						$widget['degrees'] = 	($(this).find('icons').find('unknown').attr('degrees') * 1) - ($rotation * 90);
						$widget['scale'] = 		$(this).find('icons').find('unknown').attr('scale');
						if ($widget.forcecontroloff != "true") {
							$widget.classes += 		$widget.element + " clickable ";
						}
						break;
					case "sensoricon" :
						$widget['name']  =		$widget.sensor; //normalize name
						$widget['element']  =	"sensor"; //what xmlio server calls this
						$widget['icon1'] = 		$(this).find('unknown').attr('url');
						$widget['icon2'] =  	$(this).find('active').attr('url');
						$widget['icon4'] =  	$(this).find('inactive').attr('url');
						$widget['icon8'] =		$(this).find('inconsistent').attr('url');
						var $rotation = 		$(this).find('unknown').find('rotation').text();
						$widget['degrees'] = 	($(this).find('unknown').attr('degrees') * 1) - ($rotation * 90);
						$widget['scale'] = 		$(this).find('unknown').attr('scale');
						if ($widget.forcecontroloff != "true") {
							$widget.classes += 		$widget.element + " clickable ";
						}
						break;
					case "signalheadicon" :
						$widget['name']  =		$widget.signalhead; //normalize name
						$widget['element']  =	"signalHead"; //what xmlio server calls this
						$widget['icon' + HELD] =  	$(this).find('icons').find('held').attr('url');
						$widget['icon' + DARK] 	=	$(this).find('icons').find('dark').attr('url');
						$widget['icon' + RED] =  	$(this).find('icons').find('red').attr('url');
						if ($widget['icon' + RED] == undefined) { //look for held if no red
							$widget['icon' + RED] =  	$(this).find('icons').find('held').attr('url');
						}
						$widget['icon' + YELLOW] 	=   $(this).find('icons').find('yellow').attr('url');
						$widget['icon' + GREEN] 	=	$(this).find('icons').find('green').attr('url');
						$widget['icon' + FLASHRED] =	$(this).find('icons').find('flashred').attr('url');
						$widget['icon' + FLASHYELLOW] =	$(this).find('icons').find('flashyellow').attr('url');
						$widget['icon' + FLASHGREEN] =	$(this).find('icons').find('flashgreen').attr('url');
						$widget['icon' + LUNAR] 	=	$(this).find('icons').find('lunar').attr('url');
						$widget['icon' + FLASHLUNAR] =	$(this).find('icons').find('lunar').attr('url');
						var $rotation = 		$(this).find('icons').find('dark').find('rotation').text();
						$widget['degrees'] = 	($(this).find('icons').find('dark').attr('degrees') * 1) - ($rotation * 90);
						$widget['scale'] = 		$(this).find('icons').find('dark').attr('scale');
						if ($widget.forcecontroloff != "true") {
							$widget.classes += 		$widget.element + " clickable ";
						}
						break;
					case "signalmasticon" :
						$widget['name']  =		$widget.signalmast; //normalize name
						$widget['element']  =	"signalMast"; //what xmlio server calls this
						var icons = $(this).find('icons').children(); //get array of icons
						icons.each(function(i, item) {  //loop thru icons array and set all iconXX urls for widget
							$widget['icon'+item.nodeName] = $(item).attr('url'); 
						});
						$widget['degrees'] = 	$(this).attr('degrees') * 1;
						$widget['scale'] = 		$(this).attr('scale');
						if ($widget.forcecontroloff != "true") {
							$widget.classes += 		$widget.element + " clickable ";
						}
						$widget['state'] = "Clear"; //set the default to a likely icon
						break;
					case "multisensoricon" :
						//create multiple widgets, 1st with all images, stack others with non-active states set to a clear image
						//  set up siblings array so each widget can also set state of the others
						$widget['element']  =	"sensor"; //what xmlio server calls this
						$widget['icon1'] = 		$(this).find('unknown').attr('url');
						$widget['icon4'] =  	$(this).find('inactive').attr('url');
						$widget['icon8'] =		$(this).find('inconsistent').attr('url');
						var $rotation = 		$(this).find('unknown').find('rotation').text();
						$widget['degrees'] = 	($(this).find('unknown').attr('degrees') * 1) - ($rotation * 90);
						$widget['scale'] = 		$(this).find('unknown').attr('scale');
						if ($widget.forcecontroloff != "true") {
							$widget.classes += 		$widget.element + " clickable ";
						}
						$widget['siblings'] = new Array();  //array of related multisensors
						$widget['hoverText'] = "";  		//for override of hovertext
						var actives = $(this).find('active'); //get array of actives used by this multisensor
						var $id = $widget.id;
						actives.each(function(i, item) {  //loop thru array once to set up siblings array, to be copied to all siblings
							$widget.siblings.push($id);
							$widget.hoverText += $(item).attr('sensor') + " "; //add sibling names to hovertext
							$id = "spWidget_" + $gUnique(); 	  	 // set new id
						});
						actives.each(function(i, item) {  //loop thru array again to create each widget
							$widget['id'] = $widget.siblings[i]; 	  	 // use id already set in sibling array
							$widget.name 	 = $(item).attr('sensor');
							$widget['icon2'] = 	$(item).attr('url');
							if (i < actives.size()-1) { //only save widget and make a new one if more than one active found
								$preloadWidgetImages($widget); //start loading all images
								$widget['safeName'] = $safeName($widget.name);  //add a html-safe version of name
								$gWidgets[$widget.id] = $widget; //store widget in persistent array
								$drawIcon($widget); //actually place and position the widget on the panel
								$widget = jQuery.extend(true, {}, $widget); //get a new copy of widget
								$widget['icon1'] = 	"/web/images/transparent_1x1.png";
								$widget['icon4'] =  "/web/images/transparent_1x1.png"; //set non-actives to transparent image 
								$widget['icon8'] =	"/web/images/transparent_1x1.png";
								$widget['state'] = ACTIVE; //to avoid sizing based on the transparent image
							}
						});
						break;
					}
					$preloadWidgetImages($widget); //start loading all images
					$widget['safeName'] = $safeName($widget.name);  //add a html-safe version of name
					$gWidgets[$widget.id] = $widget; //store widget in persistent array
					$drawIcon($widget); //actually place and position the widget on the panel
					break;

				case "text" :
					$widget['styles'] = $getTextCSSFromObj($widget);
					switch ($widget.widgetType) {
					case "sensoricon" :
						$widget['name']  =		$widget.sensor; //normalize name
						$widget['element']  =	"sensor"; //what xmlio server calls this
						//set each state's text
						$widget['text1'] = 		$(this).find('unknownText').attr('text');
						$widget['text2'] =  	$(this).find('activeText').attr('text');
						$widget['text4'] =  	$(this).find('inactiveText').attr('text');
						$widget['text8'] =		$(this).find('inconsistentText').attr('text');
						//set each state's css attribute array (text color, etc.)
						$widget['css1'] = 		$getTextCSSFromObj($getObjFromXML($(this).find('unknownText')[0]));
						$widget['css2'] = 		$getTextCSSFromObj($getObjFromXML($(this).find('activeText')[0]));
						$widget['css4'] = 		$getTextCSSFromObj($getObjFromXML($(this).find('inactiveText')[0]));
						$widget['css8'] = 		$getTextCSSFromObj($getObjFromXML($(this).find('inconsistentText')[0]));
						if ($widget.name != undefined && $widget.forcecontroloff != "true") {
							$widget.classes += 		$widget.element + " clickable ";
						}
						break;
					case "locoicon" :
					case "trainicon" :
						//also set the background icon for this one (additional css in .html file)
						$widget['icon1'] = 		$(this).find('icon').attr('url');
						$widget.styles['background-image'] 	= "url('" + $widget.icon1 + "')";
						$widget['scale'] = 		$(this).find('icon').attr('scale');
						if ($widget.scale != 1.0) {
							$widget.styles['background-size']	= $widget.scale * 100 + "%";
							$widget.styles['line-height']	= $widget.scale * 20 + "px";  //center vertically
						}
						break;
					case "fastclock" :
						$widget['name'] = 		'IMCURRENTTIME';
						$widget['element']  =	'memory';
						$widget.styles['width'] 	= "166px";  //hard-coded to match original size of clock image
						$widget.styles['height'] 	= "166px";
						$widget['scale'] = 		$(this).attr('scale');
						if ($widget.level == undefined) {
							$widget['level'] =		10;  //if not included in xml
						}
						$widget['text']  =		"00:00 AM";
						$widget['state']  =		"00:00 AM";
						break;
					case "memoryicon" :
						$widget['name']  =		$widget.memory; //normalize name
						$widget['element']  =	"memory"; //what xmlio server calls this
						$widget['text']  =		$widget.memory; //use name for initial text
						break;
					case "memoryInputIcon" :
					case "memoryComboIcon" :
						$widget['name']  =		$widget.memory; //normalize name
						$widget['element']  =	"memory"; //what xmlio server calls this
						$widget['text']  =		$widget.memory; //use name for initial text
						$widget.styles['border']	= "1px solid black" //add border for looks (temporary)
						break;
					case "linkinglabel" :
						$url = $(this).find('url').text();
						$widget['url'] = $url; //just store url value in widget, for use in click handler 		
						if ($widget.forcecontroloff != "true") {
							$widget.classes += 		$widget.element + " clickable ";
						}
						break;
					}
					$widget['safeName'] = $safeName($widget.name);
					$gWidgets[$widget.id] = $widget; //store widget in persistent array
					//put the text element on the page
					$("div#panelArea").append("<div id=" + $widget.id + " class='"+$widget.classes+"'>" + $widget.text + "</div>");
					$("div#panelArea>#"+$widget.id).css( $widget.styles ); //apply style array to widget
					$setWidgetPosition($("div#panelArea>#"+$widget.id));
					break;

				case "drawn" :
					switch ($widget.widgetType) {
					case "positionablepoint" :
						//just store these points in persistent variable for use when drawing tracksegments and layoutturnouts
						//id is ident plus ".type", e.g. "A4.2"
						$gPts[$widget.ident+"."+$widget.type] = $widget;
						if ($widget.ident.substring(0,2) == "EB") {  //End bumpers use wrong type, so also store type 1
							$gPts[$widget.ident+".1"] = $widget;
						}
						break;
					case "layoutblock" :
						$widget['state'] = UNKNOWN;  //add a state member for this block
						//store these blocks in a persistent var
						//id is username
						$gBlks[$widget.username] = $widget;
						break;
					case "layoutturnout" :
						$widget['name']  =		$widget.turnoutname; //normalize name
						$widget['safeName'] = 	$safeName($widget.name);  //add a html-safe version of name
						$widget['element']  =	"turnout"; //what xmlio server calls this
						$widget['x']  		=	$widget.xcen; //normalize x,y 
						$widget['y']  		=	$widget.ycen;
						if ($widget.name != undefined) { //make it clickable (unless no turnout assigned)
							$widget.classes	+= 	$widget.element + " clickable ";
						}
						//set widget occupancysensor from block to speed affected changes later
						if ($gBlks[$widget.blockname] != undefined) {
							$widget['occupancysensor'] = $gBlks[$widget.blockname].occupancysensor; 
							$widget['occupancystate'] =  $gBlks[$widget.blockname].state; 
						}
						$gWidgets[$widget.id] = $widget; //store widget in persistent array 
						$storeTurnoutPoints($widget); //also store the turnout's 3 end points for other connections  
						$drawTurnout($widget); //draw the turnout  
						//add an empty, but clickable, div to the panel and position it over the turnout circle
						$hoverText = " title='"+$widget.name+"' alt='"+$widget.name+"'";
						$("div#panelArea").append("<div id=" + $widget.id + " class='"+$widget.classes+"' "+ $hoverText +"></div>");
						var $cr = $gPanel.turnoutcirclesize * SIZE;  //turnout circle radius
						var $cd = $cr * 2;
						$("div#panelArea>#"+$widget.id).css(
								{position:'absolute',left:($widget.x-$cr)+'px',top:($widget.y-$cr)+'px',zIndex:3,
									width:$cd+'px', height:$cd+'px'});
						break;
					case "tracksegment" :
						//set widget occupancysensor from block to speed affected changes later
						if ($gBlks[$widget.blockname] != undefined) {
							$widget['occupancysensor'] = $gBlks[$widget.blockname].occupancysensor; 
							$widget['occupancystate'] =  $gBlks[$widget.blockname].state; 
						}
						//store this widget in persistent array, with ident as key
						$widget['id'] = $widget.ident; 
						$gWidgets[$widget.id] = $widget;
						//draw the tracksegment
						$drawTrackSegment($widget);
						break;
					case "levelxing" :
						$widget['x']  		=	$widget.xcen; //normalize x,y 
						$widget['y']  		=	$widget.ycen;
						//set widget occupancysensor from block to speed affected changes later
						//TODO: handle BD block
						if ($gBlks[$widget.blocknameac] != undefined) {
							$widget['blockname'] = $widget.blocknameac; //normalize blockname 
							$widget['occupancysensor'] = $gBlks[$widget.blocknameac].occupancysensor; 
							$widget['occupancystate'] =  $gBlks[$widget.blocknameac].state; 
						}
						//store widget in persistent array
						$gWidgets[$widget.id] = $widget; 
						//also store the xing's 4 end points for other connections
						$storeLevelXingPoints($widget);  
						//draw the xing
						$drawLevelXing($widget);  
						break;
					case "layoutturntable" :
						//just draw the circle for now, don't even store it
						$drawCircle($widget.xcen, $widget.ycen, $widget.radius); 
						break;
					case "backgroundColor" :  //set background color of the panel itself
						$('div#panelArea').css({"background-color" : "rgb(" + $widget.red + "," + $widget.green + "," + $widget.blue + ")"});
						break;
					}
					break;
				default:
					//log any unsupported widgets, listing childnodes as info
					$("div#logArea").append("<br />Unsupported: " + $widget.widgetType + ":"); 
					$(this.attributes).each(function(){
						$("div#logArea").append(" " + this.name);
					});
					$("div#logArea").append(" | ");
					$(this.childNodes).each(function(){
						$("div#logArea").append(" " + this.nodeName);
					});
					break;
				}
			}  //end of function
	);  //end of each

	//hook up mouseup state toggle function to non-momentary clickable widgets, except for multisensor and linkinglabel
	$('.clickable:not(.momentary):not(.multisensoricon):not(.linkinglabel)').bind(UPEVENT, $handleClick);
	
	//hook up mouseup state change function to multisensor (special handling)
	$('.clickable.multisensoricon').bind(UPEVENT, $handleMultiClick);
	
	//hook up mouseup function to linkinglabel (special handling)
	$('.clickable.linkinglabel').bind(UPEVENT, $handleLinkingLabelClick);
	
	//momentary widgets always go active on mousedown, and inactive on mouseup, current state is ignored
	$('.clickable.momentary').bind(DOWNEVENT, function(e) {
		$sendElementChange($gWidgets[this.id].element, $gWidgets[this.id].name, ACTIVE);  //send active on down
	}).bind(UPEVENT, function(e) {
		$sendElementChange($gWidgets[this.id].element, $gWidgets[this.id].name, INACTIVE);  //send inactive on up
	});
	
	$drawAllDrawnWidgets(); //draw all the drawn widgets once more, to address some bidirectional dependencies in the xml
	$('div#workingMessage').hide();
	$sendXMLIOList('<xmlio>' + $getXMLStateList() + '</xmlio>'); //send initial states to xmlio server
	endAndStartTimer(); //initiate the heartbeat timer

//	window.setTimeout(function(){  //wait three seconds and then redraw icon widgets, to (hopefully) give them a chance to load
//		$drawAllIconWidgets();  //TODO: not working, as non-FF browsers will scale objects _again_
//	}, 3000);

};    	

//perform regular click-handling, bound to click event for clickable, non-momentary widgets, except for multisensor and linkinglabel.
function $handleClick(e) {
	var $widget = $gWidgets[this.id];
    var $newState = $getNextState($widget);  //determine next state from current state
	$sendElementChange($widget.element, $widget.name, $newState);  //send new value to xmlio server
	if ($widget.secondturnoutname != undefined) {  //TODO: put this in a more logical place?
		$sendElementChange($widget.element, $widget.secondturnoutname, $newState);  //also send 2nd turnout
	}
};

//perform multisensor click-handling, bound to click event for clickable multisensor widgets.
function $handleMultiClick(e) {
	var $widget = $gWidgets[this.id];
	var clickX = e.pageX - this.offsetLeft;  //get click location on widget
	var clickY = e.pageY - this.offsetTop;
//find if we want to increment or decrement
	var dec = false;
	if ($widget.updown == "true") {
		if (clickY > this.height/2) dec = true;
	} else {
		if (clickX < this.width/2)  dec = true;
	}
	var displaying = 0;
	for (i in $widget.siblings) {  //determine which is currently active
		if ($gWidgets[$widget.siblings[i]].state == ACTIVE) {
			displaying = i; //flag the current active sibling
		}
	};
	var next;  //determine which is the next one which should be set to active
	if (dec) {
		next = displaying-1;
		if (next < 0) next = 0;
	} else {
		next = displaying*1 +1;
		if (next > i) next = i;
	}
	for (i in $widget.siblings) {  //loop through siblings and send changes as needed
		if (i == next) {
			if ($gWidgets[$widget.siblings[i]].state != ACTIVE) {
				$sendElementChange('sensor', $gWidgets[$widget.siblings[i]].name, ACTIVE);  //set next sensor to active and send command to xmlio server
			}
		} else {
			if ($gWidgets[$widget.siblings[i]].state != INACTIVE) {
				$sendElementChange('sensor', $gWidgets[$widget.siblings[i]].name, INACTIVE);  //set all other siblings to inactive if not already
			}
		}
	};
};

//perform click-handling of linkinglabel widgets (3 cases: complete url or frame:<name> where name is a panel or a frame)
function $handleLinkingLabelClick(e) {
	var $widget = $gWidgets[this.id];
	var $url = $widget.url; 
	if ($url.toLowerCase().indexOf("frame:") == 0) {
		$frameName = $url.substring(6); //if "frame" found, remove it
		$frameType = $gPanelList[$frameName];  //find panel type in panel list
		if ($frameType == undefined) {  
			$url = "/frame/" + $frameName + ".html"; //not in list, open using frameserver  
		} else {  
			$url = "?name=" + $frameType + "/" + $frameName; //format for panel server  
		}
	}
	window.location = $url;  //navigate to the specified url
};


//draw a Circle (color and width are optional)
function $drawCircle($ptx, $pty, $radius, $color, $width) {
	var $savStrokeStyle = $gCtx.strokeStyle;
	var $savLineWidth = $gCtx.lineWidth;
	if ($color != undefined)  $gCtx.strokeStyle = $color;
	if ($width != undefined)  $gCtx.lineWidth = $width;
	$gCtx.beginPath();
	$gCtx.arc($ptx, $pty, $radius, 0, 2*Math.PI, false);
	$gCtx.stroke();
	// put color and widths back to default
	$gCtx.lineWidth = $savLineWidth;    
	$gCtx.strokeStyle = $savStrokeStyle;    
};

//draw a Tracksegment (pass in widget)
function $drawTrackSegment($widget) {
	//if set to hidden, don't draw anything
	if ($widget.hidden == "yes") {
		return;
	}
	
	//get the endpoints by name
	var $pt1 = $gPts[$widget.connect1name+"."+$widget.type1];
	if ($pt1 == undefined) {
		if (window.console) console.log("can't draw tracksegment "+$widget.connect1name+"."+$widget.type1+" undefined (1)");
		return;
	}
	var $pt2 = $gPts[$widget.connect2name+"."+$widget.type2];
	if ($pt2 == undefined) {
		if (window.console) console.log("can't draw tracksegment "+$widget.connect2name+"."+$widget.type2+" undefined (2)");
		return;
	}
	
	//	set trackcolor based on block occupancy state
	var $color = $gPanel.defaulttrackcolor;
	var $blk = $gBlks[$widget.blockname];
	if ($blk != undefined) {
		if ($blk.occupiedsense == $blk.state) { //set the color based on occupancy state
			$color = $blk.occupiedcolor;
		} else {
			$color = $blk.trackcolor;
		}
	}

	var $width = $gPanel.sidetrackwidth;
	if ($widget.mainline == "yes") {
		$width = $gPanel.mainlinetrackwidth;
	}
	if ($widget.angle == undefined) {
		//draw straight line between the points
		if ($widget.dashed == "yes") {
			$drawDashedLine($pt1.x, $pt1.y, $pt2.x, $pt2.y,  $color, $width, $gDashArray);	
		} else {
			$drawLine($pt1.x, $pt1.y, $pt2.x, $pt2.y, $color, $width);
		}
	} else {
		//draw curved line 
		if ($widget.flip == "yes") {
			$drawArc($pt2.x, $pt2.y, $pt1.x, $pt1.y, $widget.angle, $color, $width);
		} else {
			$drawArc($pt1.x, $pt1.y, $pt2.x, $pt2.y, $widget.angle, $color, $width);
		}
	}
};
//drawLine, passing in values from xml
function $drawDashedLine($pt1x, $pt1y, $pt2x, $pt2y, $color, $width, dashArray) {
	var $savLineWidth = $gCtx.lineWidth;
	var $savStrokeStyle = $gCtx.strokeStyle;
	if ($color != undefined)  $gCtx.strokeStyle = $color;
	if ($width != undefined)  $gCtx.lineWidth = $width;
	$gCtx.beginPath();  
	$gCtx.dashedLine($pt1x, $pt1y, $pt2x, $pt2y, dashArray);  
	$gCtx.closePath();  
	$gCtx.stroke();  
	// put color and width back to default
	$gCtx.strokeStyle = $savStrokeStyle;    
	$gCtx.lineWidth = $savLineWidth;    
};    	

//dashed line code copied from: http://stackoverflow.com/questions/4576724/dotted-stroke-in-canvas
var CP = window.CanvasRenderingContext2D && CanvasRenderingContext2D.prototype;
if (CP.lineTo) {
    CP.dashedLine = function(x, y, x2, y2, da) {
        if (!da) da = [10,5];
        this.save();
        var dx = (x2-x), dy = (y2-y);
        var len = Math.sqrt(dx*dx + dy*dy);
        var rot = Math.atan2(dy, dx);
        this.translate(x, y);
        this.moveTo(0, 0);
        this.rotate(rot);       
        var dc = da.length;
        var di = 0, draw = true;
        x = 0;
        while (len > x) {
            x += da[di++ % dc];
            if (x > len) x = len;
            draw ? this.lineTo(x, 0): this.moveTo(x, 0);
            draw = !draw;
        }       
        this.restore();
    }
}

//draw the analog clock (pass in widget), called on each update of clock
function $drawClock($widget) {
	var $fs = $widget.scale * 100;  //scale percentage, used for text
	var $fcr = $gWidgets['IMRATEFACTOR'].state * 1; //get the fast clock rate factor from its widget
	var $h = "";
	$h += "<div class='clocktext' style='font-size:" + $fs + "%;' >" + $widget.state + "<br />" + $fcr +":1</div>";  //add the text
	$h += "<img class='clockface' src='/web/images/clockface.png' />"; 				//add the clockface
	$h += "<img class='clockhourhand' src='/web/images/clockhourhand.png' />"; 		//add the hour hand
	$h += "<img class='clockminutehand' src='/web/images/clockminutehand.png' />"; 	//add the minute hand
	$("div#panelArea>#"+$widget.id).html($h); //set the html for the widget
	
	var hours = $widget.state.split(':')[0]; 	//extract hours from format "H:MM AM"
	var mins =  $widget.state.split(':')[1].split(' ')[0]; //extract minutes
    var hdegree = hours * 30 + (mins / 2);
    var hrotate = "rotate(" + hdegree + "deg)";
    $("div.fastclock>img.clockhourhand").css({ "transform": hrotate}); //set rotation for hour hand
    var mdegree = mins * 6;
    var mrotate = "rotate(" + mdegree + "deg)";
    $("div.fastclock>img.clockminutehand").css({ "transform" : mrotate }); //set rotation for minute hand
}

//draw an icon-type widget (pass in widget)
function $drawIcon($widget) {
	var $hoverText = "";
	if ($widget.hoverText != undefined) {
		$hoverText = " title='"+ $widget.hoverText+"' alt='"+$widget.hoverText+"'";
	}
	if ($hoverText == "" && $widget.name != undefined) { //if name available, use it as hover text if still blank
		$hoverText = " title='"+$widget.name+"' alt='"+$widget.name+"'";
	}

	//add the image to the panel area, with appropriate css classes and id (skip any unsupported)
	if ($widget['icon'+$widget.state] != undefined) {
		$imgHtml = "<img id=" + $widget.id + " class='" + $widget.classes +
		"' src='" + $widget["icon"+$widget['state']] + "' " + $hoverText + "/>"

		$("div#panelArea").append($imgHtml);  //put the html in the panel

		//add in overlay text if specified  (append "overlay" to id to keep them unique)
		if ($widget.text != undefined) {
			$("div#panelArea").append("<div id=" + $widget.id + "overlay class='overlay'>" + $widget.text + "</div>");
			$("div#panelArea>#"+$widget.id+"overlay").css({position:'absolute',left:$widget.x+'px',top:$widget.y+'px',zIndex:($widget.level-1)});
		}
	}
	$setWidgetPosition($("div#panelArea #"+$widget.id));

};

//draw a LevelXing (pass in widget)
function $drawLevelXing($widget) {
	//if set to hidden, don't draw anything
	if ($widget.hidden == "yes") {
		return;
	}
	var $width = $gPanel.sidetrackwidth;
	 //set levelxing width same as the A track
	if ($gWidgets[$widget.connectaname] !=undefined) {
		if ($gWidgets[$widget.connectaname].mainline == "yes") {
			$width = $gPanel.mainlinetrackwidth; 
		}
	} else {
//		if (window.console) console.log("could not get trackwidth of "+$widget.connectaname+" for "+$widget.name);
	}
	if ($gPanel.turnoutcircles == "yes") {
		$drawCircle($widget.xcen, $widget.ycen, $gPanel.turnoutcirclesize * SIZE, $gPanel.turnoutcirclecolor, 1);
	}
	//	set trackcolor based on block occupancy state of AC block
	var $color = $gPanel.defaulttrackcolor;
	var $blk = $gBlks[$widget.blocknameac];
	if ($blk != undefined) {
		if ($blk.occupiedsense == $blk.state) { //set the color based on occupancy state
			$color = $blk.occupiedcolor;
		} else {
			$color = $blk.trackcolor;
		}
	}
	var cenx = $widget.xcen;
	var ceny = $widget.ycen
	var ax = $gPts[$widget.ident+LEVEL_XING_A].x;  //retrieve the points
	var ay = $gPts[$widget.ident+LEVEL_XING_A].y;
	var bx = $gPts[$widget.ident+LEVEL_XING_B].x;
	var by = $gPts[$widget.ident+LEVEL_XING_B].y;
	var cx = $gPts[$widget.ident+LEVEL_XING_C].x;
	var cy = $gPts[$widget.ident+LEVEL_XING_C].y;
	var	dx = $gPts[$widget.ident+LEVEL_XING_D].x;
	var	dy = $gPts[$widget.ident+LEVEL_XING_D].y;

	//levelxing   A
	//          D-+-B
	//            C
	$drawLine(ax, ay, cx, cy, $color, $width); //A to B
	$drawLine(dx, dy, bx, by, $color, $width); //D to B
};

//draw a Turnout (pass in widget)
//  see LayoutEditor.drawTurnouts()
function $drawTurnout($widget) {
	//if set to hidden, don't draw anything
	if ($widget.hidden == "yes") {
		return;
	}
	var $width = $gPanel.sidetrackwidth;
	 //set turnout width same as the A track
	if ($gWidgets[$widget.connectaname] !=undefined) {
		if ($gWidgets[$widget.connectaname].mainline == "yes") {
			$width = $gPanel.mainlinetrackwidth; 
		}
	} else {
//		if (window.console) console.log("could not get trackwidth of "+$widget.connectaname+" for "+$widget.name);
	}
	var cenx = $widget.xcen;
	var ceny = $widget.ycen
	var ax = $gPts[$widget.ident+PT_A].x;
	var ay = $gPts[$widget.ident+PT_A].y;
	var bx = $gPts[$widget.ident+PT_B].x;
	var by = $gPts[$widget.ident+PT_B].y;
	var cx = $gPts[$widget.ident+PT_C].x;
	var cy = $gPts[$widget.ident+PT_C].y;
	var abx = (ax*1)+((bx-ax)*0.5); // midpoint AB
	var aby = (ay*1)+((by-ay)*0.5);
	if ($gPanel.turnoutdrawunselectedleg == 'yes'){ //only calculate midpoints if needed
		var cenbx = (cenx*1)+((bx-cenx)*0.5); // midpoint cenB
		var cenby = (ceny*1)+((by-ceny)*0.5);
		var cencx = (cenx*1)+((cx-cenx)*0.5); // midpoint cenC
		var cency = (ceny*1)+((cy-ceny)*0.5);
	}
	var dx, dy, dcx, dcy;
	if ($gPts[$widget.ident+PT_D] != undefined) {
		dx = $gPts[$widget.ident+PT_D].x;
		dy = $gPts[$widget.ident+PT_D].y;
		dcx = (dx*1)+((cx-dx)*0.5); // midpoint DC
		dcy = (dy*1)+((cy-dy)*0.5);
	}
	var erase = $gPanel.backgroundcolor;
	
	//	set trackcolor based on block occupancy state
	var $color = $gPanel.defaulttrackcolor;
	var $blk = $gBlks[$widget.blockname];
	if ($blk != undefined) {
		if ($blk.occupiedsense == $blk.state) { //set the color based on occupancy state
			$color = $blk.occupiedcolor;
		} else {
			$color = $blk.trackcolor;
		}
	}

	//turnout A--B
	//         \-C
	if ($widget.type==LH_TURNOUT||$widget.type==RH_TURNOUT||$widget.type==WYE_TURNOUT) {
		//if closed or thrown, draw the selected leg in the default track color and erase the other one
		if ($widget.state == CLOSED || $widget.state == THROWN) {
			if ($widget.state == $widget.continuing) {
				$drawLine(cenx, ceny, cx, cy, erase, $width); //erase center to C (diverging leg)
				if ($gPanel.turnoutdrawunselectedleg == 'yes'){
					$drawLine(cx, cy, cencx, cency, $color, $width); //C to midC (diverging leg)
				}
				$drawLine(cenx, ceny, bx, by, $color, $width); //center to B (straight leg)
			} else {
				$drawLine(cenx, ceny, bx, by, erase, $width); //erase center to B (straight leg)
				if ($gPanel.turnoutdrawunselectedleg == 'yes'){
					$drawLine(bx, by, cenbx, cenby, $color, $width); //B to midB (straight leg)
				}
				$drawLine(cenx, ceny, cx, cy, $color, $width); //center to C (diverging leg)
			}
		} else {  //if undefined, draw both legs
			$drawLine(cenx, ceny, bx, by, $color, $width); //center to B (straight leg)
			$drawLine(cenx, ceny, cx, cy, $color, $width); //center to C (diverging leg)
		}
		$drawLine(ax, ay, cenx, ceny, $color, $width); //A to center (incoming)
	// xover A--B
	//         D--C
	} else if ($widget.type==LH_XOVER||$widget.type==RH_XOVER||$widget.type==DOUBLE_XOVER) {
		if ($widget.state == CLOSED || $widget.state == THROWN) {
			$drawLine(ax, ay, bx, by, erase, $width); //erase A to B
			$drawLine(dx, dy, cx, cy, erase, $width); //erase D to C
			$drawLine(abx, aby, dcx, dcy, erase, $width); //erase midAB to midDC
			$drawLine(abx, aby, dcx, dcy, erase, $width); //erase midAB to midDC
			$drawLine(ax, ay, cx, cy, erase, $width); //erase A to C
			$drawLine(dx, dy, bx, by, erase, $width); //erase D to B
			if ($widget.state == $widget.continuing) {
				$drawLine(ax, ay, bx, by, $color, $width); //A to B
				$drawLine(dx, dy, cx, cy, $color, $width); //D to C
			} else {
				if ($widget.type==DOUBLE_XOVER) {
					$drawLine(ax, ay, cx, cy, $color, $width); //A to C
					$drawLine(dx, dy, bx, by, $color, $width); //D to B
				} else if ($widget.type==RH_XOVER) {
					$drawLine(ax, ay, abx, aby, $color, $width); //A to midAB
					$drawLine(abx, aby, dcx, dcy, $color, $width); //midAB to midDC
					$drawLine(dcx, dcy, cx, cy, $color, $width); //midDC to C
				} else {  //LH_XOVER
					$drawLine(bx, by, abx, aby, $color, $width); //B to midAB
					$drawLine(abx, aby, dcx, dcy, $color, $width); //midAB to midDC
					$drawLine(dcx, dcy, dx, dy, $color, $width); //midDC to D
				}
			}
		}
	}
	if ($gPanel.turnoutcircles == "yes") {  //draw turnout circle if requested
		$drawCircle($widget.xcen, $widget.ycen, $gPanel.turnoutcirclesize * SIZE, $gPanel.turnoutcirclecolor, 1);
	}
};

//store the various points defined with a Turnout (pass in widget)
//see jmri.jmrit.display.layoutEditor.LayoutTurnout.java for background
function $storeTurnoutPoints($widget) {
var $t = [];
$t['ident'] = $widget.ident+PT_B;  //store B endpoint
$t['x'] = $widget.xb * 1.0;
$t['y'] = $widget.yb * 1.0;
$gPts[$t.ident] = $t;
$t = [];
$t['ident'] = $widget.ident+PT_C;  //store C endpoint
$t['x'] = $widget.xc * 1.0;
$t['y'] = $widget.yc * 1.0;
$gPts[$t.ident] = $t;
if ($widget.type==LH_TURNOUT||$widget.type==RH_TURNOUT||$widget.type==WYE_TURNOUT) {
	$t = [];
	$t['ident'] = $widget.ident+PT_A;  //calculate and store A endpoint (mirror of B for these)
	$t['x'] = $widget.xcen - ($widget.xb - $widget.xcen);
	$t['y'] = $widget.ycen - ($widget.yb - $widget.ycen);
	$gPts[$t.ident] = $t;
} else if ($widget.type==LH_XOVER||$widget.type==RH_XOVER||$widget.type==DOUBLE_XOVER) {
	$t = [];
	$t['ident'] = $widget.ident+PT_A;  //calculate and store A endpoint (mirror of C for these)
	$t['x'] = $widget.xcen - ($widget.xc - $widget.xcen);
	$t['y'] = $widget.ycen - ($widget.yc - $widget.ycen);
	$gPts[$t.ident] = $t;
	$t = [];
	$t['ident'] = $widget.ident+PT_D;  //calculate and store D endpoint (mirror of B for these)
	$t['x'] = $widget.xcen - ($widget.xb - $widget.xcen);
	$t['y'] = $widget.ycen - ($widget.yb - $widget.ycen);
	$gPts[$t.ident] = $t;
}
};    	

//store the various points defined with a LevelXing (pass in widget)
//see jmri.jmrit.display.layoutEditor.LevelXing.java for background
function $storeLevelXingPoints($widget) {
	var $t = [];
	$t['ident'] = $widget.ident+LEVEL_XING_A;  //store A endpoint
	$t['x'] = $widget.xa * 1.0;
	$t['y'] = $widget.ya * 1.0;
	$gPts[$t.ident] = $t;
	$t = [];
	$t['ident'] = $widget.ident+LEVEL_XING_B;  //store B endpoint
	$t['x'] = $widget.xb * 1.0;
	$t['y'] = $widget.yb * 1.0;
	$gPts[$t.ident] = $t;
	$t = [];
	$t['ident'] = $widget.ident+LEVEL_XING_C;  //calculate and store A endpoint (mirror of A for these)
	$t['x'] = $widget.xcen - ($widget.xa - $widget.xcen);
	$t['y'] = $widget.ycen - ($widget.ya - $widget.ycen);
	$gPts[$t.ident] = $t;
	$t = [];
	$t['ident'] = $widget.ident+LEVEL_XING_D;  //calculate and store D endpoint (mirror of B for these)
	$t['x'] = $widget.xcen - ($widget.xb - $widget.xcen);
	$t['y'] = $widget.ycen - ($widget.yb - $widget.ycen);
	$gPts[$t.ident] = $t;
};    	

//drawLine, passing in values from xml
function $drawLine($pt1x, $pt1y, $pt2x, $pt2y, $color, $width) {
	var $savLineWidth = $gCtx.lineWidth;
	var $savStrokeStyle = $gCtx.strokeStyle;
	if ($color != undefined)  $gCtx.strokeStyle = $color;
	if ($width != undefined)  $gCtx.lineWidth = $width;
	$gCtx.beginPath();  
	$gCtx.moveTo($pt1x, $pt1y);  
	$gCtx.lineTo($pt2x, $pt2y);  
	$gCtx.closePath();  
	$gCtx.stroke();  
	// put color and width back to default
	$gCtx.strokeStyle = $savStrokeStyle;    
	$gCtx.lineWidth = $savLineWidth;    
};    	

//drawArc, passing in values from xml
function $drawArc(pt1x, pt1y, pt2x, pt2y, degrees, $color, $width) {
    // Compute arc's chord
    var a = pt2x - pt1x;
    var o = pt2y - pt1y;
    var chord=Math.sqrt(((a*a)+(o*o))); //in pixels

    if (chord > 0.0) {  //don't bother if no length
    	//save track settings for restore
    	var $savLineWidth = $gCtx.lineWidth;
    	var $savStrokeStyle = $gCtx.strokeStyle;
    	if ($color != undefined)  $gCtx.strokeStyle = $color;
    	if ($width != undefined)  $gCtx.lineWidth = $width;

    	var halfAngle = (degrees/2) * Math.PI / 180; //in radians
        var radius = (chord/2)/(Math.sin(halfAngle));  //in pixels
        // Circle
        var startRad = Math.atan2(a, o) - halfAngle; //in radians
        // calculate center of circle
        var cx = ((pt2x * 1.0) - Math.cos(startRad) * radius);  
        var cy = ((pt2y * 1.0) + Math.sin(startRad) * radius);

        //calculate start and end angle
        var startAngle 	= Math.atan2(pt1y-cy, pt1x-cx); //in radians
        var endAngle 	= Math.atan2(pt2y-cy, pt2x-cx); //in radians
        var counterClockwise = false;

		$gCtx.beginPath();
        $gCtx.arc(cx, cy, radius, startAngle, endAngle, counterClockwise);
		$gCtx.stroke();
    	// put color and width back to default
    	$gCtx.strokeStyle = $savStrokeStyle;    
    	$gCtx.lineWidth = $savLineWidth;    
    }
}

//set object attributes from xml attributes, returning object 
var $getObjFromXML = function(e){
	var $widget = {};
	$(e.attributes).each(function() {
		$widget[this.name] = this.value;
	});
	return $widget;
};    	

//build and return CSS array from attributes passed in
var $getTextCSSFromObj = function($widget){
	var $retCSS = {};
	$retCSS['color'] = '';  //only clear attributes 
	$retCSS['background-color'] = '';
	if ($widget.red != undefined) {
		$retCSS['color'] = "rgb(" + $widget.red + "," + $widget.green + "," + $widget.blue + ") ";
	}
	if ($widget.redBack != undefined) {
		$retCSS['background-color'] = "rgb(" + $widget.redBack + "," + $widget.greenBack + "," + $widget.blueBack + ") ";
	}
	if ($widget.size != undefined) {
		$retCSS['font-size'] = $widget.size + "px ";
	}
	if ($widget.margin != undefined) {
		$retCSS['padding'] = $widget.margin + "px ";
	}
	if ($widget.borderSize != undefined) {
		$retCSS['border-width'] = $widget.borderSize + "px ";
	}
	if ($widget.redBorder != undefined) {
		$retCSS['border-color'] = "rgb(" + $widget.redBorder + "," + $widget.greenBorder + "," + $widget.blueBorder + ") ";;
		$retCSS['border-style'] = 'solid';
	}
	if ($widget.fixedWidth != undefined) {
		$adj = 0;
		if ($widget.margin != undefined) { $adj = $widget.margin * 2; }  //margins are subtracted from JMRI fixedwidth 
		$retCSS['width'] = ($widget.fixedWidth - $adj) + "px ";
	}
	if ($widget.justification != undefined) {
		if ($widget.justification == "centre") {
			$retCSS['text-align'] = "center";
		} else {
			$retCSS['text-align'] = $widget.justification;
		}
	}
	if ($widget.style != undefined) {
		switch ($widget.style) { //set font based on style attrib from xml
		case "1":
			$retCSS['font-weight'] = 'bold';
			break;
		case "2":
			$retCSS['font-style'] = 'italic';
			break;
		case "3":
			$retCSS['font-weight'] = 'bold';
			$retCSS['font-style'] = 'italic';
			break;
		}
	}

    return $retCSS;
};

//place widget in correct position, rotation, z-index and scale. (pass in dom element, to simplify calling from e.load())
var $setWidgetPosition = function(e) {

	var $id = e.attr('id');
	var $widget = $gWidgets[$id];  //look up the widget and get its panel properties

	if ($widget != undefined) {  //don't bother if widget not found

		var $height = e.height() * $widget.scale;
		var $width =  e.width()  * $widget.scale;

		//if image needs rotating or scaling, but is not loaded yet, set callback to do this again when it is loaded
		if (e.is("img") && ($widget.degrees != 0 || $widget.scale != 1.0) && $(e).get(0).complete == false ) {
			e.load(function(){
				$setWidgetPosition($(this));
				e.unbind('load');  //only do this once
			});
		} else {
			//calculate x and y adjustment needed to keep upper left of bounding box in the same spot
			//  adapted to match JMRI's NamedIcon.rotate().  Note: transform-origin set in .html file
			var tx = 0.0;
			var ty = 0.0;

			if ( $height > 0 && ($widget.degrees != 0 || $widget.scale != 1.0)) { //only do this if needed
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
				e.css({"transform":$rot});
			}
			//set new height and width if scale specified 
			if ($widget.scale != 1 && $height > 0){
				e.css({height:$height+'px',width:$width+'px'});
			}
		} //if height == 0
	}
};

//set new value for all widgets having specified type and element name
var $setElementState = function($element, $name, $newState) {
//	if (window.console) console.log( "setting " + $element + " " + $name + " --> " + $newState);
	//loop thru widgets, setting all matches
	jQuery.each($gWidgets, function($id, $widget) {
		//make the change if widget matches, and not already this value
		if ($widget.element == $element && $widget.name == $name && $widget.state != $newState) {
			$setWidgetState($id, $newState);
		}
		//if sensor and changed, also check each widget's occupancy sensor and redraw widget's color if matched 
		if ($element == 'sensor' && $widget.occupancysensor == $name && $widget.occupancystate != $newState) {
			$gBlks[$widget.blockname].state = $newState; //set the block to the newstate first
			$gWidgets[$widget.id].occupancystate = $newState;
			if (window.console) console.log( "redraw " + $widget.widgetType + " " + $widget.name + " for " + $widget.occupancysensor);
			switch ($widget.widgetType) {
			case 'layoutturnout' :
				$drawTurnout($widget);
				break;
			case 'tracksegment' :
				$drawTrackSegment($widget);
				break;
			}
		}

	});
};

//set new value for widget, showing proper icon, return widgets changed
var $setWidgetState = function($id, $newState) {
	var $widget = $gWidgets[$id];
	if ($widget.state != $newState) {  //don't bother if already this value
		if (window.console) console.log( "setting " + $id + " for " + $widget.element + " " + $widget.name + " --> " + $newState);
		$widget.state = $newState;  
		switch ($widget.widgetFamily) {
		case "icon" :
			$('img#'+$id).attr('src', $widget['icon'+$newState.replace(/ /g, "_")]);  //set image src to next state's image
			break;
		case "text" :
			if ($widget.element == "memory") {
				if ($widget.widgetType == "fastclock") {
					$drawClock($widget);
				} else{
					$('div#'+$id).text($newState);  //set memory text to new value from server
				}
			} else {
				if ($widget['text'+$newState] != undefined) {
					$('div#'+$id).text($widget['text'+$newState]);  //set text to new state's text
				}
				if ($widget['css'+$newState] != undefined) {
					$('div#'+$id).css($widget['css'+$newState]); //set css to new state's css
				}
			}
			break;
		case "drawn" :
			if ($widget.widgetType == "layoutturnout") {
				$drawTurnout($widget);
			}
			break;
		}
		$gWidgets[$id].state = $newState;  //update the persistent widget to the new state
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
	if (window.console) console.log("sending " + $element + " " + $name + " --> " + $nextState);
	var $commandstr = "<xmlio><" + $element + " name='" + $name + "' set='" + $nextState +"'/></xmlio>";
	$sendXMLIOChg($commandstr);
};

//build xml for current state values for all jmri elements, to be sent to xmlio server to wait on changes 
var $getXMLStateList = function(){
	var $ela = []; //element array to check for already sent
	var $retXml = "";
	jQuery.each($gWidgets, function($id, $widget) {
		var $elkey = $widget.element + '.' + $widget.name;
		if ($widget.element != undefined && $widget.name != undefined && $ela[$elkey] == undefined) { //add each valid new element to the monitored list 
			$retXml += "<" + $widget.element + " name='" + $widget.name + "' value='" + $widget.state +"'/>";
			$ela[$elkey] = $elkey; //mark as already in list
		}
		$elkey = 'sensor.' + $widget.occupancysensor;
		if ($widget.occupancysensor != undefined && $ela[$elkey] == undefined) { //also add each new occupancy sensor to the monitored list 
			$retXml += "<sensor name='" + $widget.occupancysensor + "' value='" + $gBlks[$widget.blockname].state +"'/>";
			$ela[$elkey] = $elkey; //mark as already in list
		}
	});
    return $retXml;
};

//send the list of current values to the server, and setup callback to process the response (will stall and wait for changes)
var $sendXMLIOList = function($commandstr){
//	if (window.console) console.log( "sending a list: " + $commandstr);
	if (window.console) console.log( "sending a list");
	if ($gXHRList != undefined && $gXHRList.readyState != 4) {  
		if (window.console) console.log( "aborting active list connection");
		$gXHRList.abort();
	}
	$gXHRList = $.ajax({ 
		type: 'POST',
		url:  '/xmlio/',
		data: $commandstr,
		success: function($r, $s, $x){
			$('div#workingMessage').hide();
			endAndStartTimer();
			if (window.console) console.log( "processing returned list");
			var $r = $($r);
			$r.xmlClean();
			$r.find("xmlio").children().each(function(){
//				if (window.console) console.log("set type="+this.nodeName+" name="+$(this).attr('name')+" to value="+$(this).attr('value'));
				$setElementState(this.nodeName, $(this).attr('name'), $(this).attr('value'));
			});
			//send current xml states to xmlio server, will "stall" and wait for changes if matched
			$sendXMLIOList('<xmlio>' + $getXMLStateList() + '</xmlio>');
		},
		async: true,
		timeout: $gTimeout * 3 * 1000,  //triple the heartbeat timeout
		dataType: 'xml' //<--dataType
	});
};

//send a "set value" command to the server, and process the returned value
var $sendXMLIOChg = function($commandstr){
	$.ajax({ 
			type: 'POST',
		url:  '/xmlio/',
		data: $commandstr,
		success: function($r, $s, $x){
			endAndStartTimer();
			var $r = $($r);
			$r.xmlClean();
			$r.find("xmlio").children().each(function(){
				$('div#workingMessage').hide();
				if (window.console) console.log("rcvd change "+this.nodeName+" "+$(this).attr('name')+" --> "+$(this).attr('value'));
				$setElementState(this.nodeName, $(this).attr('name'), $(this).attr('value'));
			});
		},
		async: true,
		timeout: 5000,  //five seconds
		dataType: 'xml' //<--dataType
	});
};

//show unexpected ajax errors
$(document).ajaxError(function(event,xhr,opt, exception){
	if (xhr.statusText !="abort" && xhr.status != 0) {
		var $msg = "AJAX Error requesting " + opt.url + ", status= " + xhr.status + " " + xhr.statusText;
		$('div#messageText').text($msg);
		$('div#workingMessage').show();
		if (window.console) console.log($msg);
		return;
	}
	if (xhr.statusText =="timeout") {
		var $msg = "AJAX timeout " + opt.url + ", status= " + xhr.status + " " + xhr.statusText + " resending list....";
		if (window.console) console.log($msg);
		//try to resend current xml states to xmlio server, will "stall" and wait for changes if matched
		$sendXMLIOList('<xmlio>' + $getXMLStateList() + '</xmlio>');
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
//handle the toggling (or whatever) of the "next" state for the passed-in widget
var $getNextState = function($widget){
	var $nextState = undefined;
	if ($widget.widgetType == 'signalheadicon') { //special case for signalheadicons
        switch ($widget.clickmode * 1) {          //   logic based on SignaHeadIcon.java
        case 0 :
            switch ($widget.state * 1) {  // (* 1 is to insure numeric comparisons)
            case RED:
            case FLASHRED:
                $nextState = YELLOW;
                break;
            case YELLOW:
            case FLASHYELLOW:
            	$nextState = GREEN;
                break;
            default: //also catches GREEN and FLASHGREEN
            	$nextState = RED;
                break;
            }
        case 1 :
//            getSignalHead().setLit(!getSignalHead().getLit());
            break;
        case 2 : 
//            getSignalHead().setHeld(!getSignalHead().getHeld());
            break;
        case 3: //loop through all iconX and get "next one"
        	var $firstState = undefined;
        	var $currentState = undefined;
        	for (k in $widget) {
        		var s = k.substr(4); //extract the state from current icon var
        		if (k.indexOf('icon') == 0 && $widget[k] != undefined && k != 'icon'+HELD) { //valid value, name starts with 'icon', but not the HELD one
        			if ($firstState == undefined) $firstState = s;  //remember the first state (for last one)
        			if ($currentState != undefined && $nextState == undefined) $nextState = s; //last one was the current, so this one must be next
        			if (s == $widget.state) $currentState = s;
            		if (window.console) console.log('key: ' + k + " first="+ $firstState);
        		}
        	};
        	if ($nextState == undefined) $nextState = $firstState;  //if still not set, start over
        } //end of switch 

	} else if ($widget.widgetType == 'signalmasticon') { //special case for signalmasticons
		//loop through all iconX and get "next one"
		var $firstState = undefined;
		var $currentState = undefined;
		for (k in $widget) {
			var s = k.substr(4).replace(/_/g, " "); //extract the state from current icon var, replace underscores with blanks
			if (k.indexOf('icon') == 0 && $widget[k] != undefined && s != 'Held' && s != 'Dark' && s != 'Unknown') { //valid value, name starts with 'icon', but not the HELD one
				if ($firstState == undefined) $firstState = s;  //remember the first state (for last one)
				if ($currentState != undefined && $nextState == undefined) $nextState = s; //last one was the current, so this one must be next
				if (s == $widget.state) $currentState = s;
				if (window.console) console.log('key: ' + k + " first="+ $firstState);
			}
		};
		if ($nextState == undefined) $nextState = $firstState;  //if still not set, start over

	} else {  //start with INACTIVE, then toggle to ACTIVE and back
		$nextState = ($widget.state == ACTIVE ? INACTIVE : ACTIVE);
	}

	if ($nextState == undefined) $nextState = $widget.state;  //default to no change
	return $nextState;
};

//parse the page's input parameter and return value for name passed in
function getParameterByName(name) {
	var match = RegExp('[?&]' + name + '=([^&]*)')
	.exec(window.location.search);
	return match && match[1];
}

//request and show a list of available panels from the server, and store in persistent var for later checks
var $getPanelList = function(){
	$.ajax({
		url:  '/xmlio/list', //request proper url
		data: {type: "panel"},
		success: function($r, $s, $x){
			var $h = "<h1>Client-side panels:</h1>";
			$h += "<table><tr><th>View Panel</th><th>Type</th></tr>";
            $($r).find("panel").each(function(){
            	var $t = $(this).attr("name").split("/");
            	var $panelType = $t[0];
            	var $panelName = $(this).attr("userName");
            	$gPanelList[$panelName] = $panelType; //store the type for each panel
            	$h += "<tr><td><a href='?name=" + $(this).attr("name") + "'>" + $panelName + "</a></td>";
            	$h += "<td>" + $panelType + "</td>";
            });
            $h += "</table>";
            $('div#panelList').html($h); //put table on page
		},
		dataType: 'xml' //<--dataType
	});
};

//request the panel xml from the server, and setup callback to process the response
var $requestPanelXML = function($panelName){
	$('div#messageText').text("requesting panel xml from JMRI, please wait...");
	$('div#workingMessage').show();

	$.ajax({
		type: 'GET',
		url:  '/panel/' + $panelName, //request proper url
		success: function($r, $s, $x){
			$processPanelXML($r, $s, $x); //handle returned data
		},
		async: true,
		timeout: 5000,  
		dataType: 'xml' //<--dataType
	});
};

//preload all images referred to by the widget
var $preloadWidgetImages = function($widget) {
	for (k in $widget) {
		if (k.indexOf('icon') == 0 && $widget[k] != undefined && $widget[k] != "yes") { //if attribute names starts with 'icon', it's an image, so preload it
			$("<img src='" + $widget[k]  + "'/>");		}
	};
};    	

//determine widget "family" for broadly grouping behaviors
//note: not-yet-supported widgets are commented out here so as to return undefined
var $getWidgetFamily = function($widget) {

	if (($widget.widgetType== "positionablelabel" || $widget.widgetType== "linkinglabel") && $widget.text != undefined) {
		return "text";  //special case to distinguish text vs. icon labels
	}
	if ($widget.widgetType== "sensoricon" && $widget.icon == "no") {
		return "text";  //special case to distinguish text vs. icon labels
	}
	switch ($widget.widgetType) {
	case "memoryicon" :
	case "locoicon" :
	case "trainicon" :
	case "memoryComboIcon" :
	case "memoryInputIcon" :
	case "fastclock" :
//	case "reportericon" :
		return "text";
		break;
	case "positionablelabel" :
	case "linkinglabel" :
	case "turnouticon" :
	case "sensoricon" :
	case "multisensoricon" :
	case "signalheadicon" :
	case "signalmasticon" :
	case "indicatortrackicon" :
	case "indicatorturnouticon" :
		return "icon";
		break;
	case "layoutturnout" :
	case "tracksegment" :
	case "positionablepoint" :
	case "backgroundColor" :
	case "layoutblock" :
	case "levelxing" :
	case "layoutturntable" :
		return "drawn";
		break;
	};
	
	return; //unrecognized widget returns undefined
};    	

var timer;  //persistent timer object used by this function
function endAndStartTimer() {
	window.clearTimeout(timer);
	if ($gWidgets["ISXMLIOHEARTBEAT"] != undefined) { //don't bother if widgets not loaded
		timer = window.setTimeout(function(){
			var $nextState = $getNextState($gWidgets["ISXMLIOHEARTBEAT"]);
			$gWidgets["ISXMLIOHEARTBEAT"].state = $nextState; 
			$sendElementChange("sensor", "ISXMLIOHEARTBEAT", $nextState)
			endAndStartTimer(); //repeat
		}, $gTimeout * 1000);
	}
}

//redraw all "drawn" elements to overcome some bidirectional dependencies in the xml
var $drawAllDrawnWidgets = function() {
	//loop thru widgets, redrawing each visible widget by proper method
	jQuery.each($gWidgets, function($id, $widget) {
		switch ($widget.widgetType) {
		case 'layoutturnout' :
			$drawTurnout($widget);
			break;
		case 'tracksegment' :
			$drawTrackSegment($widget);
			break;
		case 'levelxing' :
			$drawLevelXing($widget);
			break;
		}
	});
};

//redraw all "icon" elements.  Called after a delay to allow loading of images.
var $drawAllIconWidgets = function() {
	//loop thru widgets, repositioning each icon widget
	jQuery.each($gWidgets, function($id, $widget) {
			switch ($widget.widgetFamily) {
			case 'icon' :
				$setWidgetPosition($("div#panelArea>#"+$widget.id));
				break;
			}
	});
};

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function() {

	//always request a list of panels from server
	$getPanelList();
	//if panelname not passed in, show list of available panels
	var $panelName = getParameterByName('name');
	if ($panelName == undefined) {
		$('div#panelList').show();  //show the list of available panels if none passed in
		$(document).attr('title', 'List Available JMRI Panels');
	} else {
		$('div#panelList').hide();
		//set up events based on browser's support for touch events
		var $is_touch_device = 'ontouchstart' in document.documentElement;
		if ($is_touch_device) {
			if (window.console) console.log("touch events enabled");
			DOWNEVENT = 'touchstart';
			UPEVENT   = 'touchend';
		} else {
			if (window.console) console.log("mouse events enabled");
			DOWNEVENT = 'mousedown';
			UPEVENT   = 'mouseup';
		}
		
        //include name of panel in page title
		$(document).attr('title', 'Show JMRI Panel: ' + $panelName);
		//add a link to the panel xml
		$("div#panelFooter").append("&nbsp;<a href='/panel/" + $panelName + "' target=_new>[Panel XML]</a>");

		//add a dummy sensor widget for use as heartbeat
		$widget = new Array();
		$widget['element'] = "sensor";
		$widget['name'] = "ISXMLIOHEARTBEAT";  //internal sensor, will be created if not there
		$widget['id'] 	= $widget['name'];
		$widget['safeName']	= $widget['name'];
		$widget['state'] = UNKNOWN;
		$gWidgets[$widget.id] = $widget;
		
		//add a widget to retrieve current fastclock rate
		$widget = new Array();
		$widget['element'] = "memory";
		$widget['name'] = "IMRATEFACTOR";  //already defined in JMRI
		$widget['id'] 	= $widget['name'];
		$widget['safeName']	= $widget['name'];
		$widget['state'] = "1.0";
		$gWidgets[$widget.id] = $widget;

		//request actual xml of panel, and process it on return
		// NOTE: uses settimeout simply to release control and allow panel list to populate
		setTimeout(function() {
			$requestPanelXML($panelName);
		},
		100);
		
		//queue a change to the heartbeat sensor, to insure previous instances of this page are not left hanging
		setTimeout(function() {
			var $nextState = $getNextState($gWidgets["ISXMLIOHEARTBEAT"]);
			$gWidgets["ISXMLIOHEARTBEAT"].state = $nextState; 
			$sendElementChange("sensor", "ISXMLIOHEARTBEAT", $nextState)
		},
		1000);  //one second
	}
	
});
