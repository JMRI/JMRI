# Script to operate a Heljan crane from screen
#
# The crane DCC addresses are set near the top of the code, and should
# be modified to match your crane setup.
#
# Author: Bob Jacobsen, copyright 2009
# Part of the JMRI distribution

import jmri

import java
import java.awt
import javax.swing

class HeljanCrane(jmri.jmrit.automat.AbstractAutomaton) :
        
    # init() is called exactly once at the beginning to do
    # any necessary configuration.
    def init(self):
    
        # define crane addresses
        loAddr = 80
        hiAddr = 81

        self.status.text = "Getting throttle"
        self.loThrottle = self.getThrottle(loAddr, False)
        if (self.loThrottle == None) :
            print "Couldn't assign low throttle!"
        self.hiThrottle = self.getThrottle(hiAddr, False)
        if (self.hiThrottle == None) :
            print "Couldn't assign high throttle!"
        return
        
    # handle() will only execute once here, and doesn't do much
    #
    # Modify this to do your calculation.
    def handle(self):

        # and stop
        return 0

    # define what each button does when clicked
    def whenSpotLightButtonClicked(self,event) :
        if (self.spotLightButton.isSelected()) :
            self.loThrottle.setF2(True)
            self.status.text = "Low F2 set on"
        else :
            self.loThrottle.setF2(False)
            self.status.text = "Low F2 set off"
        return
    
    def whenFloodLightButtonClicked(self,event) :
        if (self.floodLightButton.isSelected()) :
            self.hiThrottle.setF3(True)
            self.status.text = "Hi F3 set on"
        else :
            self.hiThrottle.setF3(False)
            self.status.text = "Hi F3 set off"
        return
    
    def whenMagnetButtonClicked(self,event) :
        if (self.magnetButton.isSelected()) :
            self.loThrottle.setF1(True)
            self.status.text = "Low F1 set on"
        else :
            self.loThrottle.setF1(False)
            self.status.text = "Low F1 set off"
        return
    
    def startMoveForward(self,event) :
        self.hiThrottle.setIsForward(True)
        self.hiThrottle.speedSetting = 0.5
        self.status.text = "Start go fwd"
        return
    def startMoveReverse(self,event) :
        self.hiThrottle.setIsForward(False)
        self.hiThrottle.speedSetting = 0.5
        self.status.text = "Start go rev"
        return
    def endStopFR(self,event) :
        self.hiThrottle.speedSetting = 0.
        self.status.text = "End go fwd/rev"
        return

    def startMoveLeft(self,event) :
        self.loThrottle.setIsForward(True)
        self.loThrottle.speedSetting = 0.5
        self.status.text = "Start go left"
        return
    def startMoveRight(self,event) :
        self.loThrottle.setIsForward(False)
        self.loThrottle.speedSetting = 0.5
        self.status.text = "Start go right"
        return
    def endStopLR(self,event) :
        self.loThrottle.speedSetting = 0.
        self.status.text = "End go left/right"
        return

    def startRotateRight(self,event) :
        self.loThrottle.setF6(True)
        self.status.text = "Low F6 set ofn"
        return
    def endRotateRight(self,event) :
        self.loThrottle.setF6(False)
        self.status.text = "Low F6 set off"
        return

    def startRotateLeft(self,event) :
        self.loThrottle.setF5(True)
        self.status.text = "Low F5 set ofn"
        return
    def endRotateLeft(self,event) :
        self.loThrottle.setF5(False)
        self.status.text = "Low F5 set off"
        return

    def startLiftUp(self,event) :
        self.loThrottle.setF7(True)
        self.status.text = "Low F7 set ofn"
        return
    def endLiftUp(self,event) :
        self.loThrottle.setF7(False)
        self.status.text = "Low F7 set off"
        return

    def startLiftDown(self,event) :
        self.loThrottle.setF8(True)
        self.status.text = "Low F8 set ofn"
        return
    def endLiftDown(self,event) :
        self.loThrottle.setF8(False)
        self.status.text = "Low F8 set off"
        return


    # routine to show the panel, starting the whole process     
    def setup(self):
        # create a frame to hold the button, set up for nice layout
        f = javax.swing.JFrame("Crane Control")       # argument is the frames title
        f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.Y_AXIS))

        # create the buttons
        self.spotLightButton = javax.swing.JToggleButton("Spot Light")
        self.spotLightButton.itemStateChanged = self.whenSpotLightButtonClicked

        self.floodLightButton = javax.swing.JToggleButton("Flood Light")
        self.floodLightButton.itemStateChanged = self.whenFloodLightButtonClicked

        self.magnetButton = javax.swing.JToggleButton("Magnet")
        self.magnetButton.itemStateChanged = self.whenMagnetButtonClicked

        self.goFwdButton = javax.swing.JButton("Fwd")
        self.goFwdButton.mousePressed = self.startMoveForward
        self.goFwdButton.mouseReleased = self.endStopFR
        self.goRevButton = javax.swing.JButton("Rev")
        self.goRevButton.mousePressed = self.startMoveReverse
        self.goRevButton.mouseReleased = self.endStopFR
        
        self.goLeftButton = javax.swing.JButton("Left")
        self.goLeftButton.mousePressed = self.startMoveLeft
        self.goLeftButton.mouseReleased = self.endStopLR
        self.goRightButton = javax.swing.JButton("Right")
        self.goRightButton.mousePressed = self.startMoveRight
        self.goRightButton.mouseReleased = self.endStopLR

        self.rotateLeftButton = javax.swing.JButton("Rotate Left")
        self.rotateLeftButton.mousePressed = self.startRotateLeft
        self.rotateLeftButton.mouseReleased = self.endRotateLeft
        self.rotateRightButton = javax.swing.JButton("Rotate Right")
        self.rotateRightButton.mousePressed = self.startRotateRight
        self.rotateRightButton.mouseReleased = self.endRotateRight

        self.liftUpButton = javax.swing.JButton("Lift Up")
        self.liftUpButton.mousePressed = self.startLiftUp
        self.liftUpButton.mouseReleased = self.endLiftUp
        self.liftDownButton = javax.swing.JButton("Lift Down")
        self.liftDownButton.mousePressed = self.startLiftDown
        self.liftDownButton.mouseReleased = self.endLiftDown

        # add the status field
        self.status = javax.swing.JLabel("Ready to start")
        
        # Put contents in frame and display
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.goFwdButton)
        temppanel.add(self.goRevButton)
        temppanel.add(self.goLeftButton)
        temppanel.add(self.goRightButton)
        f.contentPane.add(temppanel)
        
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.rotateLeftButton)
        temppanel.add(self.rotateRightButton)
        f.contentPane.add(temppanel)

        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.liftUpButton)
        temppanel.add(self.liftDownButton)
        f.contentPane.add(temppanel)

        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.spotLightButton)
        temppanel.add(self.floodLightButton)
        temppanel.add(self.magnetButton)
        f.contentPane.add(temppanel)

        f.contentPane.add(self.status)
        f.pack()
        f.show()
        
        # get throttles
        self.start()
        return

# create one of these
a = HeljanCrane()

# set the name
a.setName("Crane setup")

# and show the initial panel
a.setup()
