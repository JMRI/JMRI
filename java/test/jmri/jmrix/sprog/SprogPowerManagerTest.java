package jmri.jmrix.sprog;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * <P>
 * Tests for SprogPowerManager
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogPowerManagerTest {

   @Test
   public void ConstructorTest(){
       SprogSystemConnectionMemo m = new SprogSystemConnectionMemo();
       m.setSprogTrafficController(new SprogTrafficController(m));  // constructor calls getSprogTrafficController.
       SprogPowerManager tc = new SprogPowerManager(m);
       Assert.assertNotNull(tc);
   }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }


}
