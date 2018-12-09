package jmri;

import jmri.implementation.AbstractNamedBean;
import org.junit.*;

/**
 * Tests for the StringIO class
 *
 * @author	Daniel Bergqvist Copyright (C) 2018
 */
public class StringIOTest {

    @Test
    public void testStringIO() throws JmriException {
        StringIO stringIO = new MyStringIO("String");
        stringIO.setCommandedStringValue("One string");
        Assert.assertTrue("StringIO has value 'One string'", "One string".equals(stringIO.getCommandedStringValue()));
        stringIO.setCommandedStringValue("Other string");
        Assert.assertTrue("StringIO has value 'Other string'", "Other string".equals(stringIO.getCommandedStringValue()));
        stringIO.setCommandedStringValue("One string");
        Assert.assertTrue("StringIO has value 'One string'", "One string".equals(stringIO.getKnownStringValue()));
        stringIO.setCommandedStringValue("Other string");
        Assert.assertTrue("StringIO has value 'Other string'", "Other string".equals(stringIO.getKnownStringValue()));
    }
    
    @Before
    public void setUp() {
          jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
          jmri.util.JUnitUtil.tearDown();
    }

    
    private class MyStringIO extends AbstractNamedBean implements StringIO {

        String _value = "";
        
        public MyStringIO(String sys) {
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
        public String getBeanType() {
            return "StringIO";
        }

        @Override
        public void setCommandedStringValue(String value) throws JmriException {
            _value = value;
        }

        @Override
        public String getCommandedStringValue() {
            return _value;
        }

    }
    
}
