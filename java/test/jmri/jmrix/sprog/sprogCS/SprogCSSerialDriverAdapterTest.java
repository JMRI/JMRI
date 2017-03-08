package jmri.jmrix.sprog.sprogCS;

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
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }


}
