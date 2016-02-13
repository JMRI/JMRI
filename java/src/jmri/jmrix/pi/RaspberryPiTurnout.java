// RaspberryPiTurnout.java

package jmri.jmrix.pi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;

/**
 * Turnout interface to RaspberryPi GPIO pins.
 * <P>
 * 
 * @author Paul Bender Copyright (C) 2015 
 * @version $Revision$
 */
public class RaspberryPiTurnout extends AbstractTurnout implements Turnout, java.io.Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 2015_02_15_001L;

   private static GpioController gpio = null;
   private GpioPinDigitalOutput pin = null;
   private int address;

   public RaspberryPiTurnout(String systemName) {
        super(systemName.toUpperCase());
	    log.debug("Provisioning turnout {}",systemName);
        if(gpio==null) gpio=GpioFactory.getInstance();
        address=Integer.parseInt(getSystemName().substring(getSystemName().lastIndexOf("T")+1));
        pin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName("GPIO "+address),getSystemName());
        pin.setShutdownOptions(true, PinState.LOW,PinPullResistance.OFF);
   }

   public RaspberryPiTurnout(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
        log.debug("Provisioning turnout {} with username {}",systemName, userName);
        if(gpio==null) gpio=GpioFactory.getInstance();
        address=Integer.parseInt(getSystemName().substring(getSystemName().lastIndexOf("T")+1));
        pin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName("GPIO "+address),getUserName());
        pin.setShutdownOptions(true, PinState.LOW,PinPullResistance.OFF);
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
         pin.high();
      } else if(s==THROWN) {
         log.debug("Setting turnout {} to THROWN", getSystemName());
         pin.low();
      }
   }

   @Override
   protected void turnoutPushbuttonLockout(boolean locked){
   }

    private final static Logger log = LoggerFactory.getLogger(RaspberryPiTurnout.class.getName());
}

/* @(#)RaspberryPiTurnout.java */


