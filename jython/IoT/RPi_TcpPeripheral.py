##################################################################################
#
# To control GPIO on networked raspberry pi using TCP/IP sockets.
# http://gpiozero.readthedocs.io
#
# Usage: <thisScript>.py [<port>(10000)]
#
# Networked devices will try to reconnect when connection is lost.
#
# Receive (change output GPIO):         OUT:<gpio>:0 or OUT:<gpio>:1    (0 - set output to ground / 1 - set output to +V)
# Receive (request input GPIO status):  IN:<gpio>
# Send (input GPIO):                    IN:<gpio>:0, IN:<gpio>:1        (0 - input is at +V / 1 - input is connected to ground)
# Send (errors):                        ERROR or IN:<gpio>:ERROR or OUT:<gpio>:ERROR
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

#:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
# imports, module variables and imediate running code

import gpiozero
import socket
import threading
import time
import sys

CONN_TIMEOUT = 3.0 # timeout (seconds)
MAX_HEARTBEAT_FAIL = 5 # multiply by CONN_TIMEOUT for maximum time interval (send heartbeat after CONN_TIMEOUT * (MAX_HEARTBEAT_FAIL / 2))

gpioOUT = {}
gpioIN = {}

#+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
# this is the code to be executed when an input is activated
def inputActivated(input):
    sock.send("IN:" + str(input.pin.number) + ":1")
    return

#+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
# this is the code to be executed when an input is deactivated
def inputDeactivated(input):
    sock.send("IN:" + str(input.pin.number) + ":0")
    return

#=================================================================================
# define the TCP server callback class
class serverTcpThread_callback(object):

#---------------------------------------------------------------------------------
# this is the code to be executed when a message is received
    def processRecvMsg(self, serverTcpThread, msg):
#       print "From '" + serverTcpThread.client + "': Received [" + msg + "]"
        cmdParams = msg.split(":")
        if len(cmdParams) < 2 or (cmdParams[0].upper() != "OUT" and cmdParams[0].upper() != "IN"): # generic error
            serverTcpThread.send("ERROR")
            return
        inout = cmdParams[0].upper()
        try:
            pin = int(cmdParams[1])
        except: # invalid GPIO
            pin = 9999
        if inout == "OUT":
            if len(cmdParams) != 3: # error for OUT command
                serverTcpThread.send(inout + ":" + str(pin) + ":ERROR")
                return
            if pin not in gpioOUT: # try to configure GPIO as output and add it to the list
                if pin in gpioIN: # already defined for input
                    gpioIN[pin].close() # close it and free resources
                    del gpioIN[pin] # remove it
                try:
                    gpioAux = gpiozero.LED(pin)
                except:
                    serverTcpThread.send(inout + ":" + str(pin) + ":ERROR")
                    return
                else:
                    gpioOUT[pin] = gpioAux
#               print "GPIO " + str(pin) + " set to output"
            try:
                status = int(cmdParams[2])
            except: # invalid status
                status = 0
            if status == 0:
                gpioOUT[pin].off()
#               print "GPIO " + str(pin) + " OUT set to 0"
            else:
                gpioOUT[pin].on()
#               print "GPIO " + str(pin) + " OUT set to 1"
        if inout == "IN":
            if len(cmdParams) != 2: # error for IN command
                serverTcpThread.send(inout + ":" + str(pin) + ":ERROR")
                return
            if pin not in gpioIN: # try to configure GPIO as input and add it to the list
                if pin in gpioOUT: # already defined for output
                    gpioOUT[pin].close() # close it and free resources
                    del gpioOUT[pin] # remove it
                try:
                    gpioAux = gpiozero.Button(pin, True) # pullup resistor
# to apply different settings for input pins, add code here ...
# example (no pullup resistor for pin 3 input):
#                   if pin == 3:
#                       gpioAux = gpiozero.Button(pin, False)
                except:
                    serverTcpThread.send(inout + ":" + str(pin) + ":ERROR")
                    return
                else:
                    gpioIN[pin] = gpioAux
                    gpioIN[pin].when_pressed = inputActivated
                    gpioIN[pin].when_released = inputDeactivated
#           print "GPIO " + str(pin) + " registered for input"
            if gpioIN[pin].is_pressed:
                inputActivated(gpioIN[pin])
            else:
                inputDeactivated(gpioIN[pin])
        return

#---------------------------------------------------------------------------------
# this is the code to be executed on stop
    def onFinished(self, serverTcpThread, msg):
#       print "Connection to '" + serverTcpThread.client + "': " + msg
        pass
        return

#=================================================================================
# define the TCP server thread class
class serverTcpThread(threading.Thread):

#---------------------------------------------------------------------------------
# this is the code to be executed when the class is instantiated
    def __init__(self, callback, port):
        threading.Thread.__init__(self)
        self.callback = callback
        self.port = port
        self.client = ""
        self.received = ""
        self.isAtive = False
        self.exit = False
        self.sock = None
        return

#---------------------------------------------------------------------------------
# this is the code to be executed on start
    def run(self):
        self.connect() # connect
        heartbeatFailCount = 0
        heartbeatCtrl = time.time() # start heartbeat delay
        while not self.exit:
            if (time.time() - heartbeatCtrl) > (CONN_TIMEOUT * (MAX_HEARTBEAT_FAIL / 2)): # send only after appropriate delay
                self.sock.sendall(" ") # send heartbeat
                heartbeatCtrl = time.time() # restart heartbeat delay
            try:
                received = self.sock.recv(256)
                if received:
#                   print "'" + self.client + "': Received (including heartbeat) [" + received + "]"
                    heartbeatFailCount = 0
                    self.received += received.replace(" ", "") # remove spaces (heartbeat)
                    cmds = self.received.split("|")
                    if len(cmds) > 0:
                        for cmd in cmds:
                            if cmd: # if not empty
                                self.callback.processRecvMsg(self, cmd)
                        procChars = self.received.rfind("|")
                        self.received = self.received[procChars:]
                else:
#                   print "'" + self.client + "': Connection broken - closing socket"
                    self.sock.close()
                    self.isAtive = False
                    self.connect() # reconnect
                    heartbeatFailCount = 0
            except socket.timeout as e:
                heartbeatFailCount += 1
                if heartbeatFailCount > MAX_HEARTBEAT_FAIL:
#                   print "'" + self.client + "': Heartbeat timeout - closing socket"
                    self.sock.close()
                    self.isAtive = False
                    self.connect() # reconnect
                    heartbeatFailCount = 0
            except:
#               print "'" + self.client + "': Connection reset by peer - closing socket"
                self.sock.close()
                self.isAtive = False
                self.connect() # reconnect
                heartbeatFailCount = 0
        self.callback.onFinished(self, "Finished")
        return

#---------------------------------------------------------------------------------
# this is the code to be executed to connect or reconnect
    def connect(self):
        server_address = ("", self.port)
        while not self.exit:
            try:
                sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                sock.bind(server_address)
                sock.settimeout(CONN_TIMEOUT)
            except socket.error as e:
#               print "Binding: ERROR - " + str(e)
                sock = None
                time.sleep(CONN_TIMEOUT)
            else:
                break # continue because binding is done
        while not self.exit:
#           print "Waiting for incoming socket connection to port " + str(self.port) + " on this device"
            try:
                sock.listen(1)
                self.sock, client_address = sock.accept()
                self.client = client_address[0] + ":" + str(client_address[1])
                self.sock.settimeout(CONN_TIMEOUT)
            except socket.error as e:
                self.sock = None
                time.sleep(CONN_TIMEOUT)
            else:
#               print "'" + self.client + "': Connected to port " + str(self.port) + " on this device"
                self.isAtive = True
                break # continue because connection is done
        return

#---------------------------------------------------------------------------------
# this is the code to be executed to send a message
    def send(self, msg):
        while (not self.isAtive) and (not self.exit):
            time.sleep(1) # wait until active or to exit
        if self.isAtive:
#           print "To '" + self.client + "', sending message:", msg
            self.sock.sendall(msg + "|") # add end of command delimiter
        return

#---------------------------------------------------------------------------------
# this is the code to be executed to close the socket and exit (usually not used on raspberry pi - just turn off the power)
    def stop(self):
#       print "'" + self.client + "': Stop the socket thread - closing socket"
        try:
            self.sock.close()
        except: # ignore possible error if connection not ok
            pass
        finally:
            self.isAtive = False
            self.exit = True
        return

#*********************************************************************************

inputArgs = sys.argv
if len(inputArgs) > 1:
    try:
        port = int(inputArgs[1])
    except: # invalid port
        port = 10000 # default
else:
    port = 10000 # default
sock = serverTcpThread(serverTcpThread_callback(), port)
sock.start()
while True:
    pass
