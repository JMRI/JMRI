package jmri.managers;

import jmri.JmriException;
import jmri.SignalMast;
import jmri.implementation.AbstractSignalMast;
import jmri.implementation.VirtualSignalMast;
import jmri.util.JUnitUtil;

import static org.hamcrest.core.StringContains.containsString;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DefaultSignalMastManagerTest {

    @Test
    public void testCTor() {
        DefaultSignalMastManager t = new DefaultSignalMastManager();
        Assert.assertNotNull("exists",t);
    }

    public static class MastA extends AbstractSignalMast {
        public MastA(String systemName) {
            super(systemName);
        }
    };

    public static class MastB extends AbstractSignalMast {
        public MastB(String systemName) {
            super(systemName);
        }
    };

    @Test
    public void testProvideCustomMast() throws Exception {
        DefaultSignalMastManager t = new DefaultSignalMastManager();

        SignalMast ma = t.provideCustomSignalMast("IM333", MastA.class);
        SignalMast mb = t.provideCustomSignalMast("IM444", MastB.class);

        Assert.assertTrue(ma instanceof MastA);
        Assert.assertTrue(mb instanceof MastB);

        SignalMast maa = t.provideCustomSignalMast("IM333", MastA.class);
        Assert.assertSame(ma, maa);
        SignalMast mbb = t.provideCustomSignalMast("IM444", MastB.class);
        Assert.assertSame(mb, mbb);

        SignalMast mac = t.provideCustomSignalMast("IM300", MastA.class);
        Assert.assertNotSame(ma, mac);
        SignalMast mbc = t.provideCustomSignalMast("IM400", MastB.class);
        Assert.assertNotSame(mb, mbc);

        try {
            t.provideCustomSignalMast("IM300", MastB.class);
            Assert.fail("provideCustomSignalMast Should have thrown exception.");
        } catch (JmriException e) {
            String s = e.toString();

            Assert.assertThat(s, containsString("system name is already used"));
            Assert.assertThat(s, containsString("MastA"));
            Assert.assertThat(s, containsString("MastB"));
        }
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastManagerTest.class);

}
