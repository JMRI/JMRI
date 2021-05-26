package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.openide.util.Exceptions;

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
        int colonIx = SystemName.indexOf (":");
        if (colonIx < 0) {
            throw new ClassNotFoundException();    
        }
        String proName = SystemName.substring(colonIx+1);
        colonIx = proName.indexOf (":");
        if (colonIx < 1) {
            throw new ClassNotFoundException();    
        }
        proName = proName.substring(0, colonIx);
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
        } catch (  ClassNotFoundException
                 | NoSuchMethodException
                 | SecurityException
                 | InstantiationException
                 | InvocationTargetException
                 | IllegalArgumentException
                 | IllegalAccessException ex) {
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
        } catch (  ClassNotFoundException
                 | NoSuchMethodException
                 | SecurityException
                 | InstantiationException
                 | InvocationTargetException
                 | IllegalArgumentException
                 | IllegalAccessException ex) {
            return null;
        }
    }
    
    public static String validateSystemNameFormat (String SystemName) throws jmri.NamedBean.BadSystemNameException {
        Class<?> proClass = null;
        try {
            proClass = getProvisionerClass (SystemName);
            Method validateName = proClass.getMethod ("validateSystemNameFormat");
            String validName = (String) validateName.invoke (proClass.newInstance(), SystemName);
            if (validName != null) {
                return validName;
            }
        } catch (Exception Ex) {
        }
        throw new jmri.NamedBean.BadSystemNameException ();
    }
}
