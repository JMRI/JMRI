# For each real or imaginary hardware turnout on a Master DCC connection,
# with a matching hardware address on a Slave DCC connection,
# provide a Listener that copies instructions for changes of state
# from the Master turnout to the Slave turnout.
#
#####
#   For your layout, modify the values for
#       MasterIdentiferPrefix
#   and
#       SlaveIdentierPrefix
#   found at lines # 70 and 71 to match the DCC local hardware.
#####
#
# The throttles on the Master DCC connection send state instructions to
# hypothetical turnouts with real hardware addresses and TurnoutMasterSlave
# echoes those commands to the Slave DCC connection that is connected to real
# hardware turnouts that are invisible to the throttles.
#
# Based in jython/SensorToTurnout.py written by Bob Jacobsen.
# Author: Cliff Anderson, with significant help from Dave Sand and Bob Jacobsen, copyright 2023
# Verified with versions 5.2 and 5.3.1
# Part of the JMRI distribution
#
import jmri
import java
import org.slf4j.Logger
import org.slf4j.LoggerFactory

# Get reference to the Logger
log = org.slf4j.LoggerFactory.getLogger(
        "jmri.jmrit.jython.exec.script.Turnouts Master Slave"
    )
# NOTE: to enable logging, see https://www.jmri.org/help/en/html/apps/Debug.shtml
# Add the Logger Category name "jmri.jmrit.jython.exec" at DEBUG Level.

'''
MasterIdentiferPrefix and SlaveIdentierPrefix values are
almost always the two-letter prefixes for turnouts.

In the case of two or more duplications of connections with
the same manufacturer, the prefix can be two lettere and a digit.

User options for alternative
prefixes are not discussed here.

Examine the Connection Tab of the Preferences Window for more information.
Some DEFAULT examples are listed here.  PLEASE VERIFY FOR YOUR LAYOUT!
    "IT2"   for None
    "DT"    for Anyma DMX512
    "XT"    for XpressNet, could be Atlas or CTI or Hornby or Lenz
    "AT"    for Bachrus AKA Speedo or for CTI Electronics or for KPF-Zeller
    "CT"    for C/MRI
    "DT"    for DCC++ or for DCC4PC
    "LT"    for Digitrax
    "UT"    for ECoS
    "ET"    for EasyDCC
    "ZT"    for IEEE802.15.4
    "JT"    for JMRI (Network)
    "MT"    for LCC or for MQTT or for MRC
    "TT"    for Lionel TMCC
    "FT"    for MERG
    "NT"    for NCE

        and a lot more

Note that the addressing has to have the same structure.  LT123 can be mapped to NT123,
but cannot be mapped to MT11.22.33.44.55.66.77.88;11.22.33.44.55.66.77.99

'''

# Example for LocoNet Master and NCE Slave:
#   Copy or edit the next two lines as needed:
MasterIdentiferPrefix = "LT" # For the Turnout commands from the throttles and Command Station
SlaveIdentierPrefix = "NT"  # For the Turnout commands that are relayed via this script's actions.

ParsePrefix = len(MasterIdentiferPrefix)    # Allow for multiple connection of the same type,
                                            # For example, both "LT" and "L2T" if dual LocoNet connections
# log.debug ( "ParsePrefix = " + str(ParsePrefix) )

# Define the Master Turnout listener:
# Makes a state change to corresponding Slave Turnout
class MasterTurnoutListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    log.debug( "A Master turnout has changed" )
    if (event.propertyName == "KnownState") :
        SlaveSystemName = SlaveIdentierPrefix + event.source.systemName[ParsePrefix:9]
        log.debug( "event systemName = \"" + SlaveSystemName +"\"" )
        # ensure exists
        log.debug( "event = \"" + SlaveSystemName +"\"" )
        turnout = turnouts.provideTurnout(SlaveSystemName)
        # copy over the user SlaveSystemName if present
        if ((event.source.userName != None) and (turnout.getUserName() == None)) :
            turnout.setUserName(event.source.userName)
        # copy the newly changed Master turnout state to the Slaved turnout
        turnout.setState(event.newValue)
    else :
        log.debug(" NOT A KNOWN STATE?" )
        pass
    return

MasterListener = MasterTurnoutListener()    # Construct an object

# Define a Manager MasterListener.  When invoked, a new
# item has been added, so go through the list of items removing the
# old MasterListener and adding a new one (works for both already registered
# and new turnouts)
class TurnoutManagerListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    log.debug( "A Turnout Definition has been created or deleted" )
    for turnout in turnouts.getNamedBeanSet().toArray():
      turnoutSystemName = turnout.toString()
      log.debug("TurnoutSystemName: = \"" + turnoutSystemName + "\"" )
      if turnoutSystemName[0:ParsePrefix] == MasterIdentiferPrefix :
        # log.debug("Revise the PropertyChangeListener")
        turnouts.getTurnout(turnoutSystemName).removePropertyChangeListener(MasterListener)
        turnouts.getTurnout(turnoutSystemName).addPropertyChangeListener(MasterListener)
      else :
        # log.debug("Skip this one")
        pass
    return

# Attach the turnout manager listener
turnouts.addPropertyChangeListener(TurnoutManagerListener())

# For all the Master turnouts that exist, attach a turnout listener
turnoutlist = turnouts.getNamedBeanSet().toArray()
log.debug ( str(turnoutlist) )
for turnout in turnoutlist :
    name = turnout.toString()
    log.debug( name )
    if name[0:ParsePrefix] == MasterIdentiferPrefix :
        log.debug("Attach a listener")
        turnout.addPropertyChangeListener(MasterListener)
    else :
        log.debug("no listener needed")
        pass

log.info(
            "Turnout Master Slave is active for "
            + MasterIdentiferPrefix + " to "
            + SlaveIdentierPrefix + " control"
        )

