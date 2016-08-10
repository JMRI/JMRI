##################################################################################

The goal of this project is to control networked devices in a standard way using JMRI.

Each device may be seen as a stationary decoder but instead of using DCC, LOCONET, NCE or any other railroad connection/protocol,
they communicate with JMRI computer using WiFi on the local area network (LAN).

----------------------------------------------------------------------------------

JMRI will control pin outputs using Turnouts defined as internal:
System name:	[IT].IOT$<pin>:<deviceId>	(pin outputs: THROWN - set output to ground / CLOSED - set output to +V)
	Examples:
IT.IOT$5:192.168.200 - GPIO pin 5 as output on device with IP address 192.168.200 listening at port 10000 (default port)
IT.IOT$7:192.168.200 - GPIO pin 7 as output on device with IP address 192.168.200 listening at port 10000 (default port)
IT.IOT$11:dev1.mylayout.com - GPIO pin 11 as output on device with server name 'dev1.mylayout.com' listening at port 10000 (default port)

JMRI will receive feedback from pin inputs using Sensors defined as internal:
System name:	[IS].IOT$<pin>:<deviceId>	(pin inputs: INACTIVE - input is at +V / ACTIVE - input is connected to ground)
	Examples:
IT.IOT$8:192.168.200 - GPIO pin 8 as input on device with IP address 192.168.200 listening at port 10000 (default port)
IS.IOT$13:dev1.mylayout.com - GPIO pin 13 as input on device with server name 'dev1.mylayout.com' listening at port 10000 (default port)
IS.IOT$5:192.168.201:12345 - GPIO pin 5 as input on device with IP address 192.168.201 listening at port 12345

----------------------------------------------------------------------------------

  PLEASE READ THE INFORMATION AT THE BEGINNING OF EACH SCRIPT FILE

- dummyTcpPeripheral.py
- testTcpPeripheral.py
- JMRI_TcpPeripheral.py
- RPi_TcpPeripheral.py
- ESP8266_TcpPeripheral.ino

This is important to get the most of this solution.

----------------------------------------------------------------------------------

For testing purposes, you may use the following scripts:

dummyTcpPeripheral.py - runs on python 2.7 (no JMRI needed) to simulate a networked device (stationary decoder)
testTcpPeripheral.py - runs on python 2.7 (no JMRI needed) to simulate JMRI running JMRI_TcpPeripheral.py (the JMRI computer)

This is the script to load at JMRI startup:
JMRI_TcpPeripheral.py

This is the script to run at startup on Raspberry Pis:
RPi_TcpPeripheral.py
(tested with several Raspberry Pi models)

This is the sketch (not a python script) to load on Arduinos with ESP8266/WiFi (using arduino IDE):
ESP8266_TcpPeripheral.ino
(tested with NodeMCU 8266 ESP-12E V1.0 and V3)

For aditional information look at the following links and search for related info:
(it is important to have some electronic knowledge to get the most of GPIO interfaces - LEDs, buttons, relays, reed switches, ...)
https://www.raspberrypi.org/
https://gpiozero.readthedocs.io/
https://www.gitbook.com/book/smartarduino/user-manual-for-esp-12e-devkit/details
https://www.arduino.cc/
http://www.codeproject.com/Articles/1073160/Programming-the-ESP-NodeMCU-with-the-Arduino-IDE

Using the implemented protocol anyone may develop script variations for these and other devices.

----------------------------------------------------------------------------------

Networked devices will try to reconnect when connection is lost.
Technically, the devices communicate using TCP sockets over IP.
There are no technical limitations to extend the control of these devices using physical network cables and over the internet.
(but I think cables are not welcome and internet security is an issue)
These networked devices may not only interact with the railroad but they may also control sound, room light, ... anything you want.
This is IoT - Internet of Things.

----------------------------------------------------------------------------------

Author: Oscar Moutinho (oscar.moutinho@gmail.com), 2016 - for JMRI
##################################################################################
