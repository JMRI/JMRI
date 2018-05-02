package jmri.util.swing;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 *
 * @author	Bob Jacobsen Copyright 2010
 */
public class JmriAbstractActionTest extends TestCase {

    public void testAccess() {
        JmriAbstractAction a = new JmriAbstractAction("foo", new jmri.util.swing.sdi.JmriJFrameInterface()) {

            @Override
            public jmri.util.swing.JmriPanel makePanel() {
                return null;
            }
        };

        Assert.assertEquals("foo", a.getValue(javax.swing.Action.NAME));

        javax.swing.Icon i = new javax.swing.ImageIcon("resources/icons/throttles/PowerRed24.png");
        a = new JmriAbstractAction("foo", i, null) {
            @Override
            public jmri.util.swing.JmriPanel makePanel() {
                return null;
            }
        };

        Assert.assertEquals("foo", a.getValue(javax.swing.Action.NAME));
        Assert.assertEquals(i, a.getValue(javax.swing.Action.SMALL_ICON));
    }

    // from here down is testing infrastructure
    public JmriAbstractActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JmriAbstractActionTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JmriAbstractActionTest.class);

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
