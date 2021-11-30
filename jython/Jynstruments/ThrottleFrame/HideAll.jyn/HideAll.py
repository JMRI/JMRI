# Author: Lionel Jeanson copyright 2010
# Part of the JMRI distribution
# 
# When clicked hides menu and toolboar in a throttle window

import java
import java.awt
import jmri
import jmri.jmrit.jython.Jynstrument as Jynstrument
import java.awt.GridBagLayout as GridBagLayout
import jmri.util.swing.ResizableImagePanel as ResizableImagePanel
import java.awt.event.MouseListener as MouseListener

class HideAll(Jynstrument, MouseListener):
# Jynstrument mandatory part
# Here this JYnstrument like to be in a ThrottleFrame and nowhere else
    def getExpectedContextClassName(self):
        return "jmri.jmrit.throttle.ThrottleFrame"

    def init(self):         
        self.setLayout( GridBagLayout() )
        self.icon = ResizableImagePanel(self.getFolder() + "/HideAll.png",32,32 ) 
        self.setPreferredSize(java.awt.Dimension(32,32))
        self.add(self.icon)           
        self.addMouseListener(self)
        self.isActive = False


    def quit(self):   # very important to clean up everything to make sure GC will collect us
        pass
        
    def switch(self):      # actually do stuff here
        self.isActive = not self.isActive
        # hide menu bar
        self.getContext().getThrottleWindow().getJMenuBar().setVisible(not self.isActive)
        # look for an hide toolbar
        for comp in self.getContext().getThrottleWindow().getContentPane().getComponents() :
            if (comp.getClass().getSimpleName() == 'JToolBar'):
                comp.setVisible(not self.isActive)
        # switch edit mode, will hide all inner windows decorations
        self.getContext().getThrottleWindow().setEditMode(not self.isActive)    
        
#MouseListener part: to listen for mouse events
    def mouseReleased(self, event):
        self.switch()

    def mousePressed(self, event):
        pass
    def mouseClicked(self, event):
        pass
    def mouseExited(self, event):
        pass
    def mouseEntered(self, event):
        pass
