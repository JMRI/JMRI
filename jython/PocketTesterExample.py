# Example of an event listener for a PRICOM PocketTester
#
# Author: Bob Jacobsen, copyright 2007
# Part of the JMRI distribution

import jmri

# First, define the listener.  This one just prints some
# information on each message, but more complicated code is
# of course possible.
class PricomListener(jmri.jmrix.pricom.pockettester.DataListener):
  def asciiFormattedMessage(self, msg):
    print msg

# Now create a pricom.DataSource window and display it
class MyDataSource(jmri.jmrix.pricom.pockettester.DataSource) :
  def initSuper(self) :
     self.init()
     return

source = MyDataSource()
source.initSuper()
source.setVisible(True)

# and finally attach my listener for data
source.addListener(source, PricomListener())

