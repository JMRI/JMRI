// RaspberryPiSensor.java

package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractSensor for RaspberryPi GPIO pins.
 * <P>
 * @author			Paul Bender Copyright (C) 2003-2010
 * @version         $Revision$
 */
public class RaspberryPiSensor extends AbstractSensor implements GpioPinListenerDigital {

    private static final long serialVersionUID = 2015_02_16L;
    private static GpioController gpio = null;
    private int address;
    private GpioPinDigitalInput pin = null;

    public RaspberryPiSensor(String systemName, String userName) {
        this(systemName,userName,GpioFactory.getInstance());
    }

    public RaspberryPiSensor(String systemName) {
        this(systemName,GpioFactory.getInstance());
    }

    public RaspberryPiSensor(String systemName, String userName,GpioController _gpio) {
        super(systemName, userName);
        init(systemName,_gpio);
    }

    public RaspberryPiSensor(String systemName,GpioController _gpio) {
        super(systemName);
        init(systemName,_gpio);
    }

    /**
     * Common initialization for all constructors
     */
    private void init(String id,GpioController _gpio){
        log.debug("Provisioning sensor {}", id);
        if(gpio==null)
           gpio=_gpio;
        address=Integer.parseInt(id.substring(id.lastIndexOf("S")+1));
        try {
           pin = gpio.provisionDigitalInputPin(RaspiPin.getPinByName("GPIO "+address),getSystemName(),PinPullResistance.PULL_DOWN);
        } catch(java.lang.RuntimeException re) {
            log.error("Provisioning sensor {} failed with: {}", id, re.getMessage());
        }
        pin.addListener(this);
        requestUpdateFromLayout(); // set state to match current value.
    }

    /**
     * request an update on status by sending an Instruction to the Pi
     */
    @Override
    public void requestUpdateFromLayout() {
       if(pin.isHigh()) 
          setOwnState(Sensor.ACTIVE);
       else setOwnState(Sensor.INACTIVE);
    }

    public void dispose() {
        super.dispose();
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event){
       // log pin state change
       log.debug("GPIO PIN STATE CHANGE: {} = {}",event.getPin(),event.getState());
       if(event.getPin()==pin){
          if(event.getState().isHigh()) { 
             setOwnState(!getInverted() ? Sensor.ACTIVE : Sensor.INACTIVE);
          } else {
             setOwnState(!getInverted() ? Sensor.INACTIVE : Sensor.ACTIVE);
          }
       }
    }

    private final static Logger log = LoggerFactory.getLogger(RaspberryPiSensor.class.getName());

}


/* @(#)RaspberryPiSensor.java */
