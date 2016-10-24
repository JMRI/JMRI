package jmri.jmrix.pi;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.WiringPiGpioProviderBase;


/**
 * Tests for ConnectionConfig class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class ConnectionConfigTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("ConnectionConfig constructor",new ConnectionConfig());
   }

   @Before
   public void setUp(){
       apps.tests.Log4JFixture.setUp();
       GpioProvider myprovider = new WiringPiGpioProviderBase(){
           @Override
           public String getName(){
              return "RaspberryPi GPIO Provider";
           }
       };

       GpioFactory.setDefaultProvider(myprovider);

       jmri.util.JUnitUtil.resetInstanceManager();
   }

   @After
   public void tearDown(){
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
   }

}
