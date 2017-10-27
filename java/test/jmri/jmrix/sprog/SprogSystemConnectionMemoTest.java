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
public class SprogSystemConnectionMemoTest {

   @Test
   public void ConstructorTest(){
       SprogSystemConnectionMemo m = new SprogSystemConnectionMemo();
       Assert.assertNotNull(m);
   }

   @Test
   public void setAndGetSProgMode(){
       SprogSystemConnectionMemo m = new SprogSystemConnectionMemo();
       m.setSprogMode(SprogMode.SERVICE);
       Assert.assertEquals("Sprog Mode",SprogMode.SERVICE,m.getSprogMode());
   }

   @Test
   public void setAndGetTrafficController(){
       SprogSystemConnectionMemo m = new SprogSystemConnectionMemo();
       SprogTrafficController tc = new SprogTrafficControlScaffold(m);
       m.setSprogTrafficController(tc);
       Assert.assertEquals("Traffic Controller",tc,m.getSprogTrafficController());
   }

   @Test
   public void configureAndGetCSTest(){
       SprogSystemConnectionMemo m = new SprogSystemConnectionMemo();
       SprogTrafficController tc = new SprogTrafficControlScaffold(m);
       m.setSprogTrafficController(tc);
       m.setSprogMode(SprogMode.SERVICE);
       m.configureCommandStation();
       Assert.assertNotNull("Command Station",m.getCommandStation());
   }


    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }


}
