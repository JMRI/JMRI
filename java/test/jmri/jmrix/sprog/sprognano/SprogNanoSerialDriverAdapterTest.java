package jmri.jmrix.sprog.sprognano;

import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SprogNanoSerialDriverAdapter.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogNanoSerialDriverAdapterTest {

   @Test
   public void ConstructorTest(){
       SprogNanoSerialDriverAdapter a = new SprogNanoSerialDriverAdapter();
       Assert.assertNotNull(a);

       // clean up
       a.getSystemConnectionMemo().getSprogTrafficController().dispose();
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
