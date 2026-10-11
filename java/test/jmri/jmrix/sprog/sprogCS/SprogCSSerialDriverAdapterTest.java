package jmri.jmrix.sprog.sprogCS;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SprogCSSerialDriverAdapter.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogCSSerialDriverAdapterTest {

   @Test
   public void testSprogCSSerialDriverAdapterConstructor(){
       SprogCSSerialDriverAdapter a = new SprogCSSerialDriverAdapter();
       Assertions.assertNotNull(a);

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
