package jmri.web.servlet.directory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DirectoryResourceTest {

    @Test
    public void testCTor() {
        DirectoryResource t = new DirectoryResource(java.util.Locale.US, org.eclipse.jetty.util.resource.EmptyResource.INSTANCE);
        Assert.assertNotNull("exists", t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DirectoryResourceTest.class.getName());

}
