# Script to assist Zimo function mapping using CV61=98 method
# works in a few Zimo decoders, allowing more complex function maps
# than normally possible.  For MX620, MX64, etc.
#
# # # Consult Zimo Manuals before use !!!
#
# Requires loco on main line, uses ops mode programming
#
# Script will step through each of the 13 pairs of function keys (F0-F12) in
# Forward and Reverse directions, allowing any of 13 outputs to be selected.
# Main purpose is to keep track of which Fn key is being set at which time, and gives on-screen
# indication of which output(s) are selected against that key (see next paragraph!)
#
# Note unusual behaviour of Fo0; this will toggle the outputs Fl(F) and
# Fl(R) through four combinations: none, Fl(F), Fl(R), both. This is the same as Zimo manual
# description.  The software tracks this, but it is strongly recommend that decoder has lights
# attached to outputs Fl(F) and Fl(R) (white and yellow wires) to make it possible to see status of Fo0,
# and to see confirmation of both lights on at end of programming sequence.
#
#
# Version 1.0, tested on one MX620 with v9 firmware, seeking comments from others !
# Version 1.1 adds function F0 toggle status indicator and initialisation of loco.
# Version 1.2 temporary changes to deal with NCE bug report
# Version 1.3 improve text and button layout to aid usability, change Fo0 to "change" button to
# toggle through the four states of front/rear lamps. Removed NCE changes; user with bug had old version of JMRI, and fixed with 2.8.
# Version 1.4 adds new FrontLamp and RearLamp check boxes, with script which toggles the F0 output
# through four states; this should make the UI better as the user can select the combinations required
# and leave the script to toggle through output states in the decoder
# Version 1.5, general tidy up and added Roster Drop-Box to select loco.
#
# Nigel Cliffe, copyright February 2010
#
# Components based on Bob Jacobsen's scripts in JMRI distribution.
#

import java
import javax.swing
import jarray
import jmri

class LocoZimoProg(jmri.jmrit.automat.AbstractAutomaton) :

    # define how long to wait between settings (seconds)
    delay = 2
    # dirCount is used to determine how many changes of direction
    dirCount = 0
    # flStatus is used to record whether it is Fl(F) or Fl(R), or both, etc.
    flStatus = 0

    # init() is called exactly once at the beginning to do
    # any necessary configuration.
    def init(self):
        self.status.text = "Getting throttle"
        number = int(self.address.text)
        if (number > 100) :
            long = True
        else :
            long = False
        self.throttle = self.getThrottle(number, long)
        self.waitMsec(self.delay*1000)
        if (self.throttle == None) :
            print "Couldn't assign throttle!"
        # setup a programmer for ops-mode
        self.programmer = addressedProgrammers.getAddressedProgrammer(long, number)
        return

    # handle() will only execute once here, to run a single test
    #
    # Modify this to do your calculation.
    def handle(self):
        # prevent running twice by hiding start button.
        self.startButton.enabled = False
        self.box.enabled = False
        # self.automaticBox.enabled = False
        # Set throttle to forward and all functions off:

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
        self.throttle.setF8(False)
        self.throttle.setF9(False)
        self.throttle.setF10(False)
        self.throttle.setF11(False)
        self.throttle.setF12(False)
        self.waitMsec(1000)
            # setup ops mode
        self.status.text = "writing ops mode"
        self.programmer.writeCV("61", 98, None)
        self.waitMsec(1000)
        # make UI visible
        self.address.enabled = False
        self.Flfbox.enabled = True
        self.Flrbox.enabled = True
        self.F1box.enabled = True
        self.F2box.enabled = True
        self.F3box.enabled = True
        self.F4box.enabled = True
        self.F5box.enabled = True
        self.F6box.enabled = True
        self.F7box.enabled = True
        self.F8box.enabled = True
        self.F9box.enabled = True
        self.F10box.enabled = True
        self.F11box.enabled = True
        self.F12box.enabled = True
        self.fwdButton.enabled = True
        self.status.text = "setting outputs for Function Key F" + str(self.dirCount) + " in Forward Direction"
        return 0
        # use return 1 for an infinite loop, or return 0 for once through !


    # define what button does when clicked and attach that routine to the button
    def whenMyButtonClicked(self,event) :
        self.start()
        return


    def whenFlfrChanged(self, event) :
        # New version 1.4 routine, cycles through the four states for Flf/Flr to give
        # correct output behaviour, yet allow user to have simple check-box UI to select
        # front/rear light combinations.
        self.Flfbox.enabled = False
        self.Flrbox.enabled = False
        # print "flf or flr pressed "
        newStatus = 0
        if (self.Flfbox.isSelected() == True) :
            newStatus = newStatus + 1
        if (self.Flrbox.isSelected() == True) :
            newStatus = newStatus + 2
        # print newStatus, self.flStatus
        while (newStatus != self.flStatus) :
            self.flStatus = self.flStatus + 1
            if (self.flStatus == 4) :
                self.flStatus = 0
            if ((self.flStatus == 0) or (self.flStatus ==2)) :
                self.throttle.setF0(False)
                self.waitMsec(1000)
                # print "set F0 off"
            if ((self.flStatus == 1) or (self.flStatus ==3)) :
                self.throttle.setF0(True)
                self.waitMsec(1000)
                # print "set F0 on"
        # print self.flStatus
        self.Flfbox.enabled = True
        self.Flrbox.enabled = True
        return



    def whenF0Changed(self,event) :
        # do nothing, F0 is now controlled by "whenToggleF0ButtonClicked"
        self.waitMsec(1000)
        return

    def whenF1Changed(self,event) :
        if (self.F1box.isSelected() ) :
            self.throttle.setF1(True)
        else :
            self.throttle.setF1(False)
        # pause a second to give the command station a chance !
        self.waitMsec(1000)
        return

    def whenF2Changed(self,event) :
        if (self.F2box.isSelected() ) :
            self.throttle.setF2(True)
        else :
            self.throttle.setF2(False)
        # pause a second to give the command station a chance !
        self.waitMsec(1000)
        return

    def whenF3Changed(self,event) :
        if (self.F3box.isSelected() ) :
            self.throttle.setF3(True)
        else :
            self.throttle.setF3(False)
        # pause a second to give the command station a chance !
        self.waitMsec(1000)
        return

    def whenF4Changed(self,event) :
        if (self.F4box.isSelected() ) :
            self.throttle.setF4(True)
        else :
            self.throttle.setF4(False)
        # pause a second to give the command station a chance !
        self.waitMsec(1000)
        return

    def whenF5Changed(self,event) :
        if (self.F5box.isSelected() ) :
            self.throttle.setF5(True)
        else :
            self.throttle.setF5(False)
        # pause a second to give the command station a chance !
        self.waitMsec(1000)
        return

    def whenF6Changed(self,event) :
        if (self.F6box.isSelected() ) :
            self.throttle.setF6(True)
        else :
            self.throttle.setF6(False)
        # pause a second to give the command station a chance !
        self.waitMsec(1000)
        return

    def whenF7Changed(self,event) :
        if (self.F7box.isSelected() ) :
            self.throttle.setF7(True)
        else :
            self.throttle.setF7(False)
        # pause a second to give the command station a chance !
        self.waitMsec(1000)
        return

    def whenF8Changed(self,event) :
        if (self.F8box.isSelected() ) :
            self.throttle.setF8(True)
        else :
            self.throttle.setF8(False)
        # pause a second to give the command station a chance !
        self.waitMsec(1000)
        return

    def whenF9Changed(self,event) :
        if (self.F9box.isSelected() ) :
            self.throttle.setF9(True)
        else :
            self.throttle.setF9(False)
        # pause a second to give the command station a chance !
        self.waitMsec(1000)
        return

    def whenF10Changed(self,event) :
        if (self.F10box.isSelected() ) :
            self.throttle.setF10(True)
        else :
            self.throttle.setF10(False)
        # pause a second to give the command station a chance !
        self.waitMsec(1000)
        return

    def whenF11Changed(self,event) :
        if (self.F11box.isSelected() ) :
            self.throttle.setF11(True)
        else :
            self.throttle.setF11(False)
        # pause a second to give the command station a chance !
        self.waitMsec(1000)
        return

    def whenF12Changed(self,event) :
        if (self.F12box.isSelected() ) :
            self.throttle.setF12(True)
        else :
            self.throttle.setF12(False)
        # pause a second to give the command station a chance !
        self.waitMsec(1000)
        return


    def whenFwdButtonClicked(self, event) :
        # hide button
        self.fwdButton.enabled = False
        # put throttle into reverse
        self.throttle.setIsForward(False)
        statusText = self.status.text
        # reveal other button
        if (self.dirCount < 13) :
            self.revButton.enabled = True
            statusText = "setting outputs for Function Key F" + str(self.dirCount) + " in Reverse Direction"
        else:
            # end of operations, release the throttle...
            self.throttle.release(None)
        self.status.text = statusText
        return

    def whenRevButtonClicked(self, event) :
        # hide button
        self.revButton.enabled = False
        # increment dirCount
        self.dirCount = self.dirCount + 1
        if (self.dirCount < 13) :
            statusText = "setting outputs for Function Key F" + str(self.dirCount) + " in Forward Direction"
        else:
            statusText = "Done all Fn keys. Loco shows two lamps. Press Commit Fwd to finish, then close script"
        self.status.text = statusText
        # put throttle into Forward
        self.throttle.setIsForward(True)
        # reveal other button
        self.fwdButton.enabled = True
        return

    def rosterBoxChange(self, event) :
        #print "roster changing in rosterBoxChange"
        entry = self.box.getSelectedItem()
        #print entry
        theDccAddress = entry.getDccAddress()
        # print theDccAddress
        self.address.text = theDccAddress
        return 0


    # routine to show the panel, starting the whole process
    def setup(self):
        # create a frame to hold the button, set up for nice layout
        f = javax.swing.JFrame("Zimo Fn Programmer v1.5")       # argument is the frames title
        f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.Y_AXIS))


        # top explanatory panel, text only
        temppanel0 = javax.swing.JPanel()
        temppanel0.add(javax.swing.JLabel("<html><b>Zimo CV61=98 function mapping tool</b>, using Ops Mode programming. Consult Zimo manuals before use.<br>The script assists the user by remembering which step has been reached, and providing visual display<br>of selections.<br>The script also ensures the loco is in correct state before starting.<br>Select your locomotive from the roster, or type in the address in the box.</html>"))

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
        temppanel1a.add(javax.swing.JLabel("<html>Automatic will setup loco for you, and program decoder with CV61=98. <br> Manual requires you to set loco forward, all functions to off, and then set CV61=98 before pressing 'Start'</html>"))
        temppanel1b = javax.swing.JPanel()
        temppanel1b.add(javax.swing.JLabel("Tick box for automatic, un-tick for manual"))
        self.automaticBox = javax.swing.JCheckBox("Automatic")
        self.automaticBox.selected = True
        temppanel1b.add(self.automaticBox)

        temppanel1c = javax.swing.JPanel()
        temppanel1c.add(javax.swing.JLabel("<html>Script steps through each <i>Function Key</i> (F0-F12), allowing the user to set the <i>Decoder Outputs</i> required <br> in each <i>Direction</i>.  The status line (bottom line of window) reports which Function Key is being set. <br> <br> For each Function Key and Direction, use the <i>Checkboxes</i> to select the <i>Outputs</i> required <br> and then <i>Commit</i> your selections in that Direction.<br><br>The Script has pauses to allow the command station and decoder to catch up, please be patient!</html>"))



        # Put contents in frame and display
        f.contentPane.add(temppanel0)

        f.contentPane.add(temppanel1)

        f.contentPane.add(temppanel1c)



        # create the button
        temppanel2a = javax.swing.JPanel()

        self.Flfbox = javax.swing.JCheckBox("Front Lamp (FLf)")
        self.Flfbox.actionPerformed = self.whenFlfrChanged
        self.Flrbox = javax.swing.JCheckBox("Rear Lamp (FLr)")
        self.Flrbox.actionPerformed = self.whenFlfrChanged
        self.Flfbox.enabled = False
        self.Flrbox.enabled = False

        temppanel2a.add(self.Flfbox)
        temppanel2a.add(self.Flrbox)

        self.F1box = javax.swing.JCheckBox("Fo1")
        self.F1box.actionPerformed = self.whenF1Changed
        self.F2box = javax.swing.JCheckBox("Fo2")
        self.F2box.actionPerformed = self.whenF2Changed
        self.F3box = javax.swing.JCheckBox("Fo3")
        self.F3box.actionPerformed = self.whenF3Changed
        self.F4box = javax.swing.JCheckBox("Fo4")
        self.F4box.actionPerformed = self.whenF4Changed
        self.F5box = javax.swing.JCheckBox("Fo5")
        self.F5box.actionPerformed = self.whenF5Changed
        self.F6box = javax.swing.JCheckBox("Fo6")
        self.F6box.actionPerformed = self.whenF6Changed
        self.F7box = javax.swing.JCheckBox("Fo7")
        self.F7box.actionPerformed = self.whenF7Changed
        self.F8box = javax.swing.JCheckBox("Fo8")
        self.F8box.actionPerformed = self.whenF8Changed
        self.F9box = javax.swing.JCheckBox("Fo9")
        self.F9box.actionPerformed = self.whenF9Changed
        self.F10box = javax.swing.JCheckBox("Fo10")
        self.F10box.actionPerformed = self.whenF10Changed
        self.F11box = javax.swing.JCheckBox("Fo11")
        self.F11box.actionPerformed = self.whenF11Changed
        self.F12box = javax.swing.JCheckBox("Fo12")
        self.F12box.actionPerformed = self.whenF12Changed

        # self.F0box.enabled = False
        self.F1box.enabled = False
        self.F2box.enabled = False
        self.F3box.enabled = False
        self.F4box.enabled = False
        self.F5box.enabled = False
        self.F6box.enabled = False
        self.F7box.enabled = False
        self.F8box.enabled = False
        self.F9box.enabled = False
        self.F10box.enabled = False
        self.F11box.enabled = False
        self.F12box.enabled = False


        self.fwdButton = javax.swing.JButton("Commit Fwd")
        self.fwdButton.actionPerformed = self.whenFwdButtonClicked
        self.fwdButton.enabled = False
        self.revButton = javax.swing.JButton("Commit Rev")
        self.revButton.actionPerformed = self.whenRevButtonClicked
        self.revButton.enabled = False



        self.status = javax.swing.JLabel("Enter address & click start                      ")

        temppanel2b = javax.swing.JPanel()
        temppanel2b.add(self.F1box)
        temppanel2b.add(self.F2box)
        temppanel2b.add(self.F3box)
        temppanel2b.add(self.F4box)
        temppanel2b.add(self.F5box)
        temppanel2b.add(self.F6box)
        temppanel2b1 = javax.swing.JPanel()
        temppanel2b1.add(self.F7box)
        temppanel2b1.add(self.F8box)
        temppanel2b1.add(self.F9box)
        temppanel2b1.add(self.F10box)
        temppanel2b1.add(self.F11box)
        temppanel2b1.add(self.F12box)
        #temppanel2b.add(self.fwdButton)
        #temppanel2b.add(self.revButton)

        temppanel2c = javax.swing.JPanel()
        temppanel2c.add(self.fwdButton)
        temppanel2c.add(self.revButton)

        temppanel3 = javax.swing.JPanel()
        temppanel3.add(self.status)

        f.contentPane.add(temppanel2a)
        f.contentPane.add(temppanel2b)
        f.contentPane.add(temppanel2b1)
        f.contentPane.add(temppanel2c)
        f.contentPane.add(temppanel3)
        f.pack()
        f.show()

        return



# create one of these
a = LocoZimoProg()

# set the name, as a example of configuring it
a.setName("Zimo Programming script")

# and show the initial panel
a.setup()
