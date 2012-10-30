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
	if ($train.comment !== "") {
		$h += "<span class='comment'> - " + $train.comment+"</span>";
	}
	$.each($train.routeLocations, function(i, $rl) {  //output each route location for this train
		$pickups = 0;
		$setouts = 0;
		$moves = 0;
		$hl = "";  //use for car list, so parent can indicate count
		$.each($train.engines, function(j, $e) {  //loop thru enginelist to find any engine pickups
			if ($rl.id == $e.locationId) {
				$hl += "  <li class='car engine pickup'><span class='action'>Pick up</span> " +
				"<span class='roadNumber'>" + $e.road + " " + $e.number + "</span> " +
				"<span class='model hideable'>" + $e.model + "</span> from " +
				"<span class='trackName'>" + $e.trackName + "</span></li>";
				$pickups++;
			}
		});
		$.each($train.cars, function(j, $c) {  //loop thru carlist to find work for this location
			if ($rl.id == $c.locationId && $rl.id == $c.destinationId) { //local move
				$hl += "  <li class='car move'><span class='action'>Move</span> "+
				"<span class='roadNumber'>"+$c.road+" "+$c.number+"</span> "+
				"<span class='description hideable'>"+$c.type+" "+$c.length+"' "+$c.color+" "+"</span> "+
				"<span class='load hideable'>"+escapeHtml($c.load)+"</span> from "+
				"<span class='trackName'>"+$c.trackName + "</span> to <span class='trackName'>" + $c.destinationTrackName + "</span></li>";
				$moves++;
			} else if ($rl.id == $c.locationId) { //pickup
				$hl += "  <li class='car pickup'><span class='action'>Pick up</span> "+
				"<span class='roadNumber'>"+$c.road+" "+$c.number+"</span> "+
				"<span class='description hideable'>"+$c.type+" "+$c.length+"' "+$c.color+" "+"</span> "+
				"<span class='load hideable'>"+escapeHtml($c.load)+"</span> from "+
				"<span class='trackName'>"+$c.trackName+ "</span></li>";
				$pickups++;
			} else if ($rl.id == $c.destinationId) { //setout
				$hl += "  <li class='car setout'><span class='action'>Set out</span> "+
				"<span class='roadNumber'>"+$c.road+" "+$c.number+"</span> "+
				"<span class='description hideable'>"+$c.type+" "+$c.length+"' "+$c.color+" "+"</span> "+
				"<span class='load hideable'>"+escapeHtml($c.load)+"</span> to "+
				"<span class='trackName'>"+$c.destinationTrackName+ "</span></li>";
				$setouts++;
			}
		});
		$.each($train.engines, function(j, $e) {  //loop thru enginelist to find any engine setouts
			if ($rl.id == $e.destinationId) {
				$hl += "  <li class='car engine setout'><span class='action'>Set out</span> " +
				"<span class='roadNumber'>" + $e.road + " " + $e.number + "</span> " +
				"<span class='model hideable'>" + $e.model + "</span> " +
				"<span class='trackName'>to " + $e.trackName + "</span></li>";
				$setouts++;
			}
		});
		$h += "  <li class='location'>" + $rl.location + " (" + $rl.expectedArrivalTime + ") "; 

		if ($pickups==0 && $moves==0 && $setouts==0) {
			$h += "No work ";
		} else {
			if ($pickups>0) $h += $pickups + " pickups ";
			if ($setouts>0)	$h += $setouts + " setouts ";
			if ($moves>0) 	$h += $moves + " local moves ";
			$h += "    <ul class='cars'>" + $hl + "</ul>";
		}
		$h += "  </li>";
	});

	$h += "</ul>";
	$('div#displayArea').html($h); //put output on page

	//insert checkbox image in all li's, hidden until row is clicked
	$('li.car>span.action').before("<img class='actionComplete' src='/images/VerySmallCheck.png'>");
	//set up click handler on car items to toggle actionCompleted class
	$('li.car').click(function () {
		$(this).toggleClass("actionCompleted");
	});
	
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

//replace chars with html-safe versions
var entityMap = {
		"&": "&amp;",
		"<": "&lt;",
		">": "&gt;",
		'"': '&quot;',
		"'": '&#39;',
		"/": '&#x2F;'
};
function escapeHtml(string) {
	return String(string).replace(/[&<>"'\/]/g, function (s) {
		return entityMap[s];
	});
}

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
