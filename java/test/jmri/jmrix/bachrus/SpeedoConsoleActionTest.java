package jmri.jmrix.bachrus;

import java.awt.GraphicsEnvironment;
import org.netbeans.jemmy.operators.JFrameOperator;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SpeedoConsoleActionTest {

    @Test
    public void testCTor() {
        SpeedoSystemConnectionMemo m = new SpeedoSystemConnectionMemo();
        SpeedoConsoleAction t = new SpeedoConsoleAction("test",m);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedoSystemConnectionMemo m = new SpeedoSystemConnectionMemo();
        SpeedoTrafficController tc = new SpeedoTrafficController(m);
        m.setSpeedoTrafficController(tc);
        SpeedoConsoleAction t = new SpeedoConsoleAction("test",m);
        jmri.util.ThreadingUtil.runOnLayout( () ->{ t.actionPerformed(new java.awt.event.ActionEvent(this,1,"test action event")); } );
        // find the resulting frame
        javax.swing.JFrame f = JFrameOperator.waitJFrame(Bundle.getMessage("SpeedoConsole"), true, true);
        Assert.assertNotNull("found output frame", f);
        // then close the frame.
        JFrameOperator fo = new JFrameOperator(f);
        fo.requestClose();
        JUnitUtil.dispose(f);
    }



    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SpeedoConsoleActionTest.class);

}
