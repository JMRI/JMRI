package jmri.jmrix.ieee802154.serialdriver;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for SerialDriverAdapter.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialDriverAdapterTest {

   @Test
   public void ConstructorTest(){
       SerialDriverAdapter a = new SerialDriverAdapter();
       Assert.assertNotNull(a);
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
