# Sample script to set all defined turnouts to Closed
#
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 17977 $

def closeTurnout(toName):
    to = turnouts.getTurnout(toName)
    print "setting " + toName + " to CLOSED"
    to.setState(CLOSED)
    return

# invoke for all defined turnouts
for tn in turnouts.getSystemNameList().toArray() :
  closeTurnout(tn)
  sleep(0.1) #pause for 1/10 second between commands


