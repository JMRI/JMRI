//JmriServerParserTests.java
package jmri.jmris.simpleserver.parser;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmris.simpleserver.parser package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class JmriServerParserTests extends TestCase {

    // from here down is testing infrastructure
    public JmriServerParserTests(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JmriServerParserTests.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmris.simpleserver.JmriServerParserTests");  // no tests in this class itself
        suite.addTest(new TestSuite(JmriServerTokenizerTest.class));
        suite.addTest(new TestSuite(JmriServerParserTest.class));
        //suite.addTest(new TestSuite(JmriServerVisitorTest.class));

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            // put any tests that require a UI here.
        }

        return suite;
    }

    static Logger log = LoggerFactory.getLogger(JmriServerParserTests.class.getName());

}
