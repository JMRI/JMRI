package jmri.server.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

/**
 * @author Randall Wood (C) 2016
 */
public class DefaultWebServerConfigurationTest {

    private DefaultWebServerConfiguration instance;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        instance = new DefaultWebServerConfiguration();
    }

    @AfterEach
    public void tearDown() {
        instance = null;
        JUnitUtil.tearDown();
    }

    @Test
    public void testConstructor() {
        assertThat(instance).as("Default constructor").isNotNull();
    }

    /**
     * Test of getFilePaths method, of class DefaultWebServerConfiguration.
     */
    @Test
    public void testGetFilePaths() {
        assertThat(instance.getFilePaths())
                .as("Default file paths")
                .satisfies(paths -> {
                    assertThat(paths).isNotNull();
                    assertThat(paths.size()).isEqualTo(15);
                });
    }

    /**
     * Test of getRedirectedPaths method, of class
     * DefaultWebServerConfiguration.
     */
    @Test
    public void testGetRedirectedPaths() {
        assertThat(instance.getRedirectedPaths())
                .as("Default redirections")
                .satisfies(paths -> {
                    assertThat(paths).isNotNull();
                    assertThat(paths).isEmpty();
                });
    }

    /**
     * Test of getForbiddenPaths method, of class DefaultWebServerConfiguration.
     */
    @Test
    public void testGetForbiddenPaths() {
        assertThat(instance.getForbiddenPaths())
                .as("Default forbidden paths")
                .satisfies(paths -> {
                    assertThat(paths).isNotNull();
                    assertThat(paths).isEmpty();
                });
    }

    /**
     * Test of load method with missing/non-existent resource.
     * 
     * @throws Exception if unable to use reflection
     */
    @Test
    public void testLoadWithMissingResource() throws Exception {
        Method method = instance.getClass()
                .getDeclaredMethod("loadMap", HashMap.class, String.class);
        method.setAccessible(true);
        method.invoke(instance, new HashMap<String, String>(), "no.such.resource");
        JUnitAppender.assertErrorMessage("Unable to load no.such.resource");
    }
}
