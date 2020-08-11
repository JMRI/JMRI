package jmri.jmrix.sprog.pi.pisprogonecs;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for PiSprogOneCSSerialDriverAdapter.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PiSprogOneCSSerialDriverAdapterTest {

   @Test
   public void ConstructorTest(){
       PiSprogOneCSSerialDriverAdapter a = new PiSprogOneCSSerialDriverAdapter();
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
