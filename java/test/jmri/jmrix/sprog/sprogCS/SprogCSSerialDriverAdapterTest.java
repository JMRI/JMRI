package jmri.jmrix.sprog.sprogCS;

import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SprogCSSerialDriverAdapter.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogCSSerialDriverAdapterTest {

   @Test
   public void ConstructorTest(){
       SprogCSSerialDriverAdapter a = new SprogCSSerialDriverAdapter();
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
