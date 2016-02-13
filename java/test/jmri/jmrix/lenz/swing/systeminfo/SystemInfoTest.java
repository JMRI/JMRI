// SystemInfoTest.java
package jmri.jmrix.lenz.swing.systeminfo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.swing.systeminfo package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class SystemInfoTest extends TestCase {

    // from here down is testing infrastructure
    public SystemInfoTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SystemInfoTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.swing.systeminfo.SystemInfoTest");  // no tests in this class itself
        suite.addTest(new TestSuite(SystemInfoFrameTest.class));
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(SystemInfoTest.class.getName());

}
