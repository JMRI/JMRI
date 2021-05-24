package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Find and load the appropriate class for a given Raspberry Pi extended GPIO pin address. 
 * Invoke the appropriate method on the class to create & provision an input or output pin.
 * 
 * @author dmj
 */
public class RaspberryPiGpioExFactory {

    /**
     * Get the class name from the pin name based on the string between the first two colons
     * and use Class.forName to load it.
     */
    private static Class<?> getProvisionerClass (String SystemName) throws ClassNotFoundException {
        String proName = SystemName.substring(SystemName.indexOf(":")+1);
        proName = proName.substring(0, proName.indexOf(":"));
        String className = "jmri.jmrix.pi.Provision" + proName;
        return Class.forName(className);
    }

    /**
     * Get an output pin
     */
    public static GpioPinDigitalOutput provisionOutputPinByName (GpioController gpio, String SystemName) {
        try {
            Class<?> proClass = getProvisionerClass (SystemName);
            Method getPin = proClass.getMethod("provisionDigitalOutputPin", GpioController.class, String.class);
            return (GpioPinDigitalOutput) getPin.invoke(proClass.newInstance(), gpio, SystemName);
        } catch (Exception ex) {
            return null;
        }
    }
    
    /**
     * Get an input pin
     */
    public static GpioPinDigitalInput provisionInputPinByName (GpioController gpio, String SystemName) {
        try {
            Class<?> proClass = getProvisionerClass (SystemName);
            Method getPin = proClass.getMethod("provisionDigitalInputPin", GpioController.class, String.class);
            return (GpioPinDigitalInput) getPin.invoke(proClass.newInstance(), gpio, SystemName);
        } catch (Exception ex) {
            return null;
        }
    }

}