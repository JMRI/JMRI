package jmri.server.web;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class AbstractWebServerConfigurationTest {

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

    private static class AbstractWebServerConfigurationImpl extends AbstractWebServerConfiguration {
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
