//JmriServerTest.java
package jmri.jmris;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmris.JmriServer class 
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class JmriServerTest extends TestCase {

    public void testCtorDefault() {
        JmriServer a = new JmriServer();
        Assert.assertNotNull(a);
    }

    public void testCtorPort() {
        JmriServer a = new JmriServer(25520);
        Assert.assertNotNull(a);
    }

    public void testCtorPortAndTimeout() {
        JmriServer a = new JmriServer(25520,100);
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public JmriServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JmriServerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(jmri.jmris.JmriServerTest.class);

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(JmriServerTest.class.getName());

}
