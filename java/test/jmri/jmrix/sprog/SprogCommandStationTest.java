package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SprogCommandStation.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogCommandStationTest {

   @Test
   public void ConstructorTest(){
       SprogSystemConnectionMemo m = new SprogSystemConnectionMemo();
       SprogTrafficController tc = new SprogTrafficControlScaffold(m);
       SprogCommandStation cs = new SprogCommandStation(tc);
       Assert.assertNotNull(cs);
       tc.dispose();
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
