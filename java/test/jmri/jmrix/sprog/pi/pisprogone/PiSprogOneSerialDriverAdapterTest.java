package jmri.jmrix.sprog.pi.pisprogone;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for PiSprogOneSerialDriverAdapter.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PiSprogOneSerialDriverAdapterTest {

   @Test
   public void ConstructorTest(){
       PiSprogOneSerialDriverAdapter a = new PiSprogOneSerialDriverAdapter();
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
