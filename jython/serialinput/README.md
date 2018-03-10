This directory contains scripts for handing CSV-formated input from a serial device, e.g. an Arduino

`SerialInputSketch.ino` is an Arduino sketch that reads analog and digital inputs, then forwards them across the serial link as CSV-formatted data.

Once that's hooked up, the .py scripts in this directory can be used as tests.

First start JMRI, open a script output window, a script input window, make sure the input window source is set to "Python", and then copy the following into the input window after changing the third line to be your new serial port, e.g. "COM5" or whatever:

execfile("jython/serialinput/SerialPortDevice.py")
a = SerialPortDevice()
a.open("/dev/cu.usbmodem1411")
a.start()

Run those lines, and you should start (within a couple seconds) seeing a rapid set of input lines on the screen.  Those are what's being read by the Arduino.  The first few are analog inputs (e.g. change a voltage on an analog pin and the value should change) and the rest are the digital ones (change a digital input to see them change).

That script is really meant for easy debugging of the connections, setup, etc.  Once that's working, we can move on.  

A second script does the minimum to run a locomotive.  To do that, quit and restart JMRI (to clear out the above) and repeat with a slightly different set of lines:

execfile("jython/serialinput/SerialPortDevice.py")
execfile("jython/serialinput/SerialPortThrottle.py")
a = SerialPortThrottle()
a.open("/dev/cu.usbserial-A7006QP9")
a.long = True
a.address = 4123
a.start()

Again, you have to include the right port name.  The "long" and "address" values refer to the DCC locomotive you want to run.  (If you don't set them, the default is short address 3)

When you run that, you should be able to set the speed, forward/backward, and control bell and horn via the analog and digital inputs to the Arduino.

