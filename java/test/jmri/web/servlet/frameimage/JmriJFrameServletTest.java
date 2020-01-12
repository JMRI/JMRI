package jmri.web.servlet.frameimage;

import java.util.HashMap;
import java.util.Map;
import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Invokes complete set of tests for the jmri.web.servlet.frameimage.JmriJFrameServlet class
 *
 * @author	Bob Jacobsen Copyright 2013
 */
public class JmriJFrameServletTest {

    @Test
    public void testAccess() {
        JmriJFrameServlet_ut out = new JmriJFrameServlet_ut();
        Assert.assertNotNull(out.populateParameterMap(new HashMap<>()));
    }

    @Test
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

    @Test
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

    // local variant class to make access to private members
    private class JmriJFrameServlet_ut extends JmriJFrameServlet {

        @Override
        public Map<String, String[]> populateParameterMap(Map<String, String[]> map) {
            return super.populateParameterMap(map);
        }
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
