// InitiateSoundTest.java
package jmri.jmrix.loconet.sdf;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.sdf.InitiateSound class.
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision$
 */
public class InitiateSoundTest extends TestCase {

    public void testCtor() {
        new InitiateSound((byte) 0, (byte) 0);
    }

    // from here down is testing infrastructure
    public InitiateSoundTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {InitiateSoundTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(InitiateSoundTest.class);
        return suite;
    }

}
