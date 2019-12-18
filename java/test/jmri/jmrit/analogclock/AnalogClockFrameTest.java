package jmri.jmrit.analogclock;

import java.awt.GraphicsEnvironment;
import jmri.*;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AnalogClockFrameTest extends jmri.util.JmriJFrameTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
            // force time, not running
            Timebase clock = InstanceManager.getDefault(jmri.Timebase.class);
            clock.setRun(false);
            clock.setTime(java.time.Instant.EPOCH);  // just a specific time

            AnalogClockFrame face;
            frame = face = new AnalogClockFrame();

            // change, but don't check consequences of, run state
                        
            clock.setRun(true);

            clock.setRun(false);
            
            // pretend run/stop button clicked
            face.b.doClick();

        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AnalogClockFrameTest.class);
}
