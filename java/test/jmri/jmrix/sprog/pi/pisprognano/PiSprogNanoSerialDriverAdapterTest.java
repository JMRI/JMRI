package jmri.jmrix.sprog.pi.pisprognano;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <P>
 * Tests for PiSprogNanoSerialDriverAdapter
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class PiSprogNanoSerialDriverAdapterTest {

   @Test
   public void ConstructorTest(){
       PiSprogNanoSerialDriverAdapter a = new PiSprogNanoSerialDriverAdapter();
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
