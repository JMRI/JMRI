package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;
import jmri.jmrix.sprog.SprogConstants.SprogMode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SprogSystemConnectionMemo.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

   @Test
   public void setAndGetSProgMode(){
       SprogSystemConnectionMemo m = (SprogSystemConnectionMemo)scm;
       m.setSprogMode(SprogMode.SERVICE);
       Assert.assertEquals("Sprog Mode",SprogMode.SERVICE,m.getSprogMode());
   }

   @Test
   public void setAndGetTrafficController(){
       SprogSystemConnectionMemo m = (SprogSystemConnectionMemo)scm;
       SprogTrafficController tc = new SprogTrafficControlScaffold(m);
       m.setSprogTrafficController(tc);
       Assert.assertEquals("Traffic Controller", tc, m.getSprogTrafficController());
       tc.dispose();
   }

   @Test
   public void configureAndGetCSTest(){
       SprogSystemConnectionMemo m = (SprogSystemConnectionMemo)scm;
       SprogTrafficController tc = new SprogTrafficControlScaffold(m);
       m.setSprogTrafficController(tc);
       m.setSprogMode(SprogMode.SERVICE);
       m.configureCommandStation();
       Assert.assertNotNull("Command Station",m.getCommandStation());
       tc.dispose();
   }

   @Override
   @Test
   public void testProvidesConsistManager(){
        SprogSystemConnectionMemo memo = new SprogSystemConnectionMemo();
       // by default, does.
       Assert.assertTrue("Provides ConsistManager", memo.provides(jmri.ConsistManager.class));
       // In service mode, does not.
       memo.setSprogMode(SprogMode.SERVICE);
       Assert.assertFalse("Provides ConsistManager", memo.provides(jmri.ConsistManager.class));
       // In ops mode, does.
       memo.setSprogMode(SprogMode.OPS);
       Assert.assertTrue("Provides ConsistManager", memo.provides(jmri.ConsistManager.class));
   }


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        SprogSystemConnectionMemo memo = new SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.OPS);
        stcs = new SprogTrafficControlScaffold(memo);
        memo.setSprogTrafficController(stcs);
        memo.configureManagers();
        scm = memo;
    }

    private SprogTrafficController stcs;
    
    @Override
    @After
    public void tearDown() {
        stcs.dispose();
        JUnitUtil.tearDown();
    }

}
