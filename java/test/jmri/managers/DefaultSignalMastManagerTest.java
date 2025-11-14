package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.JmriException;
import jmri.SignalMast;
import jmri.implementation.AbstractSignalMast;
import jmri.implementation.SignalMastRepeater;
import jmri.util.JUnitUtil;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultSignalMastManagerTest extends AbstractProvidingManagerTestBase<jmri.SignalMastManager,SignalMast> {

    @Test
    public void testCTor() {
        DefaultSignalMastManager t = (DefaultSignalMastManager) l;
        assertNotNull( t, "exists");
    }

    @SuppressWarnings("ProtectedInnerClass") // accessible to DefaultSignalMastManager
    protected static class MastA extends AbstractSignalMast {
        @SuppressWarnings("PublicConstructorInNonPublicClass") // reflection requires public access
        public MastA(String systemName) {
            super(systemName);
        }
    }

    @SuppressWarnings("ProtectedInnerClass") // accessible to DefaultSignalMastManager
    protected static class MastB extends AbstractSignalMast {
        @SuppressWarnings("PublicConstructorInNonPublicClass") // reflection requires public access
        public MastB(String systemName) {
            super(systemName);
        }
    }

    @Test
    public void testProvideCustomMast() throws JmriException {
        DefaultSignalMastManager mgr = (DefaultSignalMastManager) l;

        SignalMast ma = mgr.provideCustomSignalMast("IM333", MastA.class);
        SignalMast mb = mgr.provideCustomSignalMast("IM444", MastB.class);

        assertInstanceOf( MastA.class, ma);
        assertInstanceOf( MastB.class, mb);

        SignalMast maa = mgr.provideCustomSignalMast("IM333", MastA.class);
        assertSame(ma, maa);
        SignalMast mbb = mgr.provideCustomSignalMast("IM444", MastB.class);
        assertSame(mb, mbb);

        SignalMast mac = mgr.provideCustomSignalMast("IM300", MastA.class);
        assertNotSame(ma, mac);
        SignalMast mbc = mgr.provideCustomSignalMast("IM400", MastB.class);
        assertNotSame(mb, mbc);

        Exception ex = assertThrows(JmriException.class, () ->
            mgr.provideCustomSignalMast("IM300", MastB.class));
        assertTrue( ex.getMessage().contains("system name is already used"),
            "system name text not in exception");
        assertTrue( ex.getMessage().contains("MastA"), "MastA not in exception text");
        assertTrue( ex.getMessage().contains("MastB"), "MastB not in exception text");

    }

    @Test
    public void testProvideRepeater() throws JmriException {
        DefaultSignalMastManager mgr = (DefaultSignalMastManager) l;

        SignalMast m1 = mgr.provideCustomSignalMast("IM331", MastA.class);
        SignalMast m2 = mgr.provideCustomSignalMast("IM332", MastA.class);
        SignalMast m3 = mgr.provideCustomSignalMast("IM333", MastA.class);

        SignalMastRepeater rpx = mgr.provideRepeater(m1, m2);
        assertSame(m1, rpx.getMasterMast());
        assertSame(m2, rpx.getSlaveMast());
        SignalMastRepeater rpy = mgr.provideRepeater(m1, m3);
        assertSame(m1, rpy.getMasterMast());
        assertSame(m3, rpy.getSlaveMast());

        assertNotSame(rpx, rpy);
        assertSame(rpx, mgr.provideRepeater(m1, m2));
        assertSame(rpy, mgr.provideRepeater(m1, m3));

        Exception ex = assertThrows(JmriException.class, () ->
            mgr.provideRepeater(m2, m1));
        String message = ex.getMessage();
        Assertions.assertNotNull(message);
        assertTrue( message.contains("repeater already exists the wrong way"),
            "wrong way repeater not in exception text");
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
