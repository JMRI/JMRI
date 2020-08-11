package jmri.server.web.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class WebServerConfigurationTest {

    public WebServerConfigurationTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

    }

    @AfterEach
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
