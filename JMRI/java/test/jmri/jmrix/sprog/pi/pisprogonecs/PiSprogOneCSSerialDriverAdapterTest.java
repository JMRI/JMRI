package jmri.jmrix.sprog.pi.pisprogonecs;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <P>
 * Tests for PiSprogOneCSSerialDriverAdapter
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class PiSprogOneCSSerialDriverAdapterTest {

   @Test
   public void ConstructorTest(){
       PiSprogOneCSSerialDriverAdapter a = new PiSprogOneCSSerialDriverAdapter();
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
