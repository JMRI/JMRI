package jmri.jmrix.nce;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * JUnit tests for the NceMessage class
 *
 * @author	Bob Jacobsen Copyright 2002-2004
  */
public class NceMessageTest extends TestCase {

    // ensure that the static useBinary value is left OK
    int saveCommandOptions;
    NceTrafficController tc = new NceTrafficController();

    @Override
    public void setUp() {
        saveCommandOptions = tc.getCommandOptions();
    }

    @Override
    public void tearDown() {
        tc.commandOptionSet = false;	// kill warning message
        tc.setCommandOptions(saveCommandOptions);
        Assert.assertTrue("Command has been set", tc.commandOptionSet);
        tc.commandOptionSet = false;	// kill warning message
    }

    public void testCreate() {
        NceMessage m = new NceMessage(1);
        Assert.assertNotNull("exists", m);
    }

    public void testToBinaryString() {
        NceMessage m = new NceMessage(4);
        m.setOpCode(0x81);
        m.setElement(1, 0x02);
        m.setElement(2, 0xA2);
        m.setElement(3, 0x00);
        m.setBinary(true);
        Assert.assertEquals("string compare ", "81 02 A2 00", m.toString());
    }

    public void testToASCIIString() {
        NceMessage m = new NceMessage(5);
        m.setOpCode(0x50);
        m.setElement(1, 0x20);
        m.setElement(2, 0x32);
        m.setElement(3, 0x36);
        m.setElement(4, 0x31);
        m.setBinary(false);
        Assert.assertEquals("string compare ", "P 261", m.toString());
    }

    // from here down is testing infrastructure
    public NceMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NceMessageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NceMessageTest.class);
        return suite;
    }

}
