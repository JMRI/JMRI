package jmri.jmrix.rfid;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RfidSystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.RfidSystemConnectionMemo class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class RfidSystemConnectionMemoTest extends TestCase {

    public void testCtor() {
        RfidSystemConnectionMemo memo=new RfidSystemConnectionMemo();
        Assert.assertNotNull("exists", memo);
    }

    // from here down is testing infrastructure
    public RfidSystemConnectionMemoTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", RfidSystemConnectionMemoTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RfidSystemConnectionMemoTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(RfidSystemConnectionMemoTest.class.getName());

}
