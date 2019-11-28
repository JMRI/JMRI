package jmri.jmrix.lenz.hornbyelite;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetListenerScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * EliteXNetInitializationManagerTest.java
 *
 * Description: tests for the jmri.jmrix.lenz.EliteXNetInitializationManager
 * class
 *
 * @author Paul Bender
 */
public class EliteXNetInitializationManagerTest {

    @Test
    public void testCtor() {

        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new HornbyEliteCommandStation());
        XNetListenerScaffold l = new XNetListenerScaffold();

        XNetSystemConnectionMemo memo = new XNetSystemConnectionMemo(t);

        EliteXNetInitializationManager m = new EliteXNetInitializationManager(memo) {
            @Override
            protected int getInitTimeout() {
                return 50; // shorten, because this will fail & delay test
            }
        };
        Assert.assertNotNull("exists", t);
        Assert.assertNotNull("exists", l);
        Assert.assertNotNull("exists", m);
        Assert.assertNotNull("exists", memo);
    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
	    jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        jmri.util.JUnitUtil.tearDown();
    }

}
