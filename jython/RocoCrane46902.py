# Script to operate a Roco crane from screen
#
# The crane DCC addresses are set by the user in the main window
# This is for the newer Roco crane (part number 46902 (DCC))and CV53 must be set to 2
# Functions are as follows
    # Rotating the crane:
    # CV53 = 02: not equal F3
    # LED (GREEN) flashes in single-cycle interval
    # Raising and lowering the Boom:
    # CV53 = 02: not equal F2
    # LED (GREEN) flashes in two-cycle interval
    # Raising and lowering the crane hook
    # CV53 = 02: ,F1
    # LED (GREEN) flashes in three-cycle interval
    # Additional function Magnet, Bucket or Spotlight
    # CV53 = 02: F0
    # LED lights up RED
#
# Author: Nelson Allison, copyright 2009
# With help from Bob Jacobsen
# Part of the JMRI distribution

import jmri
import java
import java.awt
import javax.swing

class RocoCrane(jmri.jmrit.automat.AbstractAutomaton) :
        
    # init() is called exactly once at the beginning to do
    # any necessary configuration.
    def init(self):

        self.status.text = "Getting throttle"
        Addr = int(self.Address.text)
    if (Addr > 100) :
        long = True
    else :
        long = False
        self.Throttle = self.getThrottle(Addr, long)
        if (self.Throttle == None) :
             print "Couldn't assign throttle!"
    else :
             self.status.text ="Crane Ready"
        return

    def handle(self):

        # and stop
        return 0

    # define what each button does when clicked
    def whenStartButtonClicked(self,event) :
        self.start()
        self.Address.enabled = False
        self.StartButton.enabled = False
    self.ReleaseButton.enabled = True
        self.AccessoryButton.enabled = True
        self.RotateLeftButton.enabled = True
        self.RotateRightButton.enabled = True
        self.BoomDownButton.enabled = True
        self.BoomUpButton.enabled = True
        self.HookDownButton.enabled = True
        self.HookUpButton.enabled = True
        return
    
    def whenReleaseButtonClicked(self,event) :
    self.Throttle.release(None)
        self.status = javax.swing.JLabel("Enter address & click start")
        self.Address.enabled = True
        self.StartButton.enabled = True
    self.ReleaseButton.enabled = False
        self.AccessoryButton.enabled = False
        self.RotateLeftButton.enabled = False
        self.RotateRightButton.enabled = False
        self.BoomDownButton.enabled = False
        self.BoomUpButton.enabled = False
        self.HookDownButton.enabled = False
        self.HookUpButton.enabled = False
    return
    
    def whenAccessoryButtonClicked(self,event) :
        if (self.AccessoryButton.isSelected()) :
            self.status.text = "Accessory on"
            self.Throttle.setF0(True)
        else :
            self.status.text = "Accessory off"
            self.Throttle.setF0(False)
        return

    def startRotateRight(self,event) :
        self.status.text = "Rotating Right"
        self.Throttle.setF3(True)
        self.Throttle.setIsForward(True)
        self.Throttle.speedSetting = 0.5
        return
    def endRotateRight(self,event) :
        self.status.text = "Rotation Stopped"
        self.Throttle.setF3(False)
        self.Throttle.speedSetting = 0.
        return

    def startRotateLeft(self,event) :
        self.status.text = "Rotating Left"
        self.Throttle.setF3(True)
        self.Throttle.setIsForward(False)
        self.Throttle.speedSetting = 0.5
        return
    def endRotateLeft(self,event) :
        self.status.text = "Rotation Stopped"
        self.Throttle.setF3(False)
        self.Throttle.speedSetting = 0.
        return

    def startBoomUp(self,event) :
        self.status.text = "Rasing Boom"
        self.Throttle.setF2(True)
        self.Throttle.setIsForward(True)
        self.Throttle.speedSetting = 0.5
        return
    def endBoomUp(self,event) :
        self.status.text = "Boom Stopped"
        self.Throttle.setF2(False)
        self.Throttle.speedSetting = 0.
        return

    def startBoomDown(self,event) :
        self.status.text = "Lowering Boom"
        self.Throttle.setF2(True)
        self.Throttle.setIsForward(False)
        self.Throttle.speedSetting = 0.5
        return
    def endBoomDown(self,event) :
        self.status.text = "Boom Stopped"
        self.Throttle.setF2(False)
        self.Throttle.speedSetting = 0.
        return
    
    def startHookUp(self,event) :
        self.status.text = "Rasing Hook"
        self.Throttle.setF1(True)
        self.Throttle.setIsForward(True)
        self.Throttle.speedSetting = 0.5
        return
    def endHookUp(self,event) :
        self.status.text = "Hook Stopped"
        self.Throttle.setF1(False)
        self.Throttle.speedSetting = 0.
        return

    def startHookDown(self,event) :
        self.status.text = "Lowering Hook"
        self.Throttle.setF1(True)
        self.Throttle.setIsForward(False)
        self.Throttle.speedSetting = 0.5
        return
    def endHookDown(self,event) :
        self.status.text = "Hook Stopped"
        self.Throttle.setF1(False)
        self.Throttle.speedSetting = 0.
        return
    
    # routine to show the panel, starting the whole process     
    def setup(self):
        # create a frame to hold the button, set up for nice layout
        f = javax.swing.JFrame("Roco Crane")       # argument is the frames title
        f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.Y_AXIS))
        
    # create the text field
        self.Address = javax.swing.JTextField(5)    # sized to hold 5 characters, initially empty
    
    # create the buttons
        self.StartButton = javax.swing.JButton("Start")
        self.StartButton.actionPerformed = self.whenStartButtonClicked
    
    self.ReleaseButton = javax.swing.JButton("Release")
    self.ReleaseButton.actionPerformed = self.whenReleaseButtonClicked

        self.AccessoryButton = javax.swing.JToggleButton("Accessory")
        self.AccessoryButton.itemStateChanged = self.whenAccessoryButtonClicked
    
        self.RotateLeftButton = javax.swing.JButton("Rotate Left")
        self.RotateLeftButton.mousePressed = self.startRotateLeft
        self.RotateLeftButton.mouseReleased = self.endRotateLeft

        self.RotateRightButton = javax.swing.JButton("Rotate Right")
        self.RotateRightButton.mousePressed = self.startRotateRight
        self.RotateRightButton.mouseReleased = self.endRotateRight

        self.BoomDownButton = javax.swing.JButton("Boom Down")
        self.BoomDownButton.mousePressed = self.startBoomDown
        self.BoomDownButton.mouseReleased = self.endBoomDown
        
        self.BoomUpButton = javax.swing.JButton("Boom Up")
        self.BoomUpButton.mousePressed = self.startBoomUp
        self.BoomUpButton.mouseReleased = self.endBoomUp
    
        self.HookDownButton = javax.swing.JButton("Hook Down")
        self.HookDownButton.mousePressed = self.startHookDown
        self.HookDownButton.mouseReleased = self.endHookDown
    
        self.HookUpButton = javax.swing.JButton("Hook Up")
        self.HookUpButton.mousePressed = self.startHookUp
        self.HookUpButton.mouseReleased = self.endHookUp
    
    self.ReleaseButton.enabled = False
        self.AccessoryButton.enabled = False
        self.RotateLeftButton.enabled = False
        self.RotateRightButton.enabled = False
        self.BoomDownButton.enabled = False
        self.BoomUpButton.enabled = False
        self.HookDownButton.enabled = False
        self.HookUpButton.enabled = False
    
        # add the status field
        self.status = javax.swing.JLabel("Enter address & click start")
        
        # Put contents in frame and display
    
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        f.contentPane.add(temppanel)
    
        # put the text field on a line preceded by a label
        temppanel = javax.swing.JPanel()
        temppanel.add(javax.swing.JLabel("Address"))
        temppanel.add(self.Address)
        f.contentPane.add(temppanel)
    
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())  
        temppanel.add(self.StartButton)
    temppanel.add(self.ReleaseButton)
        f.contentPane.add(temppanel)
    
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.RotateLeftButton)
        temppanel.add(self.RotateRightButton)
        f.contentPane.add(temppanel)
    
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.BoomDownButton)
        temppanel.add(self.BoomUpButton)
        f.contentPane.add(temppanel)
    
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.HookDownButton)
        temppanel.add(self.HookUpButton)
        f.contentPane.add(temppanel)
        
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.AccessoryButton)
        f.contentPane.add(temppanel)
    
        f.contentPane.add(self.status)
        f.pack()
        f.show()
        
        return

# create one of these
a = RocoCrane()

# set the name
a.setName("Crane setup")

# and show the initial panel
a.setup()


