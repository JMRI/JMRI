package jmri.jmrix.pi;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;

/**
 * Tests for RaspberryPiConnectionConfig class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class RaspberryPiConnectionConfigTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("ConnectionConfig constructor",new RaspberryPiConnectionConfig());
   }

   @Before
   public void setUp(){
       apps.tests.Log4JFixture.setUp();
       GpioProvider myprovider = new PiGpioProviderScaffold();
       GpioFactory.setDefaultProvider(myprovider);

       jmri.util.JUnitUtil.resetInstanceManager();
   }

   @After
   public void tearDown(){
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
   }

}
