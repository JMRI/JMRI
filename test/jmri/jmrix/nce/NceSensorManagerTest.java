// NceSensorManagerTest.java

package jmri.jmrix.nce;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the NceAIU class.
 * @author	Bob Jacobsen Copyright 2002
 * @version	$Revision: 1.6 $
 */
public class NceSensorManagerTest extends TestCase {

    public void testCtor() {
        NceSensorManager s = new NceSensorManager();
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
