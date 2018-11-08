package jmri;

import jmri.implementation.AbstractNamedBean;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the Light class
 *
 * @author	Daniel Bergqvist Copyright (C) 2018
 */
public class AnalogIOTest {

    @Test
    public void testAnalogIO() throws JmriException {
        AnalogIO analogIO = new MyAnalogIO("Analog");
        analogIO.setState(AnalogIO.MIN_VALUE);
        Assert.assertTrue("AnalogIO has MIN_VALUE", analogIO.getState() == AnalogIO.MIN_VALUE);
        analogIO.setState(AnalogIO.MAX_VALUE);
        Assert.assertTrue("AnalogIO has MAX_VALUE", analogIO.getState() == AnalogIO.MAX_VALUE);
        analogIO.setCommandedAnalogValue(AnalogIO.MIN_VALUE);
        Assert.assertTrue("AnalogIO has MIN_VALUE", analogIO.getState() == AnalogIO.MIN_VALUE);
        analogIO.setCommandedAnalogValue(AnalogIO.MAX_VALUE);
        Assert.assertTrue("AnalogIO has MAX_VALUE", analogIO.getState() == AnalogIO.MAX_VALUE);
        analogIO.setState(AnalogIO.MIN_VALUE);
        Assert.assertTrue("AnalogIO has MIN_VALUE", analogIO.getCommandedAnalogValue() == AnalogIO.MIN_VALUE);
        analogIO.setState(AnalogIO.MAX_VALUE);
        Assert.assertTrue("AnalogIO has MAX_VALUE", analogIO.getCommandedAnalogValue() == AnalogIO.MAX_VALUE);
        analogIO.setState(AnalogIO.MIN_VALUE);
        Assert.assertTrue("AnalogIO has MIN_VALUE", analogIO.getKnownAnalogValue() == AnalogIO.MIN_VALUE);
        analogIO.setState(AnalogIO.MAX_VALUE);
        Assert.assertTrue("AnalogIO has MAX_VALUE", analogIO.getKnownAnalogValue() == AnalogIO.MAX_VALUE);
    }
    
    @Test
    public void testStateConstants() {
        Assert.assertTrue("MIN_VALUE less than MAX_VALUE", AnalogIO.MIN_VALUE < AnalogIO.MAX_VALUE);
        Assert.assertTrue("MIN_VALUE is the smallest number", (AnalogIO.MIN_VALUE-1) == AnalogIO.MAX_VALUE);
        Assert.assertTrue("MAX_VALUE is the biggest number", AnalogIO.MIN_VALUE == (AnalogIO.MAX_VALUE+1));
    }

    @Before
    public void setUp() {
          jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
          jmri.util.JUnitUtil.tearDown();
    }

    
    private class MyAnalogIO extends AbstractNamedBean implements AnalogIO {

        int _state = AnalogIO.MIDDLE_VALUE;
        
        public MyAnalogIO(String sys) {
            super(sys);
        }
        
        @Override
        public void setState(int s) throws JmriException {
            _state = s;
        }

        @Override
        public int getState() {
            return _state;
        }

        @Override
        public String getBeanType() {
            return "AnalogIO";
        }
    
    }
    
}
