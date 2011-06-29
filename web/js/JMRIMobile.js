/**********************************************************************************************
*  JMRI Mobile - Browser interface to JMRI operational functions
*  
*  Written using jQuery Mobile and Templates, ajax calls for retrieving the data from JMRI XMLIO servlet
*  
*  HTML page contains templates for "pages" and list items, with actual values replaced by template processing
*    
*  Uses two connections to XMLIO server:
*    1) a "monitoring" connection that sends a full list of items to monitor, and waits for the server
*          to reply with a new list when anything changes.  This new list is then sent back as the basis for 
*          starting a new monitoring connection
*     2) a "change" connection which only sends "set" commands based on user input  
*     
*     Refresh required to see structure changes (new items, user data changes, etc.)
*
**********************************************************************************************/

//TODO: fix memory variable monitoring (sending constantly now, so removed from list)
//TODO: add button? link to type's page from settings Include list
//TODO: checking a new function creates a new, duplicate connection (not needed)
//TODO: "wide-screen" version that shows multiple "pages" at once, for use on wider browsers
//TODO: add edit of memory variable values
//TODO: support addition of memory variables, maybe turnouts?
//TODO: (long-term) read panel xml and "draw" panels on page
//TODO: set static parms as defaults in ajaxSetup()

var $gPrevType = ""; //persistent variable to help refresh views only on change 
var $gLastLogMsgTime =new Date().getTime();  //save last time (for logging elapsed time in debug messages)
var $gValues = new Array();  //persistent variable to keep track of current values, to avoid reprocessing unchanged items

//handle button press, send request for immediate change 
var $sendChange = function($type, $name, $nextValue){
	$.mobile.showPageLoadingMsg();  //show pageloading message
	var $commandstr = '<xmlio><item><type>' + $type + '</type><name>' + $name + '</name><set>' + $nextValue +'</set></item>' 
	    + '</xmlio>';
	$sendXMLIOChg($commandstr);
};

//send a command to the server, and setup callback to process the response (used for lists)
var $sendXMLIOList = function($commandstr){
	$.ajax({ 
		type: 'POST',
		url:  '/xmlio',
		data: $commandstr,
		success: function($r, $s, $x){
			$processResponse($r, $s, $x); //handle returned data
		},
		async: true,
		timeout: 15000,  //refresh every 15 seconds to (hopefully) avoid device timeout
		dataType: 'xml' //<--dataType
	});
};

//send a command to the server, and ignore the response (used for change commands)
var $sendXMLIOChg = function($commandstr){
	$.ajax({  
		type: 'POST',
		url:  '/xmlio',
		data: $commandstr,
		success: function($r, $s, $x){
			//ignore this response
		},
		async: true,
		timeout: 2000,  //two seconds
		dataType: 'xml' //<--dataType
	});
};

//process the response returned for the "list" command
var $processResponse = function($returnedData, $success, $xhr) {

	$.mobile.showPageLoadingMsg();  //show pageloading message

	$('div.errorMessage').html("");  //clear out the error message onscreen

	var $xml = $($returnedData);  //jQuery-ize returned data for easier access

	$xml.xmlClean();  //remove whitespace 

	$xml.find('item').each( //find and process all "item" entries (list)
			function() {

				//put data from current xml items into $currentItem object for easier reference and template use
				var $currentItem = {};
				for (var $i=0; $i < this.childNodes.length; $i++) {
					if (this.childNodes[$i].nodeName != "#text") { //skip empty elements (whitespace, etc.)
						if (this.childNodes[$i].textContent) {
							$currentItem[this.childNodes[$i].nodeName] = this.childNodes[$i].textContent;
						} else {
							$currentItem[this.childNodes[$i].nodeName] = this.childNodes[$i].text;  //another IE workaround
						}
					}
				}
				if ($currentItem.value == undefined) { //if no "value" included, use the name as the value (simplifies later code)
					$currentItem.value = $currentItem.name;  
				}
				var $type = $currentItem.type;  //shortcut since this is used so many times

				//remove non-monitorable from xml
				if ($type == 'roster' || $type == 'panel') {
					$(this).remove();
				}

				//if value not changed, skip the update 
				var $key = $type + $currentItem.name;
				if ($gValues[$key] != $currentItem.value) { 
					
					$gValues[$key] = $currentItem.value;  //save this value for later comparison
					
					//reapply jqm formatting to previous page, if current page is different
					if ($type != $gPrevType) {
						if ($gPrevType != "") {
							$("#type-" + $gPrevType + " ul.listview").listview("refresh");
						}
						$gPrevType = $type;
					}

					//add nextValue from current value
					if ($currentItem.value) {
						$currentItem.nextValue = $getNextValue($currentItem.type, $currentItem.value); 
						$currentItem.valueText = $getValueText($currentItem.type, $currentItem.value); 
					}
					//include a "safe" version of name for use as ID   TODO: other cleanup needed?
					$currentItem.safeName = $currentItem.name.replace(/:/g, "_").replace(/ /g, "_").replace(/%20/g, "_");

					//if a "page" of this type doesn't exist yet, create it, and add menu buttons to all
					if (!$("#type-" + $type).length) {
						//add the new page, following the settings page  TODO: support specific page templates
						var $templateID = $getTemplate('Page', $type);
						$("#settings").after($($templateID).tmpl({type: $type}));
						//add the menu item _inside_ footer on each page
						$("#footer").append("<a data-role='button' href='#type-" + $type + "' data-theme='b'>" + $type +"</a>");
						//make sure the buttons have correct mobile formatting
						$("#settings #footer").find('[data-role="button"]').not('.ui-btn').buttonMarkup();

						//render the changes to settings page, and then the new page
						$("#settings").page();
						$("#type-" + $type).page();

						//copy footer from settings to all pages
						$("div#footer").html($("div#settings div#footer").html());
					}  //end of adding new page

					//if a list item for this name, for this card, doesn't exist yet, add it or update existing item
					var $index = '#type-' + $type + ' ul.listview li#name-' + $currentItem.safeName;
					var $templateID = $getTemplate('Item', $type);
					if (!$($index).length) {
						$($templateID).tmpl($currentItem).appendTo("#type-" + $type + " ul.listview");
					} else {  //update this list item if already exists 
						$($index).replaceWith($($templateID).tmpl($currentItem));
					} //item found
				} //if value changed
			}  //end of function
	);  //end of each

	//apply mobile formatting to last page
	if ($gPrevType != "") {
		$("#type-" + $gPrevType + " ul.listview").listview("refresh");
	}

	$gPrevType = "";

	//update the string with changes (removed items) and cleanup		
	var $xmlstr = xml2Str($xml[0]);

	//echo value list back to server, which will cause server to monitor for changes
	$sendXMLIOList($xmlstr);

	$.mobile.hidePageLoadingMsg(); //hide the pageloading message

};    	

//handle the toggling of the next value for buttons
var $getNextValue = function($type, $value){
	if ($type == 'memory') {
		return $value + '.';  //default to appending a dot for testing, will add prompt/edit later
	}
	var $nextValue = ($value=='4' ? '2' : '4');
	return $nextValue;
};

//get name for template (must already exist on html page)
var $getTemplate = function( $cat, $type){
    var $tempateID = '';
	//use specific item template if found, generic if not 
	if ($('#' + $type + $cat + 'Template').length) {
		$templateID = '#' + $type + $cat + 'Template'
	} else {
		$templateID = '#gen' + $cat + 'Template';
	}
	return $templateID;
};

//return the description for a value TODO: streamline this
var $getValueText = function($type, $value){
	if ($type == 'turnout') {
		if ($value=='2') {
			return 'Closed';
		} else if ($value=='4') {
			return 'Thrown';
		}
	} else if ($type == 'power' || $type == 'route' || $type == 'sensor') {
		if ($value=='2') {
			return '<img src="/web/inControl/PowerGreen24.png">';
		} else if ($value=='4') {
			return '<img src="/web/inControl/PowerRed24.png">';
		} else {
			return '<img src="/web/inControl/PowerGrey24.png">';
		}
	} else if ($type == 'memory' || ($type == 'metadata')) {
		return $value;
	}
	return '???';
};

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

//workaround for IE (from http://www.webdeveloper.com/forum/showthread.php?t=187378)
function xml2Str(xmlNode) {
	try {  // Gecko-based browsers, Safari, Opera.
		return (new XMLSerializer()).serializeToString(xmlNode);
	}
	catch (e) {
		try {	// Internet Explorer.
			return xmlNode.xml;
		}
		catch (e)	{  //Strange Browser ??
			alert('Xmlserializer not supported');
		}
	}
	return false;
}

//get xml for list elements of selected types
function $getXMLListCommands($includeMonitorables) {
	var $cmdStr = '';
	$.each($('#includes input:checkbox:checked'),function(i,e){
		var $t = $(e).attr("id").substring(8);
		if ($includeMonitorables || ($t != 'roster' && $t != 'panel' ))  {
			// add each checked value to list
			$cmdStr += '<list><type>' + $t  + '</type></list>';  //drop "include-" from string
		}
	});
	return $cmdStr;
}

//get array of checked types for persistent local storage
function $getSettingsArray() {
	var $arrInputs = [];
	$.each($('#includes input:checkbox:checked'), function(i,e){
		var $t = $(e).attr("id").substring(8);
		$arrInputs.push($t); //append to list
	});
	return $arrInputs;
}

//handle ajax errors.  retry list on timeout, show other errors
$(document).ajaxError(function(event,xhr,opt, exception){
	if (xhr.statusText =="timeout") {
//		$logMsg("AJAX timeout, retrying....");
		$sendXMLIOList('<xmlio>' + $getXMLListCommands(true) + '</xmlio>');		
	} else {
		$logMsg("AJAX Error requesting " + opt.url + ", status= " + xhr.status + " " + xhr.statusText+ " exception=" + exception);
	}
});

//output log messages to console log and to screen
function $logMsg(msg) {
	if (window.console && window.console.log) {
		var elapsedTime = new Date().getTime() - $gLastLogMsgTime;
		$('div.errorMessage').html(msg);
		console.log(elapsedTime + "     " + msg);
		$gLastLogMsgTime = new Date().getTime();  //save last time
	}
}

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function() {

	//if trying to load a page with a hash (#), remove it and reload
	//  needed because hash is used by jQueryMobile
	//TODO: improve or remove this as jqm advances
	var $hashLoc = location.href.indexOf("#"); 
	if ( $hashLoc > -1) {
	    location.assign(location.href.substr(0, $hashLoc));
	}
	
	$.mobile.showPageLoadingMsg();  //show pageloading message

	//retrieve checked values from localstorage and set checkboxes to match
	var $savedInputs = ["turnout","panel"];  //default selections
	if (localStorage['savedInputs']) {
		$savedInputs=JSON.parse(localStorage['savedInputs']);
	}
	$.each($savedInputs, function(key, value) {  //check the boxes 
		$('#includes input:checkbox#include-' + value).attr('checked', true); 
	});

	//listen for changes to checkboxes, and refresh page after a change
	$('#includes input:checkbox').change(function() {
		//save current settings
		localStorage['savedInputs']=JSON.stringify($getSettingsArray());
	    location.assign(location.href);  //jump to this page
	});

	$.mobile.hidePageLoadingMsg();  //hide pageloading message

	//setup pages based on selected items
	$sendXMLIOList('<xmlio>' + $getXMLListCommands(true) + '</xmlio>');


});
