package jmri.jmrit.ampmeter;

import java.awt.GraphicsEnvironment;
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
public class AmpMeterFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AmpMeterFrame t = new AmpMeterFrame();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.InstanceManager.setDefault(jmri.MultiMeter.class,new TestMeter());
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
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
                return false;
             }
             @Override
             public boolean hasVoltage(){
                return false;
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
