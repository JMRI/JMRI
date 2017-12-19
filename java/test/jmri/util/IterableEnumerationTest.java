package jmri.util;

import java.util.Vector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class IterableEnumerationTest {

    @Test
    public void testCTor() {
        Vector v = new Vector<String>();
        IterableEnumeration t = new IterableEnumeration(v.elements());
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testIterate() {
        Vector v = new Vector<String>();
        v.addElement("Hello");
        v.addElement("World");
        v.addElement("From");
        v.addElement("JMRI");
        IterableEnumeration<String> t = new IterableEnumeration<String>(v.elements());
        int x =0;
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
