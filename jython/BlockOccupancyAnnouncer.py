# Listen to all sensors, printing an info line when they change state.
#  If the sensor has a "matching" block, print some block detail and speak
#  the most useful attributes audibly.  
#  This is useful for validating occupancy and block setup and wiring
#
# Author: mstevetodd, copyright 2013
# based on SensorLog.py and BlockLister.py
# Part of the JMRI distribution

import java
import java.beans
import jmri

# Define routine to map status numbers to text
def stateName(state) :
    if (state == ACTIVE) :
        return "ACTIVE"
    if (state == INACTIVE) :
        return "INACTIVE"
    if (state == INCONSISTENT) :
        return "INCONSISTENT"
    if (state == UNKNOWN) :
        return "UNKNOWN"
    return "(invalid)"

# convert block state to english
def cvtBlockStateToText(state) :
    rep = ""
    if (state == jmri.Block.OCCUPIED) :
        rep = rep + "Occupied "
    if (state == jmri.Block.UNOCCUPIED) :
        rep = rep + "Unoccupied "
    return rep

# use external "nircmd" command to "speak" some text  (I prefer this voice to eSpeak)
def speak(msg) :
    #uncomment next line for speech (Jenkins doesn't like this command)
    #java.lang.Runtime.getRuntime().exec('C:\\Progra~2\\nircmd\\nircmd speak text "' + msg +'"')
    return
    
# Define the sensor listener: 
# Print details on all sensor changes, and speak key blocks
# items if sensor is a block occupancy sensor 
class SensorListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    if (event.propertyName == "KnownState") :
        sensor_num = event.source.systemName[2:]
        mesg = "Sensor "+event.source.systemName
        if (event.source.userName != None) :
            mesg += " ("+event.source.userName+")"
        mesg += " from "+stateName(event.oldValue)
        mesg += " to "+stateName(event.newValue)

        #look for block based on our naming convention, and speak some info if found
        block_name = "ILB" + sensor_num
        b = blocks.getByUserName(block_name)
        if (b != None) :
            block_length = str(round(b.getLengthIn())).rstrip('0').rstrip('.') + " inches "
            mesg += " for block " + block_name + ", " + block_length
            #only speak for occupied blocks (cuts down on noise level)
            if (b.getState() == jmri.Block.OCCUPIED) :
                spoken_mesg    = b.userName + " "
                #spoken_mesg += cvtBlockStateToText(b.getState()) + ", "
                spoken_mesg += block_length
                speak(spoken_mesg)
        print mesg
    return
    
listener = SensorListener()

# Define a Manager listener.  When invoked, a new
# item has been added, so go through the list of items removing the 
# old listener and adding a new one (works for both already registered
# and new sensors)
class ManagerListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    list = event.source.getNamedBeanSet()
    for sensor in list :
        sensor.removePropertyChangeListener(listener)
        sensor.addPropertyChangeListener(listener)

# Attach the sensor manager listener
sensors.addPropertyChangeListener(ManagerListener())

# For the sensors that exist, attach a sensor listener
list = sensors.getNamedBeanSet()
for i in list :
    sensor.addPropertyChangeListener(listener)

speak("block occupancy announcer started")

 
