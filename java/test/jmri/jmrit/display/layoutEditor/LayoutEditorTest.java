// LayoutEditorTest.java
package jmri.jmrit.display.layoutEditor;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import jmri.util.JUnitUtil;

/**
 * Test simple functioning of LayoutEditor
 *
 * @author	Paul Bender Copyright (C) 2015
 * @version	$Revision$
 */
public class LayoutEditorTest extends TestCase {

    public void testCtor() {
        LayoutEditor  e = new LayoutEditor();
        Assert.assertNotNull("exists", e );
    }

    public void testStringCtor() {
        LayoutEditor  e = new LayoutEditor("Test Layout");
        Assert.assertNotNull("exists", e );
    }

    public void testGetFinder() {
        LayoutEditor e = new LayoutEditor();
        LayoutEditorFindItems f = e.getFinder();
        Assert.assertNotNull("exists", f );
    }

    public void testSetSize() {
        LayoutEditor e = new LayoutEditor();
        e.setSize(100,100);
        java.awt.Dimension d = e.getSize();
        // the java.awt.Dimension stores the values as floating point
        // numbers, but setSize expects integer parameters.
        Assert.assertEquals("Width Set", 100.0, d.getWidth());
        Assert.assertEquals("Height Set", 100.0, d.getHeight());
    }

    // from here down is testing infrastructure


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        // dispose of the single PanelMenu instance
        jmri.jmrit.display.PanelMenu.instance().dispose();
        // reset the instance manager. 
        JUnitUtil.resetInstanceManager();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // dispose of the single PanelMenu instance
        jmri.jmrit.display.PanelMenu.instance().dispose();
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }




    public LayoutEditorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LayoutEditorTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LayoutEditorTest.class);
        return suite;
    }

}
