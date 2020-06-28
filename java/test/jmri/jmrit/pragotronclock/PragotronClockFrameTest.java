package jmri.jmrit.pragotronclock;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Petr Sidlo Copyright (C) 2019
 *
 * Based on NixieClockFrameTest by Paul Bender 
 */
public class PragotronClockFrameTest extends jmri.util.JmriJFrameTestBase {

    /**
     * Tests button is displayed and starts / stops clock.
     */
    @Test
    public void testButton(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.setVisible(true);
        java.util.Calendar cal = new java.util.GregorianCalendar();
        cal.set(2020, 5, 4, 0, 0, 00); // 02:00:00
        clock.setTime(cal.getTime());

        JFrameOperator jfo = new JFrameOperator( frame);
        Assert.assertTrue("run button found",
    new JButtonOperator(jfo,Bundle.getMessage("ButtonRunClock")).isEnabled());
        new JButtonOperator(jfo,Bundle.getMessage("ButtonRunClock")).doClick();
        Assert.assertTrue("clock started running",clock.getRun());

        new JButtonOperator(jfo,Bundle.getMessage("ButtonPauseClock")).doClick();
        Assert.assertFalse("clock paused running",clock.getRun());
        Assert.assertTrue("button back to run text",
    new JButtonOperator(jfo,Bundle.getMessage("ButtonRunClock")).isEnabled());

    }
    
    /**
     * Tests button is displayed and starts / stops clock.
     */
    @Test
    public void testMins(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.setVisible(true);
        java.util.Calendar cal = new java.util.GregorianCalendar();
        for (int i = 0; i<=59; i++) {
            cal.set(2020, 5, 4, Math.min(23, i), i, 00); // 02:00:00
            clock.setTime(cal.getTime());
            new org.netbeans.jemmy.QueueTool().waitEmpty();
            Assert.assertNotNull(frame);
        }
    }
    
    @Test
    public void testNoButton(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.dispose();
        clock.setShowStopButton(false);
        
        java.util.Calendar cal = new java.util.GregorianCalendar();
        cal.set(2020, 5, 4, 13, 33, 00); // 02:00:00
        clock.setTime(cal.getTime());
        
        frame = new PragotronClockFrame();
        frame.setVisible(true);
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        Assert.assertNotNull(frame);
        
    }
    
    public void testClockMinuteListener(){
        Assert.assertEquals("1 listener when clock started",1,clock.getMinuteChangeListeners().length);
        frame.dispose();
        Assert.assertEquals("0 listener when clock disposed",0,clock.getMinuteChangeListeners().length);
    }
    
    private jmri.Timebase clock;
    
    /**
     * Clock started paused.
     */
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
            // force time, not running
            clock = jmri.InstanceManager.getDefault(jmri.Timebase.class);
            clock.setRun(false);
            clock.setTime(java.time.Instant.EPOCH);  // just a specific time
            clock.setShowStopButton(true);
            frame = new PragotronClockFrame();
        }
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

    // private final static Logger log = LoggerFactory.getLogger(PragotronClockFrameTest.class);
}
