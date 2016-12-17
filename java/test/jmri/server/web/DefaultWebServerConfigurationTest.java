package jmri.server.web;

import java.util.HashMap;
import java.util.List;
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
public class DefaultWebServerConfigurationTest {

    public DefaultWebServerConfigurationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstructor() {
        DefaultWebServerConfiguration instance = new DefaultWebServerConfiguration();
        Assert.assertNotNull("Default constructor", instance);
    }

    /**
     * Test of getFilePaths method, of class DefaultWebServerConfiguration.
     */
    @Test
    public void testGetFilePaths() {
        DefaultWebServerConfiguration instance = new DefaultWebServerConfiguration();
        HashMap<String, String> result = instance.getFilePaths();
        Assert.assertNotNull("Default file paths", result);
        Assert.assertEquals("Default file paths", 15, result.size());
    }

    /**
     * Test of getRedirectedPaths method, of class
     * DefaultWebServerConfiguration.
     */
    @Test
    public void testGetRedirectedPaths() {
        DefaultWebServerConfiguration instance = new DefaultWebServerConfiguration();
        HashMap<String, String> result = instance.getRedirectedPaths();
        Assert.assertNotNull("Default redirections", result);
        Assert.assertTrue("Default redirections", result.isEmpty());
    }

    /**
     * Test of getForbiddenPaths method, of class DefaultWebServerConfiguration.
     */
    @Test
    public void testGetForbiddenPaths() {
        DefaultWebServerConfiguration instance = new DefaultWebServerConfiguration();
        List<String> result = instance.getForbiddenPaths();
        Assert.assertNotNull("Default forbidden paths", result);
        Assert.assertTrue("Default forbidden paths", result.isEmpty());
    }

}
