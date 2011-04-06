//TODO: send periodic request for refresh, to verify server connection
//TODO: fix route setting (here and in DefaultXmlIOServer.java) and put routes back in here 

var $globalXhr; //global variable to allow closing earlier connections  TODO:use array to support limited number of open requests

//send request for immediate change, plus request for lists  TODO: allow user to turn off some of the lists
var $sendChange = function($type, $name, $nextValue){
	var $commandstr = '<xmlio><item><type>' + $type + '</type><name>' + $name + '</name><set>' + $nextValue + 
	  '</set></item><list><type>power</type></list><list><type>turnout</type></list></xmlio>';
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
	$xmlstr = (new XMLSerializer()).serializeToString($returnedData);

	$xml = $($returnedData);  //jQuery-ize returned data for easier access
	$xml.xmlClean();  //remove whitespace 

	$xml.find('item').each( //find and process all "item" entries (list)
			function() {
				//remove whitespace items
				$(this).find('#text').remove();
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
				//clean up the name by getting rid of colons TODO: other cleanup needed?
				$currentItem.name = $currentItem.name.replace(/:/g, "_");

				//remove non-monitorable from xml
				if ($type == 'roster' || $type == 'panel') {
					$(this).remove();
				}

				//if a "page" of this type doesn't exist yet, create it, and add menu buttons to all
				if (!$("div#type-" + $type).length) {
					//add the new page, following the settings page  TODO: support specific page templates
					$("div#settings").after($('#genPageTemplate').tmpl({type: $type}));
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

				//use specific item template if found, generic if not  TODO: put this in a function
				if ($('#' + $type + 'ItemTemplate').length) {
					$templateID = '#' + $type + 'ItemTemplate'
				} else {
					$templateID = '#genItemTemplate';
				}

				//if a list item for this name, for this card, doesn't exist yet, add it or update existing item
				$index = 'div#type-' + $type + ' ul.listview li#name-' + $currentItem.name;
				if (!$($index).length) {
					$($templateID).tmpl($currentItem).appendTo("div#type-" + $type + " ul.listview");

				} else {  //update this list item if already exists 
					$($index).replaceWith($($templateID).tmpl($currentItem));
				}
				//apply mobile formatting to changed items
				$("div#type-" + $type + " ul.listview").listview("refresh");
			}
	);

		//echo last command received back to server, (after removing unmonitorable items) which will cause server to monitor for changes
		$xmlstr = (new XMLSerializer()).serializeToString($xml[0]);
		$sendXMLIO($xmlstr);
};    	

//handle the toggling of the next value for buttons
var $getNextValue = function($type, $value){
	var $nextValue = ($value=='4' ? '2' : '4');
	return $nextValue;
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
			return 'On';
		} else if ($value=='4') {
			return 'Off';
		}
	} else if ($type == 'route') {
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


//javascript processing starts here (main)
$(document).ready(function() {

	//ask for all list items
	var $getAllLists = '<xmlio><list><type>power</type></list><list><type>turnout</type></list><list><type>roster</type></list><list><type>panel</type></list></xmlio>';
	$sendXMLIO($getAllLists);

});
