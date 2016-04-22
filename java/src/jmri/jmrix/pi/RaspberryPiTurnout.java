// RaspberryPiTurnout.java

package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turnout interface to RaspberryPi GPIO pins.
 * <P>
 * 
 * @author Paul Bender Copyright (C) 2015 
 */
public class RaspberryPiTurnout extends AbstractTurnout implements Turnout, java.io.Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 2015_02_15_001L;

    // in theory gpio can be static, because there will only ever
    // be one, but the library handles the details that make it a 
    // singleton.
   private GpioController gpio = null;
   private GpioPinDigitalOutput pin = null;
   private int address;

   public RaspberryPiTurnout(String systemName) {
        super(systemName.toUpperCase());
	log.debug("Provisioning turnout {}",systemName);
        gpio=GpioFactory.getInstance();
        address=Integer.parseInt(getSystemName().substring(getSystemName().lastIndexOf("T")+1));
        pin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName("GPIO "+address),getSystemName());
        pin.setShutdownOptions(true, PinState.LOW,PinPullResistance.OFF);
   }

   public RaspberryPiTurnout(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
        log.debug("Provisioning turnout {} with username '{}'",systemName, userName);
        gpio=GpioFactory.getInstance();
        address=Integer.parseInt(getSystemName().substring(getSystemName().lastIndexOf("T")+1));
        pin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName("GPIO "+address),getUserName());
        pin.setShutdownOptions(true, PinState.LOW,PinPullResistance.OFF);
   }
    
   //support inversion for RPi turnouts
   public boolean canInvert() {
       return true;
   }
   
   /**
    * Handle a request to change state, typically by sending a message to the
    * layout in some child class. Public version (used by TurnoutOperator)
    * sends the current commanded state without changing it.
    * 
    * @param s new state value
    */
   @Override
   protected void forwardCommandChangeToLayout(int s){
      if(s==CLOSED){
         log.debug("Setting turnout {} to CLOSED", getSystemName());
         if (!getInverted()) pin.high();
         else pin.low();
      } else if(s==THROWN) {
         log.debug("Setting turnout {} to THROWN", getSystemName());
         if (!getInverted()) pin.low();
         else pin.high();
      }
   }

   @Override
   protected void turnoutPushbuttonLockout(boolean locked){
   }

    private final static Logger log = LoggerFactory.getLogger(RaspberryPiTurnout.class.getName());
}

/* @(#)RaspberryPiTurnout.java */


