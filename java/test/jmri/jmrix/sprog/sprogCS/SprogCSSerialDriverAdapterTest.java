package jmri.jmrix.sprog.sprogCS;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <P>
 * Tests for SprogCSSerialDriverAdapter
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogCSSerialDriverAdapterTest {

   @Test
   public void ConstructorTest(){
       SprogCSSerialDriverAdapter a = new SprogCSSerialDriverAdapter();
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
