package jmri.web.servlet.frameimage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jmri.util.JUnitUtil;

import static org.assertj.core.api.Assertions.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Invokes complete set of tests for the jmri.web.servlet.frameimage.JmriJFrameServlet class
 *
 * @author Bob Jacobsen Copyright 2013
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

    @Test
    public void testExceptionHandling() {
        Throwable thrown;
        
        // create testable object that runs clean
        JmriJFrameServlet_ut out1 = new JmriJFrameServlet_ut() {
            protected void doGetOnSwing(HttpServletRequest request, HttpServletResponse response) 
                    throws ServletException, IOException {
            }
        };
        // should not throw, so just invoke
        try {
            out1.test_doGet();
        } catch (Exception ex) {
            Assert.fail(ex.toString());
        }
        
        // create testable object that throws IOException
        JmriJFrameServlet_ut out2 = new JmriJFrameServlet_ut() {
            protected void doGetOnSwing(HttpServletRequest request, HttpServletResponse response) 
                    throws ServletException, IOException {
                throw new IOException("on purpose");
            }
        };
        // invoke and check
        thrown = catchThrowable(() -> { out2.test_doGet(); });
        assertThat(thrown).isInstanceOf(IOException.class)
                            .hasNoCause();

        
        // create testable object that throws ServletException
        JmriJFrameServlet_ut out3 = new JmriJFrameServlet_ut() {
            protected void doGetOnSwing(HttpServletRequest request, HttpServletResponse response) 
                    throws ServletException, IOException {
                throw new ServletException("on purpose");
            }
        };
        // invoke and check
        thrown = catchThrowable(() -> { out3.test_doGet(); });
        assertThat(thrown).isInstanceOf(ServletException.class)
                            .hasNoCause();
    }
    
    
    // local variant class to make access to private members
    private class JmriJFrameServlet_ut extends JmriJFrameServlet {

        @Override
        public Map<String, String[]> populateParameterMap(Map<String, String[]> map) {
            return super.populateParameterMap(map);
        }

        public void test_doGet() throws ServletException, IOException {
            doGet(null, null);
        }
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
