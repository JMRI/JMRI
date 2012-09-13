package jmri.jmrix.srcp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.srcp.parser.SRCPClientParser;
import jmri.jmrix.srcp.parser.ParseException;

import java.io.StringReader;

/**
 * SRCPSensorTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPSensor class
 *
 * @author	Bob Jacobsen
 * @version $Revision: 19890 $
 */
public class SRCPSensorTest extends TestCase {

    public void testCtor() {
        SRCPSystemConnectionMemo sm=new SRCPSystemConnectionMemo(new SRCPTrafficController(){
          @Override
          public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
           }
        });
        SRCPSensor s = new SRCPSensor(1,sm);
        Assert.assertNotNull(s);
    }

    // from here down is testing infrastructure
    public SRCPSensorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SRCPSensorTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SRCPSensorTest.class);
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SRCPSensorTest.class.getName());
}
