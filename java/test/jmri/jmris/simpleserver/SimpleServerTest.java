//SimpleServerTest.java
package jmri.jmris.simpleserver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.jmris.simpleserver.parser.JmriServerParserTests.class,
   SimpleTurnoutServerTest.class,
   SimplePowerServerTest.class,
   SimpleReporterServerTest.class,
   SimpleSensorServerTest.class,
   SimpleLightServerTest.class,
   SimpleSignalHeadServerTest.class,
   SimpleOperationsServerTest.class,
   SimpleServerManagerTest.class,
   BundleTest.class,
   SimpleServerFrameTest.class,
   SimpleServerActionTest.class
})

/**
 * Tests for the jmri.jmris.simpleserver package
 *
 * @author Paul Bender
 */
public class SimpleServerTest {

    @Test
    public void testCtor() {
        SimpleServer a = new SimpleServer();
        Assert.assertNotNull(a);
        jmri.util.JUnitAppender.suppressErrorMessage("Failed to connect to port 2048");
    }

    @Test
    public void testCtorwithParameter() {
        SimpleServer a = new SimpleServer(2048);
        Assert.assertNotNull(a);
        jmri.util.JUnitAppender.suppressErrorMessage("Failed to connect to port 2048");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
    }

}
