package jmri;

import jmri.implementation.AbstractNamedBean;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Light class
 *
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class AnalogIOTest {

    @Test
    public void testAnalogIO() throws JmriException {
        double min = -1.0;
        double max = 1.0;
        AnalogIO analogIO = new MyAnalogIO("Analog");
        analogIO.setCommandedAnalogValue(min);
        assertTrue( analogIO.getCommandedAnalogValue() == min, "AnalogIO has value -1.0");
        analogIO.setCommandedAnalogValue(max);
        assertTrue( analogIO.getCommandedAnalogValue() == max, "AnalogIO has value 1.0");
        analogIO.setCommandedAnalogValue(min);
        assertTrue( analogIO.getKnownAnalogValue() == min, "AnalogIO has value -1.0");
        analogIO.setCommandedAnalogValue(max);
        assertTrue( analogIO.getKnownAnalogValue() == max, "AnalogIO has value 1.0");
        
        assertEquals( "Absolute", AnalogIO.AbsoluteOrRelative.ABSOLUTE.toString(),
            "String value is Absolute");
        assertEquals( "Relative", AnalogIO.AbsoluteOrRelative.RELATIVE.toString(),
            "String value is Relative");
    }
    
    @BeforeEach
    public void setUp() {
          jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
          jmri.util.JUnitUtil.tearDown();
    }

    
    private static class MyAnalogIO extends AbstractNamedBean implements AnalogIO {

        double _value = 0.0;
        
        private MyAnalogIO(String sys) {
            super(sys);
        }
        
        @Override
        public void setState(int s) throws JmriException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public int getState() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setState(double value) throws JmriException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public double getState(double v) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getBeanType() {
            return "AnalogIO";
        }

        @Override
        public void setCommandedAnalogValue(double value) throws JmriException {
            _value = value;
        }

        @Override
        public double getCommandedAnalogValue() {
            return _value;
        }

        @Override
        public double getMin() {
            return Float.MIN_VALUE;
        }

        @Override
        public double getMax() {
            return Float.MAX_VALUE;
        }

        @Override
        public double getResolution() {
            return 0.1;
        }

        @Override
        public AbsoluteOrRelative getAbsoluteOrRelative() {
            return AbsoluteOrRelative.ABSOLUTE;
        }
    
    }
    
}
