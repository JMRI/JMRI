package jmri.web.servlet.directory;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DirectoryResourceTest.class);

}
