// SerialSensorManagerTest.java

package jmri.jmrix.cmri.serial;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialSensorManager class.
 * @author	Bob Jacobsen  Copyright 2003
 * @version	$Revision: 1.1 $
 */
public class SerialSensorManagerTest extends TestCase {

    public void testScan1() {
        SerialSensorManager s = new SerialSensorManager();
        Assert.assertEquals("none expected", null, s.nextPoll());
        s.provideSensor("3");
    }

    public void testScan2() {
        SerialSensorManager s = new SerialSensorManager();
        s.provideSensor("3");
        SerialMessage m = s.nextPoll();
        Assert.assertEquals("UA 0 ", 'A', m.getElement(0));
    }

    public void testScan3() {
        SerialSensorManager s = new SerialSensorManager();
        s.provideSensor("17");
        s.provideSensor("127");
        s.provideSensor("128");
        s.provideSensor("129");
        SerialMessage m = s.nextPoll();
        Assert.assertEquals("UA 0 ", 'A', m.getElement(0));
    }

    public void testScan4() {
        SerialSensorManager s = new SerialSensorManager();
        s.provideSensor("1001");
        SerialMessage m = s.nextPoll();
        Assert.assertEquals("UA 1 ", 'B', m.getElement(0));
    }

    public void testScan5() {
        SerialSensorManager s = new SerialSensorManager();
        s.provideSensor("17");
        s.provideSensor("1017");
        SerialMessage m = s.nextPoll();
        Assert.assertEquals("UA 1 ", 'B', m.getElement(0));
        m = s.nextPoll();
        Assert.assertEquals("UA 0 ", 'A', m.getElement(0));
        m = s.nextPoll();
        Assert.assertEquals("UA 1 ", 'B', m.getElement(0));
    }

    // from here down is testing infrastructure
    public SerialSensorManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialSensorManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialSensorManagerTest.class);
        return suite;
    }

}
