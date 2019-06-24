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
 *  TODO: connect turnouts to layoutturntable rays and make clickable (see WhichWay)
 *  TODO: address color differences between java panel and javascript panel (e.g. lightGray)
 *  TODO: deal with mouseleave, mouseout, touchout, etc. Slide off Stop button on rb1 for example.
 *  TODO: make turnout, levelXing occupancy work like LE panels (more than just checking A)
 *  TODO: draw dashed curves
 *  TODO: handle inputs/selection on various memory widgets
 *  TODO: alignment of memoryIcons without fixed width is very different.  Recommended workaround is to use fixed width.
 *  TODO:    ditto for sensorIcons with text
 *  TODO: add support for slipturnouticon (one2beros)
 *  TODO: handle (and test) disableWhenOccupied for layoutslip
 *
 **********************************************************************************************/

//persistent (global) variables
var $gWidgets = {};         //array of all widget objects, key=CSSId
var $gPanelList = {};       //store list of available panels
var $gPanel = {};           //store overall panel info
var whereUsed = {};         //associative array of array of elements indexed by systemName or userName
var occupancyNames = {};    //associative array of array of elements indexed by occupancy sensor name
var $gPts = {};             //array of all points, key="pointname.pointtype" (used for layoutEditor panels)
var $gBlks = {};            //array of all blocks, key="blockname" (used for layoutEditor panels)
var $gCtx;                  //persistent context of canvas layer
var $gDashArray = [12, 12]; //on,off of dashed lines
var DOWNEVENT = 'touchstart mousedown';  //check both touch and mouse events
var UPEVENT = 'touchend mouseup';
var SIZE = 3;               //default factor for circles

var UNKNOWN = '0';          //constants to match JSON Server state names
var ACTIVE = '2';
var CLOSED = '2';
var INACTIVE = '4';
var THROWN = '4';
var INCONSISTENT = '8';

var PT_CEN = ".1";          //named constants for point types
var PT_A = ".2";
var PT_B = ".3";
var PT_C = ".4";
var PT_D = ".5";

var LEVEL_XING_A = ".6";
var LEVEL_XING_B = ".7";
var LEVEL_XING_C = ".8";
var LEVEL_XING_D = ".9";

var SLIP_A = ".21";
var SLIP_B = ".22";
var SLIP_C = ".23";
var SLIP_D = ".24";

var STATE_AC = 0x02;
var STATE_BD = 0x04;
var STATE_AD = 0x06;
var STATE_BC = 0x08;

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

var jmri_logging = false;

//
//  debug functions
//

// log object properties
function $logProperties(obj, force = false) {
	if (jmri_logging || force) {
		var $propList = "";
		for (var $propName in obj) {
			if (typeof obj[$propName] != "undefined") {
				$propList += ($propName + "='" + obj[$propName] + "', ");
			}
		}
		jmri.log("$logProperties(obj): " + $propList + ".");
	}
}

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

    //set up context used by switchboardeditor "beanswitch" objects, set some defaults
    if ($gPanel.paneltype === "Switchboard") {
        // TODO add contents
        //$("#panel-area").prepend("<canvas id='panelCanvas' width=95% height=95% style='position:absolute;z-index:2;'>");
        //set background color from panel attribute
        $("#panel-area").css({backgroundColor: $gPanel.backgroundcolor});

        //set short notice
        $("#panel-area").append("<div id=info class=show>Hello " + $gPanel.text + "</div>");
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
                if (typeof($widget["class"]) !== "undefined") {
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
                            case "LightIcon" :
                                $widget['name'] = $widget.light; //normalize name
                                $widget.jsonType = "light"; // JSON object type
                                $widget['icon' + UNKNOWN] = $(this).find('icons').find('unknown').attr('url');
                                $widget['icon2'] = $(this).find('icons').find('on').attr('url');
                                $widget['icon4'] = $(this).find('icons').find('off').attr('url');
                                $widget['icon8'] = $(this).find('icons').find('inconsistent').attr('url');
                                $widget['rotation'] = $(this).find('icons').find('unknown').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('icons').find('unknown').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('unknown').attr('scale');
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                if (typeof $widget["systemName"] == "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getLight($widget["systemName"]);
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
                                $widget['hoverText'] = "";          //for override of hovertext
                                var actives = $(this).find('active'); //get array of actives used by this multisensor
                                var $id = $widget.id;
                                actives.each(function(i, item) {  //loop thru array once to set up siblings array, to be copied to all siblings
                                    $widget.siblings.push($id);
                                    $widget.hoverText += $(item).attr('sensor') + " "; //add sibling names to hovertext
                                    $id = "widget-" + $gUnique(); //set new id to a unique value for each sibling
                                });
                                actives.each(function(i, item) {  //loop thru array again to create each widget
                                    $widget['id'] = $widget.siblings[i];         // use id already set in sibling array
                                    $widget.name = $(item).attr('sensor');
                                    $widget['icon2'] = $(item).attr('url');
                                    if (i < actives.size() - 1) { //only save widget and make a new one if more than one active found
                                        $preloadWidgetImages($widget); //start loading all images
                                        $widget['safeName'] = $safeName($widget.name);  //add a html-safe version of name
                                        $widget["systemName"] = $widget.name;
                                        $gWidgets[$widget.id] = $widget; //store widget in persistent array
                                        $drawIcon($widget); //actually place and position the widget on the panel
                                        jmri.getSensor($widget["systemName"]);
                                        if (!($widget.systemName in whereUsed)) {  //set where-used for this new sensor
                                            whereUsed[$widget.systemName] = new Array();
                                        }
                                        whereUsed[$widget.systemName][whereUsed[$widget.systemName].length] = $widget.id;
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
                                $widget['iconnull']="/web/images/transparent_19x16.png"; //transparent for null value
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
                                jmri.getMemory("IMRATEFACTOR"); //enable updates for fast clock rate                               
                                $widget['name'] = 'IMCURRENTTIME';  // already defined in JMRI
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
                            case "reportericon" :
                                $widget['name'] = $widget.reporter; //normalize name
                                $widget.jsonType = "reporter"; // JSON object type
                                $widget['text'] = $widget.reporter; //use name for initial text
                                if (typeof $widget["systemName"] == "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getReporter($widget["systemName"]);
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
                            case "beanswitch" : // Switchboard BeanSwitch of shape "button"
                                $widget['name'] = $widget.label; // normalize name
                                $widget['text'] = $widget.label; // use label as initial button text
                                if (typeof $widget["systemName"] == "undefined")
                                    $widget["systemName"] = $widget.name;
                                switch  ($widget["type"]) {
                                    case "T" :
                                        $widget.jsonType = "turnout"; // JSON object type
                                        jmri.getTurnout($widget["systemName"]); // have the html button immediately follow the state on the layout
                                        break;
                                    case "S" :
                                        $widget.jsonType = "sensor"; // JSON object type
                                        jmri.getSensor($widget["systemName"]);
                                        break;
                                    default :
                                        $widget.jsonType = "light"; // JSON object type
                                        jmri.getLight($widget["systemName"]);
                                }
                                // set each state's text (this is only applied when shape is button)
                                $widget['text' + UNKNOWN] = $(this).find('unknownText').attr('text'); // mimick java switchboard buttons
                                $widget['text2'] = $(this).find('activeText').attr('text');
                                $widget['text4'] = $(this).find('inactiveText').attr('text');
                                $widget['text8'] = $(this).find('inconsistentText').attr('text');
                                $widget['state'] = 0; // use 0 for initial state
                                $widget.styles['border'] = "2px solid black" //add border for looks (temporary)
                                $widget.styles['border-radius'] = "8px" // mimick JButtons
                                $widget.styles['color'] = $widget.textcolor; // use jmri color
                                $widget.styles['width'] = (90/$widget.columns) + "%"; // use jmri column number, 90pc to fit on screen
                                // CSS properties
                                $("#panel-area>#" + $widget.id).css(
                                {position: 'relative', float: 'left'});
                                $widget.classes += "button ";
                                if ($widget.connected == "true") {
                                    $widget['text'] = $widget['text0']; // add UNKNOWN state to label of connected switches
                                    $widget.classes += $widget.jsonType + " clickable ";
                                    $widget.styles['background-color'] = "rgb(240,240,240)" // very light grey to mark connected buttons
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
                        if (jmri_logging) {
                            jmri.log("case drawm " + $widget.widgetType);
                            $logProperties($widget);
                        }
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
                                $widget["blockcolor"] = $widget.trackcolor; //init blockcolor to trackcolor
                                //store these blocks in a persistent var
                                //by both username and systemname since references may be by either
                                $gBlks[$widget.username] = $widget
                                $gBlks[$widget.systemname] = $widget;
                                jmri.getLayoutBlock($widget.systemname);
                                break;
                            case "layoutturnout" :
                                $widget['name'] = $widget.turnoutname; //normalize name
                                $widget['safeName'] = $safeName($widget.name);  //add a html-safe version of name
                                $widget.jsonType = "turnout"; // JSON object type
                                $widget['x'] = $widget.xcen; //normalize x,y
                                $widget['y'] = $widget.ycen;
                                if ((typeof $widget.name !== "undefined") && ($widget.disabled !== "yes")) { 
                                    $widget.classes += $widget.jsonType + " clickable "; //make it clickable (unless no turnout assigned)
                                }
                                //set widget occupancy sensor from block to speed affected changes later
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
                            case "layoutSlip" :
                                //save the slip state to turnout state information
                                $widget['turnout'] = $(this).find('turnout:first').text();
                                $widget['turnoutB'] = $(this).find('turnoutB:first').text();

                                //jmri.log("tA: " + $widget.turnout + ", tB: " + $widget.turnoutB);

                                $widget['turnoutA_AC'] = Number($(this).find('states').find('A-C').find('turnout').text());
                                $widget['turnoutA_AD'] = Number($(this).find('states').find('A-D').find('turnout').text());
                                $widget['turnoutA_BC'] = Number($(this).find('states').find('B-C').find('turnout').text());
                                $widget['turnoutA_BD'] = Number($(this).find('states').find('B-D').find('turnout').text());

                                $widget['turnoutB_AC'] = Number($(this).find('states').find('A-C').find('turnoutB').text());
                                $widget['turnoutB_AD'] = Number($(this).find('states').find('A-D').find('turnoutB').text());
                                $widget['turnoutB_BC'] = Number($(this).find('states').find('B-C').find('turnoutB').text());
                                $widget['turnoutB_BD'] = Number($(this).find('states').find('B-D').find('turnoutB').text());

                                // default to this state
                                $widget['state'] = UNKNOWN;

                                $widget['name'] = $widget.ident;
                                $widget['safeName'] = $safeName($widget.name);  //add a html-safe version of name
                                $widget.jsonType = "turnout"; // JSON object type

                                $widget['x'] = $widget.xcen; //normalize x,y
                                $widget['y'] = $widget.ycen;

                                if (((typeof $widget.turnout !== "undefined") || (typeof $widget.turnoutB !== "undefined"))
                                		&& ($widget.disabled !== "yes")) {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }

                                //set widget occupancy sensor from block to speed affected changes later
                                if (typeof $gBlks[$widget.blockname] !== "undefined") {
                                    $widget['occupancysensor'] = $gBlks[$widget.blockname].occupancysensor;
                                    $widget['occupancystate'] = $gBlks[$widget.blockname].state;
                                }

                                $gWidgets[$widget.id] = $widget;    //store widget in persistent array
                                $storeSlipPoints($widget);          //also store the slip's 4 end points for other connections
                                $drawSlip($widget);                 //draw the slip

                                // convenience variables for points (A, B, C, D)
                                var ax = $gPts[$widget.ident + SLIP_A].x;
                                var ay = $gPts[$widget.ident + SLIP_A].y;
                                var bx = $gPts[$widget.ident + SLIP_B].x;
                                var by = $gPts[$widget.ident + SLIP_B].y;
                                var cx = $gPts[$widget.ident + SLIP_C].x;
                                var cy = $gPts[$widget.ident + SLIP_C].y;
                                var dx = $gPts[$widget.ident + SLIP_D].x;
                                var dy = $gPts[$widget.ident + SLIP_D].y;

                                var $cr = $gPanel.turnoutcirclesize * SIZE; //turnout circle radius
                                var $cd = $cr * 2;                          //turnout circle diameter

                                // left center
                                var lcx = (ax + bx) / 2;
                                var lcy = (ay + by) / 2;
                                // left fraction
                                var lf = $cr / Math.hypot($widget.xcen - lcx, $widget.ycen - lcy);
                                // left circle
                                var lccx = $lerp($widget.xcen, lcx, lf);
                                var lccy = $lerp($widget.ycen, lcy, lf);

                                //add an empty, but clickable, div to the panel and position it over the left turnout circle
                                $hoverText = " title='" + $widget.turnout + "' alt='" + $widget.turnout + "'";
                                $("#panel-area").append("<div id=" + $widget.id + "l class='" + $widget.classes + "' " + $hoverText + "></div>");
                                $("#panel-area>#" + $widget.id + "l").css(
                                    {position: 'absolute', left: (lccx - $cr) + 'px', top: (lccy - $cr) + 'px', zIndex: 3,
                                        width: $cd + 'px', height: $cd + 'px'});

                                // right center
                                var rcx = (cx + dx) / 2;
                                var rcy = (cy + dy) / 2;
                                // right fraction
                                var rf = $cr / Math.hypot($widget.xcen - rcx, $widget.ycen - rcy);
                                // right circle
                                var rccx = $lerp($widget.xcen, rcx, rf);
                                var rccy = $lerp($widget.ycen, rcy, rf);

                                //add an empty, but clickable, div to the panel and position it over the right turnout circle
                                $hoverText = " title='" + $widget.turnoutB + "' alt='" + $widget.turnoutB + "'";
                                $("#panel-area").append("<div id=" + $widget.id + "r class='" + $widget.classes + "' " + $hoverText + "></div>");
                                $("#panel-area>#" + $widget.id + "r").css(
                                    {position: 'absolute', left: (rccx - $cr) + 'px', top: (rccy - $cr) + 'px', zIndex: 3,
                                        width: $cd + 'px', height: $cd + 'px'});

                                // setup notifications (?)
                                jmri.getTurnout($widget["turnout"]);
                                jmri.getTurnout($widget["turnoutB"]);

                                if ($widget["occupancysensor"])
                                    jmri.getSensor($widget["occupancysensor"]); //listen for occupancy changes

                                // NOTE: turnout & turnoutB may appear to be swapped here however this is intentional
                                // (since the left turnout controls the right points and vice-versa) and we want
                                // the slip circles to toggle the points (not the turnout) on the corresponding side.
                                //
                                // note: the <div> areas above have their titles & alts turnouts swapped (left <-> right) also

                                // add turnout to whereUsed array (as $widget + 'r')
                                if (!($widget.turnout in whereUsed)) {  //set where-used for this new turnout
                                   whereUsed[$widget.turnout] = new Array();
                                }
                                whereUsed[$widget.turnout][whereUsed[$widget.turnout].length] = $widget.id + "r";

                                // add turnoutB to whereUsed array (as $widget + 'l')
                                if (!($widget.turnoutB in whereUsed)) {  //set where-used for this new turnout
                                   whereUsed[$widget.turnoutB] = new Array();
                                }
                                whereUsed[$widget.turnoutB][whereUsed[$widget.turnoutB].length] = $widget.id + "l";
                                break;
                            case "tracksegment" :
                                //set widget occupancy sensor from block to speed affected changes later
                                if (typeof $gBlks[$widget.blockname] !== "undefined") {
                                    $widget['occupancysensor'] = $gBlks[$widget.blockname].occupancysensor;
                                    $widget['occupancystate'] = $gBlks[$widget.blockname].state;
                                }
                                //store this widget in persistent array, with ident as key
                                $widget['id'] = $widget.ident;
                                $gWidgets[$widget.id] = $widget;

                                if ($widget.bezier == "yes") {
                                    $widget['controlpoints'] = $(this).find('controlpoint');
                                }

                                //draw the tracksegment
                                $drawTrackSegment($widget);
                                if ($widget["occupancysensor"])
                                    jmri.getSensor($widget["occupancysensor"]); //listen for occupancy changes
                                break;
                            case "levelxing" :
                                $widget['x'] = $widget.xcen; //normalize x,y
                                $widget['y'] = $widget.ycen;
                                //set widget occupancy sensor from block to speed affected changes later
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
                                //from jmri.jmrit.display.layoutEditor.layoutTurntable
                                $drawCircle($widget.xcen, $widget.ycen, $widget.radius); //draw the turnout circle
                                var $raytracks = $(this).find('raytrack');
                                var $txcen = $widget.xcen*1.0;
                                var $tycen = $widget.ycen*1.0;
                                $raytracks.each(function(i, item) {  //loop thru raytracks, calc and store end of ray point for each
                                    var $t = [];
                                    //note: .5 is due to TrackSegment.java TURNTABLE_RAY_OFFSET
                                    $t['ident'] = $widget.ident + ".5" + item.attributes['index'].value * 1;
                                    $angle = (item.attributes['angle'].value/180.0)*Math.PI;
                                    $t['x'] = $txcen + (($widget.radius*1.25)*Math.sin($angle)); //from getRayCoordsIndexed()
                                    $t['y'] = $tycen - (($widget.radius*1.25)*Math.cos($angle));
                                    $gPts[$t.ident] = $t; //store the endpoint of this ray
                                    //draw the line from ray endpoint to turntable edge
                                    var $t1 = [];
                                    $t1['x'] = $t.x - (($t.x - $txcen) * 0.2); //from drawTurntables()
                                    $t1['y'] = $t.y - (($t.y - $tycen) * 0.2);
                                    $drawLine($t1.x, $t1.y, $t.x, $t.y, $gPanel.defaulttrackcolor, $gPanel.sidetrackwidth);
                                });
                                break;
                            case "backgroundColor" :  //set background color of the panel itself
                                $("#panel-area").css({"background-color": "rgb(" + $widget.red + "," + $widget.green + "," + $widget.blue + ")"});
                                break;
                            default:
                                jmri.log("unknown $widget.widgetType: " + $widget.widgetType + ".");
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
                //add widgetid to whereused array to support updates
                if ($widget.systemName) {
                    if (!($widget.systemName in whereUsed)) {
                        whereUsed[$widget.systemName] = new Array();
                    }
                    whereUsed[$widget.systemName][whereUsed[$widget.systemName].length] = $widget.id;
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
    $('.clickable.multisensoricon').bind('click', $handleMultiClick);

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
    if (jmri_logging) jmri.log("$handleClick()");

    e.stopPropagation();
    e.preventDefault(); //prevent double-firing (touch + click)
    var $widget = $gWidgets[this.id];

    // if (null == $widget) {
    //     $logProperties(this);
    // }

    // special case for layoutSlips
    if (this.className.startsWith('layoutSlip ')) {
        if (this.id.startsWith("widget-") && (this.id.endsWith("r") || this.id.endsWith("l"))) {
            var slipID = this.id.slice(0, -1);
            $widget = $gWidgets[slipID];

            if (this.id.endsWith("l")) {
                $widget["side"] = "left";
            } else if (this.id.endsWith("r")) {
                $widget["side"] = "right";
            }
            if (jmri_logging) jmri.log("  layoutSlip-side:" + $widget.side);

            // convert current slip state to current turnout states
            var $oldStateA, $oldStateB;
            [$oldStateA, $oldStateB] = getTurnoutStatesForSlip($widget);

            // determine next slip state
            var $newState = getNextSlipState($widget);

            if (jmri_logging) jmri.log("$handleClick:layoutSlip: change state from " +
                slipStateToString($widget.state) + " to " + slipStateToString($newState) + ".");

            // convert new slip state to new turnout states
            var $newStateA, $newStateB;
            [$newStateA, $newStateB] = getTurnoutStatesForSlipState($widget, $newState);

            if ($oldStateA != $newStateA) {
                sendElementChange($widget.jsonType, $widget.turnout, $newStateA);
            }
            if ($oldStateB != $newStateB) {
                sendElementChange($widget.jsonType, $widget.turnoutB, $newStateB);
            }
        } else {
            jmri.log("$handleClick(e): unknown slip widget " + this.id);
            $logProperties(this);
        }
    } else {
        var $newState = $getNextState($widget);  //determine next state from current state
        sendElementChange($widget.jsonType, $widget.systemName, $newState);
        //also send new state to related turnout
        if (typeof $widget.turnoutB !== "undefined") {  
            sendElementChange($widget.jsonType, $widget.turnoutB, $newState);
        } 
        //used for crossover, layoutTurnout type 5
        if (typeof $widget.secondturnoutname !== "undefined") {
        	//invert 2nd turnout if requested
        	if ($widget.secondturnoutinverted == "true") {
        		$newState = ($newState==CLOSED ? THROWN : CLOSED); 
        	}
        	sendElementChange($widget.jsonType, $widget.secondturnoutname, $newState);
        }
    }
}

//perform multisensor click-handling, bound to click event for clickable multisensor widgets.
function $handleMultiClick(e) {
    e.stopPropagation();
    e.preventDefault(); //prevent double-firing (touch + click)
    var $widget = $gWidgets[this.id];
    var clickX = (e.offsetX || e.pageX - $(e.target).offset().left); //get click position on the widget
    var clickY = (e.offsetY || e.pageY - $(e.target).offset().top );
    jmri.log("handleMultiClick X,Y on WxH: " + clickX + "," + clickY + " on " + this.width + "x" + this.height);

    //increment or decrement based on where the click occurred on image
    var missed = true; //flag if click x,y outside image bounds, indicates we didn't get good values
    var dec = false;
    if ($widget.updown == "true") {
        if (clickY >= 0 && clickY <= this.height) missed = false;
        if (clickY > this.height / 2) dec = true;
    } else {
        if (clickX >= 0 && clickX <= this.width)  missed = false;
        if (clickX < this.width / 2)  dec = true;
    }
    var displaying = 0;
    for (i in $widget.siblings) {  //determine which is currently active
        if ($gWidgets[$widget.siblings[i]].state == ACTIVE) {
            displaying = i; //flag the current active sibling
        }
    }
    var next;  //determine which is the next one to be set active (loop around only if click outside object)
    if (dec) {
        next = displaying - 1;
        if (next < 0)
            if (missed)
                next = i;
            else
                next = 0;
    } else {
        next = displaying * 1 + 1;
        if (next > i)
            if (missed)
                next = 0;
            else
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
    if (typeof $color !== "undefined" && $savStrokeStyle != $color) //only change context if needed
        $gCtx.strokeStyle = $color;
    if (typeof $width !== "undefined" && $savLineWidth != $width)
        $gCtx.lineWidth = $width;
    $gCtx.beginPath();
    $gCtx.arc($ptx, $pty, $radius, 0, 2 * Math.PI, false);
    $gCtx.stroke();
    // put color and widths back to default, if changed
    if ($savStrokeStyle != $color)
        $gCtx.strokeStyle = $savStrokeStyle;
    if ($savLineWidth != $width)
      $gCtx.lineWidth = $savLineWidth;
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
        jmri.log("can't draw tracksegment " + $widget.ident + ": connect1: " + $widget.connect1name + "." + $widget.type1 + " undefined.");
        return;
    }
    var $pt2 = $gPts[$widget.connect2name + "." + $widget.type2];
    if (typeof $pt2 == "undefined") {
        jmri.log("can't draw tracksegment " + $widget.ident + ": connect2: " + $widget.connect2name + "." + $widget.type2 + " undefined.");
        return;
    }

    //set trackcolor based on blockcolor
    var $color = $gPanel.defaulttrackcolor;
    var $blk = $gBlks[$widget.blockname];
    if (typeof $blk !== "undefined") {
        $color = $blk.blockcolor;
    }

    var $width = $gPanel.sidetrackwidth;
    if ($widget.mainline == "yes") {
        $width = $gPanel.mainlinetrackwidth;
    }

    if ($widget.bezier == "yes") {
        //jmri.log("drawing bezier tracksegment " + $widget.ident + ".");

        var $cps = $widget.controlpoints;   // get the control points

        var points = [[$pt1.x, $pt1.y]];    // first point
        $cps.each(function( idx, elem ) {   // control points
            points.push([elem.attributes.x.value, elem.attributes.y.value]);
        });
        points.push([$pt2.x, $pt2.y]);  // last point
        $drawBezier(points, $gPanel.defaulttrackcolor, $gPanel.sidetrackwidth);

        if (false) {    // set true to draw construction lines thru control points
            var lastX = $pt1.x, lastY = $pt1.y; //start at end point 1
            $cps.each(function( idx, elem ) {
                var x = elem.attributes.x.value;
                var y = elem.attributes.y.value;
                //draw the line from last to this control point
                $drawLine(lastX, lastY, x, y, $gPanel.defaulttrackcolor, $gPanel.sidetrackwidth);
                lastX = x;
                lastY = y;
            });
            //draw the line from last to end point 2
            $drawLine(lastX, lastY, $pt2.x, $pt2.y, $gPanel.defaulttrackcolor, $gPanel.sidetrackwidth);
        }
    } else if (typeof $widget.angle == "undefined") {
        //jmri.log("drawing non-bezier tracksegment " + $widget.ident + ".");
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
    $h += "<img class='clockface' src='/web/images/clockface.png' />";              //add the clockface
    $h += "<img class='clockhourhand' src='/web/images/clockhourhand.png' />";      //add the hour hand
    $h += "<img class='clockminutehand' src='/web/images/clockminutehand.png' />";  //add the minute hand
    $("#panel-area>#" + $widget.id).html($h); //set the html for the widget

    var hours = $widget.state.split(':')[0];    //extract hours from format "H:MM AM"
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

        //add overlay text if specified, one layer above, and copy attributes (except background-color)
        if (typeof $widget.text !== "undefined") {
            $("#panel-area").append("<div id=" + $widget.id + "-overlay class='overlay'>" + $widget.text + "</div>");
			ovlCSS = {position:'absolute', left: $widget.x + 'px', top: $widget.y + 'px', zIndex: $widget.level*1.0 + 1, pointerEvents: 'none'};
			$.extend(ovlCSS, $widget.styles); //append the styles from the widget  
			delete ovlCSS['background-color'];  //clear the background color
            $("#panel-area>#" + $widget.id + "-overlay").css(ovlCSS);
        }
    } else {
        jmri.log("ERROR: image not defined for " + $widget.widgetType + " " + $widget.id + ", state=" + $widget.state + ", occ=" + $widget.occupancystate);
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
//    } else {
//        jmri.log("could not get trackwidth of "+$widget.connectaname+" for "+$widget.name);
    }
    if ($gPanel.turnoutcircles == "yes") {
        $drawCircle($widget.xcen, $widget.ycen, $gPanel.turnoutcirclesize * SIZE, $gPanel.turnoutcirclecolor, 1);
    }
    //  set trackcolor based on block color of AC block
    var $color = $gPanel.defaulttrackcolor;
    var $blk = $gBlks[$widget.blocknameac];
    if (typeof $blk !== "undefined") {
        $color = $blk.blockcolor;
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
//  see LayoutTurnout.draw()
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
//    } else {
//       jmri.log("could not get trackwidth of "+$widget.connectaname+" for "+$widget.name);
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

    //set trackcolor based on blockcolor
    var $color = $gPanel.defaulttrackcolor;
    var $blk = $gBlks[$widget.blockname];
    if (typeof $blk !== "undefined") {
        $color = $blk.blockcolor;
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
    
    // erase and draw turnout circles if enabled, including occupancy check
    if (($gPanel.turnoutcircles == "yes") && ($widget.disabled !== "yes")) {
    	$drawCircle($widget.xcen, $widget.ycen, $gPanel.turnoutcirclesize * SIZE, erase, 1);
    	if  (($widget.disableWhenOccupied !== "yes") || ($widget.occupancystate != ACTIVE)) {
    		$drawCircle($widget.xcen, $widget.ycen, $gPanel.turnoutcirclesize * SIZE, $gPanel.turnoutcirclecolor, 1);
    	}
    	// if disableWhenOccupied requested, disable click if enabled and active
    	if  ($widget.disableWhenOccupied == "yes") {
    		if ($widget.occupancystate == ACTIVE) {
    			$('#'+$widget.id).removeClass("clickable");
    			$('#'+$widget.id).unbind(UPEVENT, $handleClick);
    		} else { 
    			$('#'+$widget.id).addClass("clickable");
    			$('#'+$widget.id).bind(UPEVENT, $handleClick);
    		}
    	}
    }
}

//draw a Slip (pass in widget)
//  see LayoutSlip.draw()
function $drawSlip($widget) {
    //if set to hidden, don't draw anything
    if ($widget.hidden == "yes") {
        return;
    }
    if (jmri_logging) jmri.log("$drawSlip(" + $widget.id + "): state = " + $widget.state);

    var $widthA = $gPanel.sidetrackwidth;
    if (typeof $gWidgets[$widget.connectaname] !== "undefined") {
        if ($gWidgets[$widget.connectaname].mainline == "yes") {
            $widthA = $gPanel.mainlinetrackwidth;
        }
    }

    var $widthB = $gPanel.sidetrackwidth;
    if (typeof $gWidgets[$widget.connectbname] !== "undefined") {
        if ($gWidgets[$widget.connectbname].mainline == "yes") {
            $widthB = $gPanel.mainlinetrackwidth;
        }
    }

    var $widthC = $gPanel.sidetrackwidth;
    if (typeof $gWidgets[$widget.connectdname] !== "undefined") {
        if ($gWidgets[$widget.connectcname].mainline == "yes") {
            $widthC = $gPanel.mainlinetrackwidth;
        }
    }

    var $widthD = $gPanel.sidetrackwidth;
    if (typeof $gWidgets[$widget.connectdname] !== "undefined") {
        if ($gWidgets[$widget.connectdname].mainline == "yes") {
            $widthD = $gPanel.mainlinetrackwidth;
        }
    }

    var cenx = $widget.xcen;
    var ceny = $widget.ycen
    var ax = $gPts[$widget.ident + SLIP_A].x;
    var ay = $gPts[$widget.ident + SLIP_A].y;
    var bx = $gPts[$widget.ident + SLIP_B].x;
    var by = $gPts[$widget.ident + SLIP_B].y;
    var cx = $gPts[$widget.ident + SLIP_C].x;
    var cy = $gPts[$widget.ident + SLIP_C].y;
    var dx = $gPts[$widget.ident + SLIP_D].x;
    var dy = $gPts[$widget.ident + SLIP_D].y;

    var $erase = $gPanel.backgroundcolor;
    var $eraseWidth = $gPanel.mainlinetrackwidth;

    //set trackcolor[ABCD] based on blockcolor[ABCD]
    var $mainColourA = $gPanel.defaulttrackcolor;
    var $subColourA = $gPanel.defaulttrackcolor;
    var $blk = $gBlks[$widget.blockname];
    if (typeof $blk !== "undefined") {
        $mainColourA = $blk.blockcolor;
        $subColourA = $gPanel.blocktrackcolor;
    }

    var $mainColourB = $gPanel.defaulttrackcolor;
    var $subColourB = $gPanel.defaulttrackcolor;
    var $blk = $gBlks[$widget.blocknameb];
    if (typeof $blk !== "undefined") {
        $mainColourB = $blk.blockcolor;
        $subColourB = $gPanel.blocktrackcolor;
    }

    var $mainColourC = $gPanel.defaulttrackcolor;
    var $subColourC = $gPanel.defaulttrackcolor;
    var $blk = $gBlks[$widget.blocknamec];
    if (typeof $blk !== "undefined") {
        $mainColourC = $blk.blockcolor;
        $subColourC = $gPanel.blocktrackcolor;
    }

    var $mainColourD = $gPanel.defaulttrackcolor;
    var $subColourD = $gPanel.defaulttrackcolor;
    var $blk = $gBlks[$widget.blocknamed];
    if (typeof $blk !== "undefined") {
        $mainColourD = $blk.blockcolor;
        $subColourD = $gPanel.blocktrackcolor;
    }

    //slip A==-==D
    //      \\ //
    //        X
    //      // \\
    //     B==-==C

    // ERASE EVERYTHING FIRST
    if ($widget.state != STATE_AC) {
        //erase one third AC to one third CA
        $drawLine($third(ax, cx), $third(ay, cy), $third(cx, ax), $third(cy, ay), $erase, $eraseWidth);
    }

    if ($widget.state != STATE_BD) {
        if ($widget.slipType == DOUBLE_SLIP) {
            //erase one third BD to one third DB
            $drawLine($third(bx, dx), $third(by, dy), $third(dx, bx), $third(dy, by), $erase, $eraseWidth);
        }
    }

    if ($widget.state != STATE_AD) {
        //erase one third AD to one third AB
        $drawLine($third(ax, dx), $third(ay, dy), $third(dx, ax), $third(dy, ay), $erase, $eraseWidth);
    }

    if ($widget.state != STATE_BC) {
        //erase one third BC to one third CB
        $drawLine($third(bx, cx), $third(by, cy), $third(cx, bx), $third(cy, by), $erase, $eraseWidth);
    }

    // THEN DRAW
    if ($widget.state == STATE_AC) {
        $drawLine(ax, ay, $half(ax, cx), $half(ay, cy), $mainColourA, $widthA); //draw A to midpoint AC
        $drawLine(cx, cy, $half(ax, cx), $half(ay, cy), $mainColourC, $widthC); //draw C to midpoint AC
    } else {
        $drawLine(ax, ay, $third(ax, cx), $third(ay, cy), $mainColourA, $widthA); //draw A to one third AC
        $drawLine(cx, cy, $third(cx, ax), $third(cy, ay), $mainColourC, $widthC); //draw C to one third CA
    }

    if ($widget.state == STATE_BD) {
        $drawLine(bx, by, $half(bx, dx), $half(by, dy), $mainColourB, $widthB); //draw B to midpoint BD
        $drawLine(dx, dy, $half(bx, dx), $half(by, dy), $mainColourD, $widthD); //draw D to midpoint BD
    } else if ($widget.slipType == DOUBLE_SLIP) {
        $drawLine(bx, by, $third(bx, dx), $third(by, dy), $mainColourB, $widthB); //draw B to one third BD
        $drawLine(dx, dy, $third(dx, bx), $third(dy, by), $mainColourD, $widthD); //draw D to one third DB
    }

    if ($widget.state == STATE_AD) {
        $drawLine(ax, ay, $half(ax, dx), $half(ay, dy), $mainColourA, $widthA); //draw A to midpoint AD
        $drawLine(dx, dy, $half(ax, dx), $half(ay, dy), $mainColourD, $widthD); //draw D to midpoint AD
    } else {
        $drawLine(ax, ay, $third(ax, dx), $third(ay, dy), $mainColourA, $widthA); //draw A to one third AD
        $drawLine(dx, dy, $third(dx, ax), $third(dy, ay), $mainColourD, $widthD); //draw D to one third DA
    }

    if ($widget.state == STATE_BC) {
        $drawLine(bx, by, $half(bx, cx), $half(by, cy), $mainColourB, $widthB); //draw B to midpoint BC
        $drawLine(cx, cy, $half(bx, cx), $half(by, cy), $mainColourC, $widthC); //draw C to midpoint BC
    } else {
        $drawLine(bx, by, $third(bx, cx), $third(by, cy), $mainColourB, $widthB); //draw B to one third BC
        $drawLine(cx, cy, $third(cx, bx), $third(cy, by), $mainColourC, $widthC); //draw C to one third CB
    }

    if (($gPanel.turnoutcircles == "yes") && ($widget.disabled !== "yes")) {
    	
        //draw the two control circles
        var $cr = $gPanel.turnoutcirclesize * SIZE;  //turnout circle radius

        var lcx = (ax + bx) / 2;
        var lcy = (ay + by) / 2;
        var lf = $cr / Math.hypot($widget.xcen - lcx, $widget.ycen - lcy);
        var lccx = $lerp($widget.xcen, lcx, lf);
        var lccy = $lerp($widget.ycen, lcy, lf);
        $drawCircle(lccx, lccy, $cr, $gPanel.turnoutcirclecolor, 1);

        var rcx = (cx + dx) / 2;
        var rcy = (cy + dy) / 2;
        var rf = $cr / Math.hypot($widget.xcen - rcx, $widget.ycen - rcy);
        var rccx = $lerp($widget.xcen, rcx, rf);
        var rccy = $lerp($widget.ycen, rcy, rf);
        $drawCircle(rccx, rccy, $cr, $gPanel.turnoutcirclecolor, 1);
    }
}   // function $drawSlip($widget)

function $lerp(value1, value2, amount) {
    return ((1 - amount) * value1) + (amount * value2);
}

function $half(value1, value2) {
    return $lerp(value1, value2, 1 / 2);
}

function $third(value1, value2) {
    return $lerp(value1, value2, 1 / 3);
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

//store the various points defined with a Slip (pass in widget)
//see jmri.jmrit.display.layoutEditor.LayoutSlip.java for background
function $storeSlipPoints($widget) {
    var $t = [];
    $t['ident'] = $widget.ident + SLIP_A;  //store A endpoint
    $t['x'] = $widget.xa * 1.0;
    $t['y'] = $widget.ya * 1.0;
    $gPts[$t.ident] = $t;

    $t = [];
    $t['ident'] = $widget.ident + SLIP_B;  //store B endpoint
    $t['x'] = $widget.xb * 1.0;
    $t['y'] = $widget.yb * 1.0;
    $gPts[$t.ident] = $t;

    $t = [];
    $t['ident'] = $widget.ident + SLIP_C;  //calculate and store C endpoint (mirror of A for these)
    $t['x'] = $widget.xcen - ($widget.xa - $widget.xcen);
    $t['y'] = $widget.ycen - ($widget.ya - $widget.ycen);
    $gPts[$t.ident] = $t;

    $t = [];
    $t['ident'] = $widget.ident + SLIP_D;  //calculate and store D endpoint (mirror of B for these)
    $t['x'] = $widget.xcen - ($widget.xb - $widget.xcen);
    $t['y'] = $widget.ycen - ($widget.yb - $widget.ycen);
    $gPts[$t.ident] = $t;
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
    if (typeof $color !== "undefined" && $savStrokeStyle != $color) //only change context if needed
        $gCtx.strokeStyle = $color;
    if (typeof $width !== "undefined" && $savLineWidth != $width)
        $gCtx.lineWidth = $width;
    $gCtx.beginPath();
    $gCtx.moveTo($pt1x, $pt1y);
    $gCtx.lineTo($pt2x, $pt2y);
    $gCtx.stroke();
    // put color and width back to default, if changed
    if ($savStrokeStyle != $color)
      $gCtx.strokeStyle = $savStrokeStyle;
    if ($savLineWidth != $width)
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
        if (typeof $color !== "undefined" && $savStrokeStyle != $color) //only change context if needed
            $gCtx.strokeStyle = $color;
        if (typeof $width !== "undefined" && $savLineWidth != $width)
            $gCtx.lineWidth = $width;

        var halfAngle = (degrees / 2) * Math.PI / 180; //in radians
        var radius = (chord / 2) / (Math.sin(halfAngle));  //in pixels
        // Circle
        var startRad = Math.atan2(a, o) - halfAngle; //in radians
        // calculate center of circle
        var cx = (pt2x * 1.0) - Math.cos(startRad) * radius;
        var cy = (pt2y * 1.0) + Math.sin(startRad) * radius;

        //calculate start and end angle
        var startAngle = Math.atan2(pt1y - cy, pt1x - cx); //in radians
        var endAngle = Math.atan2(pt2y - cy, pt2x - cx); //in radians
        var counterClockwise = false;

        $gCtx.beginPath();
        $gCtx.arc(cx, cy, radius, startAngle, endAngle, counterClockwise);
        $gCtx.stroke();
        // put color and width back to default (if changed)
        if ($savStrokeStyle != $color)
          $gCtx.strokeStyle = $savStrokeStyle;
        if ($savLineWidth != $width)
          $gCtx.lineWidth = $savLineWidth;
    }
}

//
//drawBezier
//
var bezier1st = true;
function $drawBezier(points, $color, $width) {
    try {
        var $savLineWidth = $gCtx.lineWidth;
        var $savStrokeStyle = $gCtx.strokeStyle;
        //only change context if needed
        if (typeof $color !== "undefined" && $savStrokeStyle != $color)
            $gCtx.strokeStyle = $color;
        if (typeof $width !== "undefined" && $savLineWidth != $width)
            $gCtx.lineWidth = $width;

        bezier1st = true;
        $gCtx.beginPath();
        $plotBezier(points);
        $gCtx.stroke();

        // put color and width back to default, if changed
        if ($savStrokeStyle != $color)
          $gCtx.strokeStyle = $savStrokeStyle;
        if ($savLineWidth != $width)
          $gCtx.lineWidth = $savLineWidth;
    } catch (e) {
        if (jmri_logging) {
            jmri.log("$plotBezier exception: " + e);
            var vDebug = "";
            for (var prop in e) {
               vDebug += "      ["+ prop+ "]: '"+ e[prop]+ "'\n";
            }
            vDebug += "toString(): " + " value: [" + e.toString() + "]";
            jmri.log(vDebug);
        }
    }
}

//
//plotBezier - recursive function to draw bezier curve
//
function $plotBezier(points, depth = 0) {
    var len = points.length, idx, jdx;

    //jmri.log("points: " + points);

    // calculate flatness to determine if we need to recurse...
    var outer_distance = 0;
    for (var idx = 1; idx < len; idx++) {
        outer_distance += $distance(points[idx - 1], points[idx]);
    }
    var inner_distance = $distance(points[0], points[len - 1]);
    var flatness = outer_distance / inner_distance;

    // depth prevents stack overflow
    // (I picked 12 because 2^12 = 2048 is larger than most monitors ;-)
    // the flatness comparison value is somewhat arbitrary.
    // (I just kept moving it closer to 1 until I got good results. ;-)
    if ((depth > 12) || (flatness <= 1.001)) {
        var p0 = points[0], pN = points[len - 1];
        if (bezier1st) {
            $gCtx.moveTo(p0[0], p0[1]);
            bezier1st = false;
        }
        $gCtx.lineTo(p0[0], pN[1]);
    } else {
        // calculate (len - 1) order of points
        // (zero'th order are the input points)
        var orderPoints = [];
        for (idx = 0; idx < len - 1; idx++) {
            var nthOrderPoints = [];
            for (jdx = 0; jdx < len - 1 - idx; jdx++) {
                if (idx == 0) {
                    nthOrderPoints.push($midpoint(points[jdx], points[jdx + 1]));
                } else {
                    nthOrderPoints.push($midpoint(orderPoints[idx - 1][jdx], orderPoints[idx - 1][jdx + 1]));
                }
            }
            orderPoints.push(nthOrderPoints);
        }

        // collect left points
        var leftPoints = [];
        leftPoints.push(points[0]);
        for (idx = 0; idx < len - 1; idx++) {
            leftPoints.push(orderPoints[idx][0]);
        }
        // draw left side Bezier
        $plotBezier(leftPoints, depth + 1);

        // collect right points
        var rightPoints = [];
        for (idx = 0; idx < len - 1; idx++) {
            rightPoints.push(orderPoints[len - 2 - idx][idx]);
        }
        rightPoints.push(points[len - 1]);

        // draw right side Bezier
        $plotBezier(rightPoints, depth + 1);
    }
}

function $distance(p1, p2) {
    var dx = p2[0] - p1[0];
    var dy = p2[1] - p1[1];
    return Math.sqrt((dx * dx) + (dy * dy));
}

function $midpoint(p1, p2) {
    var result = [];
    result.push($half(p1[0], p2[0]));
    result.push($half(p1[1], p2[1]));
    return result;
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
    if (typeof $widget.fontname !== "undefined") {
        $retCSS['font-family'] = $widget.fontname;
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

    if (typeof $widget !== "undefined" && $widget.widgetType !== "beanswitch") {  //don't bother if widget not found or BeanSwitch

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
        jmri.log("ERROR: image not defined for " + $widget.widgetType + " " + $widget.id + ", state=" + $widget.state + ", occ=" + $widget.occupancystate);
    }
};

// set new value for widget, showing proper icon, return widgets changed
var $setWidgetState = function($id, $newState) {
    var $widget = $gWidgets[$id];

    // if undefined widget this must be a slip
    if (typeof $widget == "undefined") {
        // does it have "l" or "r" suffix?
        if ($id.endsWith("l") || $id.endsWith("r")) {   // (yes!)

            if (jmri_logging) jmri.log("#### INFO: clicked slip " + $id + " to state " + $newState);

            // remove suffix
            var slipID = $id.slice(0, -1);
            // get the slip widget
            $widget = $gWidgets[slipID];

            // convert current slip state to current turnout states
            var $stateA, $stateB;
            [$stateA, $stateB] = getTurnoutStatesForSlip($widget);

            if (jmri_logging) jmri.log("#### Slip " + $widget.name +
                " before: " + slipStateToString($widget.state) +
                ", stateA: " + turnoutStateToString($stateA) +
                ", stateB: " + turnoutStateToString($stateB));

            // change appropriate turnout state
            if ($id.endsWith("r")) {
                if ($stateA != $newState) {
                    if (jmri_logging) jmri.log("#### Changed slip " + $widget.name +
                        " $stateA from " + turnoutStateToString($stateA) +
                        " to " + turnoutStateToString($newState));
                    $stateA = $newState;
                }
            } else if ($id.endsWith("l")) {
                if ($stateB != $newState) {
                    if (jmri_logging) jmri.log("#### Changed slip " + $widget.name +
                        " $stateB from " + turnoutStateToString($stateB) +
                        " to " + turnoutStateToString($newState));
                    $stateB = $newState;
                }
            }

            // turn turnout states back into slip state
            $newState = getSlipStateForTurnoutStates($widget, $stateA, $stateB);
            if (jmri_logging) jmri.log("#### Slip " + $widget.name +
                " after: " + slipStateToString($newState) +
                ", stateA: " + turnoutStateToString($stateA) +
                ", stateB: " + turnoutStateToString($stateB));

            //if ($widget.state != $newState) {
            //    if (jmri_logging) jmri.log("#### Changing slip " + $widget.name + " from " + slipStateToString($widget.state) +
            //        " to " + slipStateToString($newState));
            //}

            // set $id to slip id
            $id = slipID;
        } else {
            jmri.log("$setWidgetState unknown $id: '" + $id + "'.");
            return;
        }
    } else if ($widget.widgetType == 'layoutSlip') {
        if (jmri_logging) jmri.log("#### $setWidgetState(slip " + $id + ", " + slipStateToString($newState) +
            "); (was " + slipStateToString($widget.state) + ")");
        // JMRI doesn't send slip states… it sends slip turnout states…
        // so ignore this (incorrect) slip state change
        return;
    }

    if ($widget.state !== $newState) {  //don't bother if already this value
        if (jmri_logging) jmri.log("JMRI changed " + $id + " (" + $widget.jsonType + " " + $widget.name + ") from state '" + $widget.state + "' to '" + $newState + "'.");
        $widget.state = $newState;

        switch ($widget.widgetFamily) {
            case "icon" :
                $reDrawIcon($widget)
                break;
            case "text" :
                if ($widget.jsonType == "memory" || $widget.jsonType == "block" || $widget.jsonType == "reporter" ) {
                    if ($widget.widgetType == "fastclock") {
                        $drawClock($widget);
                    } else {  //set memory/block/reporter text to new value from server, suppressing "null"
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
                } else if ($widget.widgetType == "layoutSlip") {
                    $drawSlip($widget);
                }
                break;
        }
        $gWidgets[$id].state = $newState;  //update the persistent widget to the new state
//    } else {
//        jmri.log("NOT setting " + $id + " for " + $widget.jsonType + " " + $widget.name + " --> " + $newState);
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
var sendElementChange = function(type, name, state) {
    jmri.log("Sending JMRI " + type + " '" + name + "' state '" + state + "'.");
    jmri.setObject(type, name, state);
};

//show unexpected ajax errors
$(document).ajaxError(function(event, xhr, opt, exception) {
    if (xhr.statusText != "abort" && xhr.status != 0) {
        var $msg = "AJAX Error requesting " + opt.url + ", status= " + xhr.status + " " + xhr.statusText;
        $('div#messageText').text($msg);
        $("#activity-alert").addClass("show").removeClass("hidden");
        $('dvi#workingMessage').position({within: "window"});
        jmri.log($msg);
        return;
    }
    if (xhr.statusText == "timeout") {
        var $msg = "AJAX timeout " + opt.url + ", status= " + xhr.status + " " + xhr.statusText + " resending list....";
        jmri.log($msg);
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
    //$logProperties($widget);
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
                    var s = k.substr(4) * 1; //extract the state from current icon var, insure it is treated as numeric
                    //get valid value, name starts with 'icon', but not the HELD or DARK ones
                    if (k.indexOf('icon') == 0 && typeof $widget[k] !== "undefined" && k != 'icon' + HELD && k != 'icon' + DARK) { 
                        if (typeof $firstState == "undefined")
                            $firstState = s;  //remember the first state (for last one)
                        if (typeof $currentState !== "undefined" && typeof $nextState == "undefined")
                            $nextState = s; //last one was the current, so this one must be next
                        if (s == $widget.state)
                            $currentState = s;
//                      jmri.log('key: '+k+" first="+$firstState+" current="+$currentState+" next="+$nextState);
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
//                  jmri.log('key: ' + k + " first=" + $firstState);
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
    $("#activity-alert").addClass("show").removeClass("hidden");
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
        case "reportericon" :
        case "beanswitch" :
            return "text";
            break;
        case "positionablelabel" :
        case "linkinglabel" :
        case "turnouticon" :
        case "sensoricon" :
        case "LightIcon" :
        case "multisensoricon" :
        case "signalheadicon" :
        case "signalmasticon" :
        case "indicatortrackicon" :
        case "indicatorturnouticon" :
        case "memoryicon" :
            return "icon";
            break;
        case "layoutSlip" :
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
    jmri.log("unknown widget type of '" + $widget.widgetType +"'");
    return; //unrecognized widget returns undefined
};

//redraw all "drawn" elements for given block (called after color change)
function $redrawBlock(blockName) {
//     jmri.log("redrawing all track for block " + blockName);
    //loop thru widgets, if block matches, redraw widget by proper method
    jQuery.each($gWidgets, function($id, $widget) {
        if ($widget.blockname == blockName) {
            switch ($widget.widgetType) {
            case 'layoutturnout' :
                $drawTurnout($widget);
                break;
            case 'layoutSlip' :
                $drawSlip($widget);
                break;
            case 'tracksegment' :
                $drawTrackSegment($widget);
                break;
            case 'levelxing' :
                $drawLevelXing($widget);
                break;
            }
        }
    });
};

//redraw all "drawn" elements to overcome some bidirectional dependencies in the xml
var $drawAllDrawnWidgets = function() {
    //loop thru widgets, redrawing each visible widget by proper method
    jQuery.each($gWidgets, function($id, $widget) {
        switch ($widget.widgetType) {
            case 'layoutturnout' :
                $drawTurnout($widget);
                break;
            case 'layoutSlip' :
                $drawSlip($widget);
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
    //update all widgets based on the element that changed, using systemname
    if (whereUsed[name]) {
        if (jmri_logging) jmri.log("updateWidgets(" + name + ", " + state + ", data);");
        $.each(whereUsed[name], function(index, widgetId) {
            $setWidgetState(widgetId, state);
        });
//    } else {
//      jmri.log("system name " + name + " not found, can't set state to " + state);
    }
    //update all widgets based on the element that changed, using username
    if (whereUsed[data.userName]) {
        if (jmri_logging) jmri.log("updateWidgets(" + data.userName + ", " + state + ", data);");
        $.each(whereUsed[data.userName], function(index, widgetId) {
            $setWidgetState(widgetId, state);
        });
//    } else {
//      jmri.log("userName " + name + " not found, can't set state to " + state);
    }
}

function updateOccupancy(occupancyName, state, data) {
    //handle occupancy sensors by systemname
    if (occupancyNames[occupancyName]) {
        updateOccupancySub(occupancyName, state);
    }
    //handle occupancy sensors by username
    if (occupancyNames[data.userName]) {
        updateOccupancySub(data.userName, state);
    }
}

function updateOccupancySub(occupancyName, state) {
    if (occupancyNames[occupancyName]) {
        //jmri.log("setting occupancies for sensor " + occupancyName + " to " + state);
        $.each(occupancyNames[occupancyName], function(index, widgetId) {
            $widget = $gWidgets[widgetId];
            if ($widget.blockname) {
                $gBlks[$widget.blockname].state = state; //set occupancy for the block (if one) to the newstate
            }
            $gWidgets[widgetId].occupancystate = state; //set occupancy for the widget to the newstate
            switch ($widget.widgetType) {
            case 'indicatortrackicon' :
            case 'indicatorturnouticon' :
                $reDrawIcon($widget);
                break;
            case 'layoutturnout' :
                $drawTurnout($widget);
                break;
            }
        });
    }
}

function setBlockColor(blockName, newColor) {
    //jmri.log("setting color for block " + blockName + " to " + newColor);
    var $blk = $gBlks[blockName];
    if (typeof $blk != "undefined") {
        $gBlks[blockName].blockcolor = newColor;
    } else {
        jmri.log("ERROR: block " + blockName + " not found for color " + newColor);
    }
    $redrawBlock(blockName);
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
                jmri.log("Reloading at reconnect");
                location.reload(false);
            },
            light: function(name, state, data) {
                updateWidgets(name, state, data);
            },
            block: function(name, value, data) {
                updateWidgets(name, value, data);
            },
            layoutBlock: function(name, value, data) {
                setBlockColor(data.userName, data.blockColor);
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
                updateOccupancy(name, state, data);
                updateWidgets(name, state, data);
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

        // Add a widget to retrieve fastclock rate
        $widget = new Array();
        $widget.jsonType = "memory";
        $widget['name'] = "IMRATEFACTOR";  // already defined in JMRI
        $widget['id'] = $widget['name'];
        $widget['safeName'] = $widget['name'];
        $widget['systemName'] = $widget['name'];       
        $widget['state'] = "1.0";
        $gWidgets[$widget.id] = $widget;
        if (!($widget.systemName in whereUsed)) {  //set where-used for this new memory
            whereUsed[$widget.systemName] = new Array();
        }
        whereUsed[$widget.systemName][whereUsed[$widget.systemName].length] = $widget.id;        

        // request actual xml of panel, and process it on return
        // uses setTimeout simply to not block other JavaScript since
        // requestPanelXML has a long timeout
        setTimeout(function() {
            requestPanelXML(panelName);
        }, 500);
    }
});

// convert turnout state to string
function turnoutStateToString(state) {
    result = "UKNONWN"
    switch (state) {
        case 2:
            result = "CLOSED";
            break;
        case 4:
            result = "THROWN";
            break;
        case 8:
            result = "INCONSISTENT";
            break;
    }
    return result;
}

// convert slip state to string
function slipStateToString(state) {
    result = "UNKNOWN";
    switch (state) {
        case STATE_AC:
            result = "STATE_AC";
            break;
        case STATE_AD:
            result = "STATE_AD";
            break;
        case STATE_BC:
            result = "STATE_BC";
            break;
        case STATE_BD:
            result = "STATE_BD";
            break;
    }
    return result;
}

function getTurnoutStatesForSlipState(slipWidget, slipState) {
    var results = [0, 0];  // unknown, unknown
    if (typeof slipWidget != "undefined") {
        if (slipWidget.widgetType == "layoutSlip") {
            switch (slipState) {
                case STATE_AC:
                    results = [slipWidget.turnoutA_AC, slipWidget.turnoutB_AC];
                    break;
                case STATE_AD:
                    results = [slipWidget.turnoutA_AD, slipWidget.turnoutB_AD];
                    break;
                case STATE_BC:
                    results = [slipWidget.turnoutA_BC, slipWidget.turnoutB_BC];
                    break;
                case STATE_BD:
                    results = [slipWidget.turnoutA_BD, slipWidget.turnoutB_BD];
                    break;
            }
        }
    }
    return results;
}   // function getTurnoutStatesForSlipState

function getTurnoutStatesForSlip(slipWidget) {
    return getTurnoutStatesForSlipState(slipWidget, slipWidget.state);
}

function getSlipStateForTurnoutStatesClosest(slipWidget, stateA, stateB, useClosest) {
    var result = 0; // unknown
    if ((stateA == slipWidget.turnoutA_AC) && (stateB == slipWidget.turnoutB_AC)) {
        result = STATE_AC;
    } else if ((stateA == slipWidget.turnoutA_AD) && (stateB == slipWidget.turnoutB_AD)) {
        result = STATE_AD;
    } else if ((stateA == slipWidget.turnoutA_BC) && (stateB == slipWidget.turnoutB_BC)) {
        result = STATE_BC;
    } else if ((stateA == slipWidget.turnoutA_BD) && (stateB == slipWidget.turnoutB_BD)) {
        result = STATE_BD;
    } else if (useClosest) {
        if ((stateA == slipWidget.turnoutA_AC) || (stateB == slipWidget.turnoutB_AC)) {
            result = STATE_AC;
        } else if ((stateA == slipWidget.turnoutA_AD) || (stateB == slipWidget.turnoutB_AD)) {
            result = STATE_AD;
        } else if ((stateA == slipWidget.turnoutA_BC) || (stateB == slipWidget.turnoutB_BC)) {
            result = STATE_BC;
        } else if ((stateA == slipWidget.turnoutA_BD) || (stateB == slipWidget.turnoutB_BD)) {
            result = STATE_BD;
        } else {
            result = STATE_AD;
        }
    }
    return result;
}

function getSlipStateForTurnoutStates(slipWidget, stateA, stateB) {
    return getSlipStateForTurnoutStatesClosest(slipWidget, stateA, stateB, true)
}

function getNextSlipState(slipWidget) {
    var result = 0; // unknown

    switch (slipWidget.side) {
        case 'left': {
            switch (slipWidget.state) {
                case STATE_AC:
                    if (slipWidget.slipType == SINGLE_SLIP) {
                        result = STATE_BD;
                    } else {
                        result = STATE_BC;
                    }
                    break;
                case STATE_AD:
                    result = STATE_BD;
                    break;
                case STATE_BC:
                default:
                    result = STATE_AC;
                    break;
                case STATE_BD:
                    result = STATE_AD;
                    break;
            }
            break;
        }
        case 'right': {
            switch (slipWidget.state) {
                case STATE_AC:
                default:
                    result = STATE_AD;
                    break;
                case STATE_AD:
                    result = STATE_AC;
                    break;
                case STATE_BC:
                    result = STATE_BD;
                    break;
                case STATE_BD:
                    if (slipWidget.slipType == SINGLE_SLIP) {
                        result = STATE_AC;
                    } else {
                        result = STATE_BC;
                    }
                    break;
            }
            break;
        }
        default: {
            jmri.log("getNextSlipState($widget): unknown $widget.side: " + slipWidget.side);
            break;
        }
    }   // switch (slipWidget.side)
    return result;
}   // function getNextSlipState(slipWidget)
