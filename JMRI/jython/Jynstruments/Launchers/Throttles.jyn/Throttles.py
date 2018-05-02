import java
import jmri
import jmri.jmrit.jython.Jynstrument as Jynstrument
import jmri.jmrit.catalog.NamedIcon as NamedIcon
import jmri.jmrit.throttle.LoadDefaultXmlThrottlesLayoutAction as LoadDefaultXmlThrottlesLayoutAction
import javax.swing.JButton as JButton

class Throttles(Jynstrument):
    def getExpectedContextClassName(self):
        return "javax.swing.JComponent"
    
    def init(self):
        jbNew= JButton( LoadDefaultXmlThrottlesLayoutAction() )
        jbNew.setIcon( NamedIcon("resources/Throttles.gif","resources/Throttles.gif") )
        jbNew.addMouseListener(self.getMouseListeners()[0]) # In order to get the popupmenu on the button too
        jbNew.setToolTipText( jbNew.getText() )
        jbNew.setText( None )
        self.add(jbNew)

    def quit(self):
        pass
