package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        scm = OlcbTestInterface.createForLegacyTests();
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
