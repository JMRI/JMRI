package jmri.jmrix.bachrus;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SpeedoSystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.bachrus.SpeedoSystemConnectionMemo class
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SpeedoSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        scm = new SpeedoSystemConnectionMemo();
    }
   
    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
