package jmri.jmrit.ussctc;

import java.util.*;
import jmri.*;
import jmri.util.JUnitUtil;
import org.junit.*;

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

        SignalHead s = new jmri.implementation.VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);

        s.setState(SignalHead.YELLOW);

        RouteLock lock = new RouteLock(new String[]{"IH1"});

        Assert.assertTrue( ! lock.isLockClear());
    }

    @Test
    public void testOneFailSingleStringCtor() throws JmriException {

        SignalHead s = new jmri.implementation.VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);

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

    @Test
    public void testBeanSettingMatch() throws JmriException {
        SignalHead s = new jmri.implementation.VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);

        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        t.setCommandedState(Turnout.CLOSED);

        s.setState(SignalHead.YELLOW);
        BeanSetting b = new BeanSetting(t, Turnout.CLOSED);
        RouteLock lock = new RouteLock(new String[]{"IH1"}, new BeanSetting[]{b});

        Assert.assertTrue( ! lock.isLockClear());
    }

    @Test
    public void testBeanSettingoMatch() throws JmriException {
        SignalHead s = new jmri.implementation.VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);

        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        t.setCommandedState(Turnout.CLOSED);

        s.setState(SignalHead.YELLOW);
        BeanSetting b = new BeanSetting(t, Turnout.THROWN);
        RouteLock lock = new RouteLock(new String[]{"IH1"}, new BeanSetting[]{b});

        Assert.assertTrue( lock.isLockClear());
    }


    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
