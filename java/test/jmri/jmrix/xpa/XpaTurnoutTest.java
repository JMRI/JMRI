package jmri.jmrix.xpa;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Description:	tests for the jmri.jmrix.xpa.XpaTurnout class
 * <P>
 * @author	Paul Bender
 */
public class XpaTurnoutTest extends TestCase {

    XpaSystemConnectionMemo memo = null;

    public void testCtor() {
        XpaTurnout t = new XpaTurnout(3,memo);
        Assert.assertNotNull(t);
    }

    // from here down is testing infrastructure
    public XpaTurnoutTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XpaTurnoutTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XpaTurnoutTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        memo = new XpaSystemConnectionMemo();
        memo.setXpaTrafficController(new XpaTrafficController());
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        memo = null;
    }

}
