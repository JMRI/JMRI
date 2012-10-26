//request data for specified trainId and display it as a manifest
var $getTrainData = function($trainId){
	$.ajax({
		url:  '/json/train/' + $trainId, //request proper url for train data
		success: function($r, $s, $x) {
			$buildManifest($r, $s, $x);  //handle returned data
		},
		dataType: 'json' //<--dataType
	});
};

//process the json data defining a train into an online manifest
//  output some header info, then loop through routelocations.  for each, 
//  output loco pickups, then car actions, then loco setouts
//  all elements have css classes to facilitate later formatting and presentation options
var $buildManifest = function($r, $s, $x){
	var $h = "";
	$train = $r.data;  //everything of interest is in the data element
	$('#trainName').text($train.name+ " " + $train.description);
	$('title').html('Train Manifest - ' + $('#trainName').text() + " - " + $('#railRoad').text());
	$h += "<ul class='manifest'>" + $train.name + " " + $train.description;
	$.each($train.routeLocations, function(i, $rl) {  //output each route location for this train
		$h += "  <li class='location'>" + $rl.location;
		$hl = "";
		$.each($train.engines, function(j, $e) {  //loop thru enginelist to find any engine pickups
			if ($rl.id == $e.locationId) {
				$hl += "  <li class='engine pickup'><span class='action'>Pick up</span> " +
				"<span class='roadNumber'>" + $e.road + " " + $e.number + "</span> " +
				"<span class='model'>" + $e.model + "</span> " +
				"<span class='trackName'>from " + $e.trackName + "</span></li>";
			}
		});
		$.each($train.cars, function(j, $c) {  //loop thru carlist to find work for this location
			if ($rl.id == $c.locationId && $rl.id == $c.destinationId) { //local move
				$hl += "  <li class='car move'><span class='action'>Move</span> "+
				"<span class='roadNumber'>"+$c.road+" "+$c.number+"</span> "+
				"<span class='description'>"+$c.type+" "+$c.length+"' "+$c.color+" "+"</span> "+
				"<span class='load'>"+$c.load+"</span> "+
				"<span class='trackName'>from "+$c.trackName + " to " + $c.destinationTrackName + "</span></li>";
			} else if ($rl.id == $c.locationId) { //pickup
				$hl += "  <li class='car pickup'><span class='action'>Pick up</span> "+
				"<span class='roadNumber'>"+$c.road+" "+$c.number+"</span> "+
				"<span class='description'>"+$c.type+" "+$c.length+"' "+$c.color+" "+"</span> "+
				"<span class='load'>"+$c.load+"</span> "+
				"<span class='trackName'>from "+$c.trackName+ "</span></li>";
			} else if ($rl.id == $c.destinationId) { //setout
				$hl += "  <li class='car setout'><span class='action'>Set out</span> "+
				"<span class='roadNumber'>"+$c.road+" "+$c.number+"</span> "+
				"<span class='description'>"+$c.type+" "+$c.length+"' "+$c.color+" "+"</span> "+
				"<span class='load'>"+$c.load+"</span> "+
				"<span class='trackName'>to "+$c.destinationTrackName+ "</span></li>";
			}
		});
		$.each($train.engines, function(j, $e) {  //loop thru enginelist to find any engine setouts
			if ($rl.id == $e.destinationId) {
				$hl += "  <li class='engine setout'><span class='action'>Set out</span> " +
				"<span class='roadNumber'>" + $e.road + " " + $e.number + "</span> " +
				"<span class='model'>" + $e.model + "</span> " +
				"<span class='trackName'>to " + $e.trackName + "</span></li>";
			}
		});
		if ($hl !== "") { //only output the ul if cars were listed
			$h += "    <ul class='cars'>" + $hl + "</ul>";
		}
		$h += "  </li>";
	});

	$h += "</ul>";
	$('div#displayArea').html($h); //put output on page

	//add simple collapsible for locations, 
	//   copied from https://codeblitz.wordpress.com/2009/04/15/jquery-animated-collapsible-list/
	$(function(){
		$('li')
			.css('pointer','default')
			.css('list-style-image','none');
		$('li:has(ul)')
			.click(function(event){
				if (this == event.target) {
					$(this).css('list-style-image',
						(!$(this).children().is(':hidden')) ? 'url(/web/images/plusbox.gif)' : 'url(/web/images/minusbox.gif)');
					$(this).children().toggle('slow');
				}
				return false;
			})
			.css({cursor:'pointer', 'list-style-image':'url(/web/images/plusbox.gif)'})
			.children().hide();
		$('li:not(:has(ul))').css({cursor:'default', 'list-style-image':'none'});
	});

};

//get railroad name as defined to jmri web server (not the operations name)
var $getRailRoad = function(e){
	$.ajax({
		url:  '/json/railroad', //request proper url for railroad name
		success: function($r, $s, $x){
            railroad = $r.data.name;  //name is returned in data object
            e.html(railroad);
		},
		dataType: 'json' //<--dataType
	});
};

//parse the page's input parameter and return value for name passed in
function getParameterByName(name) {
	var match = RegExp('[?&]' + name + '=([^&]*)')
	.exec(window.location.search);
	return match && match[1];
};


//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function() {
	$getRailRoad($('#railRoad'));

	//if trainid not passed in, redirect to train list
	var $trainId = getParameterByName('trainid');
	if ($trainId == undefined) {
		window.location.href = '/web/operationsTrains.html';
	} else {
		$getTrainData($trainId);
	}
});
