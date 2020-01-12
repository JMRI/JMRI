# Script to assist Zimo pseudo programming in Sound decoders.
#
# See CV300 in Zimo Manual.
# CV300 enables a "pseudo programming" mode in a Zimo sound decoder, allowing the user
# to select sounds and set volumes from all of the sounds stored within the decoder.
# For example, one could select a whistle from Loco-1 and a brake sound from Loco-3.
# Once CV300 is set, the Function Keys have special meanings for selecting, playing
# and saving sounds. The speed control is (generally) used to set the volume.
#
# Note that the writer and testers of this script could not get the CV300=100 feature to
# do anything useful (theoretically it selects the chuff sounds), so it is commented out of this version
# To restore this feature, there is one line to edit lower down, in the setup of the window, just remove
# the single comment marker indicated in the comments.
#
# This script presents the options for CV300, then offers function keys and a speed (volume) slider.
#
# # # Consult Zimo Manuals before use   # # #
#
# Requires loco on main line, uses ops mode programming
#
# Version 1.0,  based on Zimo Function Programmer
# Nigel Cliffe, copyright April 2011
# Version 1.1. Nigel Cliffe, Extends function keys for F13 to F19.   December 2012.
#
#
# Components based on Bob Jacobsen's scripts in JMRI distribution.
#

import java
import javax.swing
import jarray
import jmri

class LocoZimoPseudoProg(jmri.jmrit.automat.AbstractAutomaton) :
    # define how long to wait between settings (seconds)
    delay = 2
    # init() is called exactly once at the beginning to do
    # any necessary configuration.
    def init(self):
        self.status.text = "Getting throttle"
        number = int(self.address.text)
        if (number > 100) :
            long = True
        else :
            long = False
        # setup a programmer for ops-mode
        self.programmer = addressedProgrammers.getAddressedProgrammer(long, number)
        self.throttle = self.getThrottle(number, long)
        self.waitMsec(self.delay*1000)
        if (self.throttle == None) :
            print "Couldn't assign throttle!"
        return

    # handle() will only execute once here, to run a single test
    #
    # Modify this to do your calculation.
    def handle(self):
        # prevent running twice by hiding start button.
        self.startButton.enabled = False
        self.box.enabled = False
        
        self.status.text = "Setting loco forward and all functions to off"
        self.throttle.setIsForward(True)
        self.throttle.setF0(False)
        self.throttle.setF1(False)
        self.throttle.setF2(False)
        self.throttle.setF3(False)
        self.throttle.setF4(False)
        self.throttle.setF5(False)
        self.throttle.setF6(False)
        self.throttle.setF7(False)
        # self.throttle.setF8(False)
        self.throttle.setF9(False)
        self.throttle.setF10(False)
        self.throttle.setF11(False)
        self.throttle.setF12(False)
        self.waitMsec(1000)

        # make UI visible
        self.hideShowRadios(True)
        self.hideShowOpsButton(True)
        self.address.enabled = False

        self.status.text = "Select Sound Allocation"
        return 0    
        # use return 1 for an infinite loop, or return 0 for once through !


    # define what button does when clicked and attach that routine to the button
    def whenMyButtonClicked(self,event) :
        self.start()
        return

    def releaseLoco(self,event) :
        # we need to quit and clear down tidily...
        self.hideShowRadios(False)
        self.hideShowOpsButton(False)
        self.throttle.release(None)
        self.address.enabled = True
        self.startButton.enabled = True
        self.box.enabled = True
        return

    def whenF0Changed(self,event) :
        oldstate = self.throttle.getF0()
        if (oldstate == False) :
            self.throttle.setF0(True)
            self.F0box.text = "Stop Sound"
        else:
            self.throttle.setF0(False)
            self.F0box.text = "Play Sound"
        return

    def whenF0On(self,event) :
        self.throttle.setF0(True)
        return
    def whenF0Off(self, event) :
        self.throttle.setF0(False)      
        return

        
    def whenF1On(self,event) :
        self.throttle.setF1(True)
        return
    def whenF1Off(self, event) :
        self.throttle.setF1(False)      
        return

    def whenF2On(self,event) :
        self.throttle.setF2(True)
        return
    def whenF2Off(self, event) :
        self.throttle.setF2(False)      
        return

    def whenF3On(self,event) :
        self.throttle.setF3(True)
        return
    def whenF3Off(self, event) :
        self.throttle.setF3(False)
        self.hideShowFunctionButtons(False)
        self.hideShowSliders(False)
        self.hideShowRadios(True)
        self.hideShowOpsButton(True)
        self.radioChange('Bah')
        self.status.text = "Select Sound Allocation"
        return

    def whenF4On(self,event) :
        self.throttle.setF4(True)
        return
    def whenF4Off(self, event) :
        self.throttle.setF4(False)      
        return

    def whenF5On(self,event) :
        self.throttle.setF5(True)
        return
    def whenF5Off(self, event) :
        self.throttle.setF5(False)      
        return

    def whenF6Changed(self,event) :
        if self.F6box.isSelected():
            self.throttle.setF6(True)
        else:
            self.throttle.setF6(False)
        return

    def whenF7Changed(self,event) :
        if self.F7box.isSelected():
            self.throttle.setF7(True)
        else:
            self.throttle.setF7(False)
        return

    def whenF8On(self,event) :
        self.throttle.setF8(True)
        return
    def whenF8Off(self, event) :
        self.throttle.setF8(False)      
        self.hideShowFunctionButtons(False)
        self.hideShowSliders(False)
        self.hideShowRadios(True)
        self.hideShowOpsButton(True)
        self.radioChange('Bah')
        self.status.text = "Select Sound Allocation"
        return

    def whenQuitChanged(self,event) :
        self.programmer.writeCV("300", 0, None)
        self.hideShowFunctionButtons(False)
        self.hideShowSliders(False)
        self.hideShowRadios(True)
        self.hideShowOpsButton(True)
        self.radioChange('Bah')
        self.status.text = "Select Sound Allocation"
        return

        
    def onSlide(self,event) :
        sender = event.getSource()
        value = sender.getValue()
        self.speedVol.text = str(value)
        self.throttle.setSpeedSetting(float(value)/128)
        return


    def rosterBoxChange(self, event) :
        #print "roster changing in rosterBoxChange"
        entry = self.box.getSelectedItem()
        #print entry
        theDccAddress = entry.getDccAddress()
        # print theDccAddress
        self.address.text = theDccAddress
        return 0

    def comboChange(self, event) :
        # show the function keys
        self.hideShowSliders(True)

        if self.radioBtn1.isSelected():
            selected = self.opsComboBox.selectedIndex
            if selected >= 0:
                data = self.opsCVVal[selected]
                text = self.radioBtn1.text + "  -  " + self.opsCVLabel[selected]
            self.hideShowSliders(False)
            self.F0box.enabled = True
            self.F1box.enabled = True
            self.F2box.enabled = True
            self.F3box.enabled = True
            self.F8box.enabled = True
            self.quitBox.enabled = True
        if self.radioBtn2.isSelected():
            selected = self.opsComboBox2.selectedIndex
            if selected >= 0:
                data = self.opsCVVal2[selected]
                text = self.radioBtn2.text + "  -  " + self.opsCVLabel2[selected]
            self.F0box.enabled = True
            self.F1box.enabled = True
            self.F2box.enabled = True
            self.F3box.enabled = True
            self.F4box.enabled = True
            self.F5box.enabled = True
            self.F8box.enabled = True
            self.quitBox.enabled = True
        if self.radioBtn3.isSelected():
            selected = self.opsComboBox3.selectedIndex
            if selected >= 0:
                data = self.opsCVVal3[selected]
                text = self.radioBtn3.text + "  -  " + self.opsCVLabel3[selected]
            self.hideShowFunctionButtons(True)
            self.F6box.text = "Loop (playable whistle)"
            self.F7box.text = "Short (omit loop)"
        if self.radioBtn4.isSelected():
            selected = self.opsComboBox4.selectedIndex
            if selected >= 0:
                data = self.opsCVVal4[selected]
                text = self.radioBtn4.text + "  -  " + self.opsCVLabel4[selected]
            self.hideShowFunctionButtons(True)
            self.F6box.text = "Standstill             "
            self.F7box.text = "Moving (cruise)  "
        if self.radioBtn5.isSelected():
            selected = self.opsComboBox5.selectedIndex
            if selected >= 0:
                data = self.opsCVVal5[selected]
                text = self.radioBtn5.text + "  -  " + self.opsCVLabel5[selected]
            self.hideShowFunctionButtons(True)
            self.F6box.text = "Standstill             "
            self.F7box.text = "Moving (cruise)  "
        self.cvVal.text = data
        # hide the radio buttons and combos
        self.hideShowCombos(False)
        self.hideShowRadios(False)
        self.hideShowOpsButton(False)
        # setup ops mode on CV300
        self.status.text = "Ops mode set for : " + text
        self.programmer.writeCV("300", int(data), None)
        self.waitMsec(1000)
        
        return 0


    def radioChange(self, event) :
        self.hideShowCombos(False)
        if self.radioBtn1.isSelected():
            self.opsComboBox.enabled = True
        if self.radioBtn2.isSelected():
            self.opsComboBox2.enabled = True
        if self.radioBtn3.isSelected():
            self.opsComboBox3.enabled = True
        if self.radioBtn4.isSelected():
            self.opsComboBox4.enabled = True
        if self.radioBtn5.isSelected():
            self.opsComboBox5.enabled = True
        return 0

    def hideShowRadios(self, state) :
        self.radioBtn1.enabled = state
        self.radioBtn2.enabled = state
        self.radioBtn3.enabled = state
        self.radioBtn4.enabled = state
        self.radioBtn5.enabled = state
        return 0
    def hideShowCombos(self, state) :
        self.opsComboBox.enabled = state
        self.opsComboBox2.enabled = state
        self.opsComboBox3.enabled = state
        self.opsComboBox4.enabled = state
        self.opsComboBox5.enabled = state
        return 0
    def hideShowFunctionButtons(self, state) :
        self.F0box.enabled = state
        self.F1box.enabled = state
        self.F2box.enabled = state
        self.F3box.enabled = state
        self.F4box.enabled = state
        self.F5box.enabled = state
        self.F6box.enabled = state
        self.F7box.enabled = state
        self.F8box.enabled = state
        self.quitBox.enabled = state
        return 0
    def hideShowSliders(self, state) :
        self.slider.enabled=state
        self.speedVol.enabled=False
        return 0
    def hideShowOpsButton(self, state) :
        self.OpsCVButton.enabled=state
        self.ReleaseButton.enabled=state
        self.cvVal.enabled=False
        return 0

    # routine to show the panel, starting the whole process
    def setup(self):
        # create a frame to hold the button, set up for nice layout
        f = javax.swing.JFrame("Zimo Sound Programmer v1.0")       # argument is the frames title
        f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.Y_AXIS))

        # top explanatory panel, text only
        temppanel0 = javax.swing.JPanel()
        temppanel0.add(javax.swing.JLabel("<html>Zimo Pseudo Programmer for Sound Locomotives. Uses Ops Mode programming (loco on main line). See Zimo<BR>manual, chapter 6 on Sound Selection.<BR>This tool provides a menu interface to the various CV300 options, and then buttons to <BR>move between the various sounds.<BR><BR>1 - Select Loco to Program from Roster.</html>"))
        
        

        # put the text field on a line preceded by a label
        temppanel1 = javax.swing.JPanel()
        temppanel1.add(javax.swing.JLabel("Locomotive "))

        # create the text field
        self.address = javax.swing.JTextField(5)    # sized to hold 10 characters, initially empty
        self.startButton = javax.swing.JButton("Start")
        self.startButton.actionPerformed = self.whenMyButtonClicked     

        self.roster = jmri.jmrit.roster.Roster.getDefault()
        self.box = jmri.jmrit.roster.swing.GlobalRosterEntryComboBox()
        self.box.itemStateChanged = self.rosterBoxChange
        temppanel1.add(self.box)
        temppanel1.add(self.address)

        temppanel1.add(self.startButton)

        #self.address.text = "3"
        #entry = self.roster.entryFromTitle(self.box.getSelectedItem())
        #theDccAddress = entry.getDccAddress()
        #self.address.text = theDccAddress

        
        # top explanatory panel, text only
        temppanel1a = javax.swing.JPanel()
        temppanel1a.add(javax.swing.JLabel("<html>press 'Start'</html>"))


        temppanel1c = javax.swing.JPanel()
        temppanel1c.add(javax.swing.JLabel("<html>2 - Sound Allocations <br>Select type of allocation by radio button, then narrow the selection from the drop menu to the right of<br>radio button.</html>"))


        # Put contents in frame and display
        f.contentPane.add(temppanel0)

        f.contentPane.add(temppanel1)
        f.contentPane.add(temppanel1c)

# CV 300 = 100 (only for steam / not possible with DIESEL engines!).
#    ( Not clear what speed controller does with first case )
#
# Selecting boiling, whistle, blow-off and brake squeal sounds: automated background sounds
#  With these, speed control sets volume level.
# CV 300 = 128 for the boiling sound (STEAM only)
# CV 300 = 129 for direction-change sound
# CV 300 = 130 for the brake squeal
# CV 300 = 131 thyristor-control sound (electric engine)
# CV 300 = 132 for the start whistle
# CV 300 = 133 for blow-off sound =cylinder valves (STEAM only)
# NOTE: the blow-off sound selected here is also used as the blow-off sound actuated with a func-
# tion key (see CV #312).
# CV 300 = 134 for the driving sound of ELECTRIC engines
# CV 300 = 136 for the switchgear sound of ELECTRIC engines
#
# Allocating sounds to function keys F0 to F12
# CV 300 = 1 for function F1
# CV 300 = 2 for function F2
# CV 300 = 3 for function F3
#     etc.
# CV 300 = 20 for function F0 (!)
# NB not sure if the key F6 and F7 should be latching in this context see bottom of Page 37 ??
#
# Allocating random sounds
# CV 300 = 101 for random generator Z1
#    (Z1 has special logic incorporated for the compressor and should therefore always be used for that)
# CV 300 = 102 for random generator Z2
# CV 300 = 103 for random generator Z3
#    etc to CV300 = 8 for Z8
# NB not sure if the key F6 and F7 should be latching in this context see bottom of Page 37 ??
#
# Switch inputs; some decoders (eg. MX642 have one switch input, others, eg. MX640 have three)
# CV #300 = 111 for switch input S0   (NB documentation changes from 0,1,2 to 1,2,3 !!).
# CV #300 = 112 for switch input S1
# CV #300 = 113 for switch input S2
# NB not sure if the key F6 and F7 should be latching in this context see bottom of Page 37 ??
#
        temppanel1d = javax.swing.JPanel()
        self.radioBtn1 = javax.swing.JRadioButton('Main Chuff Sounds (steam)        ')
        self.radioBtn2 = javax.swing.JRadioButton('Automated Background Sounds      ')
        self.radioBtn3 = javax.swing.JRadioButton('Allocating Sound to Function Keys')
        self.radioBtn4 = javax.swing.JRadioButton('Selecting Random Generated Sounds')
        self.radioBtn5 = javax.swing.JRadioButton('Allocating Sound to Switch Inputs')
        rbBtnGroup = javax.swing.ButtonGroup()
        rbBtnGroup.add(self.radioBtn1)
        rbBtnGroup.add(self.radioBtn2)
        rbBtnGroup.add(self.radioBtn3)
        rbBtnGroup.add(self.radioBtn4)
        rbBtnGroup.add(self.radioBtn5)
        self.radioBtn1.actionPerformed = self.radioChange
        self.radioBtn2.actionPerformed = self.radioChange
        self.radioBtn3.actionPerformed = self.radioChange
        self.radioBtn4.actionPerformed = self.radioChange
        self.radioBtn5.actionPerformed = self.radioChange

        temppanel1d.add(self.radioBtn1)
        self.opsCVLabel = ('Select', 'Steam Chuff Set')
        self.opsCVVal = ('0', '100' )
        self.opsComboBox = javax.swing.JComboBox(self.opsCVLabel)
        temppanel1d.add(self.opsComboBox)

#  Because CV300=100 appears to not function as described in the Zimo manual, it is disabled in this
# version.  Remove the comment on the next line to restore that feature to the User Interface and
# the script.
#         f.contentPane.add(temppanel1d)

        temppanel1e = javax.swing.JPanel()
        temppanel1e.add(self.radioBtn2)
        self.opsCVLabel2 = ('Select', 'Boiling Sound','Direction Change Sound','Brake Sound','Thyrister Control','Start Sound','Blow-off Sound','Driving Sound Electric', 'Switchgear Sound Electric')
        self.opsCVVal2 = ('0', '128', '129', '130', '131', '132', '133', '134', '136' )
        self.opsComboBox2 = javax.swing.JComboBox(self.opsCVLabel2)
        temppanel1e.add(self.opsComboBox2)

        f.contentPane.add(temppanel1e)

        temppanel1f = javax.swing.JPanel()
        temppanel1f.add(self.radioBtn3)
        self.opsCVLabel3 = ('Select', 'Function F0','Function F1','Function F2','Function F3','Function F4','Function F5','Function F6', 'Function F7','Function F8','Function F9','Function F10','Function F11','Function F12', 'Function F13', 'Function F14', 'Function F15', 'Function F16', 'Function F17', 'Function F18', 'Function F19')
        self.opsCVVal3 = ('0', '20', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19' )
        self.opsComboBox3 = javax.swing.JComboBox(self.opsCVLabel3)
        temppanel1f.add(self.opsComboBox3)
        f.contentPane.add(temppanel1f)

        temppanel1g = javax.swing.JPanel()
        temppanel1g.add(self.radioBtn4)
        self.opsCVLabel4 = ('Select', 'Generator Z1 (Compressor)','Generator Z2','Generator Z3','Generator Z4','Generator Z5','Generator Z6', 'Generator Z7','Generator Z8')
        self.opsCVVal4 = ('0', '101', '102', '103', '104', '105', '106', '107', '108' )
        self.opsComboBox4 = javax.swing.JComboBox(self.opsCVLabel4)
        temppanel1g.add(self.opsComboBox4)
        f.contentPane.add(temppanel1g)

        temppanel1h = javax.swing.JPanel()
        temppanel1h.add(self.radioBtn5)
        self.opsCVLabel5 = ('Select', 'Switch input S0 (Chuff sensor)','Switch input S1','Switch input S2')
        self.opsCVVal5 = ('0', '111', '112', '113' )
        self.opsComboBox5 = javax.swing.JComboBox(self.opsCVLabel5)
        temppanel1h.add(self.opsComboBox5)

        f.contentPane.add(temppanel1h)

        temppanel1j = javax.swing.JPanel()
        self.cvVal = javax.swing.JTextField(5)    # sized to hold 10 characters, initially empty
        temppanel1j.add(self.cvVal)
        self.OpsCVButton = javax.swing.JButton("Setup Sound")
        self.OpsCVButton.actionPerformed = self.comboChange
        temppanel1j.add(self.OpsCVButton)
        self.ReleaseButton = javax.swing.JButton("Exit, Release Loco")
        self.ReleaseButton.actionPerformed = self.releaseLoco
        temppanel1j.add(self.ReleaseButton)


        f.contentPane.add(temppanel1j)

        temppanel1k = javax.swing.JPanel()
        temppanel1k.add(javax.swing.JLabel("<html>3 - Select the sounds played using the Prev/Next buttons, in most cases, alter the Volume with the<BR>slider. When finished, use one of the three Exit options - Clear (which results in NO SOUND for<BR>that option), Accept which stores the current choice, and Exit which just exits without changes.</html>"))

        f.contentPane.add(temppanel1k)

        temppanel1s = javax.swing.JPanel()
        temppanel1s.add(javax.swing.JLabel("Speed/Volume  "))
        self.slider = javax.swing.JSlider(0, 128, 0, stateChanged=self.onSlide)
        temppanel1s.add(self.slider)
        self.speedVol = javax.swing.JTextField(5)    # sized to hold 10 characters, initially empty
        temppanel1s.add(self.speedVol)
        self.slider.enabled=False
        self.speedVol.enabled=False

        f.contentPane.add(temppanel1s)

        # Hide the radio buttons and combo boxes.
        self.hideShowCombos(False)
        self.hideShowRadios(False)
        self.hideShowOpsButton(False)


        # create the button

        self.F0box = javax.swing.JButton("Play Sound")
        self.F0box.actionPerformed = self.whenF0Changed
        #self.F0box.mousePressed = self.whenF0On
        #self.F0box.mouseReleased = self.whenF0Off

        self.F0box.enabled = False

        self.F1box = javax.swing.JButton("Prev Sound")
        # self.F1box.actionPerformed = self.whenF1Changed
        self.F1box.mousePressed = self.whenF1On
        self.F1box.mouseReleased = self.whenF1Off
        self.F2box = javax.swing.JButton("Next Sound")
        # self.F2box.actionPerformed = self.whenF2Changed
        self.F2box.mousePressed = self.whenF2On
        self.F2box.mouseReleased = self.whenF2Off
        self.F3box = javax.swing.JButton("Clear Sound & end (Silence!)")
        # self.F3box.actionPerformed = self.whenF3Changed
        self.F3box.mousePressed = self.whenF3On
        self.F3box.mouseReleased = self.whenF3Off
        self.F4box = javax.swing.JButton("Prev Group")
        self.F4box.mousePressed = self.whenF4On
        self.F4box.mouseReleased = self.whenF4Off
        # self.F4box.actionPerformed = self.whenF4Changed
        self.F5box = javax.swing.JButton("Next Group")
        #self.F5box.actionPerformed = self.whenF5Changed
        self.F5box.mousePressed = self.whenF5On
        self.F5box.mouseReleased = self.whenF5Off
        self.F6box = javax.swing.JCheckBox("F6 loop")
        self.F6box.actionPerformed = self.whenF6Changed
        self.F7box = javax.swing.JCheckBox("F7 short")
        self.F7box.actionPerformed = self.whenF7Changed
        self.F8box = javax.swing.JButton("Accept Sound, Store & end")
        # self.F8box.actionPerformed = self.whenF8Changed
        self.F8box.mousePressed = self.whenF8On
        self.F8box.mouseReleased = self.whenF8Off

        self.quitBox = javax.swing.JButton("Exit, no save")
        self.quitBox.actionPerformed = self.whenQuitChanged


        # hide the function keys (make them grey)
        self.hideShowFunctionButtons(False)





        self.status = javax.swing.JLabel("Enter address & click start                      ")
        temppanel2z = javax.swing.JPanel()

        temppanel2a = javax.swing.JPanel()
        temppanel2a.add(self.F0box)
        temppanel2a.add(javax.swing.JLabel("        "))
        temppanel2a.add(self.F1box)
        temppanel2a.add(self.F2box)
        temppanel2bz = javax.swing.JPanel()
        # temppanel2bz.add(javax.swing.JLabel(" Clear    ------- Group -------- "))
        temppanel2b = javax.swing.JPanel()
        temppanel2b.add(self.F3box)     
        temppanel2b.add(javax.swing.JLabel("  "))
        temppanel2b.add(self.F4box)
        temppanel2b.add(self.F5box)
        temppanel2b1z = javax.swing.JPanel()
        # temppanel2b1z.add(javax.swing.JLabel(" ------- Loop  --------   Store  "))
        temppanel2b1 = javax.swing.JPanel()
        temppanel2b1.add(self.F8box)
        temppanel2b1.add(self.quitBox)
        temppanel2b1.add(javax.swing.JLabel("   "))
        temppanel2b1.add(self.F6box)
        temppanel2b1.add(self.F7box)

        temppanel2c = javax.swing.JPanel()
        # panel2c is currently empty !
        
        temppanel3 = javax.swing.JPanel()
        temppanel3.add(self.status)     

        f.contentPane.add(temppanel2z)
        f.contentPane.add(temppanel2a)
        # f.contentPane.add(temppanel2bz)
        f.contentPane.add(temppanel2b)
        # f.contentPane.add(temppanel2b1z)
        f.contentPane.add(temppanel2b1)
#        f.contentPane.add(temppanel2c)
        f.contentPane.add(temppanel3)
        f.pack()
        f.show()

        return



# create one of these
a = LocoZimoPseudoProg()

# set the name, as a example of configuring it
a.setName("Zimo Pseudo Programming script")

# and show the initial panel
a.setup()
