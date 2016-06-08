/**
 * EasyDccMessageTest.java
 *
 * Description:	JUnit tests for the EasyDccMessage class
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.easydcc;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class EasyDccMessageTest extends TestCase {

    public void testCreate() {
        EasyDccMessage m = new EasyDccMessage(1);
        Assert.assertNotNull("exists", m);
    }

    public void testToASCIIString() {
        EasyDccMessage m = new EasyDccMessage(5);
        m.setOpCode(0x50);
        m.setElement(1, 0x20);
        m.setElement(2, 0x32);
        m.setElement(3, 0x36);
        m.setElement(4, 0x31);
        Assert.assertEquals("string compare ", "P 261", m.toString());
    }

    public void testGetEnable() {
        EasyDccMessage m = EasyDccMessage.getEnableMain();
        Assert.assertEquals("length", 1, m.getNumDataElements());
        Assert.assertEquals("opCode", 'E', m.getOpCode());
    }

    public void testRecognizeEnable() {
        EasyDccMessage m = EasyDccMessage.getEnableMain();
        Assert.assertEquals("isEnableMain", true, m.isEnableMain());
        Assert.assertEquals("isKillMain", false, m.isKillMain());
    }

    public void testReadPagedCV() {
        EasyDccMessage m = EasyDccMessage.getReadPagedCV(12);
        Assert.assertEquals("string compare ", "R 00C", m.toString());
    }

    public void testWritePagedCV() {
        EasyDccMessage m = EasyDccMessage.getWritePagedCV(12, 126);
        Assert.assertEquals("string compare ", "P 00C 7E", m.toString());
    }

    public void testReadRegister() {
        EasyDccMessage m = EasyDccMessage.getReadRegister(2);
        Assert.assertEquals("string compare ", "V2", m.toString());
    }

    public void testWriteRegister() {
        EasyDccMessage m = EasyDccMessage.getWriteRegister(2, 250);
        Assert.assertEquals("string compare ", "S2 FA", m.toString());
    }

    // from here down is testing infrastructure
    public EasyDccMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EasyDccMessageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EasyDccMessageTest.class);
        return suite;
    }

}
