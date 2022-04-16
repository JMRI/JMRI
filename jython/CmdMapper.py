# Map Turnout commands from one interface to another
#
# Author: Luca Dentella
# From:   https://github.com/lucadentella/jmri-cmdmapper

import java
import java.beans
import java.awt
import javax.swing
import jmri

# CmdMapper class, listens for changes with Loconet turnouts
class CmdMapper(java.beans.PropertyChangeListener):

    def propertyChange(self, event):

        if(event.propertyName == "CommandedState"):

            targetTurnoutName = 'D' + event.source.systemName[1:]

            print "Received commanded state " + str(event.newValue) + " for turnout " + event.source.systemName
            print "Mapping the command to target turnout: " + targetTurnoutName

            targetTurnout = turnouts.getBySystemName(targetTurnoutName)
            if(targetTurnout is not None):
                targetTurnout.commandedState = event.newValue
                print "Target turnout status updated :)"
            else:
                print "Unable to find target turnout :("

            print


# create cmdMapper instance
cmdMapper = CmdMapper()

# button events (onclick)

def startMapping(event):

    for turnout in turnouts.getNamedBeanSet():
        turnoutSystemName = turnout.getSystemName()
        turnoutPrefix = turnoutSystemName[0]
        if(turnoutPrefix == 'L'):
            turnout.addPropertyChangeListener(cmdMapper)
            print turnoutSystemName + " linked to CmdMapper"

def stopMapping(event):

    for turnout in turnouts.getNamedBeanSet():
        turnoutSystemName = turnout.getSystemName()
        turnoutPrefix = turnoutSystemName[0]
        if(turnoutPrefix == 'L'):
            turnout.removePropertyChangeListener(cmdMapper)
            print turnoutSystemName + " unlinked from CmdMapper"


# Create GUI with two buttons to start/stop command mapping

startButton = javax.swing.JButton("Start command mapping")
startButton.actionPerformed = startMapping

stopButton = javax.swing.JButton("Stop command mapping")
stopButton.actionPerformed = stopMapping

f = javax.swing.JFrame("CmdMapper")
f.contentPane.setLayout(java.awt.FlowLayout())
f.contentPane.add(startButton)
f.contentPane.add(stopButton)
f.pack()
f.show()
