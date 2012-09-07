// MonitorFrameDemo.java

package jmri.jmrix.openlcb.swing.monitor;

import javax.swing.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.can.*;
/**
 * Tests for the jmri.jmrix.can.swing.monitor.MonitorFrame class
 *
 * @author      Bob Jacobsen  Copyright 2010
 * @version   $Revision$
 */
public class MonitorFrameDemo extends TestCase {

    String testFormatted;
    String testRaw;
    
    class OurScaffold extends TrafficControllerScaffold {
        /*
         * Forward CanMessage to object under test
         */
        public void testMessage(CanMessage f) {
            for ( jmri.jmrix.AbstractMRListener c : cmdListeners) {
                ((CanListener)c).message(f);
            }
        }
        public void testReply(CanReply f) {
            for ( jmri.jmrix.AbstractMRListener c : cmdListeners) {
                ((CanListener)c).reply(f);
            }
        }
    }
    
    public void testFireViaAction () throws Exception {
        // skip if headless, as requires display to show
        if (System.getProperty("jmri.headlesstest","false").equals("true")) return;
                
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
        OurScaffold tcs = new OurScaffold();
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcs);

        new MonitorPane.Default().actionPerformed(null);

        jmri.jmrix.can.CanMessage msg 
            = new jmri.jmrix.can.CanMessage(
                new int[]{1,2}, 0x12345678);
        msg.setExtended(true);
        
        tcs.testMessage(msg);

        jmri.jmrix.can.CanReply reply
            = new jmri.jmrix.can.CanReply(
                new int[]{1,2});
        reply.setExtended(true);
        reply.setHeader(0x12345678);

        tcs.testReply(reply);
        
    }
    
    public void XtestFormatMsg() throws Exception {
        // skip if headless, as requires display to show
        if (System.getProperty("jmri.headlesstest","false").equals("true")) return;
                
        TrafficControllerScaffold tcs = new TrafficControllerScaffold();

        MonitorPane f = new MonitorPane(){
            public void nextLine(String s1, String s2) {
                testFormatted = s1;
                testRaw = s2;
            }
        };
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcs);
        f.initComponents(memo);
        
        jmri.jmrix.can.CanMessage msg 
            = new jmri.jmrix.can.CanMessage(
                new int[]{1,2}, 0x12345678);
        msg.setExtended(true);
        
        f.message(msg);
        
        JFrame frame = new JFrame("Monitor OpenLCB message");
        frame.setLayout(new java.awt.FlowLayout());
        frame.add(f);
        frame.pack();
        frame.setVisible(true);
        
        Assert.assertEquals("formatted", "M: [12345678] 01 02\n", testFormatted);
        Assert.assertEquals("raw", "01 02", testRaw);
        memo.dispose();
    }
    
    public void XtestFormatReply() throws Exception {
        // skip if headless, as requires display to show
        if (System.getProperty("jmri.headlesstest","false").equals("true")) return;
        
        TrafficControllerScaffold tcs = new TrafficControllerScaffold();

        MonitorPane f = new MonitorPane(){
            public void nextLine(String s1, String s2) {
                testFormatted = s1;
                testRaw = s2;
            }
        };
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcs);
        f.initComponents(memo);
        
        jmri.jmrix.can.CanReply msg 
            = new jmri.jmrix.can.CanReply(
                new int[]{1,2});
        msg.setExtended(true);
        msg.setHeader(0x12345678);
        
        f.setVisible(true);

        f.reply(msg);
        
        JFrame frame = new JFrame("Monitor OpenLCB message");
        frame.setLayout(new java.awt.FlowLayout());
        frame.add(f);
        frame.pack();
        frame.setVisible(true);

        Assert.assertEquals("formatted", "R: [12345678] 01 02\n", testFormatted);
        Assert.assertEquals("raw", "01 02", testRaw);
        memo.dispose();
    }
    
    // from here down is testing infrastructure

    public MonitorFrameDemo(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", MonitorFrameDemo.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(MonitorFrameDemo.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { 
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.setUp(); 
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
