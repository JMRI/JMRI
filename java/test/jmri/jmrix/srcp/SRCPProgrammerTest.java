package jmri.jmrix.srcp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * SRCPProgrammerTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPProgrammer class
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class SRCPProgrammerTest extends TestCase {

    public void testCtor() {
        SRCPSystemConnectionMemo sm=new SRCPSystemConnectionMemo(new SRCPTrafficController(){
          @Override
          public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
           }
        });
        SRCPProgrammer s = new SRCPProgrammer(sm);
        Assert.assertNotNull(s);
    }

    // from here down is testing infrastructure
    public SRCPProgrammerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SRCPProgrammerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SRCPProgrammerTest.class);
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SRCPProgrammerTest.class.getName());
}
