##################################################################################
#
# Script that controls GPIO change/status on networked devices using TCP/IP sockets.
# The goal is to use cheap versatile computers as stationary decoders (independent of railway network - DCC, loconet, NCE, Lenz, ...).
# This script communication was tested with:
# - raspberry pi 2, zero and 3 (wifi)
# - NodeMCU 1.0 8266 ESP-12E (wifi)
#
# Networked devices will try to reconnect when connection is lost.
#
# JMRI Turnouts and Sensors name definition (always defined as internal):
#
# Internal Turnouts (system name):      [IT].IOT$<gpio>:<id>    (GPIO outputs: THROWN - set output to ground / CLOSED - set output to +V)
# Internal Sensors (system name):       [IS].IOT$<gpio>:<id>    (GPIO inputs: INACTIVE - input is at +V / ACTIVE - input is connected to ground)
# Examples:
# IT.IOT$5:192.168.200 - GPIO 5 as output on device with IP address 192.168.200 listening at port 10000 (default port)
# IS.IOT$13:dev1.mylayout.com - GPIO 13 as input on device with server name 'dev1.mylayout.com' listening at port 10000 (default port)
# IS.IOT$5:192.168.201:12345 - GPIO 5 as input on device with IP address 192.168.201 listening at port 12345
#
# JMRI should manage Sensors debounce delays
#
# This script should be loaded at JMRI startup (preferences).
# After adding or removing these Turnouts and Sensors this script (and JMRI) must be reloaded - before restarting, remember to save the panel.
#
# For testing purposes, you may use the following scripts:
# dummyTcpPeripheral.py - runs on python 2.7 (no JMRI needed) to simulate a networked device (stationary decoder)
# testTcpPeripheral.py - runs on python 2.7 (no JMRI needed) to simulate JMRI running JMRI_TcpPeripheral.py (this script)
#
# For aditional information look at the following files and links:
# (it is important to have some electronic knowledge to get the most of GPIO interfaces - LEDs, buttons, relays, reed switches, ...)
# - dummyTcpPeripheral.py
# - testTcpPeripheral.py
# - JMRI_TcpPeripheral.py (this script)
# - RPi_TcpPeripheral.py (to run at startup on raspberry pi)
# - ESP8266_TcpPeripheral.ino (to upload to NodeMCU 1.0 8266 ESP-12E using arduino IDE)
# https://www.raspberrypi.org/
# https://gpiozero.readthedocs.io/
# https://www.gitbook.com/book/smartarduino/user-manual-for-esp-12e-devkit/details
# https://www.arduino.cc/
# http://www.codeproject.com/Articles/1073160/Programming-the-ESP-NodeMCU-with-the-Arduino-IDE
#
# WARNING:
# Devices GPIOs will be defined as INPUT or OUTPUT from a remote machine.
# Hardware protect (using resistors) each GPIO implemented as INPUT because a remote machine (JMRI) may set it as OUTPUT.
#
# To show debug messages, add the following line (without quotes) to the file 'default.lcf'
# located in the JMRI program directory: 'log4j.category.jmri.jmrit.jython.exec=DEBUG'
#
# Author: Oscar Moutinho (oscar.moutinho@gmail.com), 2016 - for JMRI
##################################################################################

#:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
# imports, module variables and imediate running code

import java
import java.beans
import socket
import threading
import time
from org.apache.log4j import Logger
import jmri

TcpPeripheral_log = Logger.getLogger("jmri.jmrit.jython.exec.TcpPeripheral")

CONN_TIMEOUT = 3.0 # timeout (seconds)
MAX_HEARTBEAT_FAIL = 5 # multiply by CONN_TIMEOUT for maximum time interval (send heartbeat after CONN_TIMEOUT * (MAX_HEARTBEAT_FAIL / 2))

#+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
# get gpio and id from turnout or sensor system name
def TcpPeripheral_getGpioId(sysName):
    gpio = None
    id = None
    _sysName = sysName.split(":")
    if len(_sysName) == 2 or len(_sysName) == 3:
        _gpio = _sysName[0].split("$")
        if len(_gpio) == 2:
            try:
                gpio = int(_gpio[1])
            except: # invalid GPIO
                gpio = 9999
            id = _sysName[1].strip() + ((":" + _sysName[2].strip()) if len(_sysName) > 2 else "")
    return gpio, id

#+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
# this is the code to be executed for a new network device
def TcpPeripheral_addDevice(id):
    alias = id.lower()
    _aux = id.split(":")
    host = _aux[0]
    try:
        port = int(_aux[1])
    except: # invalid port
        port = 10000 # default
    if alias not in TcpPeripheral_sockets:
        TcpPeripheral_sockets[alias] = TcpPeripheral_clientTcpThread(alias, TcpPeripheral_clientTcpThread_callback(), host, port)
        TcpPeripheral_sockets[alias].start()
    count = MAX_HEARTBEAT_FAIL # loop n times max (use this constant for convenience)
    while (not TcpPeripheral_sockets[alias].isAtive) and (count > 0): # try to wait for slow connection
        count -= 1
        time.sleep(CONN_TIMEOUT)
    return

#+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
# this is the code to be executed to close and remove a network device
def TcpPeripheral_removeDevice(id):
    alias = id.lower()
    if alias in TcpPeripheral_sockets:
        TcpPeripheral_sockets[alias].stop()
        del TcpPeripheral_sockets[alias]
    return

#+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
# this is the code to be executed to send a message to a network device
def TcpPeripheral_sendToDevice(out, gpio, active, id):
    alias = id.lower()
    if out:
        msg = "OUT:" + str(gpio) + ":" + ("1" if active else "0")
    else:
        msg = "IN:" + str(gpio)
    sent = TcpPeripheral_sockets[alias].send(msg)
    return sent

#+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
# this is the code to be executed when a valid sensor status is received from a network device
def TcpPeripheral_receivedFromDevice(alias, gpio, value):
    sensorSysName = "IS.IOT$" + str(gpio) + ":" + alias.upper()
    sensor = sensors.getBySystemName(sensorSysName)
    if sensor != None: # sensor exists
        if value:
            sensor.setKnownState(jmri.Sensor.ACTIVE)
        else:
            sensor.setKnownState(jmri.Sensor.INACTIVE)
    else: # sensor does not exist
        TcpPeripheral_log.error("'TcpPeripheral' - " + alias + ": Feedback for non-existent Sensor [" + sensorSysName + "]")
    return

#=================================================================================
# define the TCP client callback class
class TcpPeripheral_clientTcpThread_callback(object):

#---------------------------------------------------------------------------------
# this is the code to be executed when a message is received
    def processRecvMsg(self, clientTcpThread, msg):
        TcpPeripheral_log.debug("'TcpPeripheral' - " + clientTcpThread.alias + ": Received [" + msg + "]")
        _msg = msg.split(":")
        alias = clientTcpThread.alias
        if len(_msg) == 3 and _msg[0].upper() == "IN":
            try:
                gpio = int(_msg[1])
            except: # invalid GPIO
                gpio = 9999
            if _msg[2] == "1":
                TcpPeripheral_receivedFromDevice(alias, gpio, True)
            if _msg[2] == "0":
                TcpPeripheral_receivedFromDevice(alias, gpio, False)
        else: # invalid feedback
            TcpPeripheral_log.error("'TcpPeripheral' - " + alias + ": Invalid feedback [" + msg + "]")
        return

#---------------------------------------------------------------------------------
# this is the code to be executed on stop
    def onFinished(self, clientTcpThread, msg):
        TcpPeripheral_log.info("'TcpPeripheral' - " + clientTcpThread.alias + ": " + msg)
        return

#=================================================================================
# define the TCP client thread class
class TcpPeripheral_clientTcpThread(threading.Thread):

#---------------------------------------------------------------------------------
# this is the code to be executed when the class is instantiated
    def __init__(self, alias, callback, ip, port):
        threading.Thread.__init__(self)
        self.alias = alias
        self.callback = callback
        self.ip = ip
        self.port = port
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
                    TcpPeripheral_log.debug("'TcpPeripheral' - " + self.alias + ": Received (including heartbeat) [" + received + "]")
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
                    TcpPeripheral_log.error("'TcpPeripheral' - " + self.alias + ": Connection broken - closing socket")
                    self.sock.close()
                    self.isAtive = False
                    self.connect() # reconnect
                    heartbeatFailCount = 0
            except socket.timeout as e:
                heartbeatFailCount += 1
                if heartbeatFailCount > MAX_HEARTBEAT_FAIL:
                    TcpPeripheral_log.error("'TcpPeripheral' - " + self.alias + ": Heartbeat timeout - closing socket")
                    self.sock.close()
                    self.isAtive = False
                    self.connect() # reconnect
                    heartbeatFailCount = 0
            except:
                TcpPeripheral_log.error("'TcpPeripheral' - " + self.alias + ": Connection reset by peer - closing socket")
                self.sock.close()
                self.isAtive = False
                self.connect() # reconnect
                heartbeatFailCount = 0
        self.callback.onFinished(self, "Finished")
        return

#---------------------------------------------------------------------------------
# this is the code to be executed to connect or reconnect
    def connect(self):
        server_address = (self.ip, self.port)
        while not self.exit:
            TcpPeripheral_log.info("'TcpPeripheral' - " + self.alias + ": Connecting socket thread to '%s' port %s" % server_address)
            try:
                self.sock = socket.create_connection(server_address, CONN_TIMEOUT)
            except socket.error as e:
                TcpPeripheral_log.error("'TcpPeripheral' - " + self.alias + ": ERROR - " + str(e))
                self.sock = None
                time.sleep(CONN_TIMEOUT)
            else:
                TcpPeripheral_log.info("'TcpPeripheral' - " + self.alias + ": Connected to '%s' port %s" % server_address)
                self.isAtive = True
                break # continue because connection is done
        return

#---------------------------------------------------------------------------------
# this is the code to be executed to send a message
    def send(self, msg):
        if self.isAtive:
            TcpPeripheral_log.debug("'TcpPeripheral' - '" + self.alias + "' sending message: " + msg)
            try:
                self.sock.sendall(msg + "|") # add end of command delimiter
            except:
                TcpPeripheral_log.error("'TcpPeripheral' - " + self.alias + ": Error sending - closing socket")
                self.sock.close()
                self.isAtive = False
                self.connect() # reconnect
                heartbeatFailCount = 0
        else:
            TcpPeripheral_log.error("'TcpPeripheral' - '" + self.alias + "' message [" + msg + "] not sent")
        return self.isAtive

#---------------------------------------------------------------------------------
# this is the code to be executed to close the socket and exit
    def stop(self):
        TcpPeripheral_log.info("'TcpPeripheral' - " + self.alias + ": Stop the socket thread - closing socket")
        try:
            self.sock.close()
        except: # ignore possible error if connection not ok
            pass
        finally:
            self.isAtive = False
            self.exit = True
        return

#=================================================================================
# define the listener class for Sensors
class TcpPeripheral_Sensor_Listener(java.beans.PropertyChangeListener):

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    def propertyChange(self, event):
        sensor = event.getSource()
        sensorName = sensor.getDisplayName(jmri.NamedBean.DisplayOptions.USERNAME_SYSTEMNAME)
        TcpPeripheral_log.debug("'TcpPeripheral' - Sensor=" + sensorName + " property=" + event.propertyName + "]: oldValue=" + str(event.oldValue) + " newValue=" + str(event.newValue))
        if event.propertyName == "KnownState": # only this property matters
            gpio, id = TcpPeripheral_getGpioId(sensor.getSystemName())
            sent = TcpPeripheral_sendToDevice(False, gpio, None, id)
            if not sent: # set as unknown
                sensor.setKnownState(jmri.Sensor.UNKNOWN)
        return

#=================================================================================
# define the listener class for Turnouts
class TcpPeripheral_Turnout_Listener(java.beans.PropertyChangeListener):

#---------------------------------------------------------------------------------
# this is the code to be executed when the class is instantiated
    def __init__(self):
        self.turnoutCtrl = None # for turnout restore control
        return

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    def propertyChange(self, event):
        turnout = event.getSource()
        turnoutName = turnout.getDisplayName(jmri.NamedBean.DisplayOptions.USERNAME_SYSTEMNAME)
        TcpPeripheral_log.debug("'TcpPeripheral' - Turnout=" + turnoutName + " property=" + event.propertyName + "]: oldValue=" + str(event.oldValue) + " newValue=" + str(event.newValue) + " turnoutCtrl=" + str(self.turnoutCtrl))
        if event.propertyName == "CommandedState": # only this property matters
            if event.newValue != self.turnoutCtrl: # this is a state change request
                gpio, id = TcpPeripheral_getGpioId(turnout.getSystemName())
                sent = True
                if event.newValue == jmri.Turnout.CLOSED:
                    sent = TcpPeripheral_sendToDevice(True, gpio, True, id)
                if event.newValue == jmri.Turnout.THROWN:
                    sent = TcpPeripheral_sendToDevice(True, gpio, False, id)
                if sent: # store the current state
                    self.turnoutCtrl = event.newValue
                else: # restore turnout state
                    self.turnoutCtrl = event.oldValue
                    turnout.setCommandedState(event.oldValue)
        return

#=================================================================================
# define the shutdown task class
class TcpPeripheral_ShutDown(jmri.implementation.AbstractShutDownTask):

#---------------------------------------------------------------------------------
# this is the code to be invoked when the program is shutting down
    def execute(self):
        auxList = []
        for alias in TcpPeripheral_sockets:
            auxList.append(alias)
        for alias in auxList:
            TcpPeripheral_removeDevice(alias)
        TcpPeripheral_log.info("Shutting down 'TcpPeripheral'.")
        time.sleep(3) # wait 3 seconds for all sockets to close
        return True # True to shutdown, False to abort shutdown

#*********************************************************************************

if globals().get("TcpPeripheral_running") != None: # Script already loaded so exit script
    TcpPeripheral_log.warn("'TcpPeripheral' already loaded and running. Restart JMRI before load this script.")
else: # Continue running script
    TcpPeripheral_log.info("'TcpPeripheral' started.")
    TcpPeripheral_running = True
    TcpPeripheral_sockets = {}
    shutdown.register(TcpPeripheral_ShutDown("TcpPeripheral"))
    for sensor in sensors.getNamedBeanSet():
        gpio, id = TcpPeripheral_getGpioId(sensor.getSystemName())
        TcpPeripheral_log.debug("'TcpPeripheral' - Sensor SystemName [" + sensor.getSystemName() + "] GPIO [" + str(gpio) + "] ID [" + str(id) + "]")
        if gpio != None and id != None:
            TcpPeripheral_addDevice(id)
            sensor.setKnownState(jmri.Sensor.INCONSISTENT) # set sensor to inconsistent state (just to detect change to unknown)
            sensor.addPropertyChangeListener(TcpPeripheral_Sensor_Listener())
            sensor.setKnownState(jmri.Turnout.UNKNOWN) # to force send a register request to device
    for turnout in turnouts.getNamedBeanSet():
        gpio, id = TcpPeripheral_getGpioId(turnout.getSystemName())
        TcpPeripheral_log.debug("'TcpPeripheral' - Turnout SystemName [" + turnout.getSystemName() + "] GPIO [" + str(gpio) + "] ID [" + str(id) + "] Kown State [" + str(turnout.getKnownState()) + "]")
        if gpio != None and id != None: # should be a valid network device and GPIO
            TcpPeripheral_addDevice(id)
            currentState = turnout.getCommandedState() # get current turnout state
            turnout.setCommandedState(jmri.Turnout.UNKNOWN) # set turnout to a state that will permit change detection by listener
            turnout.addPropertyChangeListener(TcpPeripheral_Turnout_Listener())
            if currentState == jmri.Turnout.CLOSED:
                turnout.setCommandedState(jmri.Turnout.CLOSED)
            if currentState == jmri.Turnout.THROWN:
                turnout.setCommandedState(jmri.Turnout.THROWN)
