// MiniServletContextTest.java

package jmri.web.miniserver;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the MiniServletContext class.
 *
 * @author	    Bob Jacobsen  Copyright 2010
 * @version         $Revision$
 */
public class MiniServletContextTest extends TestCase {

    public void testSetGet() {
        MiniServletContext m = new MiniServletContext();
        m.setAttribute("key", "value");
        Assert.assertEquals("value", m.getAttribute("key"));
    }
    
    public void testRemove() {
        MiniServletContext m = new MiniServletContext();
        m.setAttribute("key", "value");
        m.removeAttribute("key");
        Assert.assertEquals(null, m.getAttribute("key"));
    }
    
    // from here down is testing infrastructure
    
    public MiniServletContextTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MiniServletContextTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MiniServletContextTest.class);
        return suite;
    }

    static protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MiniServletContextTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
