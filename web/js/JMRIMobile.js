//TODO: send periodic request for refresh, to verify server connection (maybe do on server side as well?)
//TODO: handle ajax errors
//TODO: preserve filter on update

var $globalXhr; //global variable to allow closing earlier connections  TODO:use array to support limited number of open requests

//handle button press, send request for immediate change, plus request for lists  TODO: allow user to turn off some of the lists
var $sendChange = function($type, $name, $nextValue){
	$.mobile.pageLoading();  //show pageloading message
	var $commandstr = '<xmlio><item><type>' + $type + '</type><name>' + $name + '</name><set>' + $nextValue +'</set></item>' +
	'<list><type>turnout</type></list><list><type>route</type></list><list><type>power</type></list><list><type>sensor</type></list></xmlio>';
//	'<list><type>power</type></list></xmlio>';
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

	$xmlstr = xml2Str($returnedData);

	$xml = $($returnedData);  //jQuery-ize returned data for easier access
	$xml.xmlClean();  //remove whitespace 

	$xml.find('item').each( //find and process all "item" entries (list)
			function() {
				//put data from current xml items into $currentItem object
				var $currentItem = {};
				for (var $i=0; $i < $(this)[0].childNodes.length; $i++) {
					if ($(this)[0].childNodes[$i].nodeName != "#text") { //skip empty elements (whitespace, etc.)
						$currentItem[$(this)[0].childNodes[$i].nodeName] = $(this)[0].childNodes[$i].textContent;
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
					$templateID = $getTemplate('Page', $type);
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
				$index = 'div#type-' + $type + ' ul.listview li#name-' + $currentItem.safeName;
				$templateID = $getTemplate('Item', $type);
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
	$xmlstr = xml2Str($xml[0]);

	//echo last command received back to server, which will cause server to monitor for changes
	$sendXMLIO($xmlstr);

	$.mobile.pageLoading(true); //hide the pageloading message
};    	

//handle the toggling of the next value for buttons
var $getNextValue = function($type, $value){
	var $nextValue = ($value=='4' ? '2' : '4');
	return $nextValue;
};

//get name for template (which exists on html page)
var $getTemplate = function( $cat, $type){
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
	} else if ($type == 'power') {
		if ($value=='2') {
			return '<img src="/web/InControl/PowerGreen24.png">';
		} else if ($value=='4') {
			return '<img src="/web/InControl/PowerRed24.png">';
		} else {
			return '<img src="/web/InControl/PowerGrey24.png">';
		}
	} else if ($type == 'route' || $type == 'sensor') {
		if ($value=='2') {
			return 'Active';
		} else if ($value=='4') {
			return 'Inactive';
		}
	}
	return 'unknown';
};

//clear out whitespace from xml, function adapted from 
// http://stackoverflow.com/questions/1539367/remove-whitespace-and-line-breaks-between-html-elements-using-jquery/3103269#3103269
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
function xml2Str(xmlNode)
{
	try {
		// Gecko-based browsers, Safari, Opera.
		return (new XMLSerializer()).serializeToString(xmlNode);
	}
	catch (e) {
		try {
			// Internet Explorer.
			return xmlNode.xml;
		}
		catch (e)
		{//Strange Browser ??
			alert('Xmlserializer not supported');
		}
	}
	return false;
}

//javascript processing starts here (main)
$(document).ready(function() {

	//ask for all list items
	var $getAllLists = '<xmlio><list><type>turnout</type></list><list><type>route</type></list><list><type>roster</type></list><list><type>panel</type></list><list><type>power</type></list><list><type>sensor</type></list></xmlio>';
//	var $getAllLists = '<xmlio><list><type>power</type></list></xmlio>';
	$sendXMLIO($getAllLists);

});
