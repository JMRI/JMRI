package jmri.jmrix.sprog.serialdriver;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SerialDriverAdapter.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialDriverAdapterTest {

   @Test
   public void testSprogSerialDriverAdapterConstructor(){
       SerialDriverAdapter a = new SerialDriverAdapter();
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
