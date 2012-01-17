// SerialMonTest.java

package jmri.jmrix.grapevine.serialmon;

import jmri.jmrix.grapevine.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.grapevine.serialmon package.
 * @author      Bob Jacobsen  Copyright 2003, 2007, 2008
 * @version   $Revision$
 */
public class SerialMonTest extends TestCase {

    // from here down is testing infrastructure

    public SerialMonTest(String s) {
        super(s);
    }

    public void testDisplay() throws Exception {
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
        // create a SerialMonFrame
        SerialMonFrame f = new SerialMonFrame(){
          { rawCheckBox.setSelected(true);}
        };
        f.initComponents();
        f.setVisible(true);
        
        // show stuff
        SerialMessage m = new SerialMessage();
        m.setOpCode(0x81);
        m.setElement(1, (byte)0x02);
        m.setElement(2, (byte)0xA2);
        m.setElement(3, (byte)0x31);
        
        f.message(m);
        
        // show stuff
        SerialReply r = new SerialReply();
        r.setOpCode(0x81);
        r.setElement(1, (byte)0x02);
        r.setElement(2, (byte)0xA2);
        r.setElement(3, (byte)0x31);
        
        f.reply(r);
        
        //close frame
        f.dispose();
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialMonTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(SerialMonTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
