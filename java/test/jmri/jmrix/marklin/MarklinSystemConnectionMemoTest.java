package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * MarklinSystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.marklin.MarklinSystemConnectionMemo class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class MarklinSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Test
    public void testCtorWithoutParameter() {
        MarklinSystemConnectionMemo c = new MarklinSystemConnectionMemo();
        Assert.assertNotNull(c);
    }

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
        MarklinTrafficController tc = new MarklinTrafficController(){
          @Override
          public void transmitLoop(){
          }
          @Override
          public void receiveLoop(){
          }
        };
        MarklinSystemConnectionMemo memo = new MarklinSystemConnectionMemo(tc);
        memo.configureManagers();
        scm = memo;
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
