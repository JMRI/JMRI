package jmri.jmrix.zimo.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.zimo.Mx1Listener;
import jmri.jmrix.zimo.Mx1Message;
import jmri.jmrix.zimo.Mx1SystemConnectionMemo;
import jmri.jmrix.zimo.Mx1TrafficController;
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
public class Mx1NamedPaneActionTest {

    private Mx1SystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Mx1 named Pane Action Test");
        Mx1NamedPaneAction t = new Mx1NamedPaneAction("Test Action",jf,"test",memo);
        Assert.assertNotNull("exists",t);
        jf.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        Mx1TrafficController tc = new Mx1TrafficController(){
           @Override
           public boolean status(){
              return true;
           }
           @Override
           public void sendMx1Message(Mx1Message m,Mx1Listener reply) {
           }
        };
        memo = new Mx1SystemConnectionMemo(tc);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Mx1NamedPaneActionTest.class);

}
