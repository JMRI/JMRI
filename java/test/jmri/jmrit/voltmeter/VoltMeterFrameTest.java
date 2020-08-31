package jmri.jmrit.voltmeter;

import java.awt.GraphicsEnvironment;

import jmri.JmriException;
import jmri.Meter;
import jmri.MeterGroup;
import jmri.implementation.DefaultMeter;
import jmri.implementation.MeterUpdateTask;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 *
 * Copied from ampmeter
 * @author Andrew Crosland Copyright (C) 2020
 */
public class VoltMeterFrameTest extends jmri.util.JmriJFrameTestBase {

    @Test
    public void testVoltageChange1Digit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        runOnLayout(() -> {
            frame.initComponents();
            jmri.InstanceManager.getDefault(jmri.MeterGroupManager.class)
                    .getNamedBeanSet().first()
                    .getMeterByName(MeterGroup.VoltageMeter)
                    .getMeter().setCommandedAnalogValue(2.1f);
        });
    }

    @Test
    public void testVoltageChange2Digit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        runOnLayout(() -> {
            frame.initComponents();
            jmri.InstanceManager.getDefault(jmri.MeterGroupManager.class)
                    .getNamedBeanSet().first()
                    .getMeterByName(MeterGroup.VoltageMeter)
                    .getMeter().setCommandedAnalogValue(32.1f);
        });
    }

    @Test
    public void testVoltageChange3Digit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        runOnLayout(() -> {
            frame.initComponents();
            jmri.InstanceManager.getDefault(jmri.MeterGroupManager.class)
                    .getNamedBeanSet().first()
                    .getMeterByName(MeterGroup.VoltageMeter)
                    .getMeter().setCommandedAnalogValue(432.1f);
        });
    }

    @Test
    public void testVoltageChange4Digit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        runOnLayout(() -> {
            frame.initComponents();
            jmri.InstanceManager.getDefault(jmri.MeterGroupManager.class)
                    .getNamedBeanSet().first()
                    .getMeterByName(MeterGroup.VoltageMeter)
                    .getMeter().setCommandedAnalogValue(5432.1f);
        });
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.InstanceManager.getDefault(jmri.MeterGroupManager.class).register(new TestMeter());
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new VoltMeterFrame();
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }
    
    private void runOnLayout(RunnableWithException r) {
        ThreadingUtil.runOnLayout(() -> {
            try {
                r.run();
            } catch (JmriException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private class TestMeter extends jmri.implementation.DefaultMeterGroup {

        public TestMeter() {
            super("IVTestMeter");
            
            Meter voltMeter = new DefaultMeter.DefaultVoltageMeter("IVVoltMeter", Meter.Unit.Milli, 0.0, 1000.0, 1.0, new MeterUpdateTask(-1, 0) {
                @Override
                public void requestUpdateFromLayout() {
                    // Do nothing
                }
            });
            
            addMeter(MeterGroup.VoltageMeter, MeterGroup.VoltageMeterDescr, voltMeter);
        }
/*
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
*/
    }

    private interface RunnableWithException {
        public void run() throws JmriException;
    }
    
    // private final static Logger log = LoggerFactory.getLogger(AmpMeterFrameTest.class);
}
