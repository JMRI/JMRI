package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
