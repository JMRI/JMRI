package jmri.jmrix.pi;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.SimulatedGpioProvider;

/**
 * Implementation of RaspberryPiAdapter that eases
 * checking whether data was forwarded or not
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PiGpioProviderScaffold extends SimulatedGpioProvider {

     @Override
     public void setPullResistance(Pin pin, PinPullResistance resistance) {
     }
            
     @Override
     public PinState getState(Pin pin) {
        return PinState.HIGH;
     }

}
