package jmri.jmrit.lcdclock;

import java.awt.GraphicsEnvironment;
import jmri.*;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LcdClockFrameTest extends jmri.util.JmriJFrameTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
            LcdClockFrame lcd;
            frame = lcd = new LcdClockFrame();
            
            // change, but don't check consequences of, run state
            Timebase clock = InstanceManager.getDefault(jmri.Timebase.class);
                        
            clock.setRun(true);

            clock.setRun(false);
            
            // pretend run/stop button clicked
            lcd.b.doClick();
            
        }
        
        
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LcdClockFrameTest.class);

}
