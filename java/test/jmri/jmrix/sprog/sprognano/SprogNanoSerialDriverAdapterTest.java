package jmri.jmrix.sprog.sprognano;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * <P>
 * Tests for SprogNanoSerialDriverAdapter
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogNanoSerialDriverAdapterTest {

   @Test
   public void ConstructorTest(){
       SprogNanoSerialDriverAdapter a = new SprogNanoSerialDriverAdapter();
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
