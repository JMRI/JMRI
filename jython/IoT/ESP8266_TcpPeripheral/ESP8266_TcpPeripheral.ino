/*
##################################################################################
#
# To control GPIO on networked (WiFi) ESP8266 using TCP/IP sockets.
# http://www.arduino.cc/en/Reference
#
# Please, see below the network info (WiFi credentials, IP address, ...) and change them to fit your requirements.
#
# Networked devices will try to reconnect when connection is lost.
#
# Receive (change output GPIO):			OUT:<gpio>:0 or OUT:<gpio>:1	(0 - set output to ground / 1 - set output to +V)
# Receive (request input GPIO status):	IN:<gpio>
# Send (input GPIO):					IN:<gpio>:0, IN:<gpio>:1		(0 - input is at +V / 1 - input is connected to ground)
# Send (errors):						ERROR or IN:<gpio>:ERROR or OUT:<gpio>:ERROR
#
# In arduino, there is no way to check if a GPIO exists and is usable or not.
# User must know in advance what GPIO (pin number) is going to be used and its behaviour.
#
# WARNING:
# GPIO will be defined as INPUT or OUTPUT from a remote machine.
# Hardware protect (using resistors) each GPIO implemented as INPUT because a remote machine may set it as OUTPUT.
#
# Each command/status sent or received must end with a '|' (pipe).
# A string received without a '|' (pipe) is not managed as a command/status until a '|' (pipe) is received in a new message.
# A trailing '|' (pipe) is automatically appended when sending a message.
# Spaces are ignored (they are used as heartbeat control).
#
# Author: Oscar Moutinho (oscar.moutinho@gmail.com), 2016 - for JMRI
##################################################################################
*/

//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
// imports and global variables

#include <ESP8266WiFi.h>

// network info:
const char* ssid = "your-ssid"; // wireless SSID
const char* password = "your-password"; // wireless password (may be empty if no security)
IPAddress ip(x, x, x, x); // this device IP address (example: 192.168.1.230)
IPAddress gateway(x, x, x, x); // usually the router IP address (example: 192.168.1.1)
IPAddress subnet(x, x, x, x); // usually 255.255.255.0

const int port = 10000; // listening port

const boolean DEBUG = true; // set to 'true' to send serial info to arduino IDE ('false' for normal running)

const unsigned long HEARTBEAT_TIMEOUT = 15000; // timeout (milliseconds)
const unsigned long HEARTBEAT_INTERVAL = 7500; // interval (milliseconds)

WiFiServer server(port);
WiFiClient client;
unsigned long heartbeatTimeout;
unsigned long heartbeatInterval;
boolean notConnected;
String gpioIN = ""; // set input GPIO list to empty
String currentMessage;

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
void setup() {
	if (DEBUG) Serial.begin(115200);
	delay(10);
	if (DEBUG) Serial.println();
	if (DEBUG) Serial.print("Connecting to ");
	if (DEBUG) Serial.println(ssid);
	WiFi.config(ip, gateway, subnet);
	if (password == "") {
		WiFi.begin(ssid); // for WiFi open connection
	} else {
		WiFi.begin(ssid, password); // for password protected  WiFi connection
	}
	while (WiFi.status() != WL_CONNECTED) { // wait for  WiFi connection
		delay(500);
		if (DEBUG) Serial.print(".");
	}
	if (DEBUG) Serial.println("");
	if (DEBUG) Serial.println("WiFi connected");
	server.begin();
	if (DEBUG) Serial.print("Server started at ");
	if (DEBUG) Serial.println(WiFi.localIP());
	if (DEBUG) Serial.println();
}

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
void loop() {
	int pinInit;
	int pinEnd;
	int newStatus;
	String received;
	int sepIndex;
	String cmd;
	if (!isConnected()) { // check if socket connected
		delay(500);
		return;
	}
	if ((millis() - heartbeatTimeout) > HEARTBEAT_TIMEOUT) { // disconnect and reconnect
		// ignore millis() restart to 0 after 70 days - very unlikely to happen exactly when a heartbeat timeout is occuring
		if (DEBUG) Serial.println("Heartbeat receive timeout");
		client.flush();
		client.stop();
		client = WiFiClient(); // remove old object (put out of scope)
		delay(500);
		return;
	}
	if ((millis() - heartbeatInterval) > HEARTBEAT_INTERVAL || millis() < heartbeatInterval) { // send only after appropriate delay
		// when millis() restarts to 0 after 70 days force a heartbeat send even if not needed
		client.write(' '); // send heartbeat
		heartbeatInterval = millis(); // restart heartbeat interval
	}
	pinInit = gpioIN.indexOf('.'); // gpioIN String example: ".3:0.11:1.13:1.1:9."
	while (pinInit != -1) {
		pinEnd = gpioIN.indexOf('.', pinInit + 1);
		if (pinEnd == -1) break;
		newStatus = sendStatus(gpioIN.substring(pinInit, pinEnd + 1)); // process each input GPIO status response (send if changed)
		if (newStatus != -1) gpioIN.setCharAt(pinEnd - 1, String(newStatus)[0]); // register new status (if changed)
		pinInit = pinEnd;
	}
	if (!client.available()) return; // check if data available
	heartbeatTimeout = millis(); // restart heartbeat timeout
	received = client.readString();
	if (DEBUG) Serial.println("Received (including heartbeat) [" + received + "]");
	received.replace(" ", ""); // remove spaces (heartbeat)
	currentMessage += received;
	sepIndex = currentMessage.indexOf('|');
	while(sepIndex != -1) {
		if (sepIndex > 0) { // if not empty
			cmd = currentMessage.substring(0, sepIndex);
			processRecvMsg(cmd);
		}
		currentMessage.remove(0, sepIndex + 1);
		sepIndex = currentMessage.indexOf('|');
	}
}

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
boolean isConnected() { // check if socket connected
	if (!client) {
		if (DEBUG) Serial.println("Checking new incoming connection");
		client = server.available();
		heartbeatTimeout = millis(); // start heartbeat timeout
		heartbeatInterval = millis(); // start heartbeat interval
		currentMessage = "";
		notConnected = true;
		return false; // connection not ready yet
	} else {
		if (!client.connected()) {
			if (DEBUG) Serial.println("Client not connected");
			client.flush();
			client.stop();
			client = WiFiClient(); // remove old object (put out of scope)
			return false; // not connected
		} else {
			if (notConnected) if (DEBUG) Serial.println("Client connected"); // inform connection established
			notConnected = false;
			return true; // connected
		}
	}
}

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
void processRecvMsg(String msg) { // message received
	int auxIndex1;
	int auxIndex2;
	String errorMsg = "";
	String auxMsg;
	boolean in;
	int pin;
	int status = 0;
	if (DEBUG) Serial.println("Message received: [" + msg + "]");
	auxIndex1 = msg.indexOf(':');
	if (auxIndex1 == -1) errorMsg = "ERROR";
	else {
		auxMsg = msg.substring(0, auxIndex1);
		auxMsg.toUpperCase();
		if (auxMsg != "IN" && auxMsg != "OUT") errorMsg = "ERROR";
		else {
			in = (auxMsg == "IN");
			if (in) { // IN
				pin = msg.substring(auxIndex1 + 1).toInt();
			} else { // OUT
				auxIndex2 = msg.indexOf(':', auxIndex1 + 1);
				if (auxIndex2 == -1) errorMsg = "ERROR";
				else {
					pin = msg.substring(auxIndex1 + 1, auxIndex2).toInt();
					status = msg.substring(auxIndex2 + 1).toInt();
				}
			}
		}
	}
	if (errorMsg.length() == 0) if (pin < 0 or status < 0) errorMsg = "ERROR";
	if (errorMsg.length() > 0) {
		client.print(errorMsg + "|"); // send error response
		return;
	}
	if (in) {
		if (gpioIN.indexOf("." + String(pin) + ":") == -1) { // register GPIO input for response
			pinMode(pin, INPUT_PULLUP);
// to apply different settings for input pins, add code here ...
// example (no pullup resistor for pin 3 input):
//			if (pin == 3) pinMode(pin, INPUT);
			if (gpioIN.length() == 0) gpioIN += ".";
			gpioIN += String(pin) + ":9."; // '9' will force first status send
			if (DEBUG) Serial.println("GPIO " + String(pin) + " registered for input");
		}
	} else { // set GPIO output status
		if (gpioIN.indexOf("." + String(pin) + ":") != -1) { // remove GPIO input from response list
			gpioIN.replace("." + String(pin) + ":", "*");
			auxIndex1 = gpioIN.indexOf('*');
			gpioIN.remove(auxIndex1, 2); // remove "*s"
			if (gpioIN.length() == 1) gpioIN = "";
		}
		pinMode(pin, OUTPUT);
		if (status == 0) {
			digitalWrite(pin, LOW);
			if (DEBUG) Serial.println("GPIO " + String(pin) + " OUT set to 0");
		} else {
			digitalWrite(pin, HIGH);
			if (DEBUG) Serial.println("GPIO " + String(pin) + " OUT set to 1");
		}
	}
	if (DEBUG) Serial.println("GPIO inputs list: [" + gpioIN + "]");
}

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
int sendStatus(String pinInfo) { // send status (pinInfo should look like ".nn:s.")
	int pin = pinInfo.substring(1).toInt(); // get GPIO pin ('nn' is a number - one or more digits)
	int lastStatus = pinInfo.substring(pinInfo.indexOf(':') + 1).toInt(); // get last registered status
	int value = digitalRead(pin); // HIGH | LOW
	int status;
	if (value == LOW) status = 1; else status = 0; // LOW will inform connection (1) to ground - HIGH is no connection (0)
	if (status == lastStatus) return -1; // don't send status if it didn't change
	client.print("IN:" + String(pin) + ":" + String(status) + "|"); // send status
	if (DEBUG) Serial.println("Status sent: [" + String(pin) + "(" + String(status) + ")]");
	return status;
}
