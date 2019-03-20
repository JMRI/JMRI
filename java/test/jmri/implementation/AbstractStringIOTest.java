package jmri.implementation;

import java.util.concurrent.atomic.AtomicBoolean;
import jmri.JmriException;
import org.junit.*;

/**
 * Tests for AbstractStringIO
 * 
 * @author Daniel Bergqvist Copyright (c) 2018
 */
public class AbstractStringIOTest {
    
    AtomicBoolean exceptionThrown = new AtomicBoolean(false);
    
    @Test
    public void testCtor() {
        MyAbstractStringIO stringIO = new MyAbstractStringIO();
        Assert.assertNotNull("AbstractStringIO constructor return", stringIO);
    }
    
    @Test
    public void testStringIO() throws JmriException {
        MyAbstractStringIO myStringIO = new MyAbstractStringIO();
        myStringIO.setCommandedStringValue("8:20. Train 21 to Vaxjo");
        Assert.assertTrue("string is correct",
                "8:20. Train 21 to Vaxjo".equals(myStringIO.getKnownStringValue()));
        
        myStringIO = new MyAbstractStringIO(22, false);
        exceptionThrown.set(false);
        try {
            myStringIO.setCommandedStringValue("8:20. Train 21 to Vaxjo");
        } catch (JmriException e) {
            exceptionThrown.set(true);
        }
        Assert.assertTrue("string is too long", exceptionThrown.get() == true);
        
        myStringIO = new MyAbstractStringIO(23, false);
        myStringIO.setCommandedStringValue("8:20. Train 21 to Vaxjo");
        Assert.assertTrue("string is correct and has valid length",
                "8:20. Train 21 to Vaxjo".equals(myStringIO.getKnownStringValue()));
        
        myStringIO = new MyAbstractStringIO(10, true);
        myStringIO.setCommandedStringValue("8:20. Train 21 to Vaxjo");
        Assert.assertTrue("string is cut",
                "8:20. Trai".equals(myStringIO.getKnownStringValue()));
        
        Assert.assertTrue("toString() matches",
                "jmri.implementation.AbstractStringIOTest$MyAbstractStringIO (MySystemName)".equals(myStringIO.toString()));
        
        Assert.assertTrue("getBeanType() matches", "String I/O".equals(myStringIO.getBeanType()));
    }
    
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
    
    
    private class MyAbstractStringIO extends AbstractStringIO {
        
        private int _maxLen = 0;
        private boolean _cut = false;
        
        MyAbstractStringIO() {
            super("MySystemName");
        }

        MyAbstractStringIO(int maxLen, boolean cut) {
            super("MySystemName");
            _maxLen = maxLen;
            _cut = cut;
        }

        @Override
        protected void sendStringToLayout(String value) throws JmriException {
            this.setString(value);
        }

        @Override
        public int getMaximumLength() {
            return _maxLen;
        }

        @Override
        public boolean cutLongStrings() {
            return _cut;
        }

    }
    
}
