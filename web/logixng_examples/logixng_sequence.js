var throttleSpeed = 0;			// The speed of the train
var throttleForward = true;		// The direction of the train
var locoPos = 100;			// The position of the loco
// var locoPos = 700;			// The position of the loco
// var locoPos = 830;			// The position of the loco
// var locoPos = 982;			// The position of the loco
// var locoPos = 765;			// The position of the loco
// var locoPos = 707;				// The position of the loco
var selectDivergedTrack = false;	// Should the train go to crane track instead of harbour track?
var turnoutThrown = true;		// Is the turnout thrown?
var diveringTrackAngle = 0;		// The angle of crane track?
var turnoutPos = 0;				// Where does the diverging track starts?
var carPos = 0;					// Position of the car
var carIsFilled = false;		// Is the car loaded with coal?

var craneX = 737;				// Crane X position
var craneY = 540;				// Crane Y position
var craneMinAngle = -160;		// Minimum angle of the crane
var craneMaxAngle = 90;			// Maximum angle of the crane
var craneAngle = craneMinAngle;	// Current angle of crane
// var craneAngle = 59;	// Current angle of crane
var commandedCraneAngle = -1;	// Commanded angle of the crane
//var craneUpDown = -1;			// Current crane arm up/down (up = 100, down = 0)
var craneUpDown = 100;			// Current crane arm up/down (up = 100, down = 0)
var commandedCraneUpDown = 0;	// Commanded crane arm up/down (up = 100, down = 0)
var craneBucketOpenClosed = -1;	// Current crane bucket open/closed (open = 0, closed = 100)
var commandedCraneBucketOpenClosed = 0;	// Commanded crane arm up/down (open = 0, closed = 100)
var craneBucketFilled = false;	// Is the crane bucket filled?

var sensor1_active = false;
var sensor1_Pos = 30;
var sensor2_active = false;
var sensor2_Pos = 760;
var sensor3_active = false;
var sensor3_Pos = 914;

var locoLength = 450 * 0.3;		// 0.3 is the scale factor
var carLength = 437 * 0.3;		// 0.3 is the scale factor



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
			jmri.getSensor("IS_7_1");	// Sensor at turnout
			jmri.getSensor("IS_7_2");	// Sensor at ship
			jmri.getSensor("IS_7_3");	// Sensor at coal yard

//			console.log("Throttle data: ");
//			console.log("MyLoco throttle");
			throttle = {"name": "MyLoco", "address": 21};
			result = jmri.getThrottle(throttle);
		},


		memory: function(name, value, data) {
			if (name == "IM_7_1") rotateCrane(value);
			if (name == "IM_7_3") liftLowerCrane(value);
			if (name == "IM_7_5") openCloseCraneBucket(value);
		},


		turnout: function(name, value, data) {
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
//				console.log("Speed: ", data.speed);
				if (data.speed >= 0) throttleSpeed = data.speed;
				else throttleSpeed = 0;
			}
			if (typeof data.forward !== 'undefined') {
//				console.log("Forward: ", data.forward);
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
//			console.log("Power: "+power);
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



function checkSensors()
{
	var sensorTriggered = false;
	sensorTriggered = (((locoPos-locoLength/2) <= sensor1_Pos) && ((locoPos+locoLength/2) >= sensor1_Pos));
	sensorTriggered |= (((carPos-carLength/2) <= sensor1_Pos) && ((carPos+carLength/2) >= sensor1_Pos));

	if (sensorTriggered) {
		// Sensor 1 triggered
		if (true ||    !sensor1_active) {
			sensor1_active = true;
			jmri.setSensor("IS_7_1", 2);
		}
	} else {
		// Sensor 1 not triggered
		if (true ||    sensor1_active) {
			sensor1_active = false;
			jmri.setSensor("IS_7_1", 4);
		}
	}


	sensorTriggered = false;
	if (!selectDivergedTrack) {
		sensorTriggered = (((locoPos-locoLength/2) <= sensor2_Pos) && ((locoPos+locoLength/2) >= sensor2_Pos));
		sensorTriggered |= (((carPos-carLength/2) <= sensor2_Pos) && ((carPos+carLength/2) >= sensor2_Pos));
	}
	if (sensorTriggered) {
		// Sensor 2 triggered
		if (true ||    !sensor2_active) {
			sensor2_active = true;
			jmri.setSensor("IS_7_2", 2);
		}
	} else {
		// Sensor 2 not triggered
		if (true ||    sensor2_active) {
			sensor2_active = false;
			jmri.setSensor("IS_7_2", 4);
		}
	}


	sensorTriggered = false;
	if (selectDivergedTrack) {
		sensorTriggered = (((locoPos-locoLength/2) <= sensor3_Pos) && ((locoPos+locoLength/2) >= sensor3_Pos));
		sensorTriggered |= (((carPos-carLength/2) <= sensor3_Pos) && ((carPos+carLength/2) >= sensor3_Pos));
	}
	if (sensorTriggered) {
		// Sensor 3 triggered
		if (true ||    !sensor3_active) {
			sensor3_active = true;
			jmri.setSensor("IS_7_3", 2);
		}
	} else {
		// Sensor 3 not triggered
		if (true ||    sensor3_active) {
			sensor3_active = false;
			jmri.setSensor("IS_7_3", 4);
		}
	}
}





window.setInterval(runTrain, 50);

function runTrain()
{
	jmri.setSensor("IS_7_2", "ACTIVE");


	if (throttleSpeed != 0)
	{
		if (carPos < turnoutPos) selectDivergedTrack = turnoutThrown;

		var speed = throttleSpeed;
		if (throttleForward) speed = -speed;

		locoPos += speed*2;
		carPos = locoPos + 150;

		var loco = document.getElementById('LocoHandle');
		moveLocoOrCar(loco, locoPos);

		var car = document.getElementById('CarHandle');
		moveLocoOrCar(car, carPos);
	} else {
		if ((carPos >= 591) && (carPos <= 858) && !selectDivergedTrack) {
			// Unload car
			carIsFilled = false;
			var carLoad = document.getElementById('CarLoad');
			carLoad.setAttribute("visibility", "hidden");
		}
	}

	// Check the sensors
	checkSensors();

	// Check the crane
	checkCrane();
}




function rotateCrane(value) {
	commandedCraneAngle = (craneMaxAngle - craneMinAngle) * value / 100 + craneMinAngle;
	if (commandedCraneAngle < craneMinAngle) commandedCraneAngle = craneMinAngle;
	if (commandedCraneAngle > craneMaxAngle) commandedCraneAngle = craneMaxAngle;
}


function liftLowerCrane(value) {
	commandedCraneUpDown = value;
	if (commandedCraneUpDown < 0) commandedCraneUpDown = 0;
	if (commandedCraneUpDown > 100) commandedCraneUpDown = 100;
}


function openCloseCraneBucket(value) {
	commandedCraneBucketOpenClosed = value;
	if (commandedCraneBucketOpenClosed < 2) commandedCraneBucketOpenClosed = 2;
	if (commandedCraneBucketOpenClosed > 100) commandedCraneBucketOpenClosed = 100;
}


function checkLoadingOfCar()
{
	// If here, the crane bucket is filled, but the bucket is opened to drop its coal

	// Check if crane bucket is close to the track
	if ((craneAngle < 0) || (craneAngle > 60)) return;

	// Calculate where the crane bucket are relative to the track
	// 857 = position of the car when the car is below the crane bucket and the crane arm is
	// perpendicular to the track.
	var cranePosRelativeToTrack = Math.sin((craneAngle-30) * 2 * Math.PI / 360) * 2 * 57 + 857;

	if (selectDivergedTrack && (Math.abs(carPos - cranePosRelativeToTrack) < carLength/3)) {
		carIsFilled = true;
		var carLoad = document.getElementById('CarLoad');
		carLoad.setAttribute("visibility", "visible");
	}
}


function checkCrane()
{
	var lastAngle = craneAngle;

	if (craneUpDown >= 80) {
		if (commandedCraneAngle < craneAngle) {
			craneAngle -= 1;
			if (craneAngle < commandedCraneAngle) craneAngle = commandedCraneAngle;
		}
		if (commandedCraneAngle > craneAngle) {
			craneAngle += 1;
			if (craneAngle > commandedCraneAngle) craneAngle = commandedCraneAngle;
		}
	}

	if (craneAngle != lastAngle) {
		var item = document.getElementById('CraneHandle');
		var data = "translate("+craneX+","+(craneY)+") scale(0.3) rotate("+craneAngle+")";
		item.setAttribute("transform", data);

		anglePercent = (craneAngle - craneMinAngle) / (craneMaxAngle - craneMinAngle) * 100;
		jmri.setMemory("IM_7_2", anglePercent);
	}



	var lastCraneUpDown = craneUpDown;
	if (commandedCraneUpDown < craneUpDown) {
		craneUpDown -= 1;
		if (craneUpDown < commandedCraneUpDown) craneUpDown = commandedCraneUpDown;
	}
	if (commandedCraneUpDown > craneUpDown) {
		craneUpDown += 1;
		if (craneUpDown > commandedCraneUpDown) craneUpDown = commandedCraneUpDown;
	}

	if (craneUpDown != lastCraneUpDown) {
		var item = document.getElementById('CraneUpDown');
		var data = "translate(0,0) rotate("+((100-craneUpDown)*1.80)+")";
		item.setAttribute("transform", data);

		jmri.setMemory("IM_7_4", craneUpDown);
	}



	var lastCraneBucketOpenClosed = craneBucketOpenClosed;
	if (commandedCraneBucketOpenClosed < craneBucketOpenClosed) {
		craneBucketOpenClosed -= 1;
		if (craneBucketOpenClosed < commandedCraneBucketOpenClosed) craneBucketOpenClosed = commandedCraneBucketOpenClosed;
		if (craneBucketFilled && (craneBucketOpenClosed < 50)) {
			checkLoadingOfCar();
			craneBucketFilled = false;
		}
	}
	if (commandedCraneBucketOpenClosed > craneBucketOpenClosed) {
		craneBucketOpenClosed += 1;
		if (craneBucketOpenClosed > commandedCraneBucketOpenClosed) craneBucketOpenClosed = commandedCraneBucketOpenClosed;
		if (!craneBucketFilled && (craneBucketOpenClosed > 50) && (craneUpDown < 20) && (anglePercent >= 22) && (anglePercent <= 48)) {
			craneBucketFilled = true;
		}
	}

	if (craneBucketOpenClosed != lastCraneBucketOpenClosed) {
		var item = document.getElementById('CraneBucket');
		var data = "translate(0,0) rotate("+((100-craneBucketOpenClosed)*1.80)+")";
		item.setAttribute("height", craneBucketOpenClosed);

		if (!craneBucketFilled) item.setAttribute("fill", "#4F81BD");
		else item.setAttribute("fill", "#948A54");

		jmri.setMemory("IM_7_6", craneBucketOpenClosed);
	}

}
