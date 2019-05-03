package jmri.jmrix.bachrus;

import java.awt.GraphicsEnvironment;
import org.netbeans.jemmy.operators.JFrameOperator;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SpeedoConsoleFrameTest extends jmri.util.JmriJFrameTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initDebugThrottleManager();
        if (!GraphicsEnvironment.isHeadless()) {
            SpeedoSystemConnectionMemo m = new SpeedoSystemConnectionMemo();
            SpeedoTrafficController tc = new SpeedoTrafficController(m);
            m.setSpeedoTrafficController(tc);
            frame = new SpeedoConsoleFrame(m);
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SpeedoConsoleFrameTest.class);

}
