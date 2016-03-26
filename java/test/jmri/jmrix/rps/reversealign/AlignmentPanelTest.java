package jmri.jmrix.rps.reversealign;

import apps.tests.Log4JFixture;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import jmri.InstanceManager;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the rps.AlignmentPanel class.
 *
 * @author	Bob Jacobsen Copyright 2006
 * @version	$Revision$
 */
public class AlignmentPanelTest extends TestCase {

    public void testShow() {
        jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("RPS Alignment");
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

        AlignmentPanel panel = new AlignmentPanel();
        panel.initComponents();
        f.getContentPane().add(panel);
        f.pack();
        f.setVisible(true);
//    }
//  test order isn't guaranteed!
//    public void testXFrameCreation() {
        JFrame f2 = jmri.util.JmriJFrame.getFrame("RPS Alignment");
        Assert.assertTrue("found frame", f2 != null);
        f2.dispose();
    }

    // from here down is testing infrastructure
    public AlignmentPanelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AlignmentPanelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AlignmentPanelTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        InstanceManager.setDefault(RosterConfigManager.class, new RosterConfigManager());
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        Log4JFixture.tearDown();
    }
}
