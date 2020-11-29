# Simple Event Listener to translate a group of 8 sensor "bits" into a single number
# In this example, 8 bits are transformed into a number put in a memory

# Jerry Grochow, copyright 2020

# Based on listenerexample.py by: Bob Jacobsen, copyright 2004. Part of the JMRI distribution

import java
import java.beans
import jmri

# Define the listener
class MyListener(java.beans.PropertyChangeListener):


  def propertyChange(self, event):

    # Create an array to translate sensor state (used as an index to the array) back into a bit stream:
    #   sensor active = 2 so statetonum[2] is set to a 1; all other entries are set to 0
    statetonum = [0, 0, 1, 0, 0]

    #Sensor name array 
    sensorName = ["CS5041", "CS5042", "CS5043", "CS5044", "CS5045", "CS5046", "CS5047", "CS5048"]


    print "Change:",event.propertyName, " from", event.oldValue, "to", event.newValue
    print " Source systemName: ", event.source.systemName, " userName: ", event.source.userName

  
    #Get the memory where the result is to be stored
    mem = memories.getMemory("IM5001")
    if mem is None:
      print "IM5001 does not exist"
      return
    mem.setComment("Speed Memory")

  
    #Check that the sensors exist 
    sXall = 0
    sX = []
    i = -1
    for sName in sensorName:
      i += 1
      sX.append(sensors.getSensor(sName))
      if sX[i] is None:
        print sName, " does not exist"
        sXall = 1
      else:
        sX[i].setComment("Speed bit " + str(i))
    if sXall != 0:
      return

    #Create bits from states 
    sBitVal = []   
    for s in sX:   
      sBitVal.append(statetonum[s.state])
  
    print sBitVal
    mem.value = sBitVal[0] + sBitVal[1] * 2 + sBitVal[2] * 4 + sBitVal[3] * 8 + sBitVal[4] * 16 + sBitVal[5] * 32 + sBitVal[6] * 64 + sBitVal[7] * 128
    print "Memory speed set", mem.value
    print " "

    return 
 

#Attach that listener to a particular turnout. The variable is used to remember the listener so we can remove it later
lstnr5 = MyListener()
sensors.getSensor("CS5002").addPropertyChangeListener(lstnr5)  #Listen on two sensors in this case
sensors.getSensor("CS5003").addPropertyChangeListener(lstnr5)
print  "Listener 5 set on CS5002 CS5003"

#remove the listener
#lstnr5.removePropertyChangeListener()
