# That one is just a testing jynstrument
# Will go anywhere

import java
import java.awt
import jmri
import jmri.jmrit.jython.Jynstrument as Jynstrument
import java.awt.CardLayout as CardLayout
import jmri.util.swing.ResizableImagePanel as ResizableImagePanel
import sys

class Test(Jynstrument):
    def getExpectedContextClassName(self):
        return "java.lang.Object"
    
    def init(self):
        print "In init"
        self.label = ResizableImagePanel(self.getFolder() + "/jython.png",20,20 ) #label
        self.add(self.label)
        print "Running class ", self.getClassName(), " from file ", self.getJythonFile(), " in folder ", self.getFolder()
        print "Context is: ", self.getContext().getClass()
        print "Jython path is: ", sys.path
       
    def quit(self):
        print "In quit"
        self.label = None

    def __del__(self):
        print "In destructor"
