package jmri.implementation;

import jmri.JmriException;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for AbstractAnalogIO
 *
 * @author Daniel Bergqvist Copyright (c) 2018
 */
public class AbstractAnalogIOTest {

    @Test
    public void testCtor() {
        MyAbstractAnalogIO stringIO = new MyAbstractAnalogIO();
        assertNotNull( stringIO, "AbstractAnalogIO constructor return");
    }

    @Test
    public void testSystemNames() {
        MyAbstractAnalogIO myAnalogIO_1 = new MyAbstractAnalogIO("IV1");
        MyAbstractAnalogIO myAnalogIO_2 = new MyAbstractAnalogIO("IV01");
        assertEquals( "IV1", myAnalogIO_1.getSystemName(), "AnalogIO system name is correct");
        assertEquals( "IV01", myAnalogIO_2.getSystemName(), "AnalogIO system name is correct");
    }

    @Test
    public void testCompareTo() {
        MyAbstractAnalogIO myAnalogIO_1 = new MyAbstractAnalogIO("IV1");
        MyAbstractAnalogIO myAnalogIO_2 = new MyAbstractAnalogIO("IV01");
        assertNotEquals( myAnalogIO_1, myAnalogIO_2, "AnalogIOs are different");
        assertNotEquals( 0, myAnalogIO_1.compareTo(myAnalogIO_2), "AnalogIO compareTo returns not zero");
    }

    @Test
    public void testCompareSystemNameSuffix() {
        MyAbstractAnalogIO myAnalogIO_1 = new MyAbstractAnalogIO("IV1");
        MyAbstractAnalogIO myAnalogIO_2 = new MyAbstractAnalogIO("IV01");
        assertEquals( -1, myAnalogIO_1.compareSystemNameSuffix("01", "1", myAnalogIO_2),
            "compareSystemNameSuffix returns correct value");
        assertEquals( 0, myAnalogIO_1.compareSystemNameSuffix("1", "1", myAnalogIO_2),
            "compareSystemNameSuffix returns correct value");
        assertEquals( 0, myAnalogIO_1.compareSystemNameSuffix("01", "01", myAnalogIO_2),
            "compareSystemNameSuffix returns correct value");
        assertEquals( +1, myAnalogIO_1.compareSystemNameSuffix("1", "01", myAnalogIO_2),
            "compareSystemNameSuffix returns correct value");
    }

    @Test
    public void testAnalogIO() throws JmriException {
        MyAbstractAnalogIO myAnalogIO = new MyAbstractAnalogIO();
        myAnalogIO.setCommandedAnalogValue(33.21);
        assertEquals( 33.21, myAnalogIO.getKnownAnalogValue(), 0.0,
            "string is correct");

        MyAbstractAnalogIO myAnalogIO2 = new MyAbstractAnalogIO(1, 2, false);
        Exception exception = assertThrows(JmriException.class, () ->
            myAnalogIO2.setCommandedAnalogValue(5) );
        assertEquals("value out of bounds", exception.getMessage());

        MyAbstractAnalogIO myAnalogIO3 = new MyAbstractAnalogIO(1, 2, false);
        exception = assertThrows(JmriException.class, () ->
            myAnalogIO3.setCommandedAnalogValue(0.2) );
        assertEquals("value out of bounds", exception.getMessage());

        myAnalogIO = new MyAbstractAnalogIO(2.0, 43, false);
        myAnalogIO.setCommandedAnalogValue(2.0);
        assertEquals( 2.0, myAnalogIO.getKnownAnalogValue(), 0,
            "string is correct and has valid length");

        myAnalogIO = new MyAbstractAnalogIO(10.0, 20.0, true);
        myAnalogIO.setCommandedAnalogValue(3.0);
        assertEquals( 10.0, myAnalogIO.getKnownAnalogValue(), 0.0,
            "out of bounds value is cut");

        myAnalogIO = new MyAbstractAnalogIO(10.0, 20.0, true);
        myAnalogIO.setCommandedAnalogValue(30.0);
        assertEquals( 20.0, myAnalogIO.getKnownAnalogValue(), 0,
            "out of bounds value is cut");

        assertEquals( "IVMySystemName", myAnalogIO.toString(), "toAnalog() matches");

        assertTrue( "Analog I/O".equals(myAnalogIO.getBeanType()), "getBeanType() matches");
    }

    @Test
    public void testState() throws JmriException {
        MyAbstractAnalogIO myAnalogIO = new MyAbstractAnalogIO(1.0, 20.0, true);
        myAnalogIO.setState(3.3);
        myAnalogIO.setCommandedAnalogValue(3.3);
        //System.err.format("Value: %1.2f%n", myAnalogIO.getCommandedAnalogValue());
        //System.err.format("Value: %1.2f%n", myAnalogIO.getKnownAnalogValue());
        assertEquals( 3.3, myAnalogIO.getKnownAnalogValue(), 0.0, "value is correct");

        myAnalogIO = new MyAbstractAnalogIO(1.0, 20.0, true);
        myAnalogIO.setCommandedAnalogValue(3.3);
        assertEquals( 3.3, myAnalogIO.getState(0.0), 0.0, "value is correct");
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }


    private static class MyAbstractAnalogIO extends AbstractAnalogIO {

        private double _min = 0;
        private double _max = 100;
        private boolean _cutOutOfBoundsValues = false;

        MyAbstractAnalogIO() {
            super("IVMySystemName", true);
        }

        MyAbstractAnalogIO(String sysName) {
            super(sysName, true);
        }

        MyAbstractAnalogIO(double min, double max, boolean cutOutOfBoundsValues) {
            super("IVMySystemName", true);
            _min = min;
            _max = max;
            _cutOutOfBoundsValues = cutOutOfBoundsValues;
        }

        @Override
        protected void sendValueToLayout(double value) throws JmriException {
            this.setValue(value);
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
        public double getMin() {
            return _min;
        }

        @Override
        public double getMax() {
            return _max;
        }

        @Override
        protected boolean cutOutOfBoundsValues() {
            return _cutOutOfBoundsValues;
        }

        @Override
        public double getResolution() {
            return 1.0;
        }

        @Override
        public AbsoluteOrRelative getAbsoluteOrRelative() {
            return AbsoluteOrRelative.ABSOLUTE;
        }

    }

}
