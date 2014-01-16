//oper 
//  add optional parm "showall=yes" to show all trains, regardless of status
var $showAll = false;  
//request and show a list of available trains from JMRI server
var $showTrainList = function(){
	$.ajax({
		url:  '/json/trains', //request proper url for train list
		success: function($r, $s, $x){
			var $h = "";
			var $count = 0;
			if ($r.length < 0) {
				$h += "No trains defined in JMRI Operations.";
			} else {
				$h += "<table><tr><th>Manifest</th><th>Name</th><th>Description</th><th>Lead Engine</th>" 
				         + "<th>Departs</th><th class='hideable'>Time</th><th>Status</th>"
				         + "<th class='hideable'>Current</th><th class='hideable'>Terminates</th>"
				         + "<th class='hideable'>Route</th><th class='hideable'>Conductor</th><th class='hideable'>Id</th>"
				         + "</tr>";
				$.each($r, function(i, item) {  //loop through the returned list of trains
					$train = item.data;  //everything of interest is in the data element
					if($showAll || $train.cars.length){  //show table of all trains, or just built ones
						$h += "<tr>";
						$h += "<td><a href='/web/operationsManifest.html?trainid=" + $train.id + "'>Manifest</td>";
						$h += "<td>" + $train.name + "</td>";
						$h += "<td>" + $train.description + "</td>";
						$h += "<td>" + ($train.leadEngine ? $train.leadEngine : "&nbsp;") + "</td>";
						$h += "<td>" + $train.trainDepartsName + "</td>";
						$h += "<td class='hideable'>" + $train.departureTime + "</td>";
						$h += "<td>" + $train.status + "</td>";
						$h += "<td class='hideable'>" + $train.location + "</td>";
						$h += "<td class='hideable'>" + $train.trainTerminatesName + "</td>";
						$h += "<td class='hideable'>" + $train.route + "</td>";
						//TODO: use Conductor web app instead of frame when it is written
						$h += "<td class='hideable'><a href='/frame/Train%20Conductor%20(" + $train.name + ").html''>Conductor</td>";
						$h += "<td class='hideable'><a href='/json/train/" + $train.id + "'>" + $train.id + "</td>";
						$h += "</tr>";
						$count++;
					};
				});
				$h += "</table>";
			}
			$('div#displayArea').html($h).hide().show(); //put output on page (hide+show needed on Android to force redraw)
		},
		error: function($r, $s, $x){
		    $err = JSON && JSON.parse($r.responseText) || $.parseJSON($r.responseText);  //extract JMRI error message from responseText
			$('div#displayArea').html("ERROR retrieving train list: " + $err.data.message).hide().show(); //put output on page (hide+show needed on Android to force redraw)
		},
		dataType: 'json' //<--dataType
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
	var $sa = getParameterByName('showall');
	$showAll = (($sa == undefined || $sa == "no") ? false : true); //show all trains, or just built ones
	$showTrainList();
	setTimeout("$('title').html('Trains - ' + $('#railRoad').text())", 1000);
});
