package jmri.jmrit.ussctc;

import java.util.*;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for TimeLock class in the jmri.jmrit.ussctc package
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class TimeLockTest {

    @Test
    public void testEmpty() {
        ArrayList<SignalHeadSection> list = new ArrayList<>();

        TimeLock lock = new TimeLock(list);

        Assert.assertTrue(lock.isLockClear(Lock.turnoutLockLogger));
    }

    @Test
    public void testOneInListPass() throws JmriException {
        ArrayList<SignalHeadSection> list = new ArrayList<>();

        SignalHeadSection s = new SignalHeadSection() {
            @Override
            public boolean isRunningTime() { return false;}
        };
        list.add(s);

        TimeLock lock = new TimeLock(list);

        Assert.assertTrue(lock.isLockClear(Lock.turnoutLockLogger));
    }

    @Test
    public void testOneFailActive() throws JmriException {
        ArrayList<SignalHeadSection> list = new ArrayList<>();

        SignalHeadSection s = new SignalHeadSection() {
            @Override
            public boolean isRunningTime() { return true;}
        };
        list.add(s);

        TimeLock lock = new TimeLock(list);

        Assert.assertTrue( ! lock.isLockClear(Lock.turnoutLockLogger));
    }

    @Test
    public void testOneFailStringArrayCtor() throws JmriException {
        SignalHeadSection s = new SignalHeadSection() {
            @Override
            public boolean isRunningTime() { return true;}
        };

        TimeLock lock = new TimeLock(new SignalHeadSection[]{s});

        Assert.assertTrue( ! lock.isLockClear(Lock.turnoutLockLogger));
    }

    @Test
    public void testOneFailSingleCtor() throws JmriException {
        SignalHeadSection s = new SignalHeadSection() {
            @Override
            public boolean isRunningTime() { return true;}
        };

        TimeLock lock = new TimeLock(s);

        Assert.assertTrue( ! lock.isLockClear(Lock.turnoutLockLogger));
    }

    @Test
    public void testSecondFailActive() throws JmriException {
        ArrayList<SignalHeadSection> list = new ArrayList<>();

        SignalHeadSection s = new SignalHeadSection() {
            @Override
            public boolean isRunningTime() { return false;}
        };
        list.add(s);

        s = new SignalHeadSection() {
            @Override
            public boolean isRunningTime() { return true;}
        };
        list.add(s);

        TimeLock lock = new TimeLock(list);

        Assert.assertTrue( ! lock.isLockClear(Lock.turnoutLockLogger));
    }

    @Test
    public void testBeanSettingMatch() throws JmriException {
        SignalHeadSection s = new SignalHeadSection() {
            @Override
            public boolean isRunningTime() { return true;}
        };

        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        t.setCommandedState(Turnout.CLOSED);

        BeanSetting b = new BeanSetting(t, Turnout.CLOSED);
        TimeLock lock = new TimeLock(new SignalHeadSection[]{s}, new BeanSetting[]{b});

        Assert.assertTrue( ! lock.isLockClear(Lock.turnoutLockLogger));
    }

    @Test
    public void testBeanSettingoMatch() throws JmriException {
        SignalHeadSection s = new SignalHeadSection() {
            @Override
            public boolean isRunningTime() { return true;}
        };

        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        t.setCommandedState(Turnout.CLOSED);

        BeanSetting b = new BeanSetting(t, Turnout.THROWN);
        TimeLock lock = new TimeLock(new SignalHeadSection[]{s}, new BeanSetting[]{b});

        Assert.assertTrue( lock.isLockClear(Lock.turnoutLockLogger));
    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        // new Lock(); // to create statics
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
