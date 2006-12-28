// EditorPaneTest.java
package jmri.jmrix.loconet.soundloader;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.loconet.LocoNetMessage;


/**
 * Tests for the jmri.jmrix.loconet.soundloader.EditorPane class.
 *
 * @author			Bob Jacobsen  Copyright 2001, 2002, 2006
 * @version         $Revision: 1.1 $
 */
public class EditorPaneTest extends TestCase {

    public void testShowPane() {
        new EditorFrame().show();
    }

    // from here down is testing infrastructure

    public EditorPaneTest(String s) {
    	super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {EditorPaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EditorPaneTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EditorPaneTest.class.getName());

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
