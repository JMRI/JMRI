// XNetSimulatorTest.java
package jmri.jmrix.lenz.xnetsimulator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.xnetsimulator package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class XNetSimulatorTest extends TestCase {

    // from here down is testing infrastructure
    public XNetSimulatorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {XNetSimulatorTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.xnetsimulator.XNetSimulatorTest");  // no tests in this class itself
        suite.addTest(new TestSuite(XNetSimulatorAdapterTest.class));
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(XNetSimulatorTest.class.getName());

}
