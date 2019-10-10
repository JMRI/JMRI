package jmri.implementation;

import java.util.ArrayList;
import java.util.List;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the SignalAppearanceMap interface.
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 */
public class DefaultSignalAppearanceMapTest {

    private SignalHead h1;
    private SignalHead h2;

    private List<NamedBeanHandle<SignalHead>> l1;
    private List<NamedBeanHandle<SignalHead>> l2;

    @Test
    public void testCtor() {
        DefaultSignalAppearanceMap map = new DefaultSignalAppearanceMap("sys", "user");
        Assert.assertNotNull(map);
    }

    @Test
    public void testSearchOrder() throws Exception {
        try {  // need try-finally to ensure junk deleted from user area
            SignalSystemTestUtil.createMockSystem();

            // check that mock (test directory) system is present
            InstanceManager.getDefault(jmri.SignalMastManager.class)
                    .provideSignalMast("IF$shsm:" + SignalSystemTestUtil.getMockSystemName() + ":one-searchlight:IH1");

        } finally {
            SignalSystemTestUtil.deleteMockSystem();
        }
    }

    @Test
    public void testDefaultMap() {
        SignalMast s = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast("IF$shsm:basic:one-searchlight:IH1");
        DefaultSignalAppearanceMap t = (DefaultSignalAppearanceMap) s.getAppearanceMap();
        t.loadDefaults();

        s.setAspect("Stop");
        Assert.assertEquals("Stop is RED", SignalHead.RED,
                h1.getAppearance());

        s.setAspect("Approach");
        Assert.assertEquals("Approach is YELLOW", SignalHead.YELLOW,
                h1.getAppearance());

        s.setAspect("Clear");
        Assert.assertEquals("Clear is GREEN", SignalHead.GREEN,
                h1.getAppearance());

        InstanceManager.getDefault(jmri.SignalMastManager.class).deregister(s);
        s.dispose();
    }

    @Test
    public void testDefaultAspects() {
        DefaultSignalAppearanceMap t = new DefaultSignalAppearanceMap("sys", "user");
        t.loadDefaults();

        java.util.Enumeration<String> e = t.getAspects();

        Assert.assertEquals("Stop", e.nextElement());
        Assert.assertEquals("Approach", e.nextElement());
        Assert.assertEquals("Clear", e.nextElement());

        Assert.assertFalse(e.hasMoreElements());
    }

    @Test
    public void testTwoHead() {

        SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:two-searchlight:IH1:IH2") {
            @Override
            protected void configureAspectTable(String signalSystemName, String aspectMapName) {
                map = new DefaultSignalAppearanceMap("sys", "user");
            }
        };

        DefaultSignalAppearanceMap t = (DefaultSignalAppearanceMap) s.getAppearanceMap();
        t.addAspect("meh", new int[]{SignalHead.LUNAR, SignalHead.DARK});
        t.addAspect("biff", new int[]{SignalHead.GREEN, SignalHead.GREEN});

        s.setAspect("meh");
        Assert.assertEquals("meh 1 is LUNAR", SignalHead.LUNAR,
                h1.getAppearance());
        Assert.assertEquals("meh 2 is LUNAR", SignalHead.DARK,
                h2.getAppearance());

        s.setAspect("biff");
        Assert.assertEquals("biff 1 is GREEN", SignalHead.GREEN,
                h1.getAppearance());
        Assert.assertEquals("biff 2 is GREEN", SignalHead.GREEN,
                h2.getAppearance());

        InstanceManager.getDefault(jmri.SignalMastManager.class).deregister(s);
        s.dispose();
    }

    @Test
    public void testGetState() {
        DefaultSignalAppearanceMap map = new DefaultSignalAppearanceMap("sys", "user");
        Assert.assertEquals(NamedBean.INCONSISTENT, map.getState());
    }

    @Test
    public void testSetState() {
        DefaultSignalAppearanceMap map = new DefaultSignalAppearanceMap("sys", "user");
        map.setState(NamedBean.UNKNOWN);
        // verify getState did not change
        Assert.assertEquals(NamedBean.INCONSISTENT, map.getState());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();        
        h1 = new DefaultSignalHead("IH1", "head1") {
            @Override
            protected void updateOutput() {
            }
        };
        h2 = new DefaultSignalHead("IH2", "head2") {
            @Override
            protected void updateOutput() {
            }
        };
        l1 = new ArrayList<>();
        l1.add(new NamedBeanHandle<>("IH1", h1));
        l2 = new ArrayList<>();
        l2.add(new NamedBeanHandle<>("IH1", h1));
        l2.add(new NamedBeanHandle<>("IH2", h2));
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h1);
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h2);
    }

    @After
    public void tearDown() {
        InstanceManager.getDefault(jmri.SignalHeadManager.class).deregister(h1);
        h1.dispose();
        h1 = null;
        InstanceManager.getDefault(jmri.SignalHeadManager.class).deregister(h2);
        h2.dispose();
        h2 = null;
        l1 = null;
        l2 = null;
        JUnitUtil.tearDown();
    }
    
}
