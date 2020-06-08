package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetInitializationManagerTest.java
 *
 * Test for the jmri.jmrix.lenz.XNetInitializationManager class
 *
 * @author Paul Bender
 */
public class XNetInitializationManagerTest {

    @Test
    public void testCtor() {

// infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        XNetListenerScaffold l = new XNetListenerScaffold();

        XNetSystemConnectionMemo memo = new XNetSystemConnectionMemo(t);

        XNetInitializationManager m = new XNetInitializationManager();
        m.memo(memo).setDefaults().setTimeout(50).versionCheck().init();
        Assert.assertNotNull("exists", t);
        Assert.assertNotNull("exists", l);
        Assert.assertNotNull("exists", m);
        Assert.assertNotNull("exists", memo);
        jmri.util.JUnitAppender.assertWarnMessage("Command Station disconnected, or powered down assuming LZ100/LZV100 V3.x");
        t.terminateThreads();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After 
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
