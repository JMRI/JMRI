package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.can.TestTrafficController;

/**
 * OlcbSystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.openlcb.OlcbSystemConnectionMemo class
 *
 * @author	Bob Jacobsen
 * @author      Paul Bender Copyright (C) 2016	
 */
public class OlcbSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Test
    public void testProvidesConsistManager(){
       ((OlcbSystemConnectionMemo)scm).configureManagers();
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        scm  = new OlcbSystemConnectionMemo();
        TestTrafficController tc = new TestTrafficController();
        ((OlcbSystemConnectionMemo)scm).setTrafficController(tc);
    }

    @Override
    @After
    public void tearDown() {
        scm = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}
