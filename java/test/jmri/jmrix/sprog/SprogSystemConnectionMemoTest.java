package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;
import jmri.jmrix.sprog.SprogConstants.SprogMode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <P>
 * Tests for SprogSystemConnectionMemo
 * </P>
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
       Assert.assertEquals("Traffic Controller",tc,m.getSprogTrafficController());
   }

   @Test
   public void configureAndGetCSTest(){
       SprogSystemConnectionMemo m = (SprogSystemConnectionMemo)scm;
       SprogTrafficController tc = new SprogTrafficControlScaffold(m);
       m.setSprogTrafficController(tc);
       m.setSprogMode(SprogMode.SERVICE);
       m.configureCommandStation();
       Assert.assertNotNull("Command Station",m.getCommandStation());
   }

   @Override
   @Test
   public void testProvidesConsistManager(){
       // by default, does not.
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
       SprogSystemConnectionMemo m = (SprogSystemConnectionMemo)scm;
       // In service mode, does not.
       m.setSprogMode(SprogMode.SERVICE);
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
       // In ops mode, does.
       m.setSprogMode(SprogMode.OPS);
       Assert.assertTrue("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
   }



    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        scm = new SprogSystemConnectionMemo();
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }


}
