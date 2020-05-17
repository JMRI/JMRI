package jmri.jmrit.voltmeter;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import org.junit.*;

/**
 *
 * Copied from ampmeter
 * @author Andrew Crosland Copyright (C) 2020
 */
public class VoltMeterFrameTest extends jmri.util.JmriJFrameTestBase {

    @Test
    public void testVoltageChange1Digit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayout(() -> {
            frame.initComponents();
            jmri.InstanceManager.getDefault(jmri.MultiMeter.class).setVoltage(2.1f);
        });
    }

    @Test
    public void testVoltageChange2Digit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayout(() -> {
            frame.initComponents();
            jmri.InstanceManager.getDefault(jmri.MultiMeter.class).setVoltage(32.1f);
        });
    }

    @Test
    public void testVoltageChange3Digit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayout(() -> {
            frame.initComponents();
            jmri.InstanceManager.getDefault(jmri.MultiMeter.class).setVoltage(432.1f);
        });
    }

    @Test
    public void testVoltageChange4Digit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayout(() -> {
            frame.initComponents();
            jmri.InstanceManager.getDefault(jmri.MultiMeter.class).setVoltage(5432.1f);
        });
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.InstanceManager.setDefault(jmri.MultiMeter.class, new TestMeter());
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new VoltMeterFrame();
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    private class TestMeter extends jmri.implementation.AbstractMultiMeter {

        public TestMeter() {
            super(0);
        }

        @Override
        public void initializeHardwareMeter() {
        }

        @Override
        public void requestUpdateFromLayout() {
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean hasCurrent() {
            return false;
        }

        @Override
        public boolean hasVoltage() {
            return true;
        }

        @Override
        public CurrentUnits getCurrentUnits() {
            return CurrentUnits.CURRENT_UNITS_PERCENTAGE;
        }

        @Override
        public String getHardwareMeterName() {
            return "test";
        }

        @Override
        public void enable() {
        }

        @Override
        public void disable() {
        }
    }

    // private final static Logger log = LoggerFactory.getLogger(AmpMeterFrameTest.class);
}
