package jmri.util.swing.sdi;

import javax.swing.JButton;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.ButtonTestAction;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Invokes complete set of tests in the jmri.util.swing.sdi tree
 *
 * @author	Bob Jacobsen Copyright 2010
 */
public class PackageTest extends TestCase {

    public void testAction() {
        JmriJFrame f = new JmriJFrame("SDI test");
        JButton b = new JButton(new ButtonTestAction(
                "new frame", new jmri.util.swing.sdi.JmriJFrameInterface()));
        f.add(b);
        f.pack();
        f.setVisible(true);
//    }
//  test order isn't guaranteed!
//    public void testFrameCreation() {
        JFrame f2 = jmri.util.JmriJFrame.getFrame("SDI test");
        Assert.assertTrue("found frame", f2 != null);
        JUnitUtil.dispose(f2);
    }

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class.getName());

        suite.addTest(SdiJfcUnitTest.suite());
        suite.addTest(new JUnit4TestAdapter(SdiWindowTest.class));
        suite.addTest(new JUnit4TestAdapter(JmriJFrameInterfaceTest.class));

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
