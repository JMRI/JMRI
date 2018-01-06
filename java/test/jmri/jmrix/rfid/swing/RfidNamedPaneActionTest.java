package jmri.jmrix.rfid.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RfidNamedPaneActionTest {
   
    private RfidSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Rfid Named Pane Test");
        RfidNamedPaneAction t = new RfidNamedPaneAction("Test Action",jf,"test",memo);
        Assert.assertNotNull("exists",t);
        jf.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new RfidSystemConnectionMemo();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RfidNamedPaneActionTest.class);

}
