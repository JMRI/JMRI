
import jmri.jmrit.jython.Jynstrument as Jynstrument
import jmri.jmrit.catalog.NamedIcon as NamedIcon
import jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction as PaneOpsProgAction
import javax.swing.JButton as JButton

class DecoderPro(Jynstrument):
    def getExpectedContextClassName(self):
        return "javax.swing.JComponent"
    
    def init(self):
        jbNew = JButton( PaneOpsProgAction() )
        jbNew.setIcon( NamedIcon("resources/decoderpro.gif","resources/decoderpro.gif") )
        jbNew.addMouseListener(self.getMouseListeners()[0]) # In order to get the popupmenu on the button too
        jbNew.setToolTipText( jbNew.getText() )
        jbNew.setText( None )
        self.add(jbNew)

    def quit(self):
        pass