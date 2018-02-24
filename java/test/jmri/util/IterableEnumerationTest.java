package jmri.util;

import java.util.Vector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class IterableEnumerationTest {

    @Test
    public void testCTor() {
        Vector<String> v = new Vector<>();
        IterableEnumeration<String> t = new IterableEnumeration<>(v.elements());
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testIterate() {
        Vector<String> v = new Vector<>();
        v.addElement("Hello");
        v.addElement("World");
        v.addElement("From");
        v.addElement("JMRI");
        IterableEnumeration<String> t = new IterableEnumeration<String>(v.elements());
        int x = 0;
        for(String s:t) {
           Assert.assertEquals("Element " + x,v.elementAt(x),s);
           x++;
        }
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(IterableEnumerationTest.class);

}
