package jmri.jmrix.pi;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.WiringPiGpioProviderBase;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.PinPullResistance;



/**
 * <P>
 * Tests for RaspberryPiAdapter
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiAdapterTest {

   @Test
   public void ConstructorTest(){
       RaspberryPiAdapter a = new RaspberryPiAdapter();
       Assert.assertNotNull(a);
   }

    // The minimal setup for log4J
    @Before
    public void setUp() {
       apps.tests.Log4JFixture.setUp();
       GpioProvider myprovider = new WiringPiGpioProviderBase(){
           @Override
           public String getName(){
              return "RaspberryPi GPIO Provider";
           }

           @Override
           public boolean hasPin(Pin pin) {
              return false;
           }

           @Override
           public void export(Pin pin, PinMode mode, PinState defaultState) {
           }

           @Override
           public void setPullResistance(Pin pin, PinPullResistance resistance) {
           }

           @Override
           protected void updateInterruptListener(Pin pin) {
           }

           @Override
           public PinState getState(Pin pin) {
                  return PinState.HIGH;
           }

       };

       GpioFactory.setDefaultProvider(myprovider);

       jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }


}
