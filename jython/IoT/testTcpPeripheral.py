##################################################################################
#
# To test GPIO change/status on networked devices using TCP/IP sockets.
#
# Networked devices will try to reconnect when connection is lost.
#
# Send (change output GPIO):        OUT:<gpio>:0 or OUT:<gpio>:1
# Send (request input GPIO status): IN:<gpio>
# Receive (input GPIO):             IN:<gpio>:0, IN:<gpio>:1
# Receive (errors):                 ERROR or IN:<gpio>:ERROR or OUT:<gpio>:ERROR
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

import socket
import threading
import time

CONN_TIMEOUT = 3.0 # timeout (seconds)
MAX_HEARTBEAT_FAIL = 5 # multiply by CONN_TIMEOUT for maximum time interval (send heartbeat after CONN_TIMEOUT * (MAX_HEARTBEAT_FAIL / 2))

#=================================================================================
# define the TCP client callback class
class clientTcpThread_callback(object):

#---------------------------------------------------------------------------------
# this is the code to be executed when a message is received
    def processRecvMsg(self, clientTcpThread, msg):
        print clientTcpThread.alias + ": Received [" + msg + "]"
        return

#---------------------------------------------------------------------------------
# this is the code to be executed on stop
    def onFinished(self, clientTcpThread, msg):
        print clientTcpThread.alias + ": " + msg
        return

#=================================================================================
# define the TCP client thread class
class clientTcpThread(threading.Thread):

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
                    print self.alias + ": Received (including heartbeat) [" + received + "]"
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
                    print self.alias + ": Connection broken - closing socket"
                    self.sock.close()
                    self.isAtive = False
                    self.connect() # reconnect
                    heartbeatFailCount = 0
            except socket.timeout as e:
                heartbeatFailCount += 1
                if heartbeatFailCount > MAX_HEARTBEAT_FAIL:
                    print self.alias + ": Heartbeat timeout - closing socket"
                    self.sock.close()
                    self.isAtive = False
                    self.connect() # reconnect
                    heartbeatFailCount = 0
            except:
                print self.alias + ": Connection reset by peer - closing socket"
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
            print self.alias + ": Connecting socket thread to '%s' port %s" % server_address
            try:
                self.sock = socket.create_connection(server_address, CONN_TIMEOUT)
            except socket.error as e:
                print self.alias + ": ERROR - " + str(e)
                self.sock = None
                time.sleep(CONN_TIMEOUT)
            else:
                print self.alias + ": Connected to '%s' port %s" % server_address
                self.isAtive = True
                break # continue because connection is done
        return

#---------------------------------------------------------------------------------
# this is the code to be executed to send a message
    def send(self, msg):
        if self.isAtive:
            print "'" + self.alias + "' sending message:", msg
            self.sock.sendall(msg + "|") # add end of command delimiter
        else:
            print self.alias + ": Message [" + msg + "] not sent"
        return self.isAtive

#---------------------------------------------------------------------------------
# this is the code to be executed to close the socket and exit
    def stop(self):
        print self.alias + ": Stop the socket thread - closing socket"
        try:
            self.sock.close()
        except: # ignore possible error if connection not ok
            pass
        finally:
            self.isAtive = False
            self.exit = True
        return

#*********************************************************************************

sockets = {}
exit = False
while not exit:
    msg = raw_input("Enter 'DEVICE <alias> <host> [<port>(10000)]' or 'EXIT [<alias> ...]' or '<alias> <cmd>':\n")
    words = msg.split()
    if len(words) > 0 and words[0].upper() == "EXIT":
        if len(words) == 1: # stop all sockets and remove from list
            auxList = []
            for alias, sock in sockets.iteritems():
                sock.stop()
                auxList.append(alias)
            for alias in auxList:
                del sockets[alias]
            exit = True
        else: # stop the sockets connected with each device <alias> and remove from list
            for idx, alias in enumerate(words):
                _alias = alias.lower()
                if idx > 0 and _alias in sockets:
                    sockets[_alias].stop()
                    del sockets[_alias]
    elif len(words) > 2 and words[0].upper() == "DEVICE": # create, add to the list and start socket for device <alias> <host> [<port>]
        _alias = words[1].lower()
        _host = words[2].lower()
        try:
            _port = int(words[3])
        except: # invalid port
            _port = 10000 # default
        if _alias not in sockets:
            sockets[_alias] = clientTcpThread(_alias, clientTcpThread_callback(), _host, _port)
            sockets[_alias].start()
    elif len(words) == 2: # send command to the socket connected with device <alias>
        _alias = words[0].lower()
        if _alias in sockets:
            sockets[_alias].send(words[1])
