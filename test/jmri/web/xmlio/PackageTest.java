// PackageTest.java

package jmri.web.xmlio;

import junit.framework.*;

/**
 * Invokes complete set of tests in the jmri.web.xmlio tree
 *
 * @author	    Bob Jacobsen  Copyright 2008, 2009, 2010
 * @version         $Revision: 1.1 $
 */
public class PackageTest extends TestCase {

    // basic something
    public void testCtor() {
        new XmlIOFactory();
    }
        
    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class);
        suite.addTest(XmlIOFactoryTest.suite());
        suite.addTest(DefaultXmlIOServerTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
