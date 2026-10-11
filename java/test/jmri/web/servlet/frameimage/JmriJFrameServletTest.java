package jmri.web.servlet.frameimage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jmri.util.JUnitUtil;

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
        assertNotNull(out.populateParameterMap(new HashMap<>()));
    }

    @Test
    public void testOneParameter() {
        Map<String, String[]> map = new java.util.HashMap<>();
        map.put("key1", new String[]{"value1-0"});

        JmriJFrameServlet_ut out = new JmriJFrameServlet_ut();

        out.populateParameterMap(map);

        assertNotNull(map);
        assertEquals( 1, map.size(), "parameters length");
        assertTrue( map.containsKey("key1"), "key1 present");
        assertEquals( "value1-0", map.get("key1")[0], "value[0]");

    }

    @Test
    public void testTwoParameters() {
        Map<String, String[]> map = new java.util.HashMap<>();
        map.put("key1", new String[]{"value1-0"});
        map.put("key2", new String[]{"value2-0"});

        JmriJFrameServlet_ut out = new JmriJFrameServlet_ut();

        map = out.populateParameterMap(map);

        assertNotNull(map);
        assertEquals( 2, map.size(), "parameters length");
        assertTrue( map.containsKey("key2"), "key2 present");
        assertEquals( "value2-0", map.get("key2")[0], "value2[0]");
        assertTrue( map.containsKey("key1"), "key1 present");
        assertEquals( "value1-0", map.get("key1")[0], "value1[0]");

    }

    @Test
    public void testExceptionHandling() {
        Throwable thrown;
        
        // create testable object that runs clean
        JmriJFrameServlet_ut out1 = new JmriJFrameServlet_ut() {
            @Override
            protected void doGetOnSwing(HttpServletRequest request, HttpServletResponse response) 
                    throws ServletException, IOException {
            }
        };
        // should not throw, so just invoke
        assertDoesNotThrow( () -> out1.assert_doGet() );
        
        // create testable object that throws IOException
        JmriJFrameServlet_ut out2 = new JmriJFrameServlet_ut() {
            @Override
            protected void doGetOnSwing(HttpServletRequest request, HttpServletResponse response) 
                    throws ServletException, IOException {
                throw new IOException("on purpose");
            }
        };
        // invoke and check
        thrown = assertThrows( IOException.class, () -> out2.assert_doGet() );
        assertNull(thrown.getCause());

        
        // create testable object that throws ServletException
        JmriJFrameServlet_ut out3 = new JmriJFrameServlet_ut() {
            @Override
            protected void doGetOnSwing(HttpServletRequest request, HttpServletResponse response) 
                    throws ServletException, IOException {
                throw new ServletException("on purpose");
            }
        };
        // invoke and check
        thrown = assertThrows( ServletException.class, () -> out3.assert_doGet() );
        assertNull(thrown.getCause());
    }
    
    
    // local variant class to make access to private members
    private class JmriJFrameServlet_ut extends JmriJFrameServlet {

        @Override
        public Map<String, String[]> populateParameterMap(Map<String, String[]> map) {
            return super.populateParameterMap(map);
        }

        void assert_doGet() throws ServletException, IOException {
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
