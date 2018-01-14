package jmri.jmrix.sprog.serialdriver;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
