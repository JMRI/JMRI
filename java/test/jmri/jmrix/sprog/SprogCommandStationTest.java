package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SprogCommandStation.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogCommandStationTest {

   @Test
   public void testSprogCommandStationConstructor(){
       SprogSystemConnectionMemo m = new SprogSystemConnectionMemo();
       SprogTrafficController tc = new SprogTrafficControlScaffold(m);
       SprogCommandStation cs = new SprogCommandStation(tc);
       Assertions.assertNotNull(cs);
       tc.dispose();
   }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
