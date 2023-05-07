package jmri.jmrix.ieee802154.serialdriver;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SerialDriverAdapter.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialDriverAdapterTest {

   @Test
   public void testCtor(){
       SerialDriverAdapter a = new SerialDriverAdapter();
       Assertions.assertNotNull(a);
       a.dispose();
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
