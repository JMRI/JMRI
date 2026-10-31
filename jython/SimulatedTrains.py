# Watch two simulated trains run, driven by Internal throttles, and compare
# what the sensors reported with where the trains really were.
#
# Run it from the Scripting menu in a profile whose connections are all
# simulators, for example one with only the Internal connection. It:
#
#  - builds a short line of five Blocks, SimA to SimE, each with an internal
#    sensor (ISSimA to ISSimE), unless they already exist;
#  - places two simulated trains on it, "Goods" (address 3) in SimB and
#    "Passenger" (address 4) in SimD;
#  - drives them with the Internal connection's throttles, as a throttle
#    window, Dispatcher or a Warrant would: Passenger runs on until its
#    sensor in SimE reports it and stops; Goods runs into SimC, stops, then
#    backs out of SimC and is stopped with an emergency stop as soon as
#    SimC's sensor reports it gone;
#  - prints, after each move, Block by Block, what each sensor reports beside
#    which trains are really in the Block (the simulator's ground truth),
#    and where each train's front really is.
#
# The trains are removed at the end; the Blocks and sensors are left in place.
# The trains have no roster entry, so each runs at a set speed for its throttle
# setting (300 mm/s at full speed) with no momentum.
#
# Author: Keith Bruce, copyright 2026
# Part of the JMRI distribution

import jmri

from jmri.jmrit.simulation import Detection
from jmri.jmrit.simulation import SimulatedLayout
from jmri.jmrit.simulation import SimulatedLoco
from jmri.jmrit.simulation import SimulationCheck
from jmri.jmrit.simulation import VirtualTrain


class SimulatedTrains(jmri.jmrit.automat.AbstractAutomaton):

    NAMES = ["SimA", "SimB", "SimC", "SimD", "SimE"]
    LENGTHS = [400, 400, 400, 400, 1200]

    def init(self):
        self.lines = []
        self.blocks = self.buildLine()
        self.goods = self.makeTrain("Goods", 3)
        self.passenger = self.makeTrain("Passenger", 4)

    def buildLine(self):
        blockManager = jmri.InstanceManager.getDefault(jmri.BlockManager)
        sensorManager = jmri.InstanceManager.getDefault(jmri.SensorManager)
        result = []
        for name, length in zip(self.NAMES, self.LENGTHS):
            block = blockManager.getBlock("IB" + name)
            if block is None:
                block = blockManager.provideBlock("IB" + name)
                block.setUserName(name)
                block.setLength(length)
                sensor = sensorManager.provideSensor("IS" + name)
                sensor.setKnownState(jmri.Sensor.INACTIVE)
                block.setSensor(sensor.getSystemName())
                if len(result) > 0:
                    block.addPath(jmri.Path(result[-1], 0, 0))
                    result[-1].addPath(jmri.Path(block, 0, 0))
            result.append(block)
        return result

    def makeTrain(self, name, address):
        loco = SimulatedLoco.builder(jmri.DccLocoAddress(address, False)).length(150).fullSpeed(300).build()
        return VirtualTrain.builder(name).loco(loco).length(150).detection(Detection.entireTrain()).build()

    def say(self, line):
        self.lines.append(line)
        print(line)

    def waitForSensor(self, block, state):
        # the sensor is what JMRI sees; give up after 30 seconds
        for i in range(600):
            if block.getSensor().getKnownState() == state:
                return
            self.waitMsec(50)
        raise RuntimeError("sensor of " + block.getUserName() + " did not change")

    def waitStopped(self, layout, train):
        for i in range(600):
            if layout.getTruth().getTrain(train).getSpeedMmPerSecond() == 0.0:
                return
            self.waitMsec(50)
        raise RuntimeError(train.getName() + " did not stop")

    def report(self, layout, title):
        truth = layout.getTruth()
        self.say(title)
        self.say("  Block  sensor reports  really there")
        for block in self.blocks:
            sensor = "active" if block.getSensor().getKnownState() == jmri.Sensor.ACTIVE else "inactive"
            there = [t.getTrain().getName() for t in truth.getTrains() if block in t.getBlocks()]
            self.say("  %-5s  %-14s  %s" % (block.getUserName(), sensor, ", ".join(there) if there else "-"))
        for t in truth.getTrains():
            self.say("  %s: front %.0f mm into %s, %.0f mm/s" % (t.getTrain().getName(),
                    t.getFrontPositionMm(), t.getFrontBlock().getUserName(), t.getSpeedMmPerSecond()))

    def handle(self):
        layout = SimulatedLayout.getDefault()
        b = self.blocks
        try:
            layout.place(self.goods, b[1], b[0], 300)
            layout.place(self.passenger, b[3], b[2], 200)
            goods = self.getThrottle(3, False)
            passenger = self.getThrottle(4, False)
            self.waitForSensor(b[1], jmri.Sensor.ACTIVE)
            self.waitForSensor(b[3], jmri.Sensor.ACTIVE)
            self.report(layout, "Placed:")

            passenger.setSpeedSetting(0.25)
            self.waitForSensor(b[4], jmri.Sensor.ACTIVE)
            passenger.setSpeedSetting(0.0)
            self.waitStopped(layout, self.passenger)
            self.report(layout, "Passenger stopped when SimE reported it:")

            goods.setSpeedSetting(0.25)
            self.waitForSensor(b[2], jmri.Sensor.ACTIVE)
            goods.setSpeedSetting(0.0)
            self.waitStopped(layout, self.goods)
            self.report(layout, "Goods stopped when SimC reported it:")

            goods.setIsForward(False)
            goods.setSpeedSetting(0.25)
            self.waitForSensor(b[2], jmri.Sensor.INACTIVE)
            goods.setSpeedSetting(-1)  # emergency stop
            self.waitStopped(layout, self.goods)
            self.report(layout, "Goods stopped at once when SimC reported it gone:")

            self.finalTruth = layout.getTruth()
            self.finalSensors = [block.getSensor().getKnownState() for block in b]
        finally:
            layout.remove(self.goods)
            layout.remove(self.passenger)
            self.finished = True
        return False  # run once


if SimulationCheck.isFullySimulated():
    simulatedTrains = SimulatedTrains()
    simulatedTrains.finished = False
    simulatedTrains.setName("SimulatedTrains")
    simulatedTrains.start()
else:
    print("SimulatedTrains.py: simulated trains can only run in a profile whose connections are all simulators.")
