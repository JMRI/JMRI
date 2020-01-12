# Script Programs DCC Specialties - BlockWatcher
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
# You should copy this file to ProgramBlockWatcher????.py Replacing ???? 
# with the Primary Address of your BlockWatcher. You should keep this file 
# for backup, and future tweaking and programming.
#
# Each group of program lines is documented. To execute a group of statements, remove 
# the # from the beginning of the proceeding program lines.  Edit the program lines  
# to include necessary information for your BlockWatcher.
#
# Be Sure to remove the # from the "self.waitMsec(750)" line. This gives the BlockWatcher sufficient
# time to process each preceding command.
#
# This script uses Locomotive Address 9983.  Please make sure there is NO Locomotive number 9983
# on your layout while running this script.  This script will reprogram several of its CV's.
#
# Instructions
# 1. Turn Off Track Power - Use a Throttle or JMRI to do this.
# 2. Move the Program Jumper J3 from either [J3-2 J3-3] or [J3-3 J3-4] To [J3-1 J3-2](Program Mode)
# 3. Turn On Track Power - Again, Use a Throttle or JMRI.
# 4. VERY IMPORTANT - Wait 30 to 45 Seconds.
# 5. In JMRI DecoderPro, Go to Panels > Run Script.  Select the file that corresponds
#    to the BlockWatcher you are programming.  Note - Do not have the file open in an editor.
#
# Important - This script can take 2 to 6 seconds to run. This depends on the number of 
#             CV's and addresses being programmed.
# 
#             When the script is complete, a small message window should appear. Usually
#             in the upper left corner of your screen.
#
# 6. Turn Off Track Power.
# 7. Move the program jumper J3 from [J3-1 J3-2] back to [J2-2 J2-3](Automatic Setting Leakage Current)
#    or [J3-3 J3-4](Leakage Current Set with CV).
# 8. Turn Track Power On.
# 9. Test you BlockWatcher.
#

import jmri
import javax.swing

class setStartup(jmri.jmrit.automat.AbstractAutomaton) :      
  def init(self):
      return

  def handle(self):
    # Next Two Lines - Reset BlockWatcher to Factory Defaults
    addressedProgrammers.getAddressedProgrammer(True, 9983).writeCV("63", 42, None) 
    self.waitMsec(750)         # time is in milliseconds

    # Next Two Lines - Point BlockWatcher to Receive Primary Address, Not Necessary, However, good practice.
    addressedProgrammers.getAddressedProgrammer(True, 9983).writeCV("63", 0, None)  
    self.waitMsec(750)

    # Next Two Lines - Set Primary Address - Change 1234 to Address Desired
    turnouts.provideTurnout("1234").setState(CLOSED)                
    self.waitMsec(750)

    # Last Two Lines of this Group - CV53 - Delay in Milliseconds
    # Sets the delay in milliseconds that the block current exceeds the trip level 
    # before the J4 output will turn on. It is also the turn-off delay for the amount
    # of time the block current is below the trip level before the J4 output turns
    # off. The default value is 32 ms.
    # addressedProgrammers.getAddressedProgrammer(True, 9983).writeCV("53", 32, None)   
    # self.waitMsec(750)

    # Last Two Lines of this Group - CV54 - Average Unoccupied Block Current
    # This CV sets the average value of the block current when it is unoccupied. 
    # The J4 turn on threshold is 3 + CV54 + CV55. The turn off threshold is 3 + CV54. 
    # The scaling value is 0.534 mA per bit. The default is 0, which will result in
    # a trip current of about 2 mA. The maximum value is 254 � CV55 � 3. 
    # This sets the maximum trip current to about 130 mA. 
    # (With J3 on pins 2-3 this value is set each time power is applied to the detector.)
    # addressedProgrammers.getAddressedProgrammer(True, 9983).writeCV("54", 0, None)    
    # self.waitMsec(750)

    # Last Two Lines of this Group - CV55 - Hysterisis Between On and Off
    # Sets the hysterisis between on and off (see CV54). Its default value is 1.
    # This keeps the detector fromfluttering when the detected current is near the switching point.
    # addressedProgrammers.getAddressedProgrammer(True, 9983).writeCV("55", 1, None)    
    # self.waitMsec(750)
    
    javax.swing.JFrame("Programming Complete!").show()

    return 

setStartup().start()

