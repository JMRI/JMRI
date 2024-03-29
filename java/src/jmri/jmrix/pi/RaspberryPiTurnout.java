package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import jmri.implementation.AbstractTurnout;
import jmri.jmrix.pi.simulator.GpioSimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turnout interface to RaspberryPi GPIO pins.
 *
 * @author Paul Bender Copyright (C) 2015
 */
public class RaspberryPiTurnout extends AbstractTurnout implements java.io.Serializable {

    // in theory gpio can be static (as in PiSensor) because there will only ever
    // be one, but the library handles the details that make it a
    // singleton.
   private GpioController gpio = null;
   private GpioPinDigitalOutput pin = null;

   public RaspberryPiTurnout(String systemName) {
        super(systemName);
        log.trace("Provisioning turnout '{}'", systemName);
        init(systemName);
   }

   public RaspberryPiTurnout(String systemName, String userName) {
        super(systemName, userName);
        log.trace("Provisioning turnout '{}' with username '{}'", systemName, userName);
        init(systemName);
   }

   /**
    * Common initialization for all constructors.
    * <p>
    * Compare {@link RaspberryPiSensor}
    */
   private void init(String systemName) {
       log.debug("Provisioning turnout {}", systemName);
       if (gpio == null) {
            if (!RaspberryPiAdapter.isSimulator()) {
               gpio = GpioFactory.getInstance();
           } else {
               gpio = GpioSimulator.getInstance();
           }
       }
       int address = Integer.parseInt(getSystemName().substring(getSystemName().lastIndexOf("T") + 1));
       String pinName = "GPIO " + address;
       Pin p = RaspiPin.getPinByName(pinName);
       if (p != null) {
           try {
            pin = gpio.provisionDigitalOutputPin(p, getSystemName());
           } catch (java.lang.RuntimeException re) {
               log.error("Provisioning turnout {} failed with: {}", systemName, re.getMessage());
               throw new IllegalArgumentException(re.getMessage());
           }
           if (pin != null) {
               pin.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
           } else {
               String msg = Bundle.getMessage("ProvisioningFailed", pinName, getSystemName());
               log.error(msg);
               throw new IllegalArgumentException(msg);
           }
       } else {
           String msg = Bundle.getMessage("PinNameNotValid", pinName, systemName);
           log.error(msg);
           throw new IllegalArgumentException(msg);
       }
   }

   //support inversion for RPi turnouts
   @Override
   public boolean canInvert() {
       return true;
   }

   /**
    * {@inheritDoc}
    * Sets the GPIO pin.
    */
   @Override
   protected void forwardCommandChangeToLayout(int newState) {
      if (newState == CLOSED) {
         log.debug("Setting turnout '{}' to CLOSED", getSystemName());
         if (!getInverted()) {
             pin.high();
         } else {
             pin.low();
         }
      } else if (newState == THROWN) {
         log.debug("Setting turnout '{}' to THROWN", getSystemName());
         if (!getInverted()) {
             pin.low();
         } else {
             pin.high();
         }
      }
   }

   @Override
   public void dispose() {
       try {
           gpio.unprovisionPin(pin);
           // will remove it from the <GpioPin> pins list in _gpio
       } catch (com.pi4j.io.gpio.exception.GpioPinNotProvisionedException npe){
           log.trace("Pin not provisioned, was this turnout already disposed?");
       }
       super.dispose();
   }

   @Override
   protected void turnoutPushbuttonLockout(boolean locked){
   }

    private final static Logger log = LoggerFactory.getLogger(RaspberryPiTurnout.class);

}
