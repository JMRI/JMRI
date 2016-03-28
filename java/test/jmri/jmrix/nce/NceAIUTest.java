package jmri.jmrix.nce;

import jmri.Sensor;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the NceAIU class
 *
 * @author	Bob Jacobsen
 * @version	$Revision$
 */
public class NceAIUTest extends TestCase {

    public void testMarkChanges() {
        NceAIU a = new NceAIU();
        NceSensor s1 = new NceSensor("1");
        NceSensor s2 = new NceSensor("2");
        NceSensor s3 = new NceSensor("3");
        a.registerSensor(s1, 0);
        a.registerSensor(s2, 1);
        a.registerSensor(s3, 2);
        a.markChanges(2);
        Assert.assertEquals("check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.ACTIVE, s3.getKnownState());
    }

    // from here down is testing infrastructure
    public NceAIUTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NceAIUTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NceAIUTest.class);
        return suite;
    }

}
