
package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sensor interface for RaspberryPi GPIO pins.
 *
 * @author   Paul Bender Copyright (C) 2003-2017
 */
public class RaspberryPiSensor extends AbstractSensor implements GpioPinListenerDigital {

    private static GpioController gpio = null;
    private GpioPinDigitalInput pin = null;
    private PinPullResistance pull = PinPullResistance.PULL_DOWN;

    public RaspberryPiSensor(String systemName, String userName) {
        this(systemName, userName, GpioFactory.getInstance(), PinPullResistance.PULL_DOWN);
    }

    public RaspberryPiSensor(String systemName, String userName, PinPullResistance p) {
        this(systemName, userName, GpioFactory.getInstance(), p);
    }

    public RaspberryPiSensor(String systemName) {
        this(systemName, GpioFactory.getInstance(), PinPullResistance.PULL_DOWN);
    }

    public RaspberryPiSensor(String systemName, PinPullResistance p) {
        this(systemName, GpioFactory.getInstance(), p);
    }

    public RaspberryPiSensor(String systemName, String userName, GpioController _gpio) {
        super(systemName, userName);
        // default pull is Pull Down
        init(systemName, _gpio, PinPullResistance.PULL_DOWN);
    }

    public RaspberryPiSensor(String systemName, String userName, GpioController _gpio, PinPullResistance p) {
        super(systemName, userName);
        init(systemName, _gpio, p);
    }

    public RaspberryPiSensor(String systemName, GpioController _gpio) {
        super(systemName);
        init(systemName, _gpio, PinPullResistance.PULL_DOWN);
    }

    public RaspberryPiSensor(String systemName, GpioController _gpio, PinPullResistance p) {
        super(systemName);
        init(systemName, _gpio, p);
    }

    /**
     * Common initialization for all constructors.
     * <p>
     * Compare {@link RaspberryPiTurnout}
     */
    private void init(String systemName, GpioController _gpio, PinPullResistance pRes){
        log.debug("Provisioning sensor {}", systemName);
        if (gpio == null) {
            gpio = _gpio;
        }
        pull = pRes;
        int address = Integer.parseInt(systemName.substring(systemName.lastIndexOf("S") + 1));
        String pinName = "GPIO " + address;
        Pin p = RaspiPin.getPinByName(pinName);
        if (p != null) {
            try {
                pin = gpio.provisionDigitalInputPin(p, getSystemName(), pull);
            } catch (java.lang.RuntimeException re) {
                log.error("Provisioning sensor {} failed with: {}", systemName, re.getMessage());
                throw new IllegalArgumentException(re.getMessage());
            }
            if (pin != null) {
                pin.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
                pin.addListener(this);
                requestUpdateFromLayout(); // set state to match current value.
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

    /**
     * Request an update on status by sending an Instruction to the Pi.
     */
    @Override
    public void requestUpdateFromLayout() {
       if (pin.isHigh())
          setOwnState(Sensor.ACTIVE);
       else setOwnState(Sensor.INACTIVE);
    }

    @Override
    public void dispose() {
        try {
            gpio.unprovisionPin(pin);
            // will remove all listeners and triggers from pin and remove it from the <GpioPin> pins list in _gpio
        } catch ( com.pi4j.io.gpio.exception.GpioPinNotProvisionedException npe ){
            log.trace("Pin not provisioned, was this sensor already disposed?");
        }
        super.dispose();
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event){
       // log pin state change
       log.debug("GPIO PIN STATE CHANGE: {} = {}", event.getPin(), event.getState());
       if (event.getPin() == pin){
          if (event.getState().isHigh()) {
             setOwnState(!getInverted() ? Sensor.ACTIVE : Sensor.INACTIVE);
          } else {
             setOwnState(!getInverted() ? Sensor.INACTIVE : Sensor.ACTIVE);
          }
       }
    }

    /**
     * Set the pull resistance on the pin.
     *
     * @param pr The new PinPullResistance value to set.
     */
    private void setPullState(PinPullResistance pr){
        pull = pr;
        pin.setPullResistance(pull);
    }

    /**
     * Set the pull resistance.
     * <p>
     * In this default implementation, the input value is ignored.
     *
     * @param r PullResistance value to use.
     */
    @Override
    public void setPullResistance(PullResistance r){
       if (r == PullResistance.PULL_DOWN) {
          setPullState(PinPullResistance.PULL_DOWN);
       } else if(r == PullResistance.PULL_UP ) {
          setPullState(PinPullResistance.PULL_UP);
       } else {
          setPullState(PinPullResistance.OFF);
       }
    }

    /**
     * Get the pull resistance
     *
     * @return the currently set PullResistance value. In this default
     * implementation, PullResistance.PULL_OFF is always returned.
     */
    @Override
    public PullResistance getPullResistance(){
       if (pull == PinPullResistance.PULL_DOWN) {
          return PullResistance.PULL_DOWN;
       } else if(pull == PinPullResistance.PULL_UP) {
          return PullResistance.PULL_UP;
       } else {
          return PullResistance.PULL_OFF;
       }
    }

    private final static Logger log = LoggerFactory.getLogger(RaspberryPiSensor.class);

}
