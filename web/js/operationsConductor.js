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
	$('#displayArea').html("----- not yet implemented -----<br />");
	setTimeout("$('title').html('Train Conductor - ' + $('#railRoad').text())", 1000);
});
