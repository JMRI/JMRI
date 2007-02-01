// RouteTableActionTest.java

package jmri.jmrit.beantable;

import junit.framework.*;

import javax.swing.JComboBox;


/**
 * Tests for the jmri.jmrit.beantable.RouteTableAction class
 * @author	Bob Jacobsen  Copyright 2004, 2007
 * @version	$Revision: 1.1 $
 */
public class RouteTableActionTest extends TestCase {

    public void testCreate() {
        new RouteTableAction();
    }

    public void testInvoke() {
        new RouteTableAction().actionPerformed(null);
    }


    // from here down is testing infrastructure

    public RouteTableActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {RouteTableActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RouteTableActionTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RouteTableActionTest.class.getName());

}
