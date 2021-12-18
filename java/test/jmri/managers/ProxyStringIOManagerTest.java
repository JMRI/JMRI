package jmri.managers;

import java.beans.PropertyChangeListener;
import javax.annotation.Nonnull;
import jmri.*;
import jmri.implementation.AbstractNamedBean;
import jmri.jmrix.internal.InternalStringIOManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test the ProxyStringIOManager.
 *
 * @author  Bob Jacobsen 2003, 2006, 2008
 * @author  Daniel Bergqvist Copyright (C) 2020
 */
public class ProxyStringIOManagerTest {

    public String getSystemName(int i) {
        return "JC" + i;
    }

    protected StringIOManager l = null;     // holds objects under test

    static protected boolean listenerResult = false;

    protected class Listen implements PropertyChangeListener {

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
        }
    }

    private StringIO newStringIO(String sysName, String userName) {
        return new MyStringIO(sysName, userName);
    }
    
    @Test
    public void testDispose() {
        l.dispose();  // all we're really doing here is making sure the method exists
    }

    @Test
    public void testStringIOPutGet() {
        // create
        StringIO t = newStringIO(getSystemName(getNumToTest1()), "mine");
        l.register(t);
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("user name correct ", t == l.getByUserName("mine"));
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        StringIO t1 = newStringIO(getSystemName(getNumToTest1()), "mine");
        l.register(t1);
        Assert.assertTrue("t1 real object returned ", t1 != null);
        Assert.assertTrue("same by user ", t1 == l.getByUserName("mine"));
        Assert.assertTrue("same by system ", t1 == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testMisses() {
        // try to get nonexistant lights
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    @Test
    public void testRename() {
        // get light
        StringIO t1 = newStringIO(getSystemName(getNumToTest1()), "before");
        Assert.assertNotNull("t1 real object ", t1);
        l.register(t1);
        t1.setUserName("after");
        StringIO t2 = l.getByUserName("after");
        Assert.assertEquals("same object", t1, t2);
        Assert.assertEquals("no old object", null, l.getByUserName("before"));
    }

    @Test
    public void testInstanceManagerIntegration() {
        jmri.util.JUnitUtil.resetInstanceManager();
        Assert.assertNotNull(InstanceManager.getDefault(StringIOManager.class));

//        jmri.util.JUnitUtil.initInternalStringIOManager();

        Assert.assertTrue(InstanceManager.getDefault(StringIOManager.class) instanceof ProxyStringIOManager);

        Assert.assertNotNull(InstanceManager.getDefault(StringIOManager.class));
        StringIO b = newStringIO("IC1", null);
        InstanceManager.getDefault(StringIOManager.class).register(b);
        Assert.assertNotNull(InstanceManager.getDefault(StringIOManager.class).getBySystemName("IC1"));
//        Assert.assertNotNull(InstanceManager.getDefault(StringIOManager.class).provideStringIO("IL1"));

        InternalStringIOManager m = new InternalStringIOManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.setStringIOManager(m);

        b = newStringIO("IC2", null);
        InstanceManager.getDefault(StringIOManager.class).register(b);
        Assert.assertNotNull(InstanceManager.getDefault(StringIOManager.class).getBySystemName("IC1"));
//        Assert.assertNotNull(InstanceManager.getDefault(StringIOManager.class).provideStringIO("JL1"));
        b = newStringIO("IC3", null);
        InstanceManager.getDefault(StringIOManager.class).register(b);
        Assert.assertNotNull(InstanceManager.getDefault(StringIOManager.class).getBySystemName("IC1"));
//        Assert.assertNotNull(InstanceManager.getDefault(StringIOManager.class).provideStringIO("IL2"));
    }

    /**
     * Number of light to test. Made a separate method so it can be overridden
     * in subclasses that do or don't support various numbers.
     * 
     * @return the number to test
     */
    protected int getNumToTest1() {
        return 9;
    }

    protected int getNumToTest2() {
        return 7;
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // create and register the manager object
        l = new InternalStringIOManager(new InternalSystemConnectionMemo("J", "Juliet"));
        jmri.InstanceManager.setStringIOManager(l);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    
    private class MyStringIO extends AbstractNamedBean implements StringIO {

        String _value = "";
        
        public MyStringIO(String sys, String userName) {
            super(sys, userName);
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
        public void setCommandedStringValue(@Nonnull String value) throws JmriException {
            _value = value;
        }

        @Override
        public String getCommandedStringValue() {
            return _value;
        }

    }
    
}
