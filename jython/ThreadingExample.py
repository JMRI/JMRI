# This is an example of deferring work to the GUI thread from an AbstractAutomat ScriptMaker
# It doesn't do anything useful, just prints.  Copy it into your own code and adapt it.
# Bob Jacobsen July 2021

import jmri

class ThreadingExample(jmri.jmrit.automat.AbstractAutomaton) :
    def init(self) : return

    def handle(self) :
        # do other stuff
        # ...
        # ...
        # Start of example
        # Define a subclass that will do the needed work. Change the
        # name to represent what this is doing for you.
        class messWithSomething(jmri.util.ThreadingUtil.ThreadAction):
             def run(self):
                # do the work that needs to access the GUI
                # Here, it's just a print operation
                print "  This ran on the GUI thread: ", jmri.util.ThreadingUtil.isGUIThread()
                # and return to normal operation
                return

        # and, at the appropriate time, invoke it
        jmri.util.ThreadingUtil.runOnGUI(messWithSomething())

        # and do whatever else is needed
        # ...
        # ...
        return False

