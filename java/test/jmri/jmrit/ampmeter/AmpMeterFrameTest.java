package jmri.jmrit.ampmeter;

import java.awt.GraphicsEnvironment;

import jmri.*;
import jmri.implementation.DefaultMeter;
import jmri.implementation.MeterUpdateTask;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AmpMeterFrameTest extends jmri.util.JmriJFrameTestBase {

    @Test
    public void testCurrentChange1Digit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        runOnLayout(() -> {
            frame.initComponents();
            jmri.InstanceManager.getDefault(jmri.MeterGroupManager.class)
                    .getNamedBeanSet().first()
                    .getMeterByName(MeterGroup.CurrentMeter)
                    .getMeter().setCommandedAnalogValue(2.1f);
        });
    }

    @Test
    public void testCurrentChange2Digit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        runOnLayout(() -> {
            frame.initComponents();
            jmri.InstanceManager.getDefault(jmri.MeterGroupManager.class)
                    .getNamedBeanSet().first()
                    .getMeterByName(MeterGroup.CurrentMeter)
                    .getMeter().setCommandedAnalogValue(32.1f);
        });
    }

    @Test
    public void testCurrentChange3Digit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        runOnLayout(() -> {
            frame.initComponents();
            jmri.InstanceManager.getDefault(jmri.MeterGroupManager.class)
                    .getNamedBeanSet().first()
                    .getMeterByName(MeterGroup.CurrentMeter)
                    .getMeter().setCommandedAnalogValue(432.1f);
        });
    }

    @Test
    public void testCurrentChange4Digit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        runOnLayout(() -> {
            frame.initComponents();
            jmri.InstanceManager.getDefault(jmri.MeterGroupManager.class)
                    .getNamedBeanSet().first()
                    .getMeterByName(MeterGroup.CurrentMeter)
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
            frame = new AmpMeterFrame();
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
            super("IVTestMeterGroup");
            
            Meter ampMeter = new DefaultMeter.DefaultVoltageMeter("IVAmpMeter", Meter.Unit.Milli, 0.0, 1000.0, 1.0, new MeterUpdateTask(-1, 0) {
                @Override
                public void requestUpdateFromLayout() {
                    // Do nothing
                }
            });
            
            addMeter(MeterGroup.CurrentMeter, MeterGroup.CurrentMeterDescr, ampMeter);
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
            return true;
        }

        @Override
        public boolean hasVoltage() {
            return false;
        }
/*
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
