// EditorPaneTest.java
package jmri.jmrix.loconet.sdfeditor;

import jmri.jmrix.loconet.sdf.SdfBuffer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.loconet.sdfeditor.EditorPane class.
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version $Revision$
 */
public class EditorPaneTest extends TestCase {

    public void testShowPane() throws java.io.IOException {
        SdfBuffer buff = new SdfBuffer("java/test/jmri/jmrix/loconet/sdf/test2.sdf");
        new EditorFrame(buff).setVisible(true);
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

    private final static Logger log = LoggerFactory.getLogger(EditorPaneTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
