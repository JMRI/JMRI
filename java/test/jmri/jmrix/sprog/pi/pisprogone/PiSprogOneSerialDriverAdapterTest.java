package jmri.jmrix.sprog.pi.pisprogone;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for PiSprogOneSerialDriverAdapter.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PiSprogOneSerialDriverAdapterTest {

   @Test
   public void testPiSprogOneSerialDriverAdapterConstructor(){
       PiSprogOneSerialDriverAdapter a = new PiSprogOneSerialDriverAdapter();
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
