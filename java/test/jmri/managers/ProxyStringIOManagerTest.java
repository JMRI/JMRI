package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import javax.annotation.Nonnull;
import jmri.*;
import jmri.implementation.AbstractNamedBean;
import jmri.jmrix.internal.InternalStringIOManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test the ProxyStringIOManager.
 *
 * @author  Bob Jacobsen 2003, 2006, 2008
 * @author  Daniel Bergqvist Copyright (C) 2020
 */
public class ProxyStringIOManagerTest extends AbstractProxyManagerTestBase<ProxyStringIOManager, StringIO> {

    public String getSystemName(int i) {
        return "JC" + i;
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
        assertNotNull( t, "real object returned ");
        assertSame( t, l.getByUserName("mine"), "user name correct ");
        assertSame( t, l.getBySystemName(getSystemName(getNumToTest1())),
            "system name correct ");
    }

    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        StringIO t1 = newStringIO(getSystemName(getNumToTest1()), "mine");
        l.register(t1);
        assertNotNull( t1, "t1 real object returned ");
        assertSame( t1, l.getByUserName("mine"), "same by user ");
        assertSame( t1, l.getBySystemName(getSystemName(getNumToTest1())),
            "same by system ");
    }

    @Test
    public void testMisses() {
        // try to get nonexistant lights
        assertNull( l.getByUserName("foo"));
        assertNull( l.getBySystemName("bar"));
    }

    @Test
    public void testRename() {
        // get light
        StringIO t1 = newStringIO(getSystemName(getNumToTest1()), "before");
        assertNotNull( t1, "t1 real object ");
        l.register(t1);
        t1.setUserName("after");
        StringIO t2 = l.getByUserName("after");
        assertEquals( t1, t2, "same object");
        assertNull( l.getByUserName("before"), "no old object");
    }

    @Test
    public void testInstanceManagerIntegration() {
        JUnitUtil.resetInstanceManager();
        assertNotNull(InstanceManager.getDefault(StringIOManager.class));

//        jmri.util.JUnitUtil.initInternalStringIOManager();

        assertInstanceOf(ProxyStringIOManager.class,
            InstanceManager.getDefault(StringIOManager.class));

        assertNotNull(InstanceManager.getDefault(StringIOManager.class));
        StringIO b = newStringIO("IC1", null);
        InstanceManager.getDefault(StringIOManager.class).register(b);
        assertNotNull(InstanceManager.getDefault(StringIOManager.class).getBySystemName("IC1"));
//        assertNotNull(InstanceManager.getDefault(StringIOManager.class).provideStringIO("IL1"));

        InternalStringIOManager m = new InternalStringIOManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.setStringIOManager(m);

        b = newStringIO("IC2", null);
        InstanceManager.getDefault(StringIOManager.class).register(b);
        assertNotNull(InstanceManager.getDefault(StringIOManager.class).getBySystemName("IC1"));
//        assertNotNull(InstanceManager.getDefault(StringIOManager.class).provideStringIO("JL1"));
        b = newStringIO("IC3", null);
        InstanceManager.getDefault(StringIOManager.class).register(b);
        assertNotNull(InstanceManager.getDefault(StringIOManager.class).getBySystemName("IC1"));
//        assertNotNull(InstanceManager.getDefault(StringIOManager.class).provideStringIO("IL2"));
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
        StringIOManager siom = new InternalStringIOManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.setStringIOManager(siom);
        StringIOManager irman = InstanceManager.getDefault(StringIOManager.class);
        assertInstanceOf( ProxyStringIOManager.class, irman,
            "StringIOManager is not a ProxyStringIOManager");
        l = (ProxyStringIOManager) irman;
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    
    private static class MyStringIO extends AbstractNamedBean implements StringIO {

        String _value = "";
        
        MyStringIO(String sys, String userName) {
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
