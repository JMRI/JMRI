package jmri.jmrix.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OlcbProgrammerManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.openlcb.OlcbProgrammerManager class
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class OlcbProgrammerManagerTest extends TestCase {

    public void testCtor() {
        new OlcbSystemConnectionMemo();
        OlcbProgrammerManager s = new OlcbProgrammerManager(new OlcbProgrammer());
        Assert.assertNotNull(s);
    }

    // from here down is testing infrastructure
    public OlcbProgrammerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OlcbProgrammerManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OlcbProgrammerManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
    static Logger log = LoggerFactory.getLogger(OlcbProgrammerManagerTest.class.getName());
}
