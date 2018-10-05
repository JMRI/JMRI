# Script to operate a turntable via a DCC decoder
#
# The DCC address and speed are set near the top of the code, and should
# be modified to match your setup.
#
# Author: Bob Jacobsen, copyright 2009
# Part of the JMRI distribution
#

import java
import java.awt
import javax.swing
import jmri

class TurntableDCC(jmri.jmrit.automat.AbstractAutomaton) :
        
    # init() is called exactly once at the beginning to do
    # any necessary configuration.
    def init(self):
    
        # define address and speed
        addr = 82
        self.speed = 7./28.

        self.status.text = "Getting throttle"
        self.throttle = self.getThrottle(addr, False)
        if (self.throttle == None) :
            print "Couldn't assign throttle!"
        self.status.text = "Ready"
        return
        
    # handle() will only execute once here, and doesn't do much
    #
    # Modify this to do your calculation.
    def handle(self):

        # and stop
        return 0

    # define what each button does when clicked
    def startMoveForward(self,event) :
        self.throttle.setIsForward(True)
        self.throttle.speedSetting = self.speed
        self.status.text = "Go fwd"
        return
    def startMoveReverse(self,event) :
        self.throttle.setIsForward(False)
        self.throttle.speedSetting = self.speed
        self.status.text = "Go rev"
        return
    def endStopFR(self,event) :
        self.throttle.speedSetting = 0.
        self.status.text = "Stop"
        return


    # routine to show the panel, starting the whole process     
    def setup(self):
        # create a frame to hold the button, set up for nice layout
        f = javax.swing.JFrame("Crane Control")       # argument is the frames title
        f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.Y_AXIS))

        # create the buttons
        self.goFwdButton = javax.swing.JButton("Fwd")
        self.goFwdButton.mousePressed = self.startMoveForward
        
        self.stopButton = javax.swing.JButton("Stop")
        self.stopButton.mousePressed = self.endStopFR
        self.stopButton.mouseReleased = self.endStopFR
        
        self.goRevButton = javax.swing.JButton("Rev")
        self.goRevButton.mousePressed = self.startMoveReverse
        
        # add the status field
        self.status = javax.swing.JLabel("Ready to start")
        
        # Put contents in frame and display
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.goFwdButton)
        temppanel.add(self.stopButton)
        temppanel.add(self.goRevButton)
        f.contentPane.add(temppanel)
        
        f.contentPane.add(self.status)
        f.pack()
        f.show()
        
        # get throttles
        self.start()
        return

# create one of these
a = TurntableDCC()

# set the name
a.setName("Turntable setup")

# and show the initial panel
a.setup()
