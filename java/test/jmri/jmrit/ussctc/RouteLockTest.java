package jmri.jmrit.ussctc;

import org.junit.*;

import jmri.*;

import java.util.*;

/**
 * Tests for RouteLock class in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
  */
public class RouteLockTest {

    @Test
    public void testEmpty() {
        ArrayList<NamedBeanHandle<SignalHead>> list = new ArrayList<>();
        
        RouteLock lock = new RouteLock(list);
        
        Assert.assertTrue(lock.isLockClear());
    }

    @Test
    public void testOneInListPass() throws JmriException {
        ArrayList<NamedBeanHandle<SignalHead>> list = new ArrayList<>();
        
        SignalHead s = new jmri.implementation.VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
        NamedBeanHandle<SignalHead> h = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IH1", s);
        
        list.add(h);
        s.setState(SignalHead.RED);

        RouteLock lock = new RouteLock(list);
        
        Assert.assertTrue(lock.isLockClear());
    }

    @Test
    public void testOneFailActive() throws JmriException {
        ArrayList<NamedBeanHandle<SignalHead>> list = new ArrayList<>();
        
        SignalHead s = new jmri.implementation.VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
        NamedBeanHandle<SignalHead> h = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IH1", s);
        
        list.add(h);
        s.setState(SignalHead.YELLOW);

        RouteLock lock = new RouteLock(list);
        
        Assert.assertTrue( ! lock.isLockClear());
    }

    @Test
    public void testOneFailStringArrayCtor() throws JmriException {
        ArrayList<NamedBeanHandle<SignalHead>> list = new ArrayList<>();
        
        SignalHead s = new jmri.implementation.VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
        NamedBeanHandle<SignalHead> h = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IH1", s);
        
        s.setState(SignalHead.YELLOW);

        RouteLock lock = new RouteLock(new String[]{"IH1"});
        
        Assert.assertTrue( ! lock.isLockClear());
    }

    @Test
    public void testOneFailSingleStringCtor() throws JmriException {
        ArrayList<NamedBeanHandle<SignalHead>> list = new ArrayList<>();
        
        SignalHead s = new jmri.implementation.VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
        NamedBeanHandle<SignalHead> h = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IH1", s);
        
        s.setState(SignalHead.YELLOW);

        RouteLock lock = new RouteLock("IH1");
        
        Assert.assertTrue( ! lock.isLockClear());
    }

    @Test
    public void testSecondFailActive() throws JmriException {
        ArrayList<NamedBeanHandle<SignalHead>> list = new ArrayList<>();
        
        SignalHead s = new jmri.implementation.VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
        NamedBeanHandle<SignalHead> h = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IH1", s);
        
        list.add(h);
        s.setState(SignalHead.RED);

        s = new jmri.implementation.VirtualSignalHead("IH2");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
        h = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IH2", s);

        list.add(h);
        s.setState(SignalHead.YELLOW);

        RouteLock lock = new RouteLock(list);
        
        Assert.assertTrue( ! lock.isLockClear());
    }
       
    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initConfigureManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
