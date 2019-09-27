#
# Listener for LocoNet MULTI_SENSE_POWER Autoreverse/ShortCircuit messages from PM42's
# that contain status (OK or SHORT) for each of the 4 channels
#
# When a PM42 message is received, update Internal Sensors
#  ISPM_nn1, ISPM_nn2, ISPM_nn3 and ISPM_nn4
#  representing PM42 sections 1 through 4
#  where "nn" is the address of the PM42 in hex
#
# The Internal Sensor state is set to ACTIVE if the PM42 reports the section
#  is set for short circuit mode and the state of the section is active.  
# The Internal Sensor state is set to INACTIVE if the PM42 reports the section
#  is set for autoreverse mode or the state of the section is ok.
#
# Note: Internal Sensor states are undefined until a message is received from
# the PM42
#
# message parsing code patterned after lnmon.java
#
# Robin Becker
# 2008 Feb
#

import jmri

import java

# set the intended LocoNet connection by its index; when you have just 1 connection index = 0
connectionIndex = 0

# LocoNet listener class
# this does all the work
#
class PM42Listener(jmri.jmrix.loconet.LocoNetListener):
    def message(self, msg):
        # is this a power msg from a PM42?
        if ((msg.getElement(0) == 0xD0) and ((msg.getElement(1) & 0x60) == 0x60)) :
            pCmd = (msg.getElement(3) & 0xF0)    # PM42 msg type
            # is this a short circuit / autoreverse message?
            if ((pCmd == 0x30) or (pCmd == 0x10)) :
                pAdr = (msg.getElement(1)& 0x1) * 128 + (msg.getElement(2)& 0x7F)+1   # PM42 address
                pAdrHex = ("0"+java.lang.Integer.toHexString(pAdr))[-2:]              # make addr a 2 char string
                pAdrHex = pAdrHex.upper()                                             # Make sure the hex character, if any, is upper case
                pSen = "ISPM_"+pAdrHex                                                # internal sensor prefix

                #bit mapped codes: bits 0-3 correspond to PM42 sections 1-4 
                mode = msg.getElement(3)    # autoreverse if 1, short circuit if 0
                state = msg.getElement(4)    # ACT if 1, OK if 0

                s = sensors.provideSensor(pSen+"1")
                if ((mode & 0x01) == 0 and (state & 0x01) != 0) : s.state = ACTIVE
                else : s.state = INACTIVE

                s = sensors.provideSensor(pSen+"2")
                if ((mode & 0x02) == 0 and (state & 0x02) != 0) : s.state = ACTIVE
                else : s.state = INACTIVE

                s = sensors.provideSensor(pSen+"3")
                if ((mode & 0x04) == 0 and (state & 0x04) != 0) : s.state = ACTIVE
                else : s.state = INACTIVE

                s = sensors.provideSensor(pSen+"4")
                if ((mode & 0x08) == 0 and (state & 0x08) != 0) : s.state= ACTIVE
                else : s.state = INACTIVE
            return

# create the loconet listener
ln = PM42Listener()
jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().addLocoNetListener(0xFF,ln)
# Start Panelpro Select Tools - Tables - Sensors - Sensor tables 
# Click Add  System name. in the small window that opens enter System Name - ISPM_nn1  Where nn is
# your pm42 board id and User name which can be a decription # of the Power District 
# E.G Yard or Power District 1, whatever helps you remember what the sensor is monitoring.
# If you make a mistake click the delete button, you will get a warning that object is in use by at
# least 1 item, just ignore it in this case and click Yes.
# Now to add LEDS to your panel, Load your panel and open panel or layout editor
# In the box next to Add Sensor enter your first sensor name ISPM_nn1
# click the change icon button, scroll to the LED icons section.  I use a small red led for unknown or
# inconsistent state a large red led icon for active and a green led for inactive.
# Now go to your panel and move the icon to the appropriate place on the screen.  You can click on the
# icon and it should cycle trough all the colors you selected.
# Repeat for ISPM_nn2 through ISPM_nn4. 
# The final step is to have this scipt load every time you start your PanelPro application.
# On PanelPro or DecoderPro Screen  Select Edit - Preferences
# Once the screen opens, click the Show Advanced Preferences box
# Scroll down to the run scripts at startup section - Click Add Script - scroll to where your
# scripts are stored - On windows normally c:\Program Files\JMRI\Jython\your script name here.
# click open and your script will now appear on the preferences screen. When you restart PanelPro
# or DecoderPro this scrpt will load with your panel.
 
