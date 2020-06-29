package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for SprogTrafficController.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogTrafficControllerTest {

   @Test
   public void ConstructorTest(){
       SprogSystemConnectionMemo m = new SprogSystemConnectionMemo();
       SprogTrafficController tc = new SprogTrafficController(m);
       Assert.assertNotNull(tc);
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
