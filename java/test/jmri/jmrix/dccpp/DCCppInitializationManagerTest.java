package jmri.jmrix.dccpp;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * DCCppInitializationManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppInitializationManager class
 *
 * @author	Paul Bender
 * @author	Mark Underwood
 */
public class DCCppInitializationManagerTest {

    @Test
    public void testCtor() {

        // infrastructure objects
        DCCppInterfaceScaffold t = new DCCppInterfaceScaffold(new DCCppCommandStation());
        DCCppListenerScaffold l = new DCCppListenerScaffold();
        
        DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(t);

        DCCppInitializationManager m = new DCCppInitializationManager(memo) {
                @Override
                protected int getInitTimeout() {
                    return 50;   // shorten, because this will fail & delay test
                }
            };
        Assert.assertNotNull("exists", t);
        Assert.assertNotNull("exists", l);
        Assert.assertNotNull("exists", m);
        Assert.assertNotNull("exists", memo);
        //jmri.util.JUnitAppender.assertWarnMessage("Command Station disconnected, or powered down");
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
