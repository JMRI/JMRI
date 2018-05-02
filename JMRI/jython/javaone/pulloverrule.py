# --- loco B pull over to wait ------
# When loco is at the start of the loop and the siding ahead is clear, 
# sometimes pull into it and throw the turnouts for a pass
import jarray
import java

class PullOver(jmri.jmrit.automat.Siglet):
  # to1 is the turnout on the siding entry
  # to2 is the turnout on the siding exit
  # block is the incoming train
  # watch is the list of sensors to check for empty
  # sensor is the sensor that says we've pulled in
 
    def defineIO(self):
        self.setInputs(jarray.array([self.block], jmri.NamedBean))
        return
 
    def setOutput(self):
        # start because the block is occupied
        if self.block.state != jmri.Block.OCCUPIED : return
        # check all clear ahead
        for x in self.watch :
            if x.state!=INACTIVE : return
        # OK, could do this, but should we this time?
        if java.lang.Math.random() > 0.33 : return  # 1/3 chance to proceed
        # Proceed: start by setting TOs
        print "Setting up a pass at "+self.to2.systemName
        self.to1.commandedState = THROWN
        self.to2.commandedState = CLOSED
        # wait for the train to make it into the siding
        self.waitSensorActive(self.sensor)
        # and throw TO behind
        self.to1.commandedState = CLOSED
        # done
        return

s = PullOver()
s.to1 = turnouts.getTurnout("LT200")
s.to2 = turnouts.getTurnout("LT201")
s.block = IB156 # start of loop
s.watch = [
    sensors.getSensor("LS157"),
    sensors.getSensor("LS162"),
    sensors.getSensor("LS159"),
    sensors.getSensor("LS161"),
]
s.sensor = sensors.getSensor("LS161")
s.start()
        
s = PullOver()
s.to1 = turnouts.getTurnout("LT203")
s.to2 = turnouts.getTurnout("LT202")
s.block = IB150 # start of loop
s.watch = [
    sensors.getSensor("LS151"),
    sensors.getSensor("LS165"),
    sensors.getSensor("LS153"),
    sensors.getSensor("LS155"),
]
s.sensor = sensors.getSensor("LS155")
s.start()
