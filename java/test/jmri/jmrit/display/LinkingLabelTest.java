package jmri.jmrit.display;

import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * LinkingLabelTest.java
 *
 * Description:
 * @author			Bob Jacobsen
 * @version			$Revision$
 */
public class LinkingLabelTest extends jmri.util.SwingTestCase {

    LinkingLabel to = null;
    jmri.jmrit.display.panelEditor.PanelEditor panel = 
            new jmri.jmrit.display.panelEditor.PanelEditor("LinkingLabel Test Panel");

	public void testShow() {
        JFrame jf = new jmri.util.JmriJFrame("LinkingLabel Target Panel");
        JPanel p = new JPanel();
        jf.getContentPane().add(p);
        p.add(new JLabel("Open 'Linking Label Test Panel' from Window menu on Editor"));
        p.setLayout(new java.awt.FlowLayout());
        jf.pack();
        jf.setVisible(true);

        to = new LinkingLabel("JMRI Link", panel,"http://jmri.org");
        to.setBounds(0,0,80,80);
        panel.putItem(to);
        to.setDisplayLevel(jmri.jmrit.display.Editor.LABELS);

        assertEquals("Display Level ", to.getDisplayLevel(),jmri.jmrit.display.Editor.LABELS);

        to = new LinkingLabel("Target link", panel,"frame:LinkingLabel Target Panel");
        to.setBounds(120,0,80,80);
        to.setDisplayLevel(jmri.jmrit.display.Editor.LABELS);
        panel.putItem(to);

        jmri.jmrit.display.PanelMenu.instance().addEditorPanel(panel);
        panel.setLocation(150,150);
    
        panel.setTitle();

        panel.pack();
        panel.setVisible(true);
        
        jf.dispose();
        panel.dispose();
        
	}

	// from here down is testing infrastructure

	public LinkingLabelTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", LinkingLabelTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LinkingLabelTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { 
       // now close panel window
        java.awt.event.WindowListener[] listeners = panel.getTargetFrame().getWindowListeners();
        for (int i=0; i<listeners.length; i++) {
            panel.getTargetFrame().removeWindowListener(listeners[i]);
        }
        junit.extensions.jfcunit.TestHelper.disposeWindow(panel.getTargetFrame(), this);
        apps.tests.Log4JFixture.tearDown(); 
    }

	// static private org.apache.log4j.Logger log = LoggerFactory.getLogger(TurnoutIconTest.class.getName());
}
