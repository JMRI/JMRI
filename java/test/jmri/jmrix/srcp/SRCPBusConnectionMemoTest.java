package jmri.jmrix.srcp;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * SRCPBusConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPBusConnectionMemo class
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class SRCPBusConnectionMemoTest extends TestCase {

    public void testCtor() {
        SRCPBusConnectionMemo m = new SRCPBusConnectionMemo(new SRCPTrafficController(){
          @Override
          public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
           }
        },"A",1);
        Assert.assertNotNull(m);
    }

    // from here down is testing infrastructure
    public SRCPBusConnectionMemoTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SRCPBusConnectionMemoTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SRCPBusConnectionMemoTest.class);
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
    static Logger log = Logger.getLogger(SRCPBusConnectionMemoTest.class.getName());
}
