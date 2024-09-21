package jmri.implementation;

import jmri.JmriException;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for AbstractAnalogIO
 *
 * @author Daniel Bergqvist Copyright (c) 2018
 */
public class AbstractAnalogIOTest {

    @Test
    public void testCtor() {
        MyAbstractAnalogIO stringIO = new MyAbstractAnalogIO();
        Assert.assertNotNull("AbstractAnalogIO constructor return", stringIO);
    }

    @Test
    public void testSystemNames() {
        MyAbstractAnalogIO myAnalogIO_1 = new MyAbstractAnalogIO("IV1");
        MyAbstractAnalogIO myAnalogIO_2 = new MyAbstractAnalogIO("IV01");
        Assert.assertEquals("AnalogIO system name is correct", "IV1", myAnalogIO_1.getSystemName());
        Assert.assertEquals("AnalogIO system name is correct", "IV01", myAnalogIO_2.getSystemName());
    }

    @Test
    public void testCompareTo() {
        MyAbstractAnalogIO myAnalogIO_1 = new MyAbstractAnalogIO("IV1");
        MyAbstractAnalogIO myAnalogIO_2 = new MyAbstractAnalogIO("IV01");
        Assert.assertNotEquals("AnalogIOs are different", myAnalogIO_1, myAnalogIO_2);
        Assert.assertNotEquals("AnalogIO compareTo returns not zero", 0, myAnalogIO_1.compareTo(myAnalogIO_2));
    }

    @Test
    public void testCompareSystemNameSuffix() {
        MyAbstractAnalogIO myAnalogIO_1 = new MyAbstractAnalogIO("IV1");
        MyAbstractAnalogIO myAnalogIO_2 = new MyAbstractAnalogIO("IV01");
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                -1, myAnalogIO_1.compareSystemNameSuffix("01", "1", myAnalogIO_2));
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                0, myAnalogIO_1.compareSystemNameSuffix("1", "1", myAnalogIO_2));
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                0, myAnalogIO_1.compareSystemNameSuffix("01", "01", myAnalogIO_2));
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                +1, myAnalogIO_1.compareSystemNameSuffix("1", "01", myAnalogIO_2));
    }

    @Test
    public void testAnalogIO() throws JmriException {
        MyAbstractAnalogIO myAnalogIO = new MyAbstractAnalogIO();
        myAnalogIO.setCommandedAnalogValue(33.21);
        Assert.assertTrue("string is correct",
                33.21 == myAnalogIO.getKnownAnalogValue());

        MyAbstractAnalogIO myAnalogIO2 = new MyAbstractAnalogIO(1, 2, false);
        Exception exception = assertThrows(JmriException.class, () ->
            myAnalogIO2.setCommandedAnalogValue(5) );
        Assert.assertEquals("value out of bounds", exception.getMessage());

        MyAbstractAnalogIO myAnalogIO3 = new MyAbstractAnalogIO(1, 2, false);
        exception = assertThrows(JmriException.class, () ->
            myAnalogIO3.setCommandedAnalogValue(0.2) );
        Assert.assertEquals("value out of bounds", exception.getMessage());

        myAnalogIO = new MyAbstractAnalogIO(2.0, 43, false);
        myAnalogIO.setCommandedAnalogValue(2.0);
        Assert.assertTrue("string is correct and has valid length",
                2.0 == myAnalogIO.getKnownAnalogValue());

        myAnalogIO = new MyAbstractAnalogIO(10.0, 20.0, true);
        myAnalogIO.setCommandedAnalogValue(3.0);
        Assert.assertTrue("out of bounds value is cut",
                10.0 == myAnalogIO.getKnownAnalogValue());

        myAnalogIO = new MyAbstractAnalogIO(10.0, 20.0, true);
        myAnalogIO.setCommandedAnalogValue(30.0);
        Assert.assertTrue("out of bounds value is cut",
                20.0 == myAnalogIO.getKnownAnalogValue());

        Assert.assertEquals("toAnalog() matches", "IVMySystemName", myAnalogIO.toString());

        Assert.assertTrue("getBeanType() matches", "Analog I/O".equals(myAnalogIO.getBeanType()));
    }

    @Test
    public void testState() throws JmriException {
        MyAbstractAnalogIO myAnalogIO = new MyAbstractAnalogIO(1.0, 20.0, true);
        myAnalogIO.setState(3.3);
        myAnalogIO.setCommandedAnalogValue(3.3);
        //System.err.format("Value: %1.2f%n", myAnalogIO.getCommandedAnalogValue());
        //System.err.format("Value: %1.2f%n", myAnalogIO.getKnownAnalogValue());
        Assert.assertTrue("value is correct",
                3.3 == myAnalogIO.getKnownAnalogValue());

        myAnalogIO = new MyAbstractAnalogIO(1.0, 20.0, true);
        myAnalogIO.setCommandedAnalogValue(3.3);
        Assert.assertTrue("value is correct",
                3.3 == myAnalogIO.getState(0.0));
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
