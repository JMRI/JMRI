package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LenzCommandStationTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.LenzCommandStation class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class LenzCommandStationTest extends TestCase {

    public void testCtor() {

        LenzCommandStation c = new LenzCommandStation();
        Assert.assertNotNull(c);
    }

    public void testVersion() {
        // test setting the command station version from an XNetReply
        LenzCommandStation c = new LenzCommandStation();
        XNetReply r = new XNetReply();
        // test a version that is BCD
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x36); // version 3.6
        r.setElement(3, 0x00);
        r.setElement(4, 0x74);
        c.setCommandStationSoftwareVersion(r);
        Assert.assertEquals(3.6f, c.getCommandStationSoftwareVersion());
        // test a version that is not BCD
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x8D); // version 8.13
        r.setElement(3, 0x00);
        r.setElement(4, 0xCF);
        c.setCommandStationSoftwareVersion(r);
        Assert.assertEquals(8.13f, c.getCommandStationSoftwareVersion());
    }

    public void testType() {
        // test setting the command station type from an XNetReply
        LenzCommandStation c = new LenzCommandStation();
        XNetReply r = new XNetReply();
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x36);
        r.setElement(3, 0x00); // type is LZV100
        r.setElement(4, 0x74);
        c.setCommandStationType(r);
        Assert.assertEquals(0, c.getCommandStationType());
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x36);
        r.setElement(3, 0x01); // type is LH200 
        r.setElement(4, 0x75);
        c.setCommandStationType(r);
        Assert.assertEquals(1, c.getCommandStationType());
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x36);
        r.setElement(3, 0x02); // type is Compact 
        r.setElement(4, 0x76);
        c.setCommandStationType(r);
        Assert.assertEquals(2, c.getCommandStationType());
    }

    public void testSetVersionFloat() {
        // test setting the command station version from using a numeric
        // value.
        LenzCommandStation c = new LenzCommandStation();
        c.setCommandStationSoftwareVersion(3.6f);
        Assert.assertEquals(3.6f, c.getCommandStationSoftwareVersion());
        c.setCommandStationSoftwareVersion(8.13f);
        Assert.assertEquals(8.13f, c.getCommandStationSoftwareVersion());
    }

    public void testSetTypeNumeric() {
        // test setting the command station type from using a numeric
        // value.
        LenzCommandStation c = new LenzCommandStation();
        c.setCommandStationType(XNetConstants.CS_TYPE_LZ100);
        Assert.assertEquals(XNetConstants.CS_TYPE_LZ100, c.getCommandStationType());
        c.setCommandStationType(XNetConstants.CS_TYPE_LH200);
        Assert.assertEquals(XNetConstants.CS_TYPE_LH200, c.getCommandStationType());
        c.setCommandStationType(XNetConstants.CS_TYPE_COMPACT);
        Assert.assertEquals(XNetConstants.CS_TYPE_COMPACT, c.getCommandStationType());
        c.setCommandStationType(XNetConstants.CS_TYPE_MULTIMAUS);
        Assert.assertEquals(XNetConstants.CS_TYPE_MULTIMAUS, c.getCommandStationType());
    }

    // from here down is testing infrastructure
    public LenzCommandStationTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LenzCommandStationTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LenzCommandStationTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LenzCommandStationTest.class.getName());

}
