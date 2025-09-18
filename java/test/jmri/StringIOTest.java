package jmri;

import jmri.implementation.AbstractNamedBean;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the StringIO class
 *
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class StringIOTest {

    @Test
    public void testStringIO() throws JmriException {
        StringIO stringIO = new MyStringIO("String");
        stringIO.setCommandedStringValue("One string");
        assertEquals( "One string", stringIO.getCommandedStringValue(), "StringIO has value 'One string'");
        stringIO.setCommandedStringValue("Other string");
        assertEquals( "Other string", stringIO.getCommandedStringValue(), "StringIO has value 'Other string'");
        stringIO.setCommandedStringValue("One string");
        assertEquals( "One string", stringIO.getKnownStringValue(), "StringIO has value 'One string'");
        stringIO.setCommandedStringValue("Other string");
        assertEquals( "Other string", stringIO.getKnownStringValue(), "StringIO has value 'Other string'");
    }
    
    @BeforeEach
    public void setUp() {
          jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
          jmri.util.JUnitUtil.tearDown();
    }

    private static class MyStringIO extends AbstractNamedBean implements StringIO {

        String _value = "";
        
        private MyStringIO(String sys) {
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
