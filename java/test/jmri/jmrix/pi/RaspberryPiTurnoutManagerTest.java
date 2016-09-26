package jmri.jmrix.pi;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
 * Tests for RaspberryPiTurnoutManager
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiTurnoutManagerTest {

   @Test
   public void ConstructorTest(){
       RaspberryPiTurnoutManager m = new RaspberryPiTurnoutManager("Pi");
       Assert.assertNotNull(m);
   }

   @Test
   public void checkPrefix(){
       RaspberryPiTurnoutManager m = new RaspberryPiTurnoutManager("Pi");
       Assert.assertEquals("Prefix","Pi",m.getSystemPrefix());
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
           };

       };

       GpioFactory.setDefaultProvider(myprovider);

       jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }


}
