            // set the jmri global variable to null
            var jmri = null;
            $(document).ready(function() {
                // once the document is loaded, assign a $.JMRI object to
                // the jmri variable and overload two functions:
                // open() and power(state)
                jmri = $.JMRI({
                    // call getPower() when the JMRI WebSocket connection opens
                    // getPower() does two things: it requests the current power
                    // status from JMRI and it starts the power status monitor
                    // on the JMRI server.
                    open: function() {
                        jmri.getPower();
						jmri.getMemory("IM_91");



                        console.log("Throttle data: ");
                        console.log("Daniel throttle");
                        throttle = {"name": "Daniel", "address": 21};
                        result = jmri.getThrottle(throttle);
//                        jmri.socket.send("throttle", throttle);




                    },


                    memory: function(name, value, data) {
//						console.log("Memory name: "+name);
//						console.log("Memory value: "+value);
//						console.log("Memory data: "+data);
//						jmri.setMemory("IM_92", "Hej");
					},


                    throttle: function(throttle, data) {
//                        console.log("Throttle: "+throttle+", data: "+data);


                        if (typeof data.speed !== 'undefined') console.log("Speed: ", data.speed);
                        if (typeof data.forward !== 'undefined') console.log("Forward: ", data.forward);
//                        for (var key in data2) {
//                          console.log(key);
//                        }
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
                $('#powerImg').click(function(event) {
                    jmri.setPower((power === jmri.POWER_ON) ? jmri.POWER_OFF : jmri.POWER_ON);
                });
            });



/*
            window.setInterval(daniel, 2000);

			function daniel()
			{
                jmri.setMemory("IM_92", "Hej");

//                throttleData = {"speed": Math.random()};
//                jmri.setThrottle("Daniel", throttleData);

                throttleData = {"speed": Math.random(), "forward": (Math.random() > 0.5)};
                jmri.setThrottle("Daniel", throttleData);
			}
*/
