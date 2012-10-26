//request and show a list of available trains from JMRI server
var $showTrainList = function(){
	$.ajax({
		url:  '/json/trains', //request proper url from train list
		success: function($r, $s, $x){
			var $h = "";
			if ($r.length > 0) {
				$h += "No trains defined in JMRI Operations.";
			} else {
				$h += "<table><tr><th>Time</th><th>Name</th><th>Description</th><th>Route</th>" + 
				         "<th>Departs</th><th>Terminates</th><th>Current</th><th>Status</th><th>Conductor</th><th>Manifest</th><th>Id</th></tr>";
				$.each($r.list, function(i, item) {  //loop through the returned list of trains
					$train = item.data;  //everything of interest is in the data element
					$h += "<tr>";
					$h += "<td>" + $train.departureTime + "</td>";
					$h += "<td>" + $train.name + " (" + $train.leadEngine + ")</td>";
					$h += "<td>" + $train.description + "</td>";
					$h += "<td>" + $train.route + "</td>";
					$h += "<td>" + $train.trainDepartsName + "</td>";
					$h += "<td>" + $train.trainTerminatesName + "</td>";
					$h += "<td>" + $train.currentLocationName + "</td>";
					$h += "<td>" + $train.status + "</td>";
//					$h += "<td>" + $train.comment + "</td>";
					$h += "<td><a href='/web/operationsConductor.html?trainid=" + $train.id + "'>Conductor</td>";
					$h += "<td><a href='/web/operationsManifest.html?trainid=" + $train.id + "'>Manifest</td>";
					$h += "<td><a href='/json/train/" + $train.id + "'>" + $train.id + "</td>";
					$h += "</tr>";
				});
				$h += "</table>";
			}
			$('div#displayArea').html($h); //put output on page
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

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function() {
	$getRailRoad($('#railRoad'));
	$showTrainList();
	setTimeout("$('title').html('Trains - ' + $('#railRoad').text())", 1000);
});
