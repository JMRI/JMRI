// EditorFrameTest.java
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
 * @version         $Revision: 1.2 $
 */
public class EditorFrameTest extends TestCase {

    public void testShowPane() {
        new EditorFrame().setVisible(true);
    }

    public void testShowFile() {
        EditorFrame f = new EditorFrame();
        // select test file and open
        f.addFile(new java.io.File("java/test/jmri/jmrix/loconet/spjfile/test.spj"));
        f.setVisible(true);
        
        // show the SDF component
        f.pane.dataModel.viewSdfButtonPressed(null, 24, 5);
    }
    // from here down is testing infrastructure

    public EditorFrameTest(String s) {
    	super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EditorPaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EditorFrameTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EditorFrameTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
