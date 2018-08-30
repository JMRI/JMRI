package jmri.server.web.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class WebServerConfigurationTest {

    public WebServerConfigurationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();

    }

    /**
     * Test of getFilePaths method, of class WebServerConfiguration.
     */
    @Test
    public void testGetFilePaths() {
        WebServerConfiguration instance = new WebServerConfigurationImpl();
        Assert.assertNotNull("Empty Map", instance.getFilePaths());
        Assert.assertTrue("Empty Map", instance.getFilePaths().isEmpty());
    }

    /**
     * Test of getRedirectedPaths method, of class WebServerConfiguration.
     */
    @Test
    public void testGetRedirectedPaths() {
        WebServerConfiguration instance = new WebServerConfigurationImpl();
        Assert.assertNotNull("Empty Map", instance.getRedirectedPaths());
        Assert.assertTrue("Empty Map", instance.getRedirectedPaths().isEmpty());
    }

    /**
     * Test of getForbiddenPaths method, of class WebServerConfiguration.
     */
    @Test
    public void testGetForbiddenPaths() {
        WebServerConfiguration instance = new WebServerConfigurationImpl();
        Assert.assertNotNull("Empty List", instance.getForbiddenPaths());
        Assert.assertTrue("Empty List", instance.getForbiddenPaths().isEmpty());
    }

    public class WebServerConfigurationImpl implements WebServerConfiguration {

        @Override
        public Map<String, String> getFilePaths() {
            return new HashMap<>();
        }

        @Override
        public Map<String, String> getRedirectedPaths() {
            return new HashMap<>();
        }

        @Override
        public List<String> getForbiddenPaths() {
            return new ArrayList<>();
        }
    }

}
