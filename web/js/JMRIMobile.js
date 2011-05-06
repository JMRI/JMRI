//TODO: send periodic request for refresh, to verify server connection (maybe do on server side as well?)
//TODO: handle ajax errors
//TODO: preserve filter on update
//TODO: use array for globalXhr to support limited number of open requests
//TODO: (long-term) read panel xml and "draw" panels on page
//TODO: "wide-screen" version that shows multiple "pages" at once, for use on wider browsers
//TODO: add edit of memory variable values
//TODO: support addition of memory variables, maybe turnouts?
//TODO: remove "page" and button on uncheck (maybe just force refresh?)

var $globalXhr; //global variable to allow closing earlier connections 

//handle button press, send request for immediate change, plus request for lists  TODO: allow user to turn off some of the lists
var $sendChange = function($type, $name, $nextValue){
	$.mobile.pageLoading();  //show pageloading message
	var $commandstr = '<xmlio><item><type>' + $type + '</type><name>' + $name + '</name><set>' + $nextValue +'</set></item>' 
	+  $getXMLListCommands(false) + '</xmlio>';
	$sendXMLIO($commandstr);
};


var $sendXMLIO = function($commandstr){

	//kill the pending requests  TODO: find way to reuse connections instead of killing them 
	if ($globalXhr) {
		$globalXhr.abort();
	}
	var $success = "";
	$globalXhr = $.ajax({  //remember the request, to kill it later if needed
		type: 'POST',
		url:  '/xmlio',
		data: $commandstr,
		success: function($r, $s, $x){
			$processResponse($r, $s, $x); //set up callback
		},
		async: true,
		dataType: 'xml' //<--dataType
	});
};

var $processResponse = function($returnedData, $success, $xhr) {

	$.mobile.pageLoading();  //show pageloading message

	var $xml = $($returnedData);  //jQuery-ize returned data for easier access
	$xml.xmlClean();  //remove whitespace 

	$xml.find('item').each( //find and process all "item" entries (list)
			function() {
				//put data from current xml items into $currentItem object
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
				var $type = $currentItem.type;  //shortcut since this is used so many times
				//add nextValue from current value
				if ($currentItem.value) {
					$currentItem.nextValue = $getNextValue($currentItem.type, $currentItem.value); 
					$currentItem.valueText = $getValueText($currentItem.type, $currentItem.value); 
				}
				//include a "safe" version of name for use as ID   TODO: other cleanup needed?
				$currentItem.safeName = $currentItem.name.replace(/:/g, "_").replace(/ /g, "_");

				//remove non-monitorable from xml
				if ($type == 'roster' || $type == 'panel') {
					$(this).remove();
				}

				//if a "page" of this type doesn't exist yet, create it, and add menu buttons to all
				if (!$("div#type-" + $type).length) {
					//add the new page, following the settings page  TODO: support specific page templates
					var $templateID = $getTemplate('Page', $type);
					$("div#settings").after($($templateID).tmpl({type: $type}));
					//add the menu item _inside_ footer on each page
					$("div# div#footer").append("<a data-role='button' href='#type-" + $type + "' data-theme='b'>" + $type +"</a>");
					//make sure the buttons have correct mobile formatting
					$("div#settings div#footer").find('[data-role="button"]').not('.ui-btn').buttonMarkup();

					//render the changes to settings page, and then the new page
					$("div#settings").page();
					$("div#type-" + $type).page();

					//copy footer from settings to all pages
					$("div#footer").html($("div#settings div#footer").html());
				}

				//if a list item for this name, for this card, doesn't exist yet, add it or update existing item
				var $index = 'div#type-' + $type + ' ul.listview li#name-' + $currentItem.safeName;
				var $templateID = $getTemplate('Item', $type);
				if (!$($index).length) {
					$($templateID).tmpl($currentItem).appendTo("div#type-" + $type + " ul.listview");

				} else {  //update this list item if already exists 
					$($index).replaceWith($($templateID).tmpl($currentItem));
				}
				//apply mobile formatting to changed items
				$("div#type-" + $type + " ul.listview").listview("refresh");
			}
	);

	//update the string with any changes and cleanup		
	var $xmlstr = xml2Str($xml[0]);

	//echo last command received back to server, which will cause server to monitor for changes
	$sendXMLIO($xmlstr);

	$.mobile.pageLoading(true); //hide the pageloading message
};    	

//handle the toggling of the next value for buttons
var $getNextValue = function($type, $value){
	if ($type == 'memory') {
		return $value + '.';  //default to appending a dot for testing, will add prompt/edit later
	}
	var $nextValue = ($value=='4' ? '2' : '4');
	return $nextValue;
};

//get name for template (which exists on html page)
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
	} else if ($type == 'memory') {
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
	$.each($('div#includes input:checkbox:checked'),function(i,e){
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
	$.each($('div#includes input:checkbox:checked'), function(i,e){
		var $t = $(e).attr("id").substring(8);
		$arrInputs.push($t); //append to list
	});
	return $arrInputs;
}

$(document).ajaxError(function(e,xhr,opt){
    if (window.console && window.console.log) {
        window.console.log("AJAX Error requesting " + opt.url + ", status= " + xhr.status + " " + xhr.statusText);
    }
});

//javascript processing starts here (main)
$(document).ready(function() {

	//if trying to load a page with a hash (#), remove it and reload
	//  needed because hash is used by jQueryMobile
	var $hashLoc = location.href.indexOf("#"); 
	if ( $hashLoc > -1) {
	    location.assign(location.href.substr(0, $hashLoc));
	}
	
	$.mobile.pageLoading();  //show pageloading message

	//retrieve checked values from localstorage and set checkboxes to match
	var $savedInputs = ["turnout","panel"];  //default selections
	if (localStorage['savedInputs']) {
		$savedInputs=JSON.parse(localStorage['savedInputs']);
	}
	$.each($savedInputs, function(key, value) {  //check the boxes 
		$('div#includes input:checkbox#include-' + value).attr('checked', true).checkboxradio("refresh"); 
	});

	//listen for changes to checkboxes, and resend updated list command on each
	$('div#includes input:checkbox').change(function() {
		$sendXMLIO('<xmlio>' + $getXMLListCommands(true) + '</xmlio>');
		//save current settings
		localStorage['savedInputs']=JSON.stringify($getSettingsArray());
	});

	//setup pages based on selected items
	$sendXMLIO('<xmlio>' + $getXMLListCommands(true) + '</xmlio>');

	$.mobile.pageLoading(true);  //hide pageloading message

});
