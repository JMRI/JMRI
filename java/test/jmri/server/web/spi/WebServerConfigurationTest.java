package jmri.server.web.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class WebServerConfigurationTest {

    /**
     * Test of getFilePaths method, of class WebServerConfiguration.
     */
    @Test
    public void testGetFilePaths() {
        WebServerConfiguration instance = new WebServerConfigurationImpl();
        assertNotNull( instance.getFilePaths(), "Empty Map");
        assertTrue( instance.getFilePaths().isEmpty(), "Empty Map");
    }

    /**
     * Test of getRedirectedPaths method, of class WebServerConfiguration.
     */
    @Test
    public void testGetRedirectedPaths() {
        WebServerConfiguration instance = new WebServerConfigurationImpl();
        assertNotNull( instance.getRedirectedPaths(), "Empty Map");
        assertTrue( instance.getRedirectedPaths().isEmpty(), "Empty Map");
    }

    /**
     * Test of getForbiddenPaths method, of class WebServerConfiguration.
     */
    @Test
    public void testGetForbiddenPaths() {
        WebServerConfiguration instance = new WebServerConfigurationImpl();
        assertNotNull( instance.getForbiddenPaths(), "Empty List");
        assertTrue( instance.getForbiddenPaths().isEmpty(), "Empty List");
    }

    private static class WebServerConfigurationImpl implements WebServerConfiguration {

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

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
