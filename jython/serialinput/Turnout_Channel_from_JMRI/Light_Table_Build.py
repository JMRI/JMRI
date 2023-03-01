# Light_Table_Build.py  helps build the Light Table for LightDataTransfer.py
#  for tranfering Light table Data from JMRI to an Arduino via Serial Transmission
# Author: Geoff Bunza 2018
#         Bob Jacobsen 2022
#  Version 1.1
#

import jarray
import jmri
import java
import purejavacomm

# Define a light
def LightDataSet(sname, uname) :
    t = lights.provideLight(sname, uname)
    t.setState(OFF)

# Create a list of lights

first_light = 2
last_light  = 69

for x in range(first_light, last_light+1) :
    ssname = "IT"+str(x)
    uuname = "AT"+str(x)
    LightDataSet(ssname,uuname)

