//ServiceHandlerTest.java
package jmri.jmris;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmris.ServiceHandler class 
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class ServiceHandlerTest extends TestCase {

    public void testCtorDefault() {
        ServiceHandler a = new ServiceHandler();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public ServiceHandlerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ServiceHandlerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(jmri.jmris.ServiceHandlerTest.class);

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

    static Logger log = LoggerFactory.getLogger(ServiceHandlerTest.class.getName());

}
