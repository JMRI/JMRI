package jmri.managers;

import jmri.JmriException;
import jmri.SignalMast;
import jmri.implementation.AbstractSignalMast;
import jmri.implementation.SignalMastRepeater;
import jmri.util.JUnitUtil;

import static org.hamcrest.core.StringContains.containsString;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DefaultSignalMastManagerTest extends AbstractProvidingManagerTestBase<jmri.SignalMastManager,SignalMast> {

    @Test
    public void testCTor() {
        DefaultSignalMastManager t = (DefaultSignalMastManager) l;
        Assert.assertNotNull("exists",t);
    }

    public static class MastA extends AbstractSignalMast {
        public MastA(String systemName) {
            super(systemName);
        }
    }

    public static class MastB extends AbstractSignalMast {
        public MastB(String systemName) {
            super(systemName);
        }
    }

    @Test
    public void testProvideCustomMast() throws Exception {
        DefaultSignalMastManager mgr = (DefaultSignalMastManager) l;

        SignalMast ma = mgr.provideCustomSignalMast("IM333", MastA.class);
        SignalMast mb = mgr.provideCustomSignalMast("IM444", MastB.class);

        Assert.assertTrue(ma instanceof MastA);
        Assert.assertTrue(mb instanceof MastB);

        SignalMast maa = mgr.provideCustomSignalMast("IM333", MastA.class);
        Assert.assertSame(ma, maa);
        SignalMast mbb = mgr.provideCustomSignalMast("IM444", MastB.class);
        Assert.assertSame(mb, mbb);

        SignalMast mac = mgr.provideCustomSignalMast("IM300", MastA.class);
        Assert.assertNotSame(ma, mac);
        SignalMast mbc = mgr.provideCustomSignalMast("IM400", MastB.class);
        Assert.assertNotSame(mb, mbc);

        try {
            mgr.provideCustomSignalMast("IM300", MastB.class);
            Assert.fail("provideCustomSignalMast Should have thrown exception.");
        } catch (JmriException e) {
            String s = e.toString();

            Assert.assertThat(s, containsString("system name is already used"));
            Assert.assertThat(s, containsString("MastA"));
            Assert.assertThat(s, containsString("MastB"));
        }
    }

    @Test
    public void testProvideRepeater() throws Exception {
        DefaultSignalMastManager mgr = (DefaultSignalMastManager) l;

        SignalMast m1 = mgr.provideCustomSignalMast("IM331", MastA.class);
        SignalMast m2 = mgr.provideCustomSignalMast("IM332", MastA.class);
        SignalMast m3 = mgr.provideCustomSignalMast("IM333", MastA.class);

        SignalMastRepeater rpx = mgr.provideRepeater(m1, m2);
        Assert.assertSame(m1, rpx.getMasterMast());
        Assert.assertSame(m2, rpx.getSlaveMast());
        SignalMastRepeater rpy = mgr.provideRepeater(m1, m3);
        Assert.assertSame(m1, rpy.getMasterMast());
        Assert.assertSame(m3, rpy.getSlaveMast());

        Assert.assertNotSame(rpx, rpy);
        Assert.assertSame(rpx, mgr.provideRepeater(m1, m2));
        Assert.assertSame(rpy, mgr.provideRepeater(m1, m3));

        try {
            mgr.provideRepeater(m2, m1);
            Assert.fail("provideRepeater Should have thrown exception.");
        } catch (JmriException e) {
            String s = e.toString();

            Assert.assertThat(s, containsString("repeater already exists the wrong way"));
        }
        jmri.util.JUnitAppender.assertErrorMessage("Signal repeater IM332:IM331 already exists the wrong way");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        l = new DefaultSignalMastManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @After
    public void tearDown() {
        l = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastManagerTest.class);

}
