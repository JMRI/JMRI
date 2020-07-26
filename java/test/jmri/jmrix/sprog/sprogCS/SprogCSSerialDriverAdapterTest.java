package jmri.jmrix.sprog.sprogCS;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
