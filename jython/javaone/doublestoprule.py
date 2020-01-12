# ----- release one if both held in sidings ------
# This is noticed when we see the blocks at the end 
# of the main & sidings go OCCUPIED

import java
import java.beans

class ReleaseFromSidings(java.beans.PropertyChangeListener):
  # to1, to2 are turnout objects; block1, block2 are corresponding
  # blocks behind them; state1, state2 are TO states (CLOSED, THROWN)
  # when the loco will stall in block1, block2 respectively  
  #
  # This listener must be attached to both blocks, because it
  # releases when the second one goes occupied
  def propertyChange(self, event):
    if event.propertyName != "state" : return   # only property changes
    # proper event, check for condition
    if ( self.to1.knownState == self.state1 and self.block1.state == jmri.Block.OCCUPIED
            and self.to2.knownState == self.state2 and self.block2.state == jmri.Block.OCCUPIED ) :
        # both blocks occupied, OK to release the 2nd one
        print "ReleaseFromSidings toggles "+self.to1.getSystemName()+" "+self.to2.getSystemName()
        if self.state1 == THROWN :
            self.to1.commandedState = CLOSED
        else :
            self.to1.commandedState = THROWN
        if self.state2 == THROWN :
            self.to2.commandedState = CLOSED
        else :
            self.to2.commandedState = THROWN
    return
# create, load, and attach these
s = ReleaseFromSidings()
s.to1 = turnouts.getTurnout("LT201")
s.to2 = turnouts.getTurnout("LT202")
s.block1 = IB161
s.block2 = IB155
s.state1 = CLOSED
s.state2 = CLOSED
s.block1.addPropertyChangeListener(s)
s.block2.addPropertyChangeListener(s)

s = ReleaseFromSidings()
s.to1 = turnouts.getTurnout("LT201")
s.to2 = turnouts.getTurnout("LT202")
s.block1 = IB160
s.block2 = IB155
s.state1 = THROWN
s.state2 = CLOSED
s.block1.addPropertyChangeListener(s)
s.block2.addPropertyChangeListener(s)

s = ReleaseFromSidings()
s.to1 = turnouts.getTurnout("LT201")
s.to2 = turnouts.getTurnout("LT202")
s.block1 = IB161
s.block2 = IB154
s.state1 = CLOSED
s.state2 = THROWN
s.block1.addPropertyChangeListener(s)
s.block2.addPropertyChangeListener(s)

s = ReleaseFromSidings()
s.to1 = turnouts.getTurnout("LT201")
s.to2 = turnouts.getTurnout("LT202")
s.block1 = IB160
s.block2 = IB154
s.state1 = THROWN
s.state2 = THROWN
s.block1.addPropertyChangeListener(s)
s.block2.addPropertyChangeListener(s)

