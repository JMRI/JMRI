# Turnout_Table_Build.py  helps build the Turnout Table for TurnoutDataTransfer.py
#  for tranfering turnout table Data from JMRI to an Arduino via Serial Transmission
# Author: Geoff Bunza 2018
#  Version 1.1
#

import jarray
import jmri
import java
import purejavacomm

# Define a turnout
def DataSet(sname, uname) :
    t = turnouts.newTurnout(sname, uname)
    t.setState(THROWN)

# Create a list of turnouts

first_turnout = 2
last_turnout  = 69

for x in range(first_turnout, last_turnout+1) :
    ssname = "IT"+str(x)
    uuname = "AT"+str(x)
    DataSet(ssname,uuname)
 
