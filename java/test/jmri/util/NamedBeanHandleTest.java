package jmri.util;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.util.NamedBeanUtil class.
 *
 * @author	Bob Jacobsen Copyright 2009
 */
public class NamedBeanHandleTest extends TestCase {

    public void testCtor() {
        new NamedBeanHandle<Turnout>("", null);
    }

    public void testHoldsTurnout() {
        Turnout t = new AbstractTurnout("name") {

            @Override
            protected void forwardCommandChangeToLayout(int s) {
            }

            @Override
            protected void turnoutPushbuttonLockout(boolean b) {
            }
        };
        NamedBeanHandle<Turnout> n = new NamedBeanHandle<Turnout>("name", t);

        Assert.assertEquals("same TO", t, n.getBean());
    }

    // from here down is testing infrastructure
    public NamedBeanHandleTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", NamedBeanHandleTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NamedBeanHandleTest.class);
        return suite;
    }

}
