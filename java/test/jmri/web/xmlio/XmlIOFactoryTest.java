// XmlIOFactoryTest.java

package jmri.web.xmlio;

import junit.framework.*;

/**
 * Invokes complete set of tests in the jmri.web.xmlio.XmlIOFactoryTest class
 *
 * @author	    Bob Jacobsen  Copyright 2008, 2009, 2010
 * @version         $Revision$
 */
public class XmlIOFactoryTest extends TestCase {

    // basic something
    public void testCtor() {
        new XmlIOFactory();
    }
    
    public void testGetServer() {
        XmlIOServer s = new XmlIOFactory().getServer();
        Assert.assertTrue(s != null);
    }
    
    public void testUnique() {
        XmlIOFactory x = new XmlIOFactory();
        XmlIOServer s1 = x.getServer();
        XmlIOServer s2 = x.getServer();
        Assert.assertTrue(s1 != s2);
    }
    
    // from here down is testing infrastructure
    public XmlIOFactoryTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {XmlIOFactoryTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XmlIOFactoryTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
