package jmri.jmrix;

import jmri.util.JUnitUtil;
import jmri.InstanceManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SystemConnectionMemoTest {

    SystemConnectionMemo t = null;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testGetConsistManagerNull() {
        Assert.assertNull("null consist manager", t.get(jmri.ConsistManager.class));
    }

    @Test
    public void testProvidesConsistManagerNull() {
        Assert.assertFalse("null consist manager", t.provides(jmri.ConsistManager.class));
    }

    @Test
    public void testGetConsistManagerWithCS() {
        SystemConnectionMemo t = new SystemConnectionMemo("T", "Test") {
            @Override
            protected java.util.ResourceBundle getActionModelResourceBundle() {
                return null;
            }

            @Override
            public boolean provides(Class<?> c) {
                if (c.equals(jmri.CommandStation.class)) {
                    return true;
                }
                return super.provides(c);
            }

            @Override
            @SuppressWarnings("unchecked") // dynamic check
            public <T> T get(Class<?> T) {
                if (T.equals(jmri.CommandStation.class)) {
                    return (T) InstanceManager.getDefault(T);
                }
                return super.get(T);
            }

        };
        Assert.assertNotNull("consist manager", t.get(jmri.ConsistManager.class));
    }

    @Test
    public void testProvidesConsistManagerWithCS() {
        SystemConnectionMemo t = new SystemConnectionMemo("T", "Test") {
            @Override
            protected java.util.ResourceBundle getActionModelResourceBundle() {
                return null;
            }

            @Override
            public boolean provides(Class<?> c) {
                if (c.equals(jmri.CommandStation.class)) {
                    return true;
                }
                return super.provides(c);
            }

            @Override
            @SuppressWarnings("unchecked") // dynamic check
            public <T> T get(Class<?> T) {
                if (T.equals(jmri.CommandStation.class)) {
                    return (T) InstanceManager.getDefault(T);
                }
                return super.get(T);
            }
        };
        Assert.assertTrue("null consist manager", t.provides(jmri.ConsistManager.class));
    }

    @Test
    public void testGetConsistManagerWithAPM() {
        SystemConnectionMemo t = new SystemConnectionMemo("T", "Test") {
            @Override
            protected java.util.ResourceBundle getActionModelResourceBundle() {
                return null;
            }

            @Override
            public boolean provides(Class<?> c) {
                if (c.equals(jmri.AddressedProgrammerManager.class)) {
                    return true;
                }
                return super.provides(c);
            }

            @Override
            @SuppressWarnings("unchecked") // dynamic check
            public <T> T get(Class<?> T) {
                if (T.equals(jmri.AddressedProgrammerManager.class)) {
                    return (T) InstanceManager.getDefault(T);
                }
                return super.get(T);
            }

        };
        Assert.assertNotNull("consist manager", t.get(jmri.ConsistManager.class));
    }

    @Test
    public void testProvidesConsistManagerWithAPM() {
        SystemConnectionMemo t = new SystemConnectionMemo("T", "Test") {
            @Override
            protected java.util.ResourceBundle getActionModelResourceBundle() {
                return null;
            }

            @Override
            public boolean provides(Class<?> c) {
                if (c.equals(jmri.AddressedProgrammerManager.class)) {
                    return true;
                }
                return super.provides(c);
            }

            @Override
            @SuppressWarnings("unchecked") // dynamic check
            public <T> T get(Class<?> T) {
                if (T.equals(jmri.AddressedProgrammerManager.class)) {
                    return (T) InstanceManager.getDefault(T);
                }
                return super.get(T);
            }
        };
        Assert.assertTrue("null consist manager", t.provides(jmri.ConsistManager.class));
    }

    @Test
    public void testSetGetOutputInterval() {
        Assert.assertEquals("default interval in memo", t.getOutputInterval(), 250);
        t.setOutputInterval(123);
        Assert.assertEquals("new interval set in memo", t.getOutputInterval(), 123);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDebugCommandStation();
        JUnitUtil.initDebugProgrammerManager();
        t = new SystemConnectionMemo("T", "Test") {
            @Override
            protected java.util.ResourceBundle getActionModelResourceBundle() {
                return null;
            }
        };
    }

    @After
    public void tearDown() {
        t = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SystemConnectionMemoTest.class);

}
