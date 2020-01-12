package jmri.jmrit.ampmeter;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AmpMeterFrameTest extends jmri.util.JmriJFrameTestBase {

    @Test
    public void testCurrentChange1Digit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayout(() -> {
             frame.initComponents();
             jmri.InstanceManager.getDefault(jmri.MultiMeter.class).setCurrent(2.1f);
    	});
    }

    @Test
    public void testCurrentChange2Digit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayout(() -> {
             frame.initComponents();
             jmri.InstanceManager.getDefault(jmri.MultiMeter.class).setCurrent(32.1f);
    	});
    }

    @Test
    public void testCurrentChange3Digit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayout(() -> {
             frame.initComponents();
             jmri.InstanceManager.getDefault(jmri.MultiMeter.class).setCurrent(432.1f);
    	});
    }

    @Test
    public void testCurrentChange4Digit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayout(() -> {
             frame.initComponents();
             jmri.InstanceManager.getDefault(jmri.MultiMeter.class).setCurrent(5432.1f);
    	});
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.InstanceManager.setDefault(jmri.MultiMeter.class,new TestMeter());
        if(!GraphicsEnvironment.isHeadless()){
           frame = new AmpMeterFrame();
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    private class TestMeter extends jmri.implementation.AbstractMultiMeter {
             public TestMeter(){
               super(0);
             }
             @Override
             public void initializeHardwareMeter(){
             }
             @Override
             public void requestUpdateFromLayout(){
             }
             @Override
             public void dispose(){
             }
             @Override
             public boolean hasCurrent(){
                return true;
             }
             @Override
             public boolean hasVoltage(){
                return false;
             }
             @Override
             public CurrentUnits getCurrentUnits() {
                 return  CurrentUnits.CURRENT_UNITS_PERCENTAGE;
             }
             @Override
             public String getHardwareMeterName(){
                return "test";
             }
             @Override
             public void enable(){
             }
             @Override
             public void disable(){
             }
        }

    // private final static Logger log = LoggerFactory.getLogger(AmpMeterFrameTest.class);

}
