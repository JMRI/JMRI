//JmriServerActionTest.java
package jmri.jmris;

import apps.tests.Log4JFixture;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.JmriServerAction class
 *
 * @author Paul Bender
 */
public class JmriServerActionTest {

    @Test
    public void testCtorDefault() {
        JmriServerAction a = new JmriServerAction();
        Assert.assertNotNull(a);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        Log4JFixture.setUp();
    }

    @After
    public void tearDown() throws Exception {
        Log4JFixture.tearDown();
    }

}
