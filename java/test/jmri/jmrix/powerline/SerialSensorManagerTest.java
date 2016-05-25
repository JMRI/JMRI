package jmri.jmrix.powerline;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008 Converted to multiple
 * connection
 * @author kcameron Copyright (C) 2011
 * @version	$Revision$
 */
public class SerialSensorManagerTest extends TestCase {

    public void testSensorCreationAndRegistration() {
        // replace the SerialTrafficController to get clean reset
        SerialTrafficController t = new jmri.jmrix.powerline.SerialTrafficController() {
            SerialTrafficController test() {
                return this;
            }
        }.test();
        Assert.assertNotNull("exists", t);

        SerialSensorManager s = new SerialSensorManager(t) {
            public void reply(SerialReply r) {
            }
        };
        s.provideSensor("PSA3");

        s.provideSensor("PSA11");

        s.provideSensor("PSP8");

        s.provideSensor("PSP9");

        s.provideSensor("PSK13");

        s.provideSensor("PSJ6");

        s.provideSensor("PSA15");

        s.provideSensor("PSB5");

        s.provideSensor("PSH7");

        s.provideSensor("PSI7");

        s.provideSensor("PSJ7");
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

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
