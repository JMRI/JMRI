package jmri.jmrix.powerline.insteon2412s;

import jmri.jmrix.powerline.SerialMessage;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the cm11.SpecficMessage class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008, 2009
 * @version	$Revision$
 */
public class SpecificMessageTest extends TestCase {

    public void testCreate() {
        SerialMessage m = new SpecificMessage(4);
        Assert.assertNotNull("exists", m);
    }

    public void testBytesToString() {
        SerialMessage m = new SpecificMessage(4);
        m.setOpCode(0x81);
        m.setElement(1, (byte) 0x02);
        m.setElement(2, (byte) 0xA2);
        m.setElement(3, (byte) 0x00);
        Assert.assertEquals("string compare ", "81 02 A2 00", m.toString());
    }

    // from here down is testing infrastructure
    public SpecificMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SpecificMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SpecificMessageTest.class);
        return suite;
    }

}
