#
# Simple example of connecting to and operating a WiThrottle server to run a train
# This should run from the Python command line, outside of JMRI itself.
#
# Bob Jacobsen   2021
#

import telnetlib

class WiThrottle :
    # Open the connection, set identification, allocate a train
    def open(self) :
        # make connection
        HOST = "localhost"
        PORT = 12090
        self.tn = telnetlib.Telnet(HOST, PORT)

        # set ID
        ID = "Telnet Train"  # arbitrary, shows in JMRI
        self.send("N"+ID+"\n")

        # allocate train with short address 3
        self.TRAIN = "S3"
        self.send("MT+"+self.TRAIN+"<;>"+self.TRAIN+"\n")

    # Read and print info back. Needs to be run periodically to flush input.
    def readAll(self) :
        lines = self.tn.read_very_eager()
        print(lines)

    def send(self, line) : # line should end in \n
        self.tn.write(bytearray(line, "UTF-8"))

    # Send a speed command (0-126; -1 for emergency stop)
    def setSpeed(self, speed) :
        self.send("MTA"+self.TRAIN+"<;>V"+str(speed)+"\n")
        return

    # Set train direction (True is forward)
    def setForward(self, dir) :
        if (dir) :
            self.send("MTA"+self.TRAIN+"<;>R1\n")
        else :
            self.send("MTA"+self.TRAIN+"<;>R0\n")

    def close(self) :
        self.tn.close()

## Simplest possible program to use those
wiT = WiThrottle()

wiT.open()

wiT.readAll()

# Set the train forward, start it, stop it, reverse it, start it, stop it.
# There are notional delays to see some movement
import time

wiT.setForward(True)

wiT.setSpeed(63)

time.sleep(3)
wiT.readAll()

wiT.setSpeed(0)
wiT.setForward(False)

time.sleep(1)
wiT.readAll()

wiT.setSpeed(32)

time.sleep(6)
wiT.readAll()

wiT.setSpeed(0)

time.sleep(1)
wiT.readAll()

# and end gracefully
wiT.close()
