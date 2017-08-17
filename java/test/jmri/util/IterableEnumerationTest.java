package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Vector;

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
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(IterableEnumerationTest.class.getName());

}
