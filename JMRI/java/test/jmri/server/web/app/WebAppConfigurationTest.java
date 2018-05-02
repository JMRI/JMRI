package jmri.server.web.app;

import apps.tests.Log4JFixture;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood (C) 2017
 */
public class WebAppConfigurationTest {

    @Before
    public void setUp() {
        Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        Log4JFixture.tearDown();
    }

    @Test
    public void testGetFilePaths() {
        WebAppConfiguration config = new WebAppConfiguration();
        Assert.assertNotNull(config.getFilePaths());
        Assert.assertEquals(3, config.getFilePaths().size());
        Assert.assertTrue(config.getFilePaths().containsKey("/app/node_modules"));
        Assert.assertTrue(config.getFilePaths().containsKey("/app/app"));
        Assert.assertTrue(config.getFilePaths().containsKey("/app/short-detector"));
    }

    @Test
    public void testGetForbiddenPaths() {
        WebAppConfiguration config = new WebAppConfiguration();
        Assert.assertNotNull(config.getForbiddenPaths());
        Assert.assertEquals(0, config.getForbiddenPaths().size());
    }

    @Test
    public void testGetRedirectedPaths() {
        WebAppConfiguration config = new WebAppConfiguration();
        Assert.assertNotNull(config.getRedirectedPaths());
        Assert.assertEquals(0, config.getRedirectedPaths().size());
    }

}
