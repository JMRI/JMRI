package jmri.jmrix.powerline;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialNode class
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008
 * @author	Dave Duchamp multi-node extensions 2003 Converted to multiple
 * connection
 * @author kcameron Copyright (C) 2011
 * @version	$Revision$
 */
public class SerialNodeTest extends TestCase {

    public void testMarkChanges() {
        //SerialSensor s1 = new SerialSensor("PSA1","a");
        //SerialSensor s2 = new SerialSensor("PSA2","ab");
        //SerialSensor s3 = new SerialSensor("PSA3","abc");

        SerialSystemConnectionMemo memo = new jmri.jmrix.powerline.simulator.SpecificSystemConnectionMemo();
        SerialTrafficController t = new jmri.jmrix.powerline.simulator.SpecificTrafficController(memo);
        SerialReply r = new jmri.jmrix.powerline.simulator.SpecificReply(t);
        r.setElement(0, 0x02);
        r.setElement(1, 0x00);

        // The following used to work, but may have stopped
        // when the node support was removed sometime in 2009.
        // Not clear if it should still be working.
        //Assert.assertEquals("check s1", Sensor.INACTIVE, s1.getKnownState());
        //Assert.assertEquals("check s2", Sensor.ACTIVE, s2.getKnownState());
        //Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
    }

    // from here down is testing infrastructure
    public SerialNodeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialNodeTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialNodeTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
