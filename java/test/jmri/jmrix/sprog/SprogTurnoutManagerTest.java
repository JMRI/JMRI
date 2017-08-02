package jmri.jmrix.sprog;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <P>
 * Tests for SprogTurnoutManager
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {
   private SprogSystemConnectionMemo m = null;

   @Override
   public String getSystemName(int i){
      return "ST" + i;
   }

   @Test
   public void ConstructorTest(){
       Assert.assertNotNull(l);
   }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
       apps.tests.Log4JFixture.setUp();
       jmri.util.JUnitUtil.resetInstanceManager();
       m = new SprogSystemConnectionMemo();
       l = new SprogTurnoutManager(m);
    }

    @After
    public void tearDown() {
        m=null;
        l=null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }


}
