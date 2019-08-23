package jmri.jmrix.pi;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.WiringPiGpioProviderBase;

/**
 * Implementation of RaspberryPiAdapter that eases
 * checking whether data was forwarded or not
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PiGpioProviderScaffold extends WiringPiGpioProviderBase {

     @Override
     public String getName(){
        return "RaspberryPi GPIO Provider";
     }

     @Override
     public boolean hasPin(Pin pin) {
        return true;
     }

     @Override
     public void export(Pin pin, PinMode mode, PinState defaultState) {
     }

     @Override
     public void unexport(Pin pin) {
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

}
