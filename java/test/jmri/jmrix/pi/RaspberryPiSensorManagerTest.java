package jmri.jmrix.pi;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * <P>
 * Tests for RaspberryPiSensorManager
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiSensorManagerTest {

   @Test
   public void ConstructorTest(){
       RaspberryPiSensorManager m = new RaspberryPiSensorManager("Pi");
       Assert.assertNotNull(m);
   }

   @Test
   public void checkPrefix(){
       RaspberryPiSensorManager m = new RaspberryPiSensorManager("Pi");
       Assert.assertEquals("Prefix","Pi",m.getSystemPrefix());
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
