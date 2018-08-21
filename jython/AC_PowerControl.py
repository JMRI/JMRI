##################################################################################
#
# Script that controls an IoT (internet of things) device using HTTP GET commands.
# This is an example on how to control other devices from JMRI.
# It uses this ethernet double relay: http://www.robot-electronics.co.uk/htm/eth002tech.htm
# (this link explains how it works and how to configure and use it)
# You may find different devices on the market that are controlled by HTTP GET commands.
#
# What this script does for me:
#
# It turns AC mains power ON when it starts and turns it OFF when JMRI shuts down.
# (for my layout power needs: Command Station, Lights, several power adapters, ...)
# I enter the room, turn on the lights, turn on the computer (well, it is always on) and just start JMRI.
# As soon as JMRI starts, my layout command station and several power adapters connected to the relays circuit turn ON.
# When I want to finish the session, I just close JMRI, turn off the lights and exit the room.
#
# I am using the two relays - each relay to cut the circuit for each electrical wire
#
# *** Be carefull working with AC mains power - it is dangerous ***
#
# --------------------------------------------------------------------------------
#
# The script should be loaded at start up (preferencies)
#
# Author: Oscar Moutinho (oscar.moutinho@gmail.com), 2016 - for JMRI
##################################################################################

import httplib
import jmri

deviceURL = "192.168.1.250:80" # The device is configured with IP 192.168.1.250 and Port 80 in my LAN

#=================================================================================
# define the shutdown task class
class ShutDown(jmri.implementation.AbstractShutDownTask):

#---------------------------------------------------------------------------------
# this is the code to be invoked when the program is shutting down
    def execute(self):
        conn = httplib.HTTPConnection(deviceURL)
        conn.request("GET", "/io.cgi=?DOI1") # Open switch 1
        conn.close()
        conn = httplib.HTTPConnection(deviceURL)
        conn.request("GET", "/io.cgi=?DOI2") # Open switch 2
        conn.close()
        return True # True to shutdown, False to abort shutdown

#*********************************************************************************

conn = httplib.HTTPConnection(deviceURL)
conn.request("GET", "/io.cgi=?DOA1") # Close switch 1
conn.close()
conn = httplib.HTTPConnection(deviceURL)
conn.request("GET", "/io.cgi=?DOA2") # Close switch 2
conn.close()

shutdown.register(ShutDown("AC_PowerControl"))

