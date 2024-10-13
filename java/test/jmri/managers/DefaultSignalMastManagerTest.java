package jmri.managers;

import jmri.JmriException;
import jmri.SignalMast;
import jmri.implementation.AbstractSignalMast;
import jmri.implementation.SignalMastRepeater;
import jmri.util.JUnitUtil;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

        Exception ex = Assert.assertThrows(JmriException.class, () ->
            mgr.provideCustomSignalMast("IM300", MastB.class));
        Assert.assertTrue("system name text not in exception",ex.getMessage().contains("system name is already used"));
        Assert.assertTrue("MastA not in exception text", ex.getMessage().contains("MastA"));
        Assert.assertTrue("MastB not in exception text", ex.getMessage().contains("MastB"));

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

        Exception ex = Assert.assertThrows(JmriException.class, () ->
            mgr.provideRepeater(m2, m1));
        String message = ex.getMessage();
        Assertions.assertNotNull(message);
        Assert.assertTrue("wrong way repeater not in exception text",
            message.contains("repeater already exists the wrong way"));
        jmri.util.JUnitAppender.assertErrorMessage("Signal repeater IM332:IM331 already exists the wrong way");
    }
    
    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}
    
    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithPrefixNotASystemName() {}

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        l = new DefaultSignalMastManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @AfterEach
    public void tearDown() {
        l.dispose();
        l = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastManagerTest.class);

}
