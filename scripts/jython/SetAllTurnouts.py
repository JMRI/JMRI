# This is a script to set all turnout to closed prior to layout operations
#
# Author: Scott CR Henry, based on Bob Jacobsen example
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $

import jmri

# turnouts.getTurnout("LT1").setState(THROWN)
# turnouts.getTurnout("LT2").setState(CLOSED)

class setStartup(jmri.jmrit.automat.AbstractAutomaton) :
  def init(self):
    return
  def handle(self):
    # Provides 30 sec wait to power on layout. This way layout is powered off at
    # start of Panel Pro. Once Panel Pro is opened, layout is powered on to
    # detected occupied blocks. Since turnout control depends on layout power
    # 30 sec is provided to ensure power is on when turnout commands sent.
    self.waitMsec(30000) 
    turnouts.provideTurnout("LT1").setState(CLOSED)
    self.waitMsec(5000)
    turnouts.provideTurnout("LT2").setState(CLOSED)
    self.waitMsec(5000)
    turnouts.provideTurnout("LT3").setState(CLOSED)
    self.waitMsec(5000)
    turnouts.provideTurnout("LT4").setState(CLOSED)
    self.waitMsec(5000)
    turnouts.provideTurnout("LT5").setState(CLOSED)
    self.waitMsec(5000)
    turnouts.provideTurnout("LT6").setState(CLOSED)
    self.waitMsec(5000)
    turnouts.provideTurnout("LT7").setState(CLOSED)
    self.waitMsec(5000)
    turnouts.provideTurnout("LT8").setState(CLOSED)
    self.waitMsec(5000)
    turnouts.provideTurnout("LT9").setState(CLOSED)
    self.waitMsec(5000)
    turnouts.provideTurnout("LT10").setState(CLOSED)
    self.waitMsec(5000)
    turnouts.provideTurnout("LT11").setState(CLOSED)
    self.waitMsec(5000)
    turnouts.provideTurnout("LT12").setState(CLOSED)
    return False # all done, don't repeat again

setStartup().start() # create one of these, and start it running