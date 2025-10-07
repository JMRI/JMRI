package jmri.jmrit.analogclock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfHeadless
public class AnalogClockFrameTest extends jmri.util.JmriJFrameTestBase {

    /**
     * Tests button is displayed and starts / stops clock.
     */
    @Test
    public void testButton(){

        ThreadingUtil.runOnGUI( () -> frame.setVisible(true));
        java.util.Calendar cal = new java.util.GregorianCalendar();
        cal.set(2020, 5, 4, 0, 0, 00); // 02:00:00
        clock.setTime(cal.getTime());

        JFrameOperator jfo = new JFrameOperator( frame);
        assertTrue( new JButtonOperator(jfo,Bundle.getMessage("ButtonRunClock")).isEnabled(),
            "run button found");
        new JButtonOperator(jfo,Bundle.getMessage("ButtonRunClock")).doClick();
        assertTrue( clock.getRun(), "clock started running");

        new JButtonOperator(jfo,Bundle.getMessage("ButtonPauseClock")).doClick();
        assertFalse( clock.getRun(), "clock paused running");
        assertTrue( new JButtonOperator(jfo,Bundle.getMessage("ButtonRunClock")).isEnabled(),
            "button back to run text");

    }
    
    /**
     * Tests button is displayed and starts / stops clock.
     */
    @Test
    public void testMins(){

        ThreadingUtil.runOnGUI( () -> frame.setVisible(true));
        java.util.Calendar cal = new java.util.GregorianCalendar();

        cal.set(2020, 5, 4, 0, 0, 00); // 02:00:00
        clock.setTime(cal.getTime());
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        assertNotNull(frame);

        cal.set(2020, 5, 4, 12, 0, 00); // 02:00:00
        clock.setTime(cal.getTime());
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        assertNotNull(frame);

        cal.set(2020, 5, 4, 13, 0, 00); // 02:00:00
        clock.setTime(cal.getTime());
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        assertNotNull(frame);

        cal.set(2020, 5, 4, 00, 27, 00); // 02:00:00
        clock.setTime(cal.getTime());
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        assertNotNull(frame);

        for (int i = 0; i<=59; i++) {
            cal.set(2020, 5, 4, Math.min(23, i), i, 00); // 02:00:00
            clock.setTime(cal.getTime());
            new org.netbeans.jemmy.QueueTool().waitEmpty();
            assertNotNull(frame);
        }

    }

    @Test
    public void testNoButton(){

        frame.dispose();
        clock.setShowStopButton(false);

        java.util.Calendar cal = new java.util.GregorianCalendar();
        cal.set(2020, 5, 4, 12, 00, 00); // 02:00:00
        clock.setTime(cal.getTime());

        frame = new AnalogClockFrame();
        ThreadingUtil.runOnGUI( () -> frame.setVisible(true));
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        assertNotNull(frame);
        
    }

    @Test
    public void testClockMinuteListener(){
        assertEquals( 1, clock.getMinuteChangeListeners().length, "1 listener when clock started");
        frame.dispose();
        assertEquals( 0, clock.getMinuteChangeListeners().length, "0 listener when clock disposed");
    }

    private jmri.Timebase clock;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        // force time, not running
        clock = InstanceManager.getDefault(jmri.Timebase.class);
        clock.setRun(false);
        clock.setTime(java.time.Instant.EPOCH);  // just a specific time
        clock.setShowStopButton(true);
        frame = new AnalogClockFrame();

    }

    @AfterEach
    @Override
    public void tearDown() {
        if(clock!=null){
            clock.dispose();
        }
        clock = null;
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AnalogClockFrameTest.class);
}
