// NceSensorManagerTest.java

package jmri.jmrix.nce;

import jmri.*;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.nce.NceReply;

/**
 * JUnit tests for the NceAIU class
 * @author			Bob Jacobsen
 * @version			$Revision: 1.1 $
 */
public class NceSensorManagerTest extends TestCase {

    public void testScan1() {
        NceSensorManager s = new NceSensorManager();
        Assert.assertEquals("none expected", null, s.nextAiuPoll());
        s.newSensor(null, "3");
    }

    public void testScan2() {
        NceSensorManager s = new NceSensorManager();
        s.newSensor(null, "3");
        NceMessage m = s.nextAiuPoll();
        Assert.assertEquals("opcode I ", 'I', m.getElement(0));
        Assert.assertEquals("space ", ' ', m.getElement(1));
        Assert.assertEquals("0 ", '0', m.getElement(2));
        Assert.assertEquals("1 ", '1', m.getElement(3));
    }

    public void testScan3() {
        NceSensorManager s = new NceSensorManager();
        s.newSensor(null, "17");
        NceMessage m = s.nextAiuPoll();
        Assert.assertEquals("opcode I ", 'I', m.getElement(0));
        Assert.assertEquals("space ", ' ', m.getElement(1));
        Assert.assertEquals("0 ", '0', m.getElement(2));
        Assert.assertEquals("2 ", '2', m.getElement(3));
    }

    public void testScan4() {
        NceSensorManager s = new NceSensorManager();
        s.newSensor(null, "172");
        NceMessage m = s.nextAiuPoll();
        Assert.assertEquals("opcode I ", 'I', m.getElement(0));
        Assert.assertEquals("space ", ' ', m.getElement(1));
        Assert.assertEquals("1 ", '1', m.getElement(2));
        Assert.assertEquals("0 ", '1', m.getElement(3));
    }

        public void testScan5() {
        NceSensorManager s = new NceSensorManager();
        s.newSensor(null, "17");
        s.newSensor(null, "172");
        NceMessage m = s.nextAiuPoll();
        Assert.assertEquals("opcode I ", 'I', m.getElement(0));
        Assert.assertEquals("space ", ' ', m.getElement(1));
        Assert.assertEquals("0 ", '0', m.getElement(2));
        Assert.assertEquals("2 ", '2', m.getElement(3));
        m = s.nextAiuPoll();
        Assert.assertEquals("opcode I ", 'I', m.getElement(0));
        Assert.assertEquals("space ", ' ', m.getElement(1));
        Assert.assertEquals("1 ", '1', m.getElement(2));
        Assert.assertEquals("0 ", '1', m.getElement(3));
        m = s.nextAiuPoll();
        Assert.assertEquals("opcode I ", 'I', m.getElement(0));
        Assert.assertEquals("space ", ' ', m.getElement(1));
        Assert.assertEquals("0 ", '0', m.getElement(2));
        Assert.assertEquals("2 ", '2', m.getElement(3));
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
