package jmri.jmrix.ieee802154.xbee;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for XBeeAdapter.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class XBeeAdapterTest {

   @Test
   public void ConstructorTest(){
       XBeeAdapter a = new XBeeAdapter();
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
