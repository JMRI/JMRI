// LIUSBTest.java
package jmri.jmrix.lenz.swing.liusb;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.swing.liusb package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class LIUSBTest extends TestCase {

    // from here down is testing infrastructure
    public LIUSBTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LIUSBTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.liusb.swing.LIUSBTest");  // no tests in this class itself
        suite.addTest(new TestSuite(LIUSBConfigFrameTest.class));
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(LIUSBTest.class.getName());

}
