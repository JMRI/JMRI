package jmri.util;

import java.util.Vector;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class IterableEnumerationTest {

    @Test
    public void testCTor() {
        Vector<String> v = new Vector<>();
        IterableEnumeration<String> t = new IterableEnumeration<>(v.elements());
        assertNotNull( t, "exists");
    }

    @Test
    public void testIterate() {
        Vector<String> v = new Vector<>();
        v.addElement("Hello");
        v.addElement("World");
        v.addElement("From");
        v.addElement("JMRI");
        IterableEnumeration<String> t = new IterableEnumeration<>(v.elements());
        int x = 0;
        for(String s:t) {
           assertEquals( v.elementAt(x),s, "Element " + x);
           x++;
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

    // private final static Logger log = LoggerFactory.getLogger(IterableEnumerationTest.class);

}
