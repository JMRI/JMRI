package jmri.web.servlet.frameimage;

import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests for the jmri.web.xmlio.JmriJFrameServlet class
 *
 * @author	Bob Jacobsen Copyright 2013
 */
public class JmriJFrameServletTest extends TestCase {

    public void testAccess() {
        JmriJFrameServlet_ut out = new JmriJFrameServlet_ut();
        Assert.assertNotNull(out.populateParameterMap(new HashMap<>()));
    }

    public void testOneParameter() {
        Map<String, String[]> map = new java.util.HashMap<>();
        map.put("key1", new String[]{"value1-0"});

        JmriJFrameServlet_ut out = new JmriJFrameServlet_ut();

        out.populateParameterMap(map);

        Assert.assertNotNull(map);
        Assert.assertEquals("parameters length", 1, map.size());
        Assert.assertTrue("key1 present", map.containsKey("key1"));
        Assert.assertEquals("value[0]", "value1-0", map.get("key1")[0]);

    }

    public void testTwoParameters() {
        Map<String, String[]> map = new java.util.HashMap<>();
        map.put("key1", new String[]{"value1-0"});
        map.put("key2", new String[]{"value2-0"});

        JmriJFrameServlet_ut out = new JmriJFrameServlet_ut();

        map = out.populateParameterMap(map);

        Assert.assertNotNull(map);
        Assert.assertEquals("parameters length", 2, map.size());
        Assert.assertTrue("key2 present", map.containsKey("key2"));
        Assert.assertEquals("value2[0]", "value2-0", map.get("key2")[0]);
        Assert.assertTrue("key1 present", map.containsKey("key1"));
        Assert.assertEquals("value1[0]", "value1-0", map.get("key1")[0]);

    }

    // local varient class to make access to private members
    private class JmriJFrameServlet_ut extends JmriJFrameServlet {

        @Override
        public Map<String, String[]> populateParameterMap(Map<String, String[]> map) {
            return super.populateParameterMap(map);
        }
    }

    // from here down is testing infrastructure
    public JmriJFrameServletTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JmriJFrameServletTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JmriJFrameServletTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @Override
    protected void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
