package jmri.server.web;

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
public class AbstractWebServerConfigurationTest {
    
    public AbstractWebServerConfigurationTest() {
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


    /**
     * Test of getFilePaths method, of class AbstractWebServerConfiguration.
     */
    @Test
    public void testGetFilePaths() {
        AbstractWebServerConfiguration instance = new AbstractWebServerConfigurationImpl();
        Assert.assertNotNull("Empty Map", instance.getFilePaths());
        Assert.assertTrue("Empty Map", instance.getFilePaths().isEmpty());
    }

    /**
     * Test of getRedirectedPaths method, of class AbstractWebServerConfiguration.
     */
    @Test
    public void testGetRedirectedPaths() {
        AbstractWebServerConfiguration instance = new AbstractWebServerConfigurationImpl();
        Assert.assertNotNull("Empty Map", instance.getRedirectedPaths());
        Assert.assertTrue("Empty Map", instance.getRedirectedPaths().isEmpty());
    }

    /**
     * Test of getForbiddenPaths method, of class AbstractWebServerConfiguration.
     */
    @Test
    public void testGetForbiddenPaths() {
        AbstractWebServerConfiguration instance = new AbstractWebServerConfigurationImpl();
        Assert.assertNotNull("Empty List", instance.getForbiddenPaths());
        Assert.assertTrue("Empty List", instance.getForbiddenPaths().isEmpty());
    }

    public class AbstractWebServerConfigurationImpl extends AbstractWebServerConfiguration {
    }
    
}
