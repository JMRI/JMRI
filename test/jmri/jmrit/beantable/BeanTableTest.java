// BeanTableTest.java

package jmri.jmrit.beantable;

import junit.framework.*;

/**
 * Tests for classes in the jmri.jmrit.beantable package
 * @author	Bob Jacobsen  Copyright 2004
 * @version	$Revision: 1.4 $
 */
public class BeanTableTest extends TestCase {

    public void testCreate() {
        new MemoryTableAction();
        new SignalHeadTableAction();
    }

    public void testExecute() {
        new MemoryTableAction().actionPerformed(null);
        new SignalHeadTableAction().actionPerformed(null);
    }

    // from here down is testing infrastructure

    public BeanTableTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {BeanTableTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(BeanTableTest.class);
        suite.addTest(RouteTableActionTest.suite());
        suite.addTest(BlockTableActionTest.suite());
        return suite;
    }
    
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BeanTableTest.class.getName());

}
