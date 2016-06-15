/**
 * QsiMessageTest.java
 *
 * Description:	JUnit tests for the QsiMessage class
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.qsi;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class QsiMessageTest extends TestCase {

    public void testCreate() {
        QsiMessage m = new QsiMessage(1);
        Assert.assertNotNull("exists", m);
    }

    public void testToASCIIString() {
        QsiMessage m = new QsiMessage(5);
        m.setOpCode(0x50);
        m.setElement(1, 0x20);
        m.setElement(2, 0x32);
        m.setElement(3, 0x36);
        m.setElement(4, 0x31);
        Assert.assertEquals("string compare ", "50 20 32 36 31 ", m.toString());
    }

    public void testGetEnable() {
        QsiMessage m = QsiMessage.getEnableMain();
        Assert.assertEquals("length", 1, m.getNumDataElements());
        Assert.assertEquals("opCode", 43, m.getOpCode());
    }

    public void testRecognizeEnable() {
        QsiMessage m = QsiMessage.getEnableMain();
        Assert.assertEquals("isEnableMain", true, m.isEnableMain());
        Assert.assertEquals("isKillMain", false, m.isKillMain());
    }

    public void testReadRegister() {
        QsiMessage m = QsiMessage.getReadRegister(2);
        Assert.assertEquals("string compare ", "20 ", m.toString());
    }

    public void testWriteRegister() {
        QsiMessage m = QsiMessage.getWriteRegister(2, 250);
        Assert.assertEquals("string compare ", "20 ", m.toString());
    }

    // from here down is testing infrastructure
    public QsiMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {QsiMessageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(QsiMessageTest.class);
        return suite;
    }

}
