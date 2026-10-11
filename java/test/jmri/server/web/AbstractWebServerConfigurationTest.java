package jmri.server.web;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertNotNull( instance.getFilePaths(), "Empty Map");
        assertTrue( instance.getFilePaths().isEmpty(), "Empty Map");
    }

    /**
     * Test of getRedirectedPaths method, of class AbstractWebServerConfiguration.
     */
    @Test
    public void testGetRedirectedPaths() {
        AbstractWebServerConfiguration instance = new AbstractWebServerConfigurationImpl();
        assertNotNull( instance.getRedirectedPaths(), "Empty Map");
        assertTrue( instance.getRedirectedPaths().isEmpty(), "Empty Map");
    }

    /**
     * Test of getForbiddenPaths method, of class AbstractWebServerConfiguration.
     */
    @Test
    public void testGetForbiddenPaths() {
        AbstractWebServerConfiguration instance = new AbstractWebServerConfigurationImpl();
        assertNotNull( instance.getForbiddenPaths(), "Empty List");
        assertTrue( instance.getForbiddenPaths().isEmpty(), "Empty List");
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
