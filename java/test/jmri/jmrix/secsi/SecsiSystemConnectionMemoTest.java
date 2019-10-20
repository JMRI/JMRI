package jmri.jmrix.secsi;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the SecsiSystemConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SecsiSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {
     
    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       SecsiSystemConnectionMemo memo = new SecsiSystemConnectionMemo();
       memo.setTrafficController(new SerialTrafficControlScaffold(memo));
       memo.configureManagers();
       scm = memo;
    }

    @Override
    @After
    public void tearDown(){
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
