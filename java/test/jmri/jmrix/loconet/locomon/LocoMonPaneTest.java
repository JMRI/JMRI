
package jmri.jmrix.loconet.locomon;

import jmri.jmrix.loconet.LocoNetMessage;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test of LocoMonPane
 * 
 * Initially written to test filtering
 *
 * @author	Bob Jacobsen   Copyright 2015
 */
public class LocoMonPaneTest extends jmri.util.SwingTestCase {

    public void testLifeCycle() throws Exception {
        // test runs lifecycle through setup, shutdown
    }

    public void testInput() throws Exception {
        LocoNetMessage m = new LocoNetMessage(new int[]{0xA0, 0x07, 0x00, 0x58});
        panel.message(m);
        flushAWT();
        Assert.assertEquals("shows message", "Set speed of loco in slot 7 to 0.\n", panel.getFrameText());
    }

    public void testFilterNot() throws Exception {
        // filter not match
        panel.setFilterText("A1");
        flushAWT();
        Assert.assertEquals("filter set", "A1", panel.getFilterText());
        
        LocoNetMessage m = new LocoNetMessage(new int[]{0xA0, 0x07, 0x00, 0x58});
        panel.message(m);
        flushAWT();
        Assert.assertEquals("shows message", "Set speed of loco in slot 7 to 0.\n", panel.getFrameText());
    }

    public void testFilterSimple() throws Exception {
        // filter A0
        panel.setFilterText("A0");
        flushAWT();
        Assert.assertEquals("filter set", "A0", panel.getFilterText());
        
        LocoNetMessage m = new LocoNetMessage(new int[]{0xA0, 0x07, 0x00, 0x58});
        panel.message(m);
        flushAWT();
        Assert.assertEquals("shows message", "", panel.getFrameText());
    }

    public void testFilterMultiple() throws Exception {
        // filter A0
        panel.setFilterText("B1 A0");
        flushAWT();
        Assert.assertEquals("filter set", "B1 A0", panel.getFilterText());
        
        LocoNetMessage m = new LocoNetMessage(new int[]{0xA0, 0x07, 0x00, 0x58});
        panel.message(m);
        flushAWT();
        Assert.assertEquals("shows message", "", panel.getFrameText());
    }

    // from here down is testing infrastructure

    public LocoMonPaneTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.Log4JFixture.initLogging();
        String[] testCaseName = {"-noloading", LocoMonPaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LocoMonPaneTest.class);
        return suite;
    }

    LocoMonPane panel;
    
    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initConfigureManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        
        panel = new LocoMonPane();  
        panel.initComponents();
    }

    protected void tearDown() {
        panel.dispose();
        
        apps.tests.Log4JFixture.tearDown();
    }
}
