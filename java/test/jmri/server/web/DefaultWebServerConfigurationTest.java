package jmri.server.web;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

/**
 * @author Randall Wood (C) 2016
 */
public class DefaultWebServerConfigurationTest {

    private DefaultWebServerConfiguration instance;

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        instance = new DefaultWebServerConfiguration();
    }

    @After
    public void tearDown() {
        instance = null;
        JUnitUtil.tearDown();
    }

    @Test
    public void testConstructor() {
        Assert.assertNotNull("Default constructor", instance);
    }

    /**
     * Test of getFilePaths method, of class DefaultWebServerConfiguration.
     */
    @Test
    public void testGetFilePaths() {
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
        HashMap<String, String> result = instance.getRedirectedPaths();
        Assert.assertNotNull("Default redirections", result);
        Assert.assertTrue("Default redirections", result.isEmpty());
    }

    /**
     * Test of getForbiddenPaths method, of class DefaultWebServerConfiguration.
     */
    @Test
    public void testGetForbiddenPaths() {
        List<String> result = instance.getForbiddenPaths();
        Assert.assertNotNull("Default forbidden paths", result);
        Assert.assertTrue("Default forbidden paths", result.isEmpty());
    }

    /**
     * Test of load method with missing/non-existent resource.
     * 
     * @throws SecurityException         if unable to use reflection
     * @throws NoSuchMethodException     if unable to use reflection
     * @throws InvocationTargetException if unable to use reflection
     * @throws IllegalArgumentException  if unable to use reflection
     * @throws IllegalAccessException    if unable to use reflection
     */
    @Test
    public void testLoadWithMissingResource() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        Class<?>[] classes = {HashMap.class, String.class};
        Object[] params = {new HashMap<String, String>(), "no.such.resource"};
        Method method = instance.getClass().getDeclaredMethod("loadMap", classes);
        method.setAccessible(true);
        method.invoke(instance, params);
        JUnitAppender.assertErrorMessage("Unable to load no.such.resource");
    }
}
