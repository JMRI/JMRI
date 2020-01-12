# Script Programs DCC Specialties - PSX or PSX-AR
# http://www.dccspecialties.com
#
# Script by Brett Scott
# Version 1.50 07/27/2014
# mgbrrclub@gmail.com or info@mgbrr.org and www.mgbrr.org
#
# Many Thanks to Bob Jacobsen and Everyone who Contributes to JMRI.
#
# Script should work for any DCC System that supports Ops Mode Programming or
# Programming on Mainline. (Digitrax, NEC, MRC, Lenz)
#
# You should copy this file to ProgramPSX&PSX-AR????.py Replacing ???? with the
# Primary Address of your PSX or PSX-AR. You should keep this file for backup, and
# future tweaking and programming.
#
# Each group of program lines is documented. To execute a group of statements, remove
# the # from the beginning of the proceeding program lines.  Edit the program lines
# to include necessary information for your PSX or PSX-AR.
#
# Be Sure to remove the # from the "self.waitMsec(750)" line. This gives the PSX or PSX-AR sufficient
# time to process each preceding command.
#
# This script uses Locomotive Address 9983.  Please make sure there is NO Locomotive number 9983
# on your layout while running this script.  This script will reprogram several of its CV's.
#
# Instructions
# 1. Turn Off Track Power - Use a Throttle or JMRI to do this.
# 2. Move the Program Jumper J3 From [J3-2 J3-3] To [J3-1 J3-2](Program Mode)
# 3. Turn On Track Power - Again, Use a Throttle or JMRI.
# 4. VERY IMPORTANT - Wait 30 to 45 Seconds.
# 5. In JMRI DecoderPro, Go to Panels > Run Script.  Select the file that corresponds
#    to the PSX or PSX-AR you are programming.  Note - Do not have the file open in an editor.
#
# Important - This script can take 3 to 6 seconds to run. This depends on the number of
#             CV's and addresses being programmed.
#
#             When the script is complete, a small message window should appear. Usually
#             in the upper left corner of your screen.
#
# 6. Turn Off Track Power.
# 7. Move the program jumper J3 from [J3-1 J3-2] to [J3-2 J3-3] (Normal Operation).
# 8. Wait at least 1 full minute before Turn Track Power On.
# 9. Test you PSX or PSX-AR.
#

import jmri
import javax.swing

class setStartup(jmri.jmrit.automat.AbstractAutomaton) :
  def init(self):
      return

  def handle(self):
    # Next Two Lines - Reset PSX or PSX-AR to Factory Defaults
    addressedProgrammers.getAddressedProgrammer(True, 9983).writeCV("63", 42, None)
    self.waitMsec(750)         # time is in milliseconds

    # Next Two Lines - Point PSX or PSX-AR to Receive Primary Address, Not Necessary, However, good practice.
    addressedProgrammers.getAddressedProgrammer(True, 9983).writeCV("63", 0, None)
    self.waitMsec(750)

    # Next Two Lines - Set Track Power On/OFF Address - Change 1234 to Address Desired
    turnouts.provideTurnout("1234").setState(CLOSED)
    self.waitMsec(750)

    # Next Two Lines - Set Arm Output for Photo Cell Address - Remove the # Symbols and Change 1234 to Address Desired
    # turnouts.provideTurnout("1234").setState(CLOSED)
    # self.waitMsec(750)

    # PSX-AR (AUTO REVERSER) ONLY! Next Two Lines - Set Turnout Accessory Address
    # turnouts.provideTurnout("1234").setState(CLOSED)
    # self.waitMsec(750)


    # CV49 � Sets the Current Trip Value. If CV49=0, then the Trip Current jumpers on J6 are enabled.
    # REMEMBER TO USE EITHER JUMPERS (J6) OR CV SETTINGS, !!! NOT BOTH !!!

    # Next Two Lines - CV49 - Current Trip Value (Values 00-15)
    # addressedProgrammers.getAddressedProgrammer(True, 9983).writeCV("49", 03, None)
    # self.waitMsec(750)

    # Next Two Lines - CV50 - Block Detection Source (Values 0,1)
    # addressedProgrammers.getAddressedProgrammer(True, 9983).writeCV("50", 1, None)
    # self.waitMsec(750)

    # PSX-AR (Auto Reverser) ONLY! Next Two Lines - CV52 - Power On Position of Reverser (Values 0,1)
    # addressedProgrammers.getAddressedProgrammer(True, 9983).writeCV("52", 0, None)
    # self.waitMsec(750)

    # Next Two Lines - CV53 - Enables/Disables Inrush Boost (Values 0,1)
    # addressedProgrammers.getAddressedProgrammer(True, 9983).writeCV("53", 0, None)
    # self.waitMsec(750)

    # Next Two Lines - CV54 - Current Level Detector Turns On (Values 0-212)
    # addressedProgrammers.getAddressedProgrammer(True, 9983).writeCV("54", 0, None)
    # self.waitMsec(750)

    # Next Two Lines - CV55 - Double Reverse Mode(Values 0, 1) (Value 0 = Primary Reverser, 1 = Secondary Reverser)
    # addressedProgrammers.getAddressedProgrammer(True, 9983).writeCV("55", 0, None)
    # self.waitMsec(750)

    # Notes regarding CV65 -  A value of 16 to 40 (2 to 5 ms) Should Solve Most Issues. Goal is the CV65 value should be as small as possible consistent with reliable operation.
    # Next Two Lines - CV65 - Double Reverse Mode Timing(Values 1-240) ( 8 is Default - 8 = 1 Millisecond, 16 = 2 Millisecond, Etc))
    # addressedProgrammers.getAddressedProgrammer(True, 9983).writeCV("65", 8, None)
    # self.waitMsec(750)

    # Some PSX-AR Documention indicates a CV64 for Current Level Detection.
    # This is an ERROR in Documetion - Use CV54

    javax.swing.JFrame("Programming Complete!").show()

    return

setStartup().start()
