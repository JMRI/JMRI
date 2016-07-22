package jmri.jmrix.sprog.pi.pisprogone;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * <P>
 * Tests for PiSprogOneSerialDriverAdapter
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class PiSprogOneSerialDriverAdapterTest {

   @Test
   public void ConstructorTest(){
       PiSprogOneSerialDriverAdapter a = new PiSprogOneSerialDriverAdapter();
       Assert.assertNotNull(a);
   }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }


}
