package jmri.implementation;

import jmri.JmriException;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for AbstractStringIO
 * 
 * @author Daniel Bergqvist Copyright (c) 2018
 */
public class AbstractStringIOTest {

    @Test
    public void testCtor() {
        MyAbstractStringIO stringIO = new MyAbstractStringIO();
        Assert.assertNotNull("AbstractStringIO constructor return", stringIO);
    }
    
    @Test
    public void testSystemNames() {
        MyAbstractStringIO myStringIO_1 = new MyAbstractStringIO("IZ1");
        MyAbstractStringIO myStringIO_2 = new MyAbstractStringIO("IZ01");
        Assert.assertEquals("StringIO system name is correct", "IZ1", myStringIO_1.getSystemName());
        Assert.assertEquals("StringIO system name is correct", "IZ01", myStringIO_2.getSystemName());
    }
    
    @Test
    public void testCompareTo() {
        MyAbstractStringIO myStringIO_1 = new MyAbstractStringIO("IZ1");
        MyAbstractStringIO myStringIO_2 = new MyAbstractStringIO("IZ01");
        Assert.assertNotEquals("StringIOs are different", myStringIO_1, myStringIO_2);
        Assert.assertNotEquals("StringIO compareTo returns not zero", 0, myStringIO_1.compareTo(myStringIO_2));
    }
    
    @Test
    public void testCompareSystemNameSuffix() {
        MyAbstractStringIO myStringIO_1 = new MyAbstractStringIO("IZ1");
        MyAbstractStringIO myStringIO_2 = new MyAbstractStringIO("IZ01");
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                -1, myStringIO_1.compareSystemNameSuffix("01", "1", myStringIO_2));
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                0, myStringIO_1.compareSystemNameSuffix("1", "1", myStringIO_2));
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                0, myStringIO_1.compareSystemNameSuffix("01", "01", myStringIO_2));
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                +1, myStringIO_1.compareSystemNameSuffix("1", "01", myStringIO_2));
    }
    
    @Test
    public void testStringIO() throws JmriException {
        MyAbstractStringIO myStringIO = new MyAbstractStringIO();
        myStringIO.setCommandedStringValue("8:20. Train 21 to Vaxjo");
        Assert.assertTrue("string is correct",
                "8:20. Train 21 to Vaxjo".equals(myStringIO.getKnownStringValue()));
        
        MyAbstractStringIO exStringIO = new MyAbstractStringIO(22, false);

        Exception ex = Assertions.assertThrows(JmriException.class,
            () -> exStringIO.setCommandedStringValue("8:20. Train 21 to Vaxjo"));
        Assertions.assertNotNull(ex);
        Assertions.assertEquals("String too long", ex.getMessage());

        myStringIO = new MyAbstractStringIO(23, false);
        myStringIO.setCommandedStringValue("8:20. Train 21 to Vaxjo");
        Assert.assertTrue("string is correct and has valid length",
                "8:20. Train 21 to Vaxjo".equals(myStringIO.getKnownStringValue()));
        
        myStringIO = new MyAbstractStringIO(10, true);
        myStringIO.setCommandedStringValue("8:20. Train 21 to Vaxjo");
        Assert.assertTrue("string is cut",
                "8:20. Trai".equals(myStringIO.getKnownStringValue()));
        
        Assert.assertEquals("toString() matches", "IZMySystemName", myStringIO.toString());
        
        Assert.assertTrue("getBeanType() matches", "String I/O".equals(myStringIO.getBeanType()));
    }
    
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }


    private static class MyAbstractStringIO extends AbstractStringIO {
        
        private int _maxLen = 0;
        private boolean _cut = false;
        
        MyAbstractStringIO() {
            super("IZMySystemName");
        }

        MyAbstractStringIO(String sysName) {
            super(sysName);
        }

        MyAbstractStringIO(int maxLen, boolean cut) {
            super("IZMySystemName");
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
