package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.*;
import jmri.util.*;

import org.junit.jupiter.api.*;

/**
 * Tests for DefaultAverageMeter
 *
 * @author Daniel Bergqvist Copyright (C) 2025
 */
public class DefaultAverageMeterTest {

    @Test
    public void testState() {
        final int COUNT = 20;
        double[] expectedValues = {0.025, 0.075, 0.150, 0.250, 0.350, 0.450, 0.550, 0.650, 0.750, 0.850, 0.950, 1.050, 1.150, 1.250, 1.350, 1.450, 1.550, 1.650, 1.750, 1.850};
        assertEquals(COUNT, expectedValues.length);
        MyMeter m = new MyMeter();
        MeterManager mm = InstanceManager.getDefault(MeterManager.class);
        AverageMeter am = ((HasAverageMeter)mm).newAverageMeter("IMAverage", null, m);
        for (int i=0; i < COUNT; i++) {
            JUnitUtil.waitFor(m::isRead, "m is read");
            assertEquals( expectedValues[i], am.getKnownAnalogValue(), 0.00001,
                String.format("Meter has correct value for value %d", i));
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }


    private static class MyMeter extends DefaultMeter {

        private double _value = 0.0;
        private boolean _isRead = false;

        MyMeter() {
            super("MySystemName", Meter.Unit.NoPrefix, 0, 100, 1, null);
        }

        /** {@inheritDoc} */
        @Override
        public double getKnownAnalogValue() {
            _isRead = true;
            _value += 0.1;
            return _value;
        }

        public boolean isRead() {
            boolean isRead = _isRead;
            _isRead = false;
            return isRead;
        }

    }

}
