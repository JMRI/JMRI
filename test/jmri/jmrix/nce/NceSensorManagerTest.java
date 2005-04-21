// NceSensorManagerTest.java

package jmri.jmrix.nce;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the NceAIU class.
 * @author	Bob Jacobsen Copyright 2002
 * @version	$Revision: 1.4 $
 */
public class NceSensorManagerTest extends TestCase {

    public void testScan1() {
        NceSensorManager s = new NceSensorManager();
        Assert.assertEquals("none expected", null, s.nextAiuPoll());
        s.provideSensor("3");
    }

    public void testScan2() {
        NceSensorManager s = new NceSensorManager();
        s.provideSensor("3");
        NceMessage m = s.nextAiuPoll();
        Assert.assertEquals("opcode ", 0x8A, m.getElement(0));
        Assert.assertEquals("AIU ", 1, m.getElement(1));
    }

    public void testScan3() {
        NceSensorManager s = new NceSensorManager();
        s.provideSensor("17");
        NceMessage m = s.nextAiuPoll();
        Assert.assertEquals("opcode ", 0x8A, m.getElement(0));
        Assert.assertEquals("AIU ", 2, m.getElement(1));
    }

    public void testScan4() {
        NceSensorManager s = new NceSensorManager();
        s.provideSensor("172");
        NceMessage m = s.nextAiuPoll();
        Assert.assertEquals("opcode ", 0x8A, m.getElement(0));
        Assert.assertEquals("AIU ", 11, m.getElement(1));
    }

    public void testScan5() {
        NceSensorManager s = new NceSensorManager();
        s.provideSensor("17");
        s.provideSensor("172");
        NceMessage m = s.nextAiuPoll();
        Assert.assertEquals("opcode ", 0x8A, m.getElement(0));
        Assert.assertEquals("AIU ", 2, m.getElement(1));
        m = s.nextAiuPoll();
        Assert.assertEquals("opcode ", 0x8A, m.getElement(0));
        Assert.assertEquals("AIU ", 11, m.getElement(1));
        m = s.nextAiuPoll();
        Assert.assertEquals("opcode ", 0x8A, m.getElement(0));
        Assert.assertEquals("AIU ", 2, m.getElement(1));
    }

    // from here down is testing infrastructure
    public NceSensorManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NceSensorManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NceSensorManagerTest.class);
        return suite;
    }

}
