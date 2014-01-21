//request data for specified trainId and display it as a manifest
//expects url of form operationsManifest.html?trainid=3
//if no trainid passed in, redirects to operationsTrains.html page to list available trains 
var $getTrainData = function($trainId){
	$.ajax({
		url:  '/json/train/' + $trainId, //request proper url for train data
		success: function($r, $s, $x) {
			$buildManifest($r, $s, $x);  //handle returned data
		},
		error: function($r, $s, $x){
		    $err = JSON && JSON.parse($r.responseText) || $.parseJSON($r.responseText);  //extract JMRI error message from responseText
			$('div#displayArea').html("ERROR retrieving train data: " + $err.data.message).hide().show(); //put output on page (hide+show needed on Android to force redraw)
		},
		dataType: 'json' //<--dataType
	});
};

//process the json data defining a train into an online manifest
//output some header info, then loop through locations.  for each, 
//output loco pickups, then car actions, then loco setouts
//all elements have css classes to facilitate later formatting and presentation options
var $buildManifest = function($r, $s, $x){
	var $h = "";
	$train = $r.data;  //everything of interest is in the data element
	$('#trainName').text($train.name+ " " + $train.description);
	$('title').html('Train Manifest - ' + $('#trainName').text() + " - " + $('#railRoad').text());
	
	//insert a link to the Conductor window "frame"  Note: the Conductor window must already be opened on the server
	$("div#operationsFooter").append(" <a href='/frame/Train%20Conductor%20(" + $train.name + ").html'>[Conductor]</td>");
	$("div#operationsFooter").append(" <a href='#' onclick='location.reload(true);return false;'>[Refresh]</td>");

	$h += "<ul class='manifest'>" + $train.name + " " + $train.description;
	if ($train.comment !== "") {
		$h += "<span class='comment'> - " + $train.comment+"</span>";
	}
	$.each($train.locations, function(i, $rl) {  //output each route location for this train
		$pickups = 0;
		$setouts = 0;
		$moves = 0;
		$aboard = 0;
		$ht = "";  //build list of cars on train, display later
		$hl = "";  //build list of work to do at current location, display later
		$locationClass = "";  //set additional class for location based on location of train
		$.each($train.engines, function(j, $e) {  //loop thru enginelist to find any engine pickups
			if ($rl.id == $e.location  && $e.locationTrack !== "") {
				$hl += "  <li class='car engine pickup'><span class='action'>Pick up</span> " +
				"<span class='roadNumber'>" + $e.road + " " + $e.number + "</span> " +
				"<span class='model hideable'>" + $e.model + "</span> from " +
				"<span class='trackName'>" + $e.locationTrack + "</span></li>";
				$pickups++;
			}
		});
		$.each($train.cars, function(j, $c) {  //loop thru carlist to determine where to show each car (on train, to be picked up, etc.)
			if ($c.locationTrack == "") {   //this car is on the train
				$ht += "  <li class='car aboard'>"+
				"<span class='roadNumber'>"+$c.road+" "+$c.number+"</span> "+
				"<span class='description hideable'>"+$c.type+" "+$c.length+"' "+$c.color+" "+"</span> "+
				"<span class='load hideable'>"+escapeHtml($c.load)+"</span></li>";
				$aboard++;
				if ($rl.id == $c.destination) { //setout
					$hl += "  <li class='car setout'><span class='action'>Set out</span> "+
					"<span class='roadNumber'>"+$c.road+" "+$c.number+"</span> "+
					"<span class='description hideable'>"+$c.type+" "+$c.length+"' "+$c.color+" "+"</span> "+
					"<span class='load hideable'>"+escapeHtml($c.load)+"</span> to "+
					"<span class='trackName'>"+$c.destinationTrack+ "</span></li>";
					$setouts++;
				}
			} else {  //this car is not on the train
				if ($rl.id == $c.location && $rl.id == $c.destination) { //local move
					$hl += "  <li class='car move'><span class='action'>Move</span> "+
					"<span class='roadNumber'>"+$c.road+" "+$c.number+"</span> "+
					"<span class='description hideable'>"+$c.type+" "+$c.length+"' "+$c.color+" "+"</span> "+
					"<span class='load hideable'>"+escapeHtml($c.load)+"</span> from "+
					"<span class='trackName'>"+$c.locationTrack + "</span> to <span class='trackName'>" + $c.destinationTrack + "</span></li>";
					$moves++;
				} else if ($rl.id == $c.location && $c.locationTrack !== "") { //pickup
					$hl += "  <li class='car pickup'><span class='action'>Pick up</span> "+
					"<span class='roadNumber'>"+$c.road+" "+$c.number+"</span> "+
					"<span class='description hideable'>"+$c.type+" "+$c.length+"' "+$c.color+" "+"</span> "+
					"<span class='load hideable'>"+escapeHtml($c.load)+"</span> from "+
					"<span class='trackName'>"+$c.locationTrack+ "</span></li>";
					$pickups++;
				} else if ($rl.id == $c.destination) { //setout
					$hl += "  <li class='car setout'><span class='action'>Set out</span> "+
					"<span class='roadNumber'>"+$c.road+" "+$c.number+"</span> "+
					"<span class='description hideable'>"+$c.type+" "+$c.length+"' "+$c.color+" "+"</span> "+
					"<span class='load hideable'>"+escapeHtml($c.load)+"</span> to "+
					"<span class='trackName'>"+$c.destinationTrack+ "</span></li>";
					$setouts++;
				}
			}
		});
		$.each($train.engines, function(j, $e) {  //loop thru enginelist to find any engine setouts
			if ($rl.id == $e.destination) {
				$hl += "  <li class='car engine setout'><span class='action'>Set out</span> " +
				"<span class='roadNumber'>" + $e.road + " " + $e.number + "</span> " +
				"<span class='model hideable'>" + $e.model + "</span> " +
				"<span class='trackName'>to " + $e.destinationTrack + "</span></li>";
				$setouts++;
			}
		});
		$msg = "";
		if ($rl.expectedArrivalTime == -1 && $train.locationId !== $rl.id) { //location has been passed
			$msg += " - complete ";
			$locationClass = "complete";
		} else  { 
			if ($train.locationId == $rl.id) {
				$msg += " (current) "; //show that train is at this location
				$locationClass = "current";
			} 
			if ($pickups==0 && $moves==0 && $setouts==0) {
				$msg += "No work ";
				$locationClass = "nowork";
			} else {
				if ($pickups>0) $msg += $pickups + " pickups ";
				if ($setouts>0)	$msg += $setouts + " setouts ";
				if ($moves>0) 	$msg += $moves + " local moves ";
				$msg += "    <ul class='cars'>" + $hl + "</ul>";
			}

		}
		$h += "  <li class='location " + $locationClass + " '>" + $rl.name;
		if ($rl.expectedArrivalTime != -1) {
			$h += " (" + $rl.expectedArrivalTime + ") ";
		}
		$h += $msg;
		$h += "  </li>";
	});

	$h += "<li class='aboard'>Train Status: " + $train.status;
	if ($ht !== "") { //add in list of cars if set
		$h += "<ul>" + $ht + "</ul>";
	}
	$h += "</li></ul>";
	$('div#displayArea').html($h); //put output on page

	//insert checkbox image in all actionable li's, hidden until row is clicked
	$('li.car>span.action').before("<img class='actionComplete' src='/images/VerySmallCheck.png'>");
	//set up click handler on car items to toggle actionCompleted class
	$('ul.cars>li.car').click(function () {
		$(this).toggleClass("actionCompleted");
	});

	//add simple collapsible for locations, 
	//   copied from https://codeblitz.wordpress.com/2009/04/15/jquery-animated-collapsible-list/
	$(function(){
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
		$('li:not(:has(ul))').css({'list-style-image':'none'});
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
