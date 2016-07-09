/**********************************************************************************************
 *  panel Servlet - Draw JMRI panels on browser screen
 *    Retrieves panel xml from JMRI and builds panel client-side from that xml, including
 *    click functions.  Sends and listens for changes to panel elements using the JSON WebSocket server.
 *    If no parm passed, page will list links to available panels.
 *  Approach:  Read panel's xml and create widget objects in the browser with all needed attributes.   
 *    There are 3 "widgetFamily"s: text, icon and drawn.  States are handled by storing members 
 *    iconX, textX, cssX where X is the state.  The corresponding members are "shown" whenever the state changes.
 *    CSS classes are used throughout to attach events to correct widgets, as well as control appearance.
 *    The JSON type is used to send changes to JSON server and to listen for changes made elsewhere.
 *    Drawn widgets are handled by drawing directly on the javascript "canvas" layer.
 *  
 *  TODO: show error dialog while retrying connection
 *  TODO: add Cancel button to return to home page on errors (not found, etc.)
 *  TODO: handle "&" in usernames (see Indicator Demo 00.xml)
 *  TODO: handle drawn ellipse (see LMRC APB)
 *  TODO: update drawn track on color and width changes (would need to create system objects to reflect these chgs)
 *  TODO: research movement of locoicons ("promote" locoicon to system entity in JMRI?, add panel-level listeners?)
 *  TODO: finish layoutturntable (draw rays) (see Mtn RR and CnyMod27)
 *  TODO: address color differences between java panel and javascript panel (e.g. lightGray)
 *  TODO: diagnose and correct the small position issues visible with footscray
 *  TODO: deal with mouseleave, mouseout, touchout, etc. Slide off Stop button on rb1 for example.
 *  TODO: make turnout, levelXing occupancy work like LE panels (more than just checking A)
 *  TODO: draw dashed curves
 *  TODO: handle inputs/selection on various memory widgets
 *  TODO: alignment of memoryIcons without fixed width is very different.  Recommended workaround is to use fixed width. 
 *  TODO: add support for LayoutSlip
 *   
 **********************************************************************************************/

//persistent (global) variables
var $gWidgets = {}; //array of all widget objects, key=CSSId
var $gPanelList = {}; 	//store list of available panels
var $gPanel = {}; 	//store overall panel info
var systemNames = {};   // associative array of array of elements indexed by systemName
var occupancyNames = {};   // associative array of array of elements indexed by occupancysensor name
var $gPts = {}; 	//array of all points, key="pointname.pointtype" (used for layoutEditor panels)
var $gBlks = {}; 	//array of all blocks, key="blockname" (used for layoutEditor panels)
var $gCtx;  		//persistent context of canvas layer   
var $gDashArray = [12, 12]; //on,off of dashed lines
var DOWNEVENT = 'touchstart mousedown';  //check both touch and mouse events
var UPEVENT = 'touchend mouseup';
var SIZE = 3;  		//default factor for circles
var UNKNOWN = '0';  //constants to match JSON Server state names
var ACTIVE = '2';
var CLOSED = '2';
var INACTIVE = '4';
var THROWN = '4';
var INCONSISTENT = '8';
var PT_CEN = ".1";  //named constants for point types
var PT_A = ".2";
var PT_B = ".3";
var PT_C = ".4";
var PT_D = ".5";
var LEVEL_XING_A = ".6";
var LEVEL_XING_B = ".7";
var LEVEL_XING_C = ".8";
var LEVEL_XING_D = ".9";

var DARK = 0x00;  //named constants for signalhead states
var RED = 0x01;
var FLASHRED = 0x02;
var YELLOW = 0x04;
var FLASHYELLOW = 0x08;
var GREEN = 0x10;
var FLASHGREEN = 0x20;
var LUNAR = 0x40;
var FLASHLUNAR = 0x80;
var HELD = 0x0100;  //additional to deal with "Held" pseudo-state

var RH_TURNOUT = 1; //named constants for turnout types
var LH_TURNOUT = 2;
var WYE_TURNOUT = 3;
var DOUBLE_XOVER = 4;
var RH_XOVER = 5;
var LH_XOVER = 6;
var SINGLE_SLIP = 7;
var DOUBLE_SLIP = 8;

var jmri = null;

//process the response returned for the requestPanelXML command
function processPanelXML($returnedData, $success, $xhr) {

    $('div#messageText').text("rendering panel from xml, please wait...");
    $("#activity-alert").addClass("show").removeClass("hidden");
    var $xml = $($returnedData);  //jQuery-ize returned data for easier access

    //remove whitespace
    $xml.xmlClean();

    //get the panel-level values from the xml
    var $panel = $xml.find('panel');
    $($panel[0].attributes).each(function() {
        $gPanel[this.name] = this.value;
    });
    $("#panel-area").width($gPanel.panelwidth);
    $("#panel-area").height($gPanel.panelheight);
    setTitle($gPanel["name"]);

    //insert the canvas layer and set up context used by layouteditor "drawn" objects, set some defaults
    if ($gPanel.paneltype === "LayoutPanel") {
        $("#panel-area").prepend("<canvas id='panelCanvas' width=" + $gPanel.panelwidth + "px height=" +
                $gPanel.panelheight + "px style='position:absolute;z-index:2;'>");
        var canvas = document.getElementById("panelCanvas");
        $gCtx = canvas.getContext("2d");
        $gCtx.strokeStyle = $gPanel.defaulttrackcolor;
        $gCtx.lineWidth = $gPanel.sidetrackwidth;

        //set background color from panel attribute
        $("#panel-area").css({backgroundColor: $gPanel.backgroundcolor});
    }

    //process all elements in the panel xml, drawing them on screen, and building persistent array of widgets
    $panel.contents().each(
            function() {
                var $widget = new Array();
                $widget['widgetType'] = this.nodeName;
                $widget['scale'] = "1.0"; //default to no scale
                $widget['degrees'] = 0.00; //default to no rotation
                $widget['rotation'] = 0; // default to no rotation
                //convert attributes to an object array
                $(this.attributes).each(function() {
                    $widget[this.name] = this.value;
                });
                //default various css attributes to not-set, then set in later code as needed
                var $hoverText = "";

                // add and normalize the various type-unique values, from the various spots they are stored
                // icon names based on states returned from JSON server,
                $widget['state'] = UNKNOWN; //initial state is unknown
                $widget.jsonType = ""; //default to no JSON type (avoid undefined)
                if (typeof $widget["id"] !== "undefined") {
                    $widget.systemName = $widget["id"];
                }
                $widget["id"] = "widget-" + $gUnique(); //set id to a unique value (since same element can be in multiple widgets)
                $widget['widgetFamily'] = $getWidgetFamily($widget, this);
                var $jc = "";
                if (typeof $widget["class"] !== "undefined") {
                    var $ta = $widget["class"].split('.'); //get last part of java class name for a css class
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
                        $widget['styles'] = $getTextCSSFromObj($widget);
                        switch ($widget.widgetType) {
                            case "positionablelabel" :
                                $widget['icon' + UNKNOWN] = $(this).find('icon').attr('url');
                                $widget['rotation'] = $(this).find('icon').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('icon').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('icon').attr('scale');
                                break;
                            case "linkinglabel" :
                                $widget['icon' + UNKNOWN] = $(this).find('icon').attr('url');
                                $widget['rotation'] = $(this).find('icon').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('icon').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('icon').attr('scale');
                                $url = $(this).find('url').text();
                                $widget['url'] = $url; //default to using url value as is
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                break;
                            case "indicatortrackicon" :
                                $widget['icon' + UNKNOWN] = $(this).find('iconmap').find('ClearTrack').attr('url');
                                $widget['icon2'] = $widget['icon' + UNKNOWN];
                                $widget['icon4'] = $widget['icon' + UNKNOWN];
                                $widget['icon8'] = $widget['icon' + UNKNOWN];
                                $widget['iconOccupied' + UNKNOWN] = $(this).find('iconmap').find('OccupiedTrack').attr('url');
                                $widget['iconOccupied2'] = $widget['iconOccupied' + UNKNOWN];
                                $widget['iconOccupied4'] = $widget['iconOccupied' + UNKNOWN];
                                $widget['iconOccupied8'] = $widget['iconOccupied' + UNKNOWN];
                                $widget['rotation'] = $(this).find('iconmap').find('ClearTrack').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('iconmap').find('ClearTrack').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('iconmap').find('ClearTrack').attr('scale');
                                if ($(this).find('occupancysensor')) {  //store the occupancy sensor name and state
                                    $widget['occupancysensor'] = $(this).find('occupancysensor').text();
                                    $widget['occupancystate'] = UNKNOWN;
                                    jmri.getSensor($widget["occupancysensor"]); //listen for occupancy changes
                                }
                                break;
                            case "indicatorturnouticon" :
                                $widget['name'] = $(this).find('turnout').text();
                                ; //normalize name
                                $widget.jsonType = 'turnout'; // JSON object type
                                $widget['icon' + UNKNOWN] = $(this).find('iconmaps').find('ClearTrack').find('BeanStateUnknown').attr('url');
                                $widget['icon2'] = $(this).find('iconmaps').find('ClearTrack').find('TurnoutStateClosed').attr('url');
                                $widget['icon4'] = $(this).find('iconmaps').find('ClearTrack').find('TurnoutStateThrown').attr('url');
                                $widget['icon8'] = $(this).find('iconmaps').find('ClearTrack').find('BeanStateInconsistent').attr('url');
                                $widget['iconOccupied' + UNKNOWN] = $(this).find('iconmaps').find('OccupiedTrack').find('BeanStateUnknown').attr('url');
                                $widget['iconOccupied2'] = $(this).find('iconmaps').find('OccupiedTrack').find('TurnoutStateClosed').attr('url');
                                $widget['iconOccupied4'] = $(this).find('iconmaps').find('OccupiedTrack').find('TurnoutStateThrown').attr('url');
                                $widget['iconOccupied8'] = $(this).find('iconmaps').find('OccupiedTrack').find('BeanStateInconsistent').attr('url');
                                $widget['rotation'] = $(this).find('iconmaps').find('ClearTrack').find('BeanStateUnknown').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('iconmaps').find('ClearTrack').find('BeanStateUnknown').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('iconmaps').find('ClearTrack').find('BeanStateUnknown').attr('scale');
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                if ($(this).find('occupancysensor')) {  //store the occupancy sensor name and state
                                    $widget['occupancysensor'] = $(this).find('occupancysensor').text();
                                    $widget['occupancystate'] = UNKNOWN;
                                    jmri.getSensor($widget["occupancysensor"]); //listen for occupancy changes
                                }
                                jmri.getTurnout($widget["systemName"]);
                                break;
                            case "turnouticon" :
                                $widget['name'] = $widget.turnout; //normalize name
                                $widget.jsonType = "turnout"; // JSON object type
                                $widget['icon' + UNKNOWN] = $(this).find('icons').find('unknown').attr('url');
                                $widget['icon2'] = $(this).find('icons').find('closed').attr('url');
                                $widget['icon4'] = $(this).find('icons').find('thrown').attr('url');
                                $widget['icon8'] = $(this).find('icons').find('inconsistent').attr('url');
                                $widget['rotation'] = $(this).find('icons').find('unknown').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('icons').find('unknown').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('icons').find('unknown').attr('scale');
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                jmri.getTurnout($widget["systemName"]);
                                break;
                            case "sensoricon" :
                                $widget['name'] = $widget.sensor; //normalize name
                                $widget.jsonType = "sensor"; // JSON object type
                                $widget['icon' + UNKNOWN] = $(this).find('unknown').attr('url');
                                $widget['icon2'] = $(this).find('active').attr('url');
                                $widget['icon4'] = $(this).find('inactive').attr('url');
                                $widget['icon8'] = $(this).find('inconsistent').attr('url');
                                $widget['rotation'] = $(this).find('unknown').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('unknown').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('unknown').attr('scale');
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                if (typeof $widget["systemName"] == "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getSensor($widget["systemName"]);
                                break;
                            case "signalheadicon" :
                                $widget['name'] = $widget.signalhead; //normalize name
                                $widget.jsonType = "signalHead"; // JSON object type
                                $widget['icon' + HELD] = $(this).find('icons').find('held').attr('url');
                                $widget['icon' + DARK] = $(this).find('icons').find('dark').attr('url');
                                $widget['icon' + RED] = $(this).find('icons').find('red').attr('url');
                                if (typeof $widget['icon' + RED] == "undefined") { //look for held if no red
                                    $widget['icon' + RED] = $(this).find('icons').find('held').attr('url');
                                }
                                $widget['icon' + YELLOW] = $(this).find('icons').find('yellow').attr('url');
                                $widget['icon' + GREEN] = $(this).find('icons').find('green').attr('url');
                                $widget['icon' + FLASHRED] = $(this).find('icons').find('flashred').attr('url');
                                $widget['icon' + FLASHYELLOW] = $(this).find('icons').find('flashyellow').attr('url');
                                $widget['icon' + FLASHGREEN] = $(this).find('icons').find('flashgreen').attr('url');
                                $widget['icon' + LUNAR] = $(this).find('icons').find('lunar').attr('url');
                                $widget['icon' + FLASHLUNAR] = $(this).find('icons').find('lunar').attr('url');
                                $widget['rotation'] = $(this).find('icons').find('dark').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('icons').find('dark').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('icons').find('dark').attr('scale');
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                jmri.getSignalHead($widget["systemName"]);
                                break;
                            case "signalmasticon" :
                                $widget['name'] = $widget.signalmast; //normalize name
                                $widget.jsonType = "signalMast"; // JSON object type
                                var icons = $(this).find('icons').children(); //get array of icons
                                icons.each(function(i, item) {  //loop thru icons array and set all iconXX urls for widget
		                          	$widget['icon' + $(item).attr('aspect')] = $(item).attr('url');
                                });
                                $widget['degrees'] = $(this).attr('degrees') * 1;
                                $widget['scale'] = $(this).attr('scale');
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                if (typeof $widget["iconUnlit"] !== "undefined") {
                                    $widget['state'] = "Unlit"; //set the initial aspect to Unlit if defined                                	
                                } else {
                                    $widget['state'] = "Unknown"; //else set to Unknown                             	                                	
                                }
                                jmri.getSignalMast($widget["systemName"]);
                                break;
                            case "multisensoricon" :
                                //create multiple widgets, 1st with all images, stack others with non-active states set to a clear image
                                //  set up siblings array so each widget can also set state of the others
                                $widget.jsonType = "sensor"; // JSON object type
                                $widget['icon' + UNKNOWN] = $(this).find('unknown').attr('url');
                                $widget['icon4'] = $(this).find('inactive').attr('url');
                                $widget['icon8'] = $(this).find('inconsistent').attr('url');
                                $widget['rotation'] = $(this).find('unknown').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('unknown').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('unknown').attr('scale');
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                $widget['siblings'] = new Array();  //array of related multisensors
                                $widget['hoverText'] = "";  		//for override of hovertext
                                var actives = $(this).find('active'); //get array of actives used by this multisensor
                                var $id = $widget.id;
                                actives.each(function(i, item) {  //loop thru array once to set up siblings array, to be copied to all siblings
                                    $widget.siblings.push($id);
                                    $widget.hoverText += $(item).attr('sensor') + " "; //add sibling names to hovertext
                                    $id = "widget-" + $gUnique(); //set new id to a unique value for each sibling
                                });
                                actives.each(function(i, item) {  //loop thru array again to create each widget
                                    $widget['id'] = $widget.siblings[i]; 	  	 // use id already set in sibling array
                                    $widget.name = $(item).attr('sensor');
                                    $widget['icon2'] = $(item).attr('url');
                                    if (i < actives.size() - 1) { //only save widget and make a new one if more than one active found
                                        $preloadWidgetImages($widget); //start loading all images
                                        $widget['safeName'] = $safeName($widget.name);  //add a html-safe version of name
                                        $widget["systemName"] = $widget.name;
                                        $gWidgets[$widget.id] = $widget; //store widget in persistent array
                                        $drawIcon($widget); //actually place and position the widget on the panel
                                        jmri.getSensor($widget["systemName"]);
                                        if (!($widget.systemName in systemNames)) {  //set where-used for this new sensor
                                            systemNames[$widget.systemName] = new Array();
                                        }
                                        systemNames[$widget.systemName][systemNames[$widget.systemName].length] = $widget.id;
                                        $widget = jQuery.extend(true, {}, $widget); //get a new copy of widget
                                        $widget['icon' + UNKNOWN] = "/web/images/transparent_1x1.png";
                                        $widget['icon4'] = "/web/images/transparent_1x1.png"; //set non-actives to transparent image
                                        $widget['icon8'] = "/web/images/transparent_1x1.png";
                                        $widget['state'] = ACTIVE; //to avoid sizing based on the transparent image
                                    }
                                });
                                $widget["systemName"] = $widget.name;
                                jmri.getSensor($widget["systemName"]);
                                break;
                            case "memoryicon" :
                                $widget['name'] = $widget.memory; //normalize name
                                $widget.jsonType = "memory"; // JSON object type
                                $widget['state'] = $widget.memory; //use name for initial state as well
                                var memorystates = $(this).find('memorystate'); 
                                memorystates.each(function(i, item) {  ////get any memorystates defined
                                    //store icon url in "iconXX" where XX is the state to match
                                	$widget['icon' + item.attributes['value'].value] = item.attributes['icon'].value; 
                                    $widget['state'] = item.attributes['value'].value; //use value for initial state
                                });                              
                                if (typeof $widget["systemName"] == "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getMemory($widget["systemName"]);
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
                                $widget['name'] = $widget.sensor; //normalize name
                                $widget.jsonType = "sensor"; // JSON object type
                                //set each state's text
                                $widget['text' + UNKNOWN] = $(this).find('unknownText').attr('text');
                                $widget['text2'] = $(this).find('activeText').attr('text');
                                $widget['text4'] = $(this).find('inactiveText').attr('text');
                                $widget['text8'] = $(this).find('inconsistentText').attr('text');
                                //set each state's css attribute array (text color, etc.)
                                $widget['css' + UNKNOWN] = $getTextCSSFromObj($getObjFromXML($(this).find('unknownText')[0]));
                                $widget['css2'] = $getTextCSSFromObj($getObjFromXML($(this).find('activeText')[0]));
                                $widget['css4'] = $getTextCSSFromObj($getObjFromXML($(this).find('inactiveText')[0]));
                                $widget['css8'] = $getTextCSSFromObj($getObjFromXML($(this).find('inconsistentText')[0]));
                                if (typeof $widget.name !== "undefined" && $widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                if (typeof $widget["systemName"] == "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getSensor($widget["systemName"]);
                                break;
                            case "locoicon" :
                            case "trainicon" :
                                //also set the background icon for this one (additional css in .html file)
                                $widget['icon' + UNKNOWN] = $(this).find('icon').attr('url');
                                $widget.styles['background-image'] = "url('" + $widget['icon' + UNKNOWN] + "')";
                                $widget['scale'] = $(this).find('icon').attr('scale');
                                if ($widget.scale != 1.0) {
                                    $widget.styles['background-size'] = $widget.scale * 100 + "%";
                                    $widget.styles['line-height'] = $widget.scale * 20 + "px";  //center vertically
                                }
                                break;
                            case "fastclock" :
                                $widget['name'] = 'IMCURRENTTIME';
                                $widget.jsonType = 'memory';
                                $widget.styles['width'] = "166px";  //hard-coded to match original size of clock image
                                $widget.styles['height'] = "166px";
                                $widget['scale'] = $(this).attr('scale');
                                if (typeof $widget.level == "undefined") {
                                    $widget['level'] = 10;  //if not included in xml
                                }
                                $widget['text'] = "00:00 AM";
                                $widget['state'] = "00:00 AM";
                                if (typeof $widget["systemName"] == "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getMemory($widget["systemName"]);
                                break;
                            case "memoryicon" :
                                $widget['name'] = $widget.memory; //normalize name
                                $widget.jsonType = "memory"; // JSON object type
                                $widget['text'] = $widget.memory; //use name for initial text
                                $widget['state'] = $widget.memory; //use name for initial state as well
                                if (typeof $widget["systemName"] == "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getMemory($widget["systemName"]);
                                break;
                            case "BlockContentsIcon" :
                                $widget['name'] = $widget.systemName; //normalize name (id got stepped on)
                                $widget.jsonType = "block"; // JSON object type
                                $widget['text'] = $widget.name; //use name for initial text
                                $widget['state'] = $widget.name; //use name for initial state as well
                                jmri.getBlock($widget["systemName"]);
                                break;
                            case "memoryInputIcon" :
                            case "memoryComboIcon" :
                                $widget['name'] = $widget.memory; //normalize name
                                $widget.jsonType = "memory"; // JSON object type
                                $widget['text'] = $widget.memory; //use name for initial text
                                $widget['state'] = $widget.memory; //use name for initial state as well
                                $widget.styles['border'] = "1px solid black" //add border for looks (temporary)
                                if (typeof $widget["systemName"] == "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getMemory($widget["systemName"]);
                                break;
                            case "linkinglabel" :
                                $url = $(this).find('url').text();
                                $widget['url'] = $url; //just store url value in widget, for use in click handler
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                break;
                        }
                        $widget['safeName'] = $safeName($widget.name);
                        $gWidgets[$widget.id] = $widget; //store widget in persistent array
                        //put the text element on the page
                        $("#panel-area").append("<div id=" + $widget.id + " class='" + $widget.classes + "'>" + $widget.text + "</div>");
                        $("#panel-area>#" + $widget.id).css($widget.styles); //apply style array to widget
                        $setWidgetPosition($("#panel-area>#" + $widget.id));
                        break;

                    case "drawn" :
                        switch ($widget.widgetType) {
                            case "positionablepoint" :
                                //just store these points in persistent variable for use when drawing tracksegments and layoutturnouts
                                //id is ident plus ".type", e.g. "A4.2"
                                $gPts[$widget.ident + "." + $widget.type] = $widget;
                              //End bumpers and Connectors use wrong type, so also store type 1
                                if (($widget.ident.substring(0, 2) == "EB") ||
                                     ($widget.ident.substring(0, 2) == "EC")) {  
                                		$gPts[$widget.ident + ".1"] = $widget;
                                }
                                break;
                            case "layoutblock" :
                                $widget['state'] = UNKNOWN;  //add a state member for this block
                                //store these blocks in a persistent var
                                //id is username
                                $gBlks[$widget.username] = $widget;
                                break;
                            case "layoutturnout" :
                                $widget['name'] = $widget.turnoutname; //normalize name
                                $widget['safeName'] = $safeName($widget.name);  //add a html-safe version of name
                                $widget.jsonType = "turnout"; // JSON object type
                                $widget['x'] = $widget.xcen; //normalize x,y
                                $widget['y'] = $widget.ycen;
                                if (typeof $widget.name !== "undefined") { //make it clickable (unless no turnout assigned)
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                //set widget occupancysensor from block to speed affected changes later
                                if (typeof $gBlks[$widget.blockname] !== "undefined") {
                                    $widget['occupancysensor'] = $gBlks[$widget.blockname].occupancysensor;
                                    $widget['occupancystate'] = $gBlks[$widget.blockname].state;
                                }
                                $gWidgets[$widget.id] = $widget; //store widget in persistent array
                                $storeTurnoutPoints($widget); //also store the turnout's 3 end points for other connections
                                $drawTurnout($widget); //draw the turnout
                                //add an empty, but clickable, div to the panel and position it over the turnout circle
                                $hoverText = " title='" + $widget.name + "' alt='" + $widget.name + "'";
                                $("#panel-area").append("<div id=" + $widget.id + " class='" + $widget.classes + "' " + $hoverText + "></div>");
                                var $cr = $gPanel.turnoutcirclesize * SIZE;  //turnout circle radius
                                var $cd = $cr * 2;
                                $("#panel-area>#" + $widget.id).css(
                                        {position: 'absolute', left: ($widget.x - $cr) + 'px', top: ($widget.y - $cr) + 'px', zIndex: 3,
                                            width: $cd + 'px', height: $cd + 'px'});
                                if (typeof $widget["systemName"] == "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getTurnout($widget["systemName"]);
                                if ($widget["occupancysensor"])
                                    jmri.getSensor($widget["occupancysensor"]); //listen for occupancy changes
                                break;
                            case "tracksegment" :
                                //set widget occupancysensor from block to speed affected changes later
                                if (typeof $gBlks[$widget.blockname] !== "undefined") {
                                    $widget['occupancysensor'] = $gBlks[$widget.blockname].occupancysensor;
                                    $widget['occupancystate'] = $gBlks[$widget.blockname].state;
                                }
                                //store this widget in persistent array, with ident as key
                                $widget['id'] = $widget.ident;
                                $gWidgets[$widget.id] = $widget;
                                //draw the tracksegment
                                $drawTrackSegment($widget);
                                if ($widget["occupancysensor"])
                                    jmri.getSensor($widget["occupancysensor"]); //listen for occupancy changes
                                break;
                            case "levelxing" :
                                $widget['x'] = $widget.xcen; //normalize x,y
                                $widget['y'] = $widget.ycen;
                                //set widget occupancysensor from block to speed affected changes later
                                //TODO: handle BD block
                                if (typeof $gBlks[$widget.blocknameac] !== "undefined") {
                                    $widget['blockname'] = $widget.blocknameac; //normalize blockname
                                    $widget['occupancysensor'] = $gBlks[$widget.blocknameac].occupancysensor;
                                    $widget['occupancystate'] = $gBlks[$widget.blocknameac].state;
                                }
                                //store widget in persistent array
                                $gWidgets[$widget.id] = $widget;
                                //also store the xing's 4 end points for other connections
                                $storeLevelXingPoints($widget);
                                //draw the xing
                                $drawLevelXing($widget);
                                if ($widget["occupancysensor"])
                                    jmri.getSensor($widget["occupancysensor"]); //listen for occupancy changes
                                break;
                            case "layoutturntable" :
                                //just draw the circle for now, don't even store it
                                $drawCircle($widget.xcen, $widget.ycen, $widget.radius);
                                break;
                            case "backgroundColor" :  //set background color of the panel itself
                                $("#panel-area").css({"background-color": "rgb(" + $widget.red + "," + $widget.green + "," + $widget.blue + ")"});
                                break;
                        }
                        break;
                    default:
                        //log any unsupported widgets, listing childnodes as info
                        $("div#logArea").append("<br />Unsupported: " + $widget.widgetType + ":");
                        $(this.attributes).each(function() {
                            $("div#logArea").append(" " + this.name);
                        });
                        $("div#logArea").append(" | ");
                        $(this.childNodes).each(function() {
                            $("div#logArea").append(" " + this.nodeName);
                        });
                        break;
                }
                if ($widget.systemName) {
                    if (!($widget.systemName in systemNames)) {
                        systemNames[$widget.systemName] = new Array();
                    }
                    systemNames[$widget.systemName][systemNames[$widget.systemName].length] = $widget.id;
                }
                //store occupancy sensor where-used
                if ($widget.occupancysensor && $gWidgets[$widget.id]) {
                    if (!($widget.occupancysensor in occupancyNames)) {
                        occupancyNames[$widget.occupancysensor] = new Array();
                    }
                    occupancyNames[$widget.occupancysensor][occupancyNames[$widget.occupancysensor].length] = $widget.id;
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
        e.stopPropagation();
        e.preventDefault(); //prevent double-firing (touch + click)
        sendElementChange($gWidgets[this.id].jsonType, $gWidgets[this.id].systemName, ACTIVE);  //send active on down
    }).bind(UPEVENT, function(e) {
        e.stopPropagation();
        e.preventDefault(); //prevent double-firing (touch + click)
        sendElementChange($gWidgets[this.id].jsonType, $gWidgets[this.id].systemName, INACTIVE);  //send inactive on up
    });

    $drawAllDrawnWidgets(); //draw all the drawn widgets once more, to address some bidirectional dependencies in the xml
    $("#activity-alert").addClass("hidden").removeClass("show");

}

//perform regular click-handling, bound to click event for clickable, non-momentary widgets, except for multisensor and linkinglabel.
function $handleClick(e) {
    e.stopPropagation();
    e.preventDefault(); //prevent double-firing (touch + click)
    var $widget = $gWidgets[this.id];
    var $newState = $getNextState($widget);  //determine next state from current state
    sendElementChange($widget.jsonType, $widget.systemName, $newState);  //send new value to JMRI
    if (typeof $widget.secondturnoutname !== "undefined") {  //TODO: put this in a more logical place?
        sendElementChange($widget.jsonType, $widget.secondturnoutname, $newState);  //also send 2nd turnout
    }
}

//perform multisensor click-handling, bound to click event for clickable multisensor widgets.
function $handleMultiClick(e) {
    e.stopPropagation();
    e.preventDefault(); //prevent double-firing (touch + click)
    var $widget = $gWidgets[this.id];
    var clickX = e.pageX - $(this).parent().offset().left - this.offsetLeft;  //get click location on widget
    var clickY = e.pageY - $(this).parent().offset().top - this.offsetTop;
//find if we want to increment or decrement
    var dec = false;
    if ($widget.updown == "true") {
        if (clickY > this.height / 2)
            dec = true;
    } else {
        if (clickX < this.width / 2)
            dec = true;
    }
    var displaying = 0;
    for (i in $widget.siblings) {  //determine which is currently active
        if ($gWidgets[$widget.siblings[i]].state == ACTIVE) {
            displaying = i; //flag the current active sibling
        }
    }
    var next;  //determine which is the next one which should be set to active
    if (dec) {
        next = displaying - 1;
        if (next < 0)
            next = 0;
    } else {
        next = displaying * 1 + 1;
        if (next > i)
            next = i;
    }
    for (i in $widget.siblings) {  //loop through siblings and send changes as needed
        if (i == next) {
            if ($gWidgets[$widget.siblings[i]].state != ACTIVE) {
                sendElementChange('sensor', $gWidgets[$widget.siblings[i]].name, ACTIVE);  //set next sensor to active and send command to JMRI server
            }
        } else {
            if ($gWidgets[$widget.siblings[i]].state != INACTIVE) {
                sendElementChange('sensor', $gWidgets[$widget.siblings[i]].name, INACTIVE);  //set all other siblings to inactive if not already
            }
        }
    }
}

//perform click-handling of linkinglabel widgets (3 cases: complete url or frame:<name> where name is a panel or a frame)
function $handleLinkingLabelClick(e) {
    e.stopPropagation();
    e.preventDefault(); //prevent double-firing (touch + click)
    var $widget = $gWidgets[this.id];
    var $url = $widget.url;
    if ($url.toLowerCase().indexOf("frame:") == 0) {
        $frameName = $url.substring(6); //if "frame" found, remove it
        $frameUrl = $gPanelList[$frameName];  //find panel in panel list
        if (typeof $frameUrl == "undefined") {
            $url = "/frame/" + $frameName + ".html"; //not in list, open using frameserver
        } else {
            $url = "/panel/" + $frameUrl; //format for panel server
        }
    }
    window.location = $url;  //navigate to the specified url
}

//draw a Circle (color and width are optional)
function $drawCircle($ptx, $pty, $radius, $color, $width) {
    var $savStrokeStyle = $gCtx.strokeStyle;
    var $savLineWidth = $gCtx.lineWidth;
    if (typeof $color !== "undefined")
        $gCtx.strokeStyle = $color;
    if (typeof $width !== "undefined")
        $gCtx.lineWidth = $width;
    $gCtx.beginPath();
    $gCtx.arc($ptx, $pty, $radius, 0, 2 * Math.PI, false);
    $gCtx.stroke();
    // put color and widths back to default
    $gCtx.lineWidth = $savLineWidth;
    $gCtx.strokeStyle = $savStrokeStyle;
}

//draw a Tracksegment (pass in widget)
function $drawTrackSegment($widget) {
    //if set to hidden, don't draw anything
    if ($widget.hidden == "yes") {
        return;
    }

    //get the endpoints by name
    var $pt1 = $gPts[$widget.connect1name + "." + $widget.type1];
    if (typeof $pt1 == "undefined") {
        if (window.console)
            console.log("can't draw tracksegment " + $widget.connect1name + "." + $widget.type1 + " undefined (1)");
        return;
    }
    var $pt2 = $gPts[$widget.connect2name + "." + $widget.type2];
    if (typeof $pt2 == "undefined") {
        if (window.console)
            console.log("can't draw tracksegment " + $widget.connect2name + "." + $widget.type2 + " undefined (2)");
        return;
    }

    //	set trackcolor based on block occupancy state
    var $color = $gPanel.defaulttrackcolor;
    var $blk = $gBlks[$widget.blockname];
    if (typeof $blk !== "undefined") {
        if ($blk.occupiedsense == $blk.state) { //set the color based on occupancy state
            $color = $blk.occupiedcolor;
            //if (window.console) console.log("set block color to occupiedcolor " + $color);
        } else {
            $color = $blk.trackcolor;
            //if (window.console) console.log("set block color to trackcolor " + $color);
        }
    }

    var $width = $gPanel.sidetrackwidth;
    if ($widget.mainline == "yes") {
        $width = $gPanel.mainlinetrackwidth;
    }
    if (typeof $widget.angle == "undefined") {
        //draw straight line between the points
        if ($widget.dashed == "yes") {
            $drawDashedLine($pt1.x, $pt1.y, $pt2.x, $pt2.y, $color, $width, $gDashArray);
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
}

//drawLine, passing in values from xml
function $drawDashedLine($pt1x, $pt1y, $pt2x, $pt2y, $color, $width, dashArray) {
    var $savLineWidth = $gCtx.lineWidth;
    var $savStrokeStyle = $gCtx.strokeStyle;
    if (typeof $color !== "undefined")
        $gCtx.strokeStyle = $color;
    if (typeof $width !== "undefined")
        $gCtx.lineWidth = $width;
    $gCtx.beginPath();
    $gCtx.dashedLine($pt1x, $pt1y, $pt2x, $pt2y, dashArray);
    $gCtx.closePath();
    $gCtx.stroke();
    // put color and width back to default
    $gCtx.strokeStyle = $savStrokeStyle;
    $gCtx.lineWidth = $savLineWidth;
}

//dashed line code copied from: http://stackoverflow.com/questions/4576724/dotted-stroke-in-canvas
var CP = window.CanvasRenderingContext2D && CanvasRenderingContext2D.prototype;
if (CP.lineTo) {
    CP.dashedLine = function(x, y, x2, y2, da) {
        if (!da)
            da = [10, 5];
        this.save();
        var dx = (x2 - x), dy = (y2 - y);
        var len = Math.sqrt(dx * dx + dy * dy);
        var rot = Math.atan2(dy, dx);
        this.translate(x, y);
        this.moveTo(0, 0);
        this.rotate(rot);
        var dc = da.length;
        var di = 0, draw = true;
        x = 0;
        while (len > x) {
            x += da[di++ % dc];
            if (x > len)
                x = len;
            draw ? this.lineTo(x, 0) : this.moveTo(x, 0);
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
    $h += "<div class='clocktext' style='font-size:" + $fs + "%;' >" + $widget.state + "<br />" + $fcr + ":1</div>";  //add the text
    $h += "<img class='clockface' src='/web/images/clockface.png' />"; 				//add the clockface
    $h += "<img class='clockhourhand' src='/web/images/clockhourhand.png' />"; 		//add the hour hand
    $h += "<img class='clockminutehand' src='/web/images/clockminutehand.png' />"; 	//add the minute hand
    $("#panel-area>#" + $widget.id).html($h); //set the html for the widget

    var hours = $widget.state.split(':')[0]; 	//extract hours from format "H:MM AM"
    var mins = $widget.state.split(':')[1].split(' ')[0]; //extract minutes
    var hdegree = hours * 30 + (mins / 2);
    var hrotate = "rotate(" + hdegree + "deg)";
    $("div.fastclock>img.clockhourhand").css({"transform": hrotate}); //set rotation for hour hand
    var mdegree = mins * 6;
    var mrotate = "rotate(" + mdegree + "deg)";
    $("div.fastclock>img.clockminutehand").css({"transform": mrotate}); //set rotation for minute hand
}

//draw an icon-type widget (pass in widget)
function $drawIcon($widget) {
    var $hoverText = "";
    if (typeof $widget.hoverText !== "undefined") {
        $hoverText = " title='" + $widget.hoverText + "' alt='" + $widget.hoverText + "'";
    }
    if ($hoverText == "" && typeof $widget.name !== "undefined") { //if name available, use it as hover text if still blank
        $hoverText = " title='" + $widget.name + "' alt='" + $widget.name + "'";
    }

    //additional naming for indicator*icon widgets to reflect occupancy
    $indicator = ($widget.occupancysensor && $widget.occupancystate == ACTIVE ? "Occupied" : "");
    //add the image to the panel area, with appropriate css classes and id (skip any unsupported)
    if (typeof $widget['icon' + $indicator + $widget.state] !== "undefined") {
        $imgHtml = "<img id=" + $widget.id + " class='" + $widget.classes +
                "' src='" + $widget["icon" + $indicator + $widget['state']] + "' " + $hoverText + "/>"

        $("#panel-area").append($imgHtml);  //put the html in the panel

        $("#panel-area>#" + $widget.id).css($widget.styles); //apply style array to widget
        
        //add in overlay text if specified  (append "overlay" to id to keep them unique)
        if (typeof $widget.text !== "undefined") {
            $("#panel-area").append("<div id=" + $widget.id + "overlay class='overlay'>" + $widget.text + "</div>");
            $("#panel-area>#" + $widget.id + "overlay").css({position: 'absolute', left: $widget.x + 'px', top: $widget.y + 'px', zIndex: ($widget.level - 1)});
        }
    } else {
        if (window.console)
            console.log("ERROR: image not defined for " + $widget.widgetType + " " + $widget.id + ", state=" + $widget.state + ", occ=" + $widget.occupancystate);
    }
    $setWidgetPosition($("#panel-area #" + $widget.id));
}

//draw a LevelXing (pass in widget)
function $drawLevelXing($widget) {
    //if set to hidden, don't draw anything
    if ($widget.hidden == "yes") {
        return;
    }
    var $width = $gPanel.sidetrackwidth;
    //set levelxing width same as the A track
    if (typeof $gWidgets[$widget.connectaname] !== "undefined") {
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
    if (typeof $blk !== "undefined") {
        if ($blk.occupiedsense == $blk.state) { //set the color based on occupancy state
            $color = $blk.occupiedcolor;
        } else {
            $color = $blk.trackcolor;
        }
    }
    var cenx = $widget.xcen;
    var ceny = $widget.ycen
    var ax = $gPts[$widget.ident + LEVEL_XING_A].x;  //retrieve the points
    var ay = $gPts[$widget.ident + LEVEL_XING_A].y;
    var bx = $gPts[$widget.ident + LEVEL_XING_B].x;
    var by = $gPts[$widget.ident + LEVEL_XING_B].y;
    var cx = $gPts[$widget.ident + LEVEL_XING_C].x;
    var cy = $gPts[$widget.ident + LEVEL_XING_C].y;
    var dx = $gPts[$widget.ident + LEVEL_XING_D].x;
    var dy = $gPts[$widget.ident + LEVEL_XING_D].y;

    //levelxing   A
    //          D-+-B
    //            C
    $drawLine(ax, ay, cx, cy, $color, $width); //A to B
    $drawLine(dx, dy, bx, by, $color, $width); //D to B
}

//draw a Turnout (pass in widget)
//  see LayoutEditor.drawTurnouts()
function $drawTurnout($widget) {
    //if set to hidden, don't draw anything
    if ($widget.hidden == "yes") {
        return;
    }
    var $width = $gPanel.sidetrackwidth;
    //set turnout width same as the A track
    if (typeof $gWidgets[$widget.connectaname] !== "undefined") {
        if ($gWidgets[$widget.connectaname].mainline == "yes") {
            $width = $gPanel.mainlinetrackwidth;
        }
    } else {
//		if (window.console) console.log("could not get trackwidth of "+$widget.connectaname+" for "+$widget.name);
    }
    var cenx = $widget.xcen;
    var ceny = $widget.ycen
    var ax = $gPts[$widget.ident + PT_A].x;
    var ay = $gPts[$widget.ident + PT_A].y;
    var bx = $gPts[$widget.ident + PT_B].x;
    var by = $gPts[$widget.ident + PT_B].y;
    var cx = $gPts[$widget.ident + PT_C].x;
    var cy = $gPts[$widget.ident + PT_C].y;
    var abx = (ax * 1) + ((bx - ax) * 0.5); // midpoint AB
    var aby = (ay * 1) + ((by - ay) * 0.5);
    if ($gPanel.turnoutdrawunselectedleg == 'yes') { //only calculate midpoints if needed
        var cenbx = (cenx * 1) + ((bx - cenx) * 0.5); // midpoint cenB
        var cenby = (ceny * 1) + ((by - ceny) * 0.5);
        var cencx = (cenx * 1) + ((cx - cenx) * 0.5); // midpoint cenC
        var cency = (ceny * 1) + ((cy - ceny) * 0.5);
    }
    var dx, dy, dcx, dcy;
    if (typeof $gPts[$widget.ident + PT_D] !== "undefined") {
        dx = $gPts[$widget.ident + PT_D].x;
        dy = $gPts[$widget.ident + PT_D].y;
        dcx = (dx * 1) + ((cx - dx) * 0.5); // midpoint DC
        dcy = (dy * 1) + ((cy - dy) * 0.5);
    }
    var erase = $gPanel.backgroundcolor;

    //	set trackcolor based on block occupancy state
    var $color = $gPanel.defaulttrackcolor;
    var $blk = $gBlks[$widget.blockname];
    if (typeof $blk != "undefined") {
        if ($blk.occupiedsense == $blk.state) { //set the color based on occupancy state
            $color = $blk.occupiedcolor;
        } else {
            $color = $blk.trackcolor;
        }
    }

    //turnout A--B
    //         \-C
    if ($widget.type == LH_TURNOUT || $widget.type == RH_TURNOUT || $widget.type == WYE_TURNOUT) {
        //if closed or thrown, draw the selected leg in the default track color and erase the other one
        if ($widget.state == CLOSED || $widget.state == THROWN) {
            if ($widget.state == $widget.continuing) {
                $drawLine(cenx, ceny, cx, cy, erase, $width); //erase center to C (diverging leg)
                if ($gPanel.turnoutdrawunselectedleg == 'yes') {
                    $drawLine(cx, cy, cencx, cency, $color, $width); //C to midC (diverging leg)
                }
                $drawLine(cenx, ceny, bx, by, $color, $width); //center to B (straight leg)
            } else {
                $drawLine(cenx, ceny, bx, by, erase, $width); //erase center to B (straight leg)
                if ($gPanel.turnoutdrawunselectedleg == 'yes') {
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
    } else if ($widget.type == LH_XOVER || $widget.type == RH_XOVER || $widget.type == DOUBLE_XOVER) {
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
                if ($widget.type == DOUBLE_XOVER) {
                    $drawLine(ax, ay, cx, cy, $color, $width); //A to C
                    $drawLine(dx, dy, bx, by, $color, $width); //D to B
                } else if ($widget.type == RH_XOVER) {
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
}

//store the various points defined with a Turnout (pass in widget)
//see jmri.jmrit.display.layoutEditor.LayoutTurnout.java for background
function $storeTurnoutPoints($widget) {
	var $t = [];
	$t['ident'] = $widget.ident + PT_B;  //store B endpoint
	$t['x'] = $widget.xb * 1.0;
	$t['y'] = $widget.yb * 1.0;
	$gPts[$t.ident] = $t;
	$t = [];
	$t['ident'] = $widget.ident + PT_C;  //store C endpoint
	$t['x'] = $widget.xc * 1.0;
	$t['y'] = $widget.yc * 1.0;
	$gPts[$t.ident] = $t;
	if ($widget.type == LH_TURNOUT || $widget.type == RH_TURNOUT) {
		$t = [];
		$t['ident'] = $widget.ident + PT_A;  //calculate and store A endpoint (mirror of B for these)
		$t['x'] = $widget.xcen - ($widget.xb - $widget.xcen);
		$t['y'] = $widget.ycen - ($widget.yb - $widget.ycen);
		$gPts[$t.ident] = $t;
	} else if ($widget.type == WYE_TURNOUT) {
		$t = [];
		$t['ident'] = $widget.ident + PT_A;  //store A endpoint
		$t['x'] = $widget.xa * 1.0;
		$t['y'] = $widget.ya * 1.0;
		$gPts[$t.ident] = $t;
	} else if ($widget.type == LH_XOVER || $widget.type == RH_XOVER || $widget.type == DOUBLE_XOVER) {
		$t = [];
		$t['ident'] = $widget.ident + PT_A;  //calculate and store A endpoint (mirror of C for these)
		$t['x'] = $widget.xcen - ($widget.xc - $widget.xcen);
		$t['y'] = $widget.ycen - ($widget.yc - $widget.ycen);
		$gPts[$t.ident] = $t;
		$t = [];
		$t['ident'] = $widget.ident + PT_D;  //calculate and store D endpoint (mirror of B for these)
		$t['x'] = $widget.xcen - ($widget.xb - $widget.xcen);
		$t['y'] = $widget.ycen - ($widget.yb - $widget.ycen);
		$gPts[$t.ident] = $t;
	}
}

//store the various points defined with a LevelXing (pass in widget)
//see jmri.jmrit.display.layoutEditor.LevelXing.java for background
function $storeLevelXingPoints($widget) {
    var $t = [];
    $t['ident'] = $widget.ident + LEVEL_XING_A;  //store A endpoint
    $t['x'] = $widget.xa * 1.0;
    $t['y'] = $widget.ya * 1.0;
    $gPts[$t.ident] = $t;
    $t = [];
    $t['ident'] = $widget.ident + LEVEL_XING_B;  //store B endpoint
    $t['x'] = $widget.xb * 1.0;
    $t['y'] = $widget.yb * 1.0;
    $gPts[$t.ident] = $t;
    $t = [];
    $t['ident'] = $widget.ident + LEVEL_XING_C;  //calculate and store A endpoint (mirror of A for these)
    $t['x'] = $widget.xcen - ($widget.xa - $widget.xcen);
    $t['y'] = $widget.ycen - ($widget.ya - $widget.ycen);
    $gPts[$t.ident] = $t;
    $t = [];
    $t['ident'] = $widget.ident + LEVEL_XING_D;  //calculate and store D endpoint (mirror of B for these)
    $t['x'] = $widget.xcen - ($widget.xb - $widget.xcen);
    $t['y'] = $widget.ycen - ($widget.yb - $widget.ycen);
    $gPts[$t.ident] = $t;
}

//drawLine, passing in values from xml
function $drawLine($pt1x, $pt1y, $pt2x, $pt2y, $color, $width) {
    var $savLineWidth = $gCtx.lineWidth;
    var $savStrokeStyle = $gCtx.strokeStyle;
    if (typeof $color !== "undefined")
        $gCtx.strokeStyle = $color;
    if (typeof $width !== "undefined")
        $gCtx.lineWidth = $width;
    $gCtx.beginPath();
    $gCtx.moveTo($pt1x, $pt1y);
    $gCtx.lineTo($pt2x, $pt2y);
    $gCtx.closePath();
    $gCtx.stroke();
    // put color and width back to default
    $gCtx.strokeStyle = $savStrokeStyle;
    $gCtx.lineWidth = $savLineWidth;
}

//drawArc, passing in values from xml
function $drawArc(pt1x, pt1y, pt2x, pt2y, degrees, $color, $width) {
    // Compute arc's chord
    var a = pt2x - pt1x;
    var o = pt2y - pt1y;
    var chord = Math.sqrt(((a * a) + (o * o))); //in pixels

    if (chord > 0.0) {  //don't bother if no length
        //save track settings for restore
        var $savLineWidth = $gCtx.lineWidth;
        var $savStrokeStyle = $gCtx.strokeStyle;
        if (typeof $color !== "undefined")
            $gCtx.strokeStyle = $color;
        if (typeof $width !== "undefined")
            $gCtx.lineWidth = $width;

        var halfAngle = (degrees / 2) * Math.PI / 180; //in radians
        var radius = (chord / 2) / (Math.sin(halfAngle));  //in pixels
        // Circle
        var startRad = Math.atan2(a, o) - halfAngle; //in radians
        // calculate center of circle
        var cx = ((pt2x * 1.0) - Math.cos(startRad) * radius);
        var cy = ((pt2y * 1.0) + Math.sin(startRad) * radius);

        //calculate start and end angle
        var startAngle = Math.atan2(pt1y - cy, pt1x - cx); //in radians
        var endAngle = Math.atan2(pt2y - cy, pt2x - cx); //in radians
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
var $getObjFromXML = function(e) {
    var $widget = {};
    $(e.attributes).each(function() {
        $widget[this.name] = this.value;
    });
    return $widget;
};

//build and return CSS array from attributes passed in
var $getTextCSSFromObj = function($widget) {
    var $retCSS = {};
    $retCSS['color'] = '';  //only clear attributes
    $retCSS['background-color'] = '';
    if (typeof $widget.red !== "undefined") {
        $retCSS['color'] = "rgb(" + $widget.red + "," + $widget.green + "," + $widget.blue + ") ";
    }
    //check for new hasBackground element, ignore background colors unless set to yes
    if (typeof $widget.hasBackground !== "undefined" && $widget.hasBackground == "yes") {
        $retCSS['background-color'] = "rgb(" + $widget.redBack + "," + $widget.greenBack + "," + $widget.blueBack + ") ";
    }
    if (typeof $widget.hasBackground == "undefined" && $widget.redBack !== "undefined") {
        $retCSS['background-color'] = "rgb(" + $widget.redBack + "," + $widget.greenBack + "," + $widget.blueBack + ") ";
    }
    if (typeof $widget.size !== "undefined") {
        $retCSS['font-size'] = $widget.size + "px ";
    }
    if (typeof $widget.margin !== "undefined") {
        $retCSS['padding'] = $widget.margin + "px ";
    }
    if (typeof $widget.borderSize !== "undefined") {
        $retCSS['border-width'] = $widget.borderSize + "px ";
    }
    if (typeof $widget.redBorder !== "undefined") {
        $retCSS['border-color'] = "rgb(" + $widget.redBorder + "," + $widget.greenBorder + "," + $widget.blueBorder + ") ";
        $retCSS['border-style'] = 'solid';
    }
    if (typeof $widget.fixedWidth !== "undefined") {
        $retCSS['width'] = $widget.fixedWidth + "px ";
    }
    if (typeof $widget.fixedHeight !== "undefined") {
        $retCSS['height'] = $widget.fixedHeight + "px ";
    }
    if (typeof $widget.justification !== "undefined") {
        if ($widget.justification == "centre") {
            $retCSS['text-align'] = "center";
        } else {
            $retCSS['text-align'] = $widget.justification;
        }
    }
    if (typeof $widget.style !== "undefined") {
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

//get width of an html element by wrapping a copy in a div, then getting width of div
function $getElementWidth($e) {
    o = $e.clone();
    o.wrap('<div></div>').css({'position': 'absolute', 'float': 'left', 'white-space': 'nowrap', 'visibility': 'hidden'}).appendTo($('body'));
    w = o.width();
    o.remove();
    return w;
}

//place widget in correct position, rotation, z-index and scale. (pass in dom element, to simplify calling from e.load())
var $setWidgetPosition = function(e) {

    var $id = e.attr('id');
    var $widget = $gWidgets[$id];  //look up the widget and get its panel properties

    if (typeof $widget !== "undefined") {  //don't bother if widget not found
    	
    	var $height = 0;
    	var $width  = 0;
    	//use html5 original sizes if available
    	if (typeof e[0].naturalHeight !== "undefined") {
    		$height = e[0].naturalHeight * $widget.scale;
    	} else {
    		$height = e.height() * $widget.scale;
    	}
    	if (typeof e[0].naturalWidth !== "undefined") {
    		$width = e[0].naturalWidth * $widget.scale;
    	} else {
    		$width = e.width() * $widget.scale;
    	}
        if ($widget.widgetFamily == "text") {  //special handling to get width of free-floating text
            $width = $getElementWidth(e) * $widget.scale;
        }

        // calculate x and y adjustment needed to keep upper left of bounding box in the same spot
		// adapted to match JMRI's NamedIcon.rotate(). Note: transform-origin set in .css file
		var tx = 0.0;
		var ty = 0.0;

		if ($height > 0 && ($widget.degrees !== 0 || $widget.scale != 1.0)) { // only calc offset if needed

			var $rad = $widget.degrees * Math.PI / 180.0;

			if (0 <= $widget.degrees && $widget.degrees < 90
					|| -360 < $widget.degrees && $widget.degrees <= -270) {
				tx = $height * Math.sin($rad);
				ty = 0.0;
			} else if (90 <= $widget.degrees && $widget.degrees < 180
					|| -270 < $widget.degrees && $widget.degrees <= -180) {
				tx = $height * Math.sin($rad) - $width * Math.cos($rad);
				ty = -$height * Math.cos($rad);
			} else if (180 <= $widget.degrees && $widget.degrees < 270
					|| -180 < $widget.degrees && $widget.degrees <= -90) {
				tx = -$width * Math.cos($rad);
				ty = -$width * Math.sin($rad) - $height * Math.cos($rad);
			} else /* if (270<=$widget.degrees && $widget.degrees<360) */{
				tx = 0.0;
				ty = -$width * Math.sin($rad);
			}
		}
		// position widget to adjusted position, set z-index, then set rotation
		e.css({
			position : 'absolute',
			left : (parseInt($widget.x) + tx) + 'px',
			top : (parseInt($widget.y) + ty) + 'px',
			zIndex : $widget.level
		});
		if ($widget.degrees !== 0) {
			var $rot = "rotate(" + $widget.degrees + "deg)";
			e.css({
				"transform" : $rot
			});
		}
		// set new height and width if scale specified
		if ($widget.scale != 1 && $height > 0) {
			e.css({
				height : $height + 'px',
				width : $width + 'px'
			});
		}
		// if this is an image that's rotated or scaled, set callback to
		// reposition on every icon load, as the icons can be different sizes.
		if (e.is("img") && ($widget.degrees !== 0 || $widget.scale != 1.0)) {
			e.unbind('load');
			e.load(function() {
				$setWidgetPosition($(this));
			});
		}

	}
};

// reDraw an icon-based widget to reflect changes to state or occupancy
var $reDrawIcon = function($widget) {
    // additional naming for indicator*icon widgets to reflect occupancy
    $indicator = ($widget.occupancysensor && $widget.occupancystate == ACTIVE ? "Occupied" : "");
	// set image src to requested state's image, if defined    
    if ($widget['icon' + $indicator + ($widget.state + "")]) { 
        $('img#' + $widget.id).attr('src', $widget['icon' + $indicator + ($widget.state + "")]);  
    } else if ($widget['defaulticon']) {  //if state icon not found, use default icon if provided
        $('img#' + $widget.id).attr('src', $widget['defaulticon']); 
    } else {
        if (window.console)
            console.log("ERROR: image not defined for " + $widget.widgetType + " " + $widget.id + ", state=" + $widget.state + ", occ=" + $widget.occupancystate);
    }
};

//set new value for widget, showing proper icon, return widgets changed
var $setWidgetState = function($id, $newState) {
    var $widget = $gWidgets[$id];
    if ($widget.state !== $newState) {  //don't bother if already this value
        if (window.console)
            console.log("setting " + $id + " for " + $widget.jsonType + " " + $widget.name + ", '" + $widget.state + "' --> '" + $newState + "'");
        $widget.state = $newState;
        switch ($widget.widgetFamily) {
            case "icon" :
                $reDrawIcon($widget)
                break;
            case "text" :
                if ($widget.jsonType == "memory" || $widget.jsonType == "block") {
                    if ($widget.widgetType == "fastclock") {
                        $drawClock($widget);
                    } else {  //set memory/block text to new value from server, suppressing "null"
                        $('div#' + $id).text(($newState != null) ? $newState : "");
                    }
                } else {
                    if (typeof $widget['text' + $newState] !== "undefined") {
                        $('div#' + $id).text($widget['text' + $newState]);  //set text to new state's text
                    }
                    if (typeof $widget['css' + $newState] !== "undefined") {
                        $('div#' + $id).css($widget['css' + $newState]); //set css to new state's css
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
//    } else {
//        if (window.console)
//            console.log("NOT setting " + $id + " for " + $widget.jsonType + " " + $widget.name + " --> " + $newState);
    }
};

//return a unique ID # when called
var $gUnique = function() {
    if (typeof $gUnique.id === 'undefined') {
        $gUnique.id = 0;
    }
    $gUnique.id++;
    return $gUnique.id;
};

//clean up a name, for example to use as an id  
var $safeName = function($name) {
    if (typeof $name == "undefined") {
        return "unique-" + $gUnique();
    } else {
        return $name.replace(/:/g, "_").replace(/ /g, "_").replace(/%20/g, "_");
    }
};

//send request for state change 
var sendElementChange = function(type, name, nextState) {
    if (window.console) {
        console.log("sending " + type + " " + name + " --> " + nextState);
    }
    jmri.setObject(type, name, nextState);
};

//show unexpected ajax errors
$(document).ajaxError(function(event, xhr, opt, exception) {
    if (xhr.statusText != "abort" && xhr.status != 0) {
        var $msg = "AJAX Error requesting " + opt.url + ", status= " + xhr.status + " " + xhr.statusText;
        $('div#messageText').text($msg);
        $("#activity-alert").addClass("show").removeClass("hidden");
        $('dvi#workingMessage').position({within: "window"});
        if (window.console)
            console.log($msg);
        return;
    }
    if (xhr.statusText == "timeout") {
        var $msg = "AJAX timeout " + opt.url + ", status= " + xhr.status + " " + xhr.statusText + " resending list....";
        if (window.console)
            console.log($msg);
        // TODO: need to recover somehow
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
var $getNextState = function($widget) {
    var $nextState = undefined;
    if ($widget.widgetType == 'signalheadicon') { //special case for signalheadicons
        switch ($widget.clickmode * 1) {          //   logic based on SignalHeadIcon.java
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
            	//TODO: handle lit/unlit toggle
            	// getSignalHead().setLit(!getSignalHead().getLit());
            	break;
            case 2 :
            	// getSignalHead().setHeld(!getSignalHead().getHeld());
            	$nextState = ($widget.state * 1 == HELD ? RED : HELD);  //toggle between red and held states
                break;
            case 3: //loop through all elements, finding iconX and get "next one", skipping special ones
                var $firstState = undefined;
                var $currentState = undefined;
                for (k in $widget) {
                    var s = k.substr(4); //extract the state from current icon var
                    if (k.indexOf('icon') == 0 && typeof $widget[k] !== "undefined" && k != 'icon' + HELD) { //valid value, name starts with 'icon', but not the HELD one
                        if (typeof $firstState == "undefined")
                            $firstState = s;  //remember the first state (for last one)
                        if (typeof $currentState !== "undefined" && typeof $nextState == "undefined")
                            $nextState = s; //last one was the current, so this one must be next
                        if (s == $widget.state)
                            $currentState = s;
                        if (window.console)
                            console.log('key: ' + k + " first=" + $firstState);
                    }
                }
                if (typeof $nextState == "undefined")
                    $nextState = $firstState;  //if still not set, start over
        } //end of switch 

    } else if ($widget.widgetType == 'signalmasticon') { //special case for signalmasticons
    	//loop through all elements, finding iconXXX and get next iconXXX, skipping special ones
    	switch ($widget.clickmode * 1) {          //   logic based on SignalMastIcon.java
    	case 0 :
    		var $firstState = undefined;
    		var $currentState = undefined;
    		for (k in $widget) {
    			var s = k.substr(4); //extract the state from current icon var
    			//look for next icon value, skipping Held, Dark and Unknown
    			if (k.indexOf('icon') == 0 && typeof $widget[k] !== "undefined" && s != 'Held' && s != 'Dark' 
              && s !='Unlit' && s !=  'Unknown') { 
    				if (typeof $firstState == "undefined")
    					$firstState = s;  //remember the first state (for last one)
    				if (typeof $currentState !== "undefined" && typeof $nextState == "undefined")
    					$nextState = s; //last one was the current, so this one must be next
    				if (s == $widget.state)
    					$currentState = s;
    				if (window.console)
    					console.log('key: ' + k + " first=" + $firstState);
    			}
    		};
    		if (typeof $nextState == "undefined")
    			$nextState = $firstState;  //if still not set, start over
    		break;
    		
    	case 1 :
    		//TODO: handle lit/unlit states 
    		break;
    		
    	case 2 :
    		//toggle between stop and held state
    		$nextState = ($widget.state == "Held" ? "Stop" : "Held");  
    		break;
    		
    	}; //end of switch clickmode

    } else {  //start with INACTIVE, then toggle to ACTIVE and back
        $nextState = ($widget.state == ACTIVE ? INACTIVE : ACTIVE);
    }

    if (typeof $nextState == "undefined")
        $nextState = $widget.state;  //default to no change
    return $nextState;
};

//request the panel xml from the server, and setup callback to process the response
var requestPanelXML = function(panelName) {
    $("activity-alert").addClass("show").removeClass("hidden");
    $.ajax({
        type: "GET",
        url: "/panel/" + panelName + "?format=xml", //request proper url
        success: function(data, textStatus, jqXHR) {
            processPanelXML(data, textStatus, jqXHR);
        },
        error: function( jqXHR, textStatus, errorThrown) {
            alert("Error retrieving panel xml from server.  Please press OK to retry.\n\nDetails: " + textStatus + " - " + errorThrown);
            window.location = window.location.pathname;
        },
        async: true,
        timeout: 15000, // very long timeout, since this can be a slow process for complicated panels
        dataType: "xml"
    });
};

//preload all images referred to by the widget
var $preloadWidgetImages = function($widget) {
    for (k in $widget) {
        if (k.indexOf('icon') == 0 && typeof $widget[k] !== "undefined" && $widget[k] != "yes") { //if attribute names starts with 'icon', it's an image, so preload it
            $("<img src='" + $widget[k] + "'/>");
        }
    }
};

//determine widget "family" for broadly grouping behaviors
//note: not-yet-supported widgets are commented out here so as to return undefined
var $getWidgetFamily = function($widget, $element) {

    if (($widget.widgetType == "positionablelabel" || $widget.widgetType == "linkinglabel") 
    		&& typeof $widget.text !== "undefined") {
        return "text";  //special case to distinguish text vs. icon labels
    }
    if ($widget.widgetType == "sensoricon" && $widget.icon == "no") {
        return "text";  //special case to distinguish text vs. icon labels
    }
    if ($widget.widgetType == "memoryicon" && $($element).find('memorystate').length == 0) {
        return "text";  //if no memorystate icons, treat as text 
    }
    switch ($widget.widgetType) {
        case "locoicon" :
        case "trainicon" :
        case "memoryComboIcon" :
        case "memoryInputIcon" :
        case "fastclock" :
        case "BlockContentsIcon" :
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
        case "memoryicon" :
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
    }
    if (window.console)
        console.log("unknown widget type of '" + $widget.widgetType +"'");
    return; //unrecognized widget returns undefined
};

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
                $setWidgetPosition($("#panel-area > #" + $widget.id));
                break;
        }
    });
};

function updateWidgets(name, state, data) {
    //if systemName not in systemNames list, replace userName with systemName
	if (!systemNames[name] && name != data.userName) {
        if (window.console)
		  console.log("replacing userName " + data.userName + " with systemName " + name);    	
		if (systemNames[data.userName]) {  										  //if found by userName
			systemNames[name] = systemNames[data.userName];  //copy entry over
			delete systemNames[data.userName];  							 //delete old one
		}
    }
	//update all widgets based on the element that changed 
    if (systemNames[name]) {
        $.each(systemNames[name], function(index, widgetId) {
            $setWidgetState(widgetId, state);
        });
    } else {
        if (window.console)
          console.log("system name " + name + " not found, can't set state to " + state);
    }
}

function updateOccupancy(occupancyName, state) {
    if (occupancyNames[occupancyName]) {
        if (window.console)
            console.log("setting occupancies for sensor " + occupancyName + " to " + state);
        $.each(occupancyNames[occupancyName], function(index, widgetId) {
            $widget = $gWidgets[widgetId];
            if ($widget.blockname) {
                $gBlks[$widget.blockname].state = state; //set occupancy for the block (if one) to the newstate
            }
            $gWidgets[widgetId].occupancystate = state; //set occupancy for the widget to the newstate
            switch ($widget.widgetType) {
                case 'layoutturnout' :
                    $drawTurnout($widget);
                    break;
                case 'tracksegment' :
                    $drawTrackSegment($widget);
                    break;
                case 'indicatortrackicon' :
                case 'indicatorturnouticon' :
                    $reDrawIcon($widget);
                    break;
            }
        });
    }
}

function listPanels(name) {
    $.ajax({
        url: "/panel/?format=json",
        data: {},
        success: function(data, textStatus, jqXHR) {
            if (data.length !== 0) {
                $.each(data, function(index, value) {
                    $gPanelList[value.data.userName] = value.data.name;
                });
            }
            if (name === null || typeof (panelName) === undefined) {
                if (data.length !== 0) {
                    $("#panel-list").empty();
                    $("#activity-alert").addClass("hidden").removeClass("show");
                    $("#panel-list").addClass("show").removeClass("hidden");
                    $.each(data, function(index, value) {
                        $("#panel-list").append("<div class=\"col-sm-6 col-md-4 col-lg-3\"><div class=\"thumbnail\"><a href=\"/panel/" + value.data.name + "\"><div class=\"thumbnail-image\"><img src=\"/panel/" + value.data.name + "?format=png\" style=\"width: 100%;\"></div><div class=\"caption\">" + value.data.userName + "</div></a></div></div>");
                        // (12 / col-lg-#) % index + 1
                        if (4 % (index + 1)) {
                            $("#panel-list").append("<div class=\"clearfix visible-lg\"></div>");
                        }
                        // (12 / col-md-#) % index + 1
                        if (3 % (index + 1)) {
                            $("#panel-list").append("<div class=\"clearfix visible-md\"></div>");
                        }
                        // (12 / col-sm-#) % index + 1
                        if (2 % (index + 1)) {
                            $("#panel-list").append("<div class=\"clearfix visible-sm\"></div>");
                        }
                    });
                    // resizeThumbnails(); // sometimes gets .thumbnail sizes too small under image. Why?
                } else {
                    $("#activity-alert").addClass("hidden").removeClass("show");
                    $("#warning-no-panels").addClass("show").removeClass("hidden");
                }
            }
        }
    });
}

function resizeThumbnails() {
    tallest = 0;
    $(".thumbnail-image").each(function() {
        thisHeight = $("img", this).height();
        if (thisHeight > tallest) {
            tallest = thisHeight;
        }
    });
    $(".thumbnail-image").each(function() {
        $(this).height(tallest);
    });
}

$(window).resize(function() {
    resizeThumbnails();
});

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function() {
    // get panel name if passed as a parameter
    var panelName = getParameterByName("name");
    // get panel name if part of the path
    if (panelName === null || typeof (panelName) === undefined) {
        var path = $(location).attr('pathname');
        path = path.split("/");
        if (path.length > 3) {
            panelName = path[path.length - 2] + "/" + path[path.length - 1];
        }
    }
    // setup the functional menu items
    $("#navbar-panel-reload > a").attr("href", location.href);
    $("#navbar-panel-xml > a").attr("href", location.href + "?format=xml");
    // show panel thumbnails if no panel name
    listPanels(panelName);
    if (panelName === null || typeof (panelName) === undefined) {
        $("#panel-list").addClass("show").removeClass("hidden");
        $("#panel-area").addClass("hidden").removeClass("show");
        // hide the Show XML menu when listing panels
        $("#navbar-panel-xml").addClass("hidden").removeClass("show");
    } else {
        jmri = $.JMRI({
            didReconnect: function() {
                // if a reconnect is triggered, reload the page - it is the
                // simplest method to refresh every object in the panel
                if (window.console) {
                    console.log("Reloading at reconnect");
                }
                location.reload(false);
            },
            light: function(name, state, data) {
                updateWidgets(name, state, data);
            },
            block: function(name, value, data) {
                updateWidgets(name, value, data);
            },
            memory: function(name, value, data) {
                updateWidgets(name, value, data);
            },
            reporter: function(name, value, data) {
                updateWidgets(name, value, data);
            },
            route: function(name, state, data) {
                updateWidgets(name, state, data);
            },
            sensor: function(name, state, data) {
                updateWidgets(name, state, data);
                updateOccupancy(name, state);
            },
            signalHead: function(name, state, data) {
                updateWidgets(name, state, data);
            },
            signalMast: function(name, state, data) {
                updateWidgets(name, state, data);
            },
            turnout: function(name, state, data) {
                updateWidgets(name, state, data);
            }
        });
        $("#panel-list").addClass("hidden").removeClass("show");
        $("#panel-area").addClass("show").removeClass("hidden");

        // include name of panel in page title. Will be updated to userName later
        setTitle(panelName);

        // Add a widget to retrieve current fastclock rate
        // this is a widget so special logic for retrieving this information
        // is not required
        $widget = new Array();
        $widget.jsonType = "memory";
        $widget['name'] = "IMRATEFACTOR";  // already defined in JMRI
        $widget['id'] = $widget['name'];
        $widget['safeName'] = $widget['name'];
        $widget['state'] = "1.0";
        $gWidgets[$widget.id] = $widget;

        // request actual xml of panel, and process it on return
        // uses setTimeout simply to not block other JavaScript since
        // requestPanelXML has a long timeout
        setTimeout(function() {
            requestPanelXML(panelName);
        },
                500);
    }
});
