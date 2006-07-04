# ----- blow for a pass ------
# Wait for a passing situation, then 
#    blow the horn
#    wait for clear
#    thrown the turnout behind
import jarray
class Passing(jmri.jmrit.automat.Siglet):
  # to is the turnout for the pass
  # state is the needed TO setting
  # block1 is the stopped train
  # block2 is the passing train 
  # tosensor is the OS
 
    def defineIO(self):
        self.setInputs(jarray.array([self.block1, self.block2], jmri.NamedBean))
        return
 
    def setOutput(self):
        # start because one of the blocks is changed
        # check that TO is set for this case
        if self.to.knownState!=self.state : return
        if self.block1.state != jmri.Block.OCCUPIED : return
        if self.block2.state != jmri.Block.OCCUPIED : return
        # both blocks occupied; pass is happening!
        print "Handle passing situation at "+self.tosensor.systemName
        # Turn on whistle
        self.block2.getValue().setF2(True)
        # wait for TO sensor active, then turn off horn
        self.waitSensorActive(self.tosensor)
        print "Turn off horn"
        self.block2.getValue().setF2(False)
        # wait for TO to be empty, then set
        self.waitSensorInactive(self.tosensor)
        print "Setting TO to allow train to leave"
        if self.state == THROWN :
            self.to.commandedState = CLOSED
        else :
            self.to.commandedState = THROWN
        # blow the whistle on the starting loco
        self.waitMsec(500)
        self.block1.getValue().setF2(True)
        self.waitMsec(1000)
        self.block1.getValue().setF2(False)
        self.waitMsec(1000)
        self.block1.getValue().setF2(True)
        self.waitMsec(1000)
        self.block1.getValue().setF2(False)
        # wait for permission from somebody else to move.
        # You'll know that happens when the TO shows occupied, then not
        self.waitSensorActive(self.tosensor)
        self.waitSensorInactive(self.tosensor)
        self.to.commandedState = CLOSED
        print "Sequence complete"
        # return to wait for next change
        return
        
# create, load, and attach these
s = Passing()
s.to = turnouts.getTurnout("LT201")
s.tosensor = sensors.getSensor("LS163")
s.block1 = IB161  # stopped
s.block2 = IB160  # passing
s.state = CLOSED  # for the pass
s.start()

s = Passing()
s.to = turnouts.getTurnout("LT201")
s.tosensor = sensors.getSensor("LS163")
s.block1 = IB160  # stopped
s.block2 = IB161  # passing
s.state = THROWN  # for the pass
s.start()

s = Passing()
s.to = turnouts.getTurnout("LT202")
s.tosensor = sensors.getSensor("LS164")
s.block1 = IB155  # stopped
s.block2 = IB154  # passing
s.state = CLOSED  # for the pass
s.start()

s = Passing()
s.to = turnouts.getTurnout("LT202")
s.tosensor = sensors.getSensor("LS164")
s.block1 = IB154  # stopped
s.block2 = IB155  # passing
s.state = THROWN  # for the pass
s.start()


