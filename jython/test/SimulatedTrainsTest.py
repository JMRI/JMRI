# Test the SimulatedTrains.py sample script: run it in a fully simulated
# profile with an Internal connection, wait for it to finish, and check that
# the trains ended where the script drove them, and that the sensors reported
# what the simulator's ground truth says.
#
# Author: Keith Bruce, copyright 2026
# Part of the JMRI distribution

import java
import jmri
import time

# a fully simulated profile: one Internal connection
connections = jmri.jmrix.ConnectionConfigManager()
jmri.InstanceManager.setDefault(jmri.jmrix.ConnectionConfigManager, connections)
connections.add(jmri.jmrix.internal.ConnectionConfig())

execfile("jython/SimulatedTrains.py")

def waitUntil(condition, what) :
    for i in range(1200) :  # 60 seconds
        if condition() : return
        time.sleep(0.05)
    raise AssertionError(what + ' did not happen')

waitUntil(lambda : simulatedTrains.finished and not simulatedTrains.isRunning(), 'SimulatedTrains.py finishing')
if not hasattr(simulatedTrains, 'finalTruth') : raise AssertionError('SimulatedTrains.py did not complete its run')

truth = simulatedTrains.finalTruth
goods = truth.getTrain(simulatedTrains.goods)
passenger = truth.getTrain(simulatedTrains.passenger)
blocks = simulatedTrains.blocks
if goods.getFrontBlock() != blocks[1] : raise AssertionError('Goods should have backed out of SimC into SimB')
if passenger.getFrontBlock() != blocks[4] : raise AssertionError('Passenger should have stopped in SimE')
if goods.getSpeedMmPerSecond() != 0.0 or passenger.getSpeedMmPerSecond() != 0.0 : raise AssertionError('both stopped')
if truth.getCollisions().size() != 0 : raise AssertionError('no collision')
for block, state in zip(blocks, simulatedTrains.finalSensors) :
    occupied = any(block in t.getBlocks() for t in truth.getTrains())
    if occupied != (state == jmri.Sensor.ACTIVE) : raise AssertionError('sensor of ' + block.getUserName() + ' disagrees with the ground truth')

# the trains are removed, and the simulator's thread ends
if not jmri.jmrit.simulation.SimulatedLayout.getDefault().getTrains().isEmpty() : raise AssertionError('trains removed')
def threadsEnded() :
    for t in java.lang.Thread.getAllStackTraces().keySet() :
        if t.getName() in ('Simulated trains', 'SimulatedTrains') and t.getState() != java.lang.Thread.State.TERMINATED : return False
    return True
waitUntil(threadsEnded, 'threads ending')
