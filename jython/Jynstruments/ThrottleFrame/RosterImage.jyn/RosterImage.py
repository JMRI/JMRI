# Author: Lionel Jeanson copyright 2010
# Part of the JMRI distribution
#

import java
import java.awt
import jmri
import jmri.jmrit.jython.Jynstrument as Jynstrument
import java.awt.BorderLayout as BorderLayout
import jmri.jmrit.throttle.panels.BackgroundPanel as BackgroundPanel

class RosterImage(Jynstrument):
# Jynstrument mandatory part
# Here this JYnstrument like to be in a ThrottleFrame and no anywhere else
    def getExpectedContextClassName(self):
        return "jmri.jmrit.throttle.ThrottleFrame"

    def init(self):
        self.setLayout( BorderLayout() )
        self.image = BackgroundPanel()
        self.image.setAddressPanel(self.getAddressPanel())
        if ((self.getContext().getRosterEntry() != None) and 
            (self.getContext().getRosterEntry().getImagePath() != None)):
            self.image.setImagePath( self.getContext().getRosterEntry().getImagePath() )
        self.setPreferredSize(java.awt.Dimension(320,200))
        self.add(self.image, BorderLayout.CENTER )        
        self.addComponentListener(self.image)
        self.getContext().addAddressListener(self.image)

    def quit(self):   # very important to clean up everything to make sure GC will collect us
        if ( self.getContext() != None) 
            self.getContext().removeAddressListener(self.image)



