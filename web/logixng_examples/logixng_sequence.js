var throttleSpeed = 0;		// The speed of the train
var throtteForward = true;	// The direction of the train
var locoPos = 100;			// The position of the loco
// var locoPos = 700;				// The position of the loco
var selectDivergedTrack = false;	// Should the train go to crane track instead of harbour track?
var turnoutThrown = false;	// Is the turnout thrown?
var diveringTrackAngle = 0;	// The angle of crane track?
var turnoutPos = 0;			// Where does the diverging track starts?
var carPos = 0;				// Position of the car

var craneX = 737;				// Crane X position
var craneY = 540;				// Crane Y position
var craneMinAngle = -160;		// Minimum angle of the crane
var craneMaxAngle = 90;			// Maximum angle of the crane
var craneAngle = craneMinAngle;	// Current angle of crane
var commandedCraneAngle = craneAngle;	// Commanded angle of the crane



// set the jmri global variable to null
var jmri = null;
$(document).ready(function() {

	calculateDivergingTrackAngle();

	jmri = $.JMRI({

		open: function() {
			jmri.getMemory("IM_7_1");	// Crane angle left - right, commanded position
			jmri.getMemory("IM_7_2");	// Crane angle left - right, actual position
			jmri.getMemory("IM_7_3");	// Crane bucket up - down, commanded position
			jmri.getMemory("IM_7_4");	// Crane bucket up - down, actual position
			jmri.getMemory("IM_7_5");	// Crane bucket closed - open, commanded position
			jmri.getMemory("IM_7_6");	// Crane bucket closed - open, actual position
			jmri.getTurnout("IT_7_1");	// Turnout

			console.log("Throttle data: ");
			console.log("MyLoco throttle");
			throttle = {"name": "MyLoco", "address": 21};
			result = jmri.getThrottle(throttle);
		},


		memory: function(name, value, data) {
			if (name == "IM_7_1") rotateCrane(value);

//			console.log("Memory name: "+name);
//			console.log("Memory value: "+value);
//			console.log("Memory data: "+data);
//			jmri.setMemory("IM_92", "Hej");
		},


		turnout: function(name, value, data) {
			console.log("Turnout name: "+name);
			console.log("Turnout value: "+value);
			console.log("Turnout data: "+data);
//			jmri.setMemory("IM_92", "Hej");
			turnoutThrown = (value == 4);

			if (turnoutThrown) {
				turnout_1_1.setAttribute("visibility", "hidden");
				turnout_1_2.setAttribute("visibility", "hidden");
				turnout_2_1.setAttribute("visibility", "visible");
				turnout_2_2.setAttribute("visibility", "visible");
			} else {
				turnout_1_1.setAttribute("visibility", "visible");
				turnout_1_2.setAttribute("visibility", "visible");
				turnout_2_1.setAttribute("visibility", "hidden");
				turnout_2_2.setAttribute("visibility", "hidden");
			}
		},


		throttle: function(throttle, data) {
			if (typeof data.speed !== 'undefined') {
				console.log("Speed: ", data.speed);
				if (data.speed >= 0) throttleSpeed = data.speed;
				else throttleSpeed = 0;
			}
			if (typeof data.forward !== 'undefined') {
				console.log("Forward: ", data.forward);
				throttleForward = data.forward;
			}
//			for (var key in data2) {
//				console.log(key);
//			}
		},


		// when the JMRI object receives a power update, call this
		// function, regardless of source of update
		power: function(state) {
			power = state;
			console.log("Power: "+power);
/*
			switch (power) {
				case jmri.UNKNOWN:
					$('#powerImg').prop('src', "/images/PowerGrey.png");
					$('#powerImg').prop('alt', "Unknown");
					$('#powerImg').prop('title', "Unknown");
					break;
				case jmri.POWER_ON:
					$('#powerImg').prop('src', "/images/PowerGreen.png");
					$('#powerImg').prop('alt', "Powered On");
					$('#powerImg').prop('title', "Powered On");
					break;
				case jmri.POWER_OFF:
					$('#powerImg').prop('src', "/images/PowerRed.png");
					$('#powerImg').prop('alt', "Powered Off");
					$('#powerImg').prop('title', "Powered Off");
					break;
			}
*/
		}
	});
	// trigger the initial connection to the JMRI server; this
	// method call ensures the jmri.open() method is called after
	// a timeout to begin using fall back methods for monitoring
	// items on the JMRI server even if a WebSocket connection
	// cannot be established
	jmri.connect();

	// make it possible to click on the power button to turn track
	// power on or off without using a javascript URI
//	$('#powerImg').click(function(event) {
//		jmri.setPower((power === jmri.POWER_ON) ? jmri.POWER_OFF : jmri.POWER_ON);
//	});
});



function calculateDivergingTrackAngle()
{
	var track = document.getElementById('DivergingCenterRail');
	var x1 = parseFloat(track.getAttributeNS(null, 'x1'));
	var y1 = parseFloat(track.getAttributeNS(null, 'y1'));
	var x2 = parseFloat(track.getAttributeNS(null, 'x2'));
	var y2 = parseFloat(track.getAttributeNS(null, 'y2'));

	turnoutPos = x1;

	var x = x2 - x1;
	var y = y2 - y1;

	diveringTrackAngle = Math.atan(y/x);

	console.log("x1: " + x1);
	console.log("y1: " + y1);
	console.log("x2: " + x2);
	console.log("y2: " + y2);
	console.log("x: " + x);
	console.log("y: " + y);
	console.log("y_div_x: " + (y/x));
	console.log("atan y_div_x: " + Math.atan(y/x));
	console.log("atan y_div_x: " + (Math.atan(y/x)*360/2/Math.PI));
	console.log("diveringTrackAngle: " + (diveringTrackAngle*360/2/Math.PI));
}




function moveLocoOrCar(item, pos)
{
	var x = pos;
	var y = 0;
	var rotate = 0;
	if (selectDivergedTrack && (pos > turnoutPos)) {
		var posAfterTurnout = pos - turnoutPos;
		rotate = diveringTrackAngle * 360 / 2 / Math.PI;
		x = turnoutPos + posAfterTurnout * Math.cos(diveringTrackAngle);
		y = posAfterTurnout * Math.sin(diveringTrackAngle);
	}
	var data = "translate("+x+","+(y+200)+") scale(0.3) rotate("+rotate+")";
	item.setAttribute("transform", data);
}



window.setInterval(runTrain, 50);

function runTrain()
{
	if (throttleSpeed != 0)
	{
		if (carPos < turnoutPos) selectDivergedTrack = turnoutThrown;

		var speed = throttleSpeed;
		if (throttleForward) speed = -speed;

		locoPos += speed*2;
		carPos = locoPos + 150;

		var loco = document.getElementById('LocoHandle');
//		var data = "translate("+(trainPos)+",200) scale(0.3) rotate(0)";
//		loco.setAttribute("transform", data);
		moveLocoOrCar(loco, locoPos);

		var car = document.getElementById('CarHandle');
//		var data = "translate("+(carPos)+",200) scale(0.3) rotate(0)";
//		car.setAttribute("transform", data);
		moveLocoOrCar(car, carPos);

//		loco.setAttribute("transform", "translate("+(trainPos+300)+",200) scale(0.3) rotate(0)");
//		console.log("Loco: "+data);
	}



//	jmri.setMemory("IM_92", "Hej");

//	throttleData = {"speed": Math.random()};
//	jmri.setThrottle("Daniel", throttleData);

//	throttleData = {"speed": Math.random(), "forward": (Math.random() > 0.5)};
//	jmri.setThrottle("MyLoco", throttleData);

	checkCrane();
}




function rotateCrane(value) {
	commandedCraneAngle = (craneMaxAngle - craneMinAngle) * value / 100 + craneMinAngle;
	if (commandedCraneAngle < craneMinAngle) commandedCraneAngle = craneMinAngle;
	if (commandedCraneAngle > craneMaxAngle) commandedCraneAngle = craneMaxAngle;
}



function checkCrane()
{
	var lastAngle = craneAngle;
	if (commandedCraneAngle < craneAngle) {
		craneAngle -= 0.5;
		if (craneAngle < commandedCraneAngle) craneAngle = commandedCraneAngle;
	}
	if (commandedCraneAngle > craneAngle) {
		craneAngle += 0.5;
		if (craneAngle > commandedCraneAngle) craneAngle = commandedCraneAngle;
	}

	if (craneAngle != lastAngle) {
		var item = document.getElementById('CraneHandle');
//		var data = "translate("+x+","+(y+200)+") scale(0.3) rotate("+rotate+")";
		var data = "translate("+craneX+","+(craneY)+") scale(0.3) rotate("+craneAngle+")";
		item.setAttribute("transform", data);

		anglePercent = (craneAngle - craneMinAngle) / (craneMaxAngle - craneMinAngle) * 100;
		jmri.setMemory("IM_7_2", anglePercent);
//		console.log("Set memory: "+anglePercent);
	}
}
