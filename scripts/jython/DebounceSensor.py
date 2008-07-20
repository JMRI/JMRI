# This script manages an internal sensor as a debounced
# version of another sensor. Both the on and off delays
# may be specified.
#
# Author: Ken Cameron, copyright 2008
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $
#
# A WindowListener is used to report when the window is closing and
# allow for the removal of the PropertyChangeListener.
#
# A ActionListener is used to get timeout events.
#
# A PropertyChangeListener is used to get the sensor changes.
#
# if you don't open a display (a.setup()) then it will put minimal
# messages to the console
#
# Suggested

import java
import javax.swing

# declare things
class DebounceSensor(java.beans.PropertyChangeListener) :
    # initialize variables
    watchedSensor = None
    resultSensor = None
    delayTimer = None
    offTimeout = 0
    onTimeout = 0
    priorInput = None
    currentState = None
    isChanging = False
    timeoutListener = None
    hasDisplay = False

    def init(self, inSensor, outSensor, onDelay, offDelay):
       #self.msgText("Getting real sensor\n") #add text to scroll field
        self.watchedSensor = sensors.getSensor(inSensor)
        if (self.watchedSensor == None) :
           self.msgText("Couldn't assign inSensor: " + inSensor + " - Run stopped\n")
           return
        else : 
           self.msgText("got inSensor: " + inSensor  + "\n")
        self.resultSensor = sensors.getSensor(outSensor)
        if (self.resultSensor == None) :
           self.msgText("Couldn't assign outSensor: " + outSensor + " - Run stopped\n")
           return
        else : 
           self.msgText("got outSensor: " + outSensor + "\n")
        self.onTimeout = int(onDelay * 1000.0)
        self.offTimeout = int(offDelay * 1000.0)
       #self.msgText("onTimeout: " + self.onTimeout.toString() + "\n")
       #self.msgText("offTimeout: " + self.offTimeout.toString() + "\n")
        self.currentState = self.watchedSensor.getKnownState()
        self.priorInput = self.currentState
        self.resultSensor.setKnownState(self.currentState)
        self.watchedSensor.addPropertyChangeListener(self)
        self.timeoutListener = self.TimeoutReceiver()
        self.timeoutListener.setCallBack(self.timeoutHandler)
        self.delayTimer = javax.swing.Timer(0, self.timeoutListener);
        self.delayTimer.setRepeats(False);
        return
        
    def propertyChange(self, event) :
        #self.msgText("handle begin:\n")
        prop = event.getPropertyName()
        old = event.getOldValue.toString()
        new = event.getNewValue.toString()
        sys = event.getSource.toString()
        newState = self.watchedSensor.getKnownState()
        #self.msgText("newState: " + newState.toString() + "\n")
        #self.msgText("currentState: " + self.currentState.toString() + "\n")
        if (newState != self.currentState) :
           #self.msgText("Found states changed\n")
            if (self.isChanging) :
                self.delayTimer.stop()
                self.isChanging = False
               #self.msgText("Canceled change\n")
            else :
                if (self.currentState == INACTIVE) :
                    self.changeOffToOn()
                else :
                    self.changeOnToOff()
                self.isChanging = True
        #self.msgText("handle done\n")
        return

    def changeOnToOff(self) :
       #self.msgText("changeOnToOff\n")
        if (self.offTimeout != 0) :
            self.delayTimer.setDelay(self.offTimeout)
            self.delayTimer.setInitialDelay(self.offTimeout)
            self.delayTimer.start()
           #self.msgText("Started off timer(" + self.offTimeout.toString() + ")\n")
        else :
            self.timeoutOnToOff(None)
       #self.msgText("changeOnToOff finished\n")
        return

    def changeOffToOn(self) :
       #self.msgText("changeOffToOn\n")
        if (self.onTimeout != 0) :
            self.delayTimer.setDelay(self.onTimeout)
            self.delayTimer.setInitialDelay(self.onTimeout)
            self.delayTimer.start()
           #self.msgText("Started on timer(" + self.onTimeout.toString() + ")\n")
        else :
            self.timeoutOffToOn(None)
       #self.msgText("changeOffToOn finished\n")
        return

    class TimeoutReceiver(java.awt.event.ActionListener):
        cb = None

        def actionPerformed(self, event) :
            if (self.cb != None) :
                self.cb(event)
            return
        
        def setCallBack(self, cbf) :
            self.cb = cbf
            return

    def timeoutHandler(self, event) :
       #self.msgText("timeoutHandler begin\n")
        if (self.currentState == ACTIVE) :
            self.timeoutOnToOff()
        else :
            self.timeoutOffToOn()
       #self.msgText("timeoutHandler end\n")
        return

    def timeoutOnToOff(self) :
       #self.msgText("timeoutOnToOff\n")
        if (self.watchedSensor.getKnownState() == INACTIVE) :
            self.resultSensor.setKnownState(INACTIVE)
            self.currentState = INACTIVE
            self.delayTimer.stop()
        self.isChanging = False
        return

    def timeoutOffToOn(self) :
       #self.msgText("timeoutOffToOn\n")
        if (self.watchedSensor.getKnownState() == ACTIVE) :
            self.resultSensor.setKnownState(ACTIVE)
            self.currentState = ACTIVE
            self.delayTimer.stop()
        self.isChanging = False
        return

    # WindowListener is a interface class and therefore all of it's
    # methods should be implemented even if not used to avoid AttributeErrors
    class WinListener(java.awt.event.WindowListener):
        f = None
        cleanUp = None

        def setCallBack(self, fr, c):
            self.f = fr
            self.cleanUp = c
            return
        
        def windowClosing(self, event):
            if (self.cleanUp != None) :
                self.cleanUp(event)
            self.f.dispose()         # close the pane (window)
            return
            
        def windowActivated(self,event):
            return

        def windowDeactivated(self,event):
            return

        def windowOpened(self,event):
            return

        def windowClosed(self,event):
            return
     
    # handle adding to message window
    def msgText(self, txt) :
        if (self.hasDisplay == True) :
            self.scrollArea.append(txt)
            if (self.autoScroll.isSelected() == True) :
                self.scrollArea.setCaretPosition(self.scrollArea.getDocument().getLength())
        else :
            print(txt)
        return

    def whenStartButtonClicked(self, event):   
       #self.msgText("start clicked\n")     # add text
        return
    
    def whenStopButtonClicked(self, event):   
       #self.msgText("stop clicked\n")     # add text
        self.watchedSensor.removePropertyChangeListener(self)
        return
    
    # cleanup
    def cleanup(self, event) :
        return
    
    # setup the user interface
    def setup(self) :
        # start to initialise the GUI
        self.hasDisplay = True
        # create buttons and define action
        self.enterButton = javax.swing.JButton("Start the Run")
        self.enterButton.actionPerformed = self.whenStartButtonClicked
        
        self.stopButton = javax.swing.JButton("Stop")
        self.stopButton.setToolTipText("Stops the run")
        self.stopButton.actionPerformed = self.whenStopButtonClicked
        
        # auto-scroll message window flag
        self.autoScroll = javax.swing.JCheckBox()
        self.autoScroll.setToolTipText("Sets message window to auto-scroll")
        self.autoScroll.setSelected(True)        
        
        # create a text area
        self.scrollArea = javax.swing.JTextArea(15, 70)    # define a text area with it's size
        srcollField = javax.swing.JScrollPane(self.scrollArea) # put text area in scroll field
        
        # create a frame to hold the buttons and fields
        # also create a window listener. This is used mainly to remove the property change listener
        # when the window is closed by clicking on the window close button
        w = self.WinListener()
        self.scriptFrame = javax.swing.JFrame("Debounce Test")       # argument is the frames title
        self.scriptFrame.contentPane.setLayout(javax.swing.BoxLayout(self.scriptFrame.contentPane, javax.swing.BoxLayout.Y_AXIS))
        self.scriptFrame.addWindowListener(w)
        w.setCallBack(self.scriptFrame, self.cleanup)
        
        temppanel3 = javax.swing.JPanel()
        temppanel3.add(javax.swing.JLabel(" AutoScroll Messages: "))
        temppanel3.add(self.autoScroll)

        butPanel = javax.swing.JPanel()
        butPanel.add(self.enterButton)
        butPanel.add(self.stopButton)


        # Put contents in frame and display
        self.scriptFrame.contentPane.add(temppanel3)

        self.scriptFrame.contentPane.add(srcollField)
        self.scriptFrame.contentPane.add(butPanel)
        self.scriptFrame.pack()
        self.scriptFrame.show()
        return
        
# invoke this script from the system directory
# then create a file (with your panels) that has
# lines like these (without the ## comments)
# Params:
#    real sensor being watched for changes
#    sensor showing the debounced state of real sensor
#    on delay, off-on-off cycles less than this will stay off
#    off delay, on-off-on cycles less than this will stay on
#
# Sensor names are system names
# time values are floating point seconds

##a = DebounceSensor() # create one of these 
#a.setup()           # only invoke this is you want to watch 
##a.init('NS775', 'ISNS775', 0.5, 3) # invoke this for the sensor pair