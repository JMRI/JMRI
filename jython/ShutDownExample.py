# Schedule something to happen when a JMRI application ends
#
# Author: Bob Jacobsen, copyright 2008
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision$

# Define the shutdown task
class MyShutDownTask(jmri.implementation.AbstractShutDownTask):
  def execute(self):
    # this is the code to be invoked when the program is shutting down
    print "Time to go!"
    return True     # True to shutdown, False to abort shutdown
    
shutdown.register(MyShutDownTask("Example"))

 