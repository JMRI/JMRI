package jmri.jmrix.srcp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.srcp.parser.SRCPClientParser;
import jmri.jmrix.srcp.parser.ParseException;

import java.io.StringReader;

/**
 * SRCPMessageTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPMessage class
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class SRCPMessageTest extends TestCase {

    public void testCtor() {
        SRCPMessage m = new SRCPMessage();
        Assert.assertNotNull(m);
    }

    // Test the string constructor.
    public void testStringCtor() {
        String s = "100 OK REASON GOES HERE\n\r";
        SRCPMessage m = new SRCPMessage(s);
        Assert.assertNotNull(m);
        Assert.assertTrue("String Constructor Correct", s.equals(m.toString()));
    }

    // from here down is testing infrastructure
    public SRCPMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SRCPMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SRCPMessageTest.class);
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SRCPMessageTest.class.getName());
}
