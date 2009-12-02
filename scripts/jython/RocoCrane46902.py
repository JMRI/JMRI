# Script to operate a Roco crane from screen
#
# The crane DCC addresses are set near the top of the code, and should
# be modified to match your crane setup
# This is for the newer Roco crane (part number 46902 (DCC))and CV53 must be set to 2
# Functions are as follows
	# Rotating the crane:
	# CV53 = 02: ­F3
	# LED (GREEN) flashes in single-cycle interval
	# Raising and lowering the Boom:
	# CV53 = 02: ­F2
	# LED (GREEN) flashes in two-cycle interval
	# Raising and lowering the crane hook
	# CV53 = 02: ,F1
	# LED (GREEN) flashes in three-cycle interval
	# Additional function Magnet, Bucket or Spotlight
	# CV53 = 02: F0
	# LED lights up RED
#
# Author: Bob Jacobsen, copyright 2009
# Modified By Nelson Allison
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $

import java
import javax.swing

class RocoCrane(jmri.jmrit.automat.AbstractAutomaton) :
        
    # init() is called exactly once at the beginning to do
    # any necessary configuration.
    def init(self):
    
        # define crane addresses default address is 6
        Addr = 6

        self.status.text = "Getting throttle"
        self.Throttle = self.getThrottle(Addr, True)
        if (self.Throttle == None) :
            print "Couldn't assign throttle!"
	else :
	    self.status.text ="Crane Ready"
	return
        
    # handle() will only execute once here, and doesn't do much
    #
    # Modify this to do your calculation.
    def handle(self):

        # and stop
        return 0

    # define what each button does when clicked
    def whenaccessoryButtonClicked(self,event) :
        if (self.accessoryButton.isSelected()) :
            self.Throttle.setF0(True)
            self.status.text = "Accessory on"
        else :
            self.Throttle.setF0(False)
            self.status.text = "Accessory off"
        return

    def startRotateRight(self,event) :
        self.Throttle.setF3(True)
	self.Throttle.setIsForward(True)
	self.Throttle.speedSetting = 0.4
        self.status.text = "Rotating Right"
        return
    def endRotateRight(self,event) :
        self.Throttle.setF3(False)
	self.Throttle.speedSetting = 0.
        self.status.text = "Rotate off"
        return

    def startRotateLeft(self,event) :
        self.Throttle.setF3(True)
        self.Throttle.setIsForward(False)
	self.Throttle.speedSetting = 0.4
	self.status.text = "Rotating Left"
        return
    def endRotateLeft(self,event) :
        self.Throttle.setF3(False)
	self.Throttle.speedSetting = 0.
        self.status.text = "Rotate off"
        return

    def startBoomUp(self,event) :
        self.Throttle.setF2(True)
	self.Throttle.setIsForward(True)
	self.Throttle.speedSetting = 0.4
        self.status.text = "Rasing Boom"
        return
    def endBoomUp(self,event) :
        self.Throttle.setF2(False)
	self.Throttle.speedSetting = 0.
        self.status.text = "Boom off"
        return

    def startBoomDown(self,event) :
        self.Throttle.setF2(True)
	self.Throttle.setIsForward(False)
	self.Throttle.speedSetting = 0.4
        self.status.text = "Lowering Boom"
        return
    def endBoomDown(self,event) :
        self.Throttle.setF2(False)
	self.Throttle.speedSetting = 0.
        self.status.text = "Boom off"
        return
	
    def startHookUp(self,event) :
        self.Throttle.setF1(True)
	self.Throttle.setIsForward(True)
	self.Throttle.speedSetting = 0.4
        self.status.text = "Rasing Hook"
        return
    def endHookUp(self,event) :
        self.Throttle.setF1(False)
	self.Throttle.speedSetting = 0.
        self.status.text = "Hook off"
        return

    def startHookDown(self,event) :
        self.Throttle.setF1(True)
	self.Throttle.setIsForward(False)
	self.Throttle.speedSetting = 0.4
        self.status.text = "Lowering Hook"
        return
    def endHookDown(self,event) :
        self.Throttle.setF1(False)
	self.Throttle.speedSetting = 0.
        self.status.text = "Hook off"
        return

    # routine to show the panel, starting the whole process     
    def setup(self):
        # create a frame to hold the button, set up for nice layout
        f = javax.swing.JFrame("Roco Crane")       # argument is the frames title
        f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.Y_AXIS))

        # create the buttons

        self.accessoryButton = javax.swing.JToggleButton("Accessory")
        self.accessoryButton.itemStateChanged = self.whenaccessoryButtonClicked
        
        self.rotateLeftButton = javax.swing.JButton("Rotate Left")
        self.rotateLeftButton.mousePressed = self.startRotateLeft
        self.rotateLeftButton.mouseReleased = self.endRotateLeft
        self.rotateRightButton = javax.swing.JButton("Rotate Right")
        self.rotateRightButton.mousePressed = self.startRotateRight
        self.rotateRightButton.mouseReleased = self.endRotateRight

        self.BoomUpButton = javax.swing.JButton("Boom Up")
        self.BoomUpButton.mousePressed = self.startBoomUp
        self.BoomUpButton.mouseReleased = self.endBoomUp
        self.BoomDownButton = javax.swing.JButton("Boom Down")
        self.BoomDownButton.mousePressed = self.startBoomDown
        self.BoomDownButton.mouseReleased = self.endBoomDown
	
	self.HookUpButton = javax.swing.JButton("Hook Up")
        self.HookUpButton.mousePressed = self.startHookUp
        self.HookUpButton.mouseReleased = self.endHookUp
        self.HookDownButton = javax.swing.JButton("Hook Down")
        self.HookDownButton.mousePressed = self.startHookDown
        self.HookDownButton.mouseReleased = self.endHookDown

        # add the status field
        self.status = javax.swing.JLabel("Ready to start")
        
        # Put contents in frame and display
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        f.contentPane.add(temppanel)
        
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.rotateLeftButton)
        temppanel.add(self.rotateRightButton)
        f.contentPane.add(temppanel)

        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.BoomUpButton)
        temppanel.add(self.BoomDownButton)
        f.contentPane.add(temppanel)
	
	temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.HookUpButton)
        temppanel.add(self.HookDownButton)
        f.contentPane.add(temppanel)
        
	temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.accessoryButton)
        f.contentPane.add(temppanel)

        f.contentPane.add(self.status)
        f.pack()
        f.show()
        
        # get throttles
        self.start()
        return

# create one of these
a = RocoCrane()

# set the name
a.setName("Crane setup")

# and show the initial panel
a.setup()


