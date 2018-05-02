
# This is an example script for putting a "N-way" selector 
# on a panel.  The panel contains N sensors (in this example
# numbered 101, 102, 103).  When one is clicked on the panel,
# the others are forced to inactive and the program sets
# outputs appropriately (in this case setting turnouts 101 and 102)
#
# For N large, this would be better done with arrays and closures, but
# for N = 3 a direct approach is better.
#
# Author: Bob Jacobsen, copyright 2005
# Part of the JMRI distribution


import jarray
import jmri

class NSelectorExample(jmri.jmrit.automat.Siglet) :
    
    # init() is called exactly once at the beginning to do
    # any necessary configuration.
    def defineIO(self):
        
        # get the sensor objects
        self.s1 = sensors.provideSensor("101")
        self.s2 = sensors.provideSensor("102")
        self.s3 = sensors.provideSensor("103")
        
        # get the turnout (output) objects
        self.t1 = turnouts. provideTurnout("101")
        self.t2 = turnouts. provideTurnout("102")
        
        # set a known starting point        
        self.current = 1
        self.s1.setState(ACTIVE)
        
        # Register the inputs so setOutput will be called when needed.
        self.setInputs(jarray.array([self.s1, self.s2, self.s3], jmri.NamedBean))
        return

    # handle() is called repeatedly until it returns false.
    #
    # Modify this to do your calculation.
    def setOutput(self):
        
        print self.current,"is current"
        
        # check each in turn
        if ( (self.current != 1) and (self.s1.state == ACTIVE) ) :
            self.s2.state = INACTIVE
            self.s3.state = INACTIVE
            self.current = 1
            self.t1.state = THROWN
            self.t2.state = CLOSED
        elif ( (self.current != 2) and (self.s2.state == ACTIVE) ) :
            self.s1.state = INACTIVE
            self.s3.state = INACTIVE
            self.current = 2
            self.t1.state = CLOSED
            self.t2.state = THROWN
        elif ( (self.current != 3) and (self.s3.state == ACTIVE) ) :
            self.s1.state = INACTIVE
            self.s2.state = INACTIVE
            self.current = 3
            self.t1.state = CLOSED
            self.t2.state = CLOSED 
        # and continue around again
        print "return"
        return None # to continue
    
# end of class definition

# create one of these
a = NSelectorExample()

# set the name, as a example of configuring it
a.setName("NSelectorExample example script")

# and start it running
a.start()

