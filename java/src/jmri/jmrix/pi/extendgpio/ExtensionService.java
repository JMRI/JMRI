/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.pi.extendgpio;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.io.gpio.*;

import jmri.jmrix.pi.extendgpio.spi.GpioExtension;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import jmri.Sensor;

/**
 *
 * @author dmj
 */
public class ExtensionService {
    private static ExtensionService service;
    private static ServiceLoader<GpioExtension> loader = ServiceLoader.load(GpioExtension.class);
    
    private static ArrayList<String> gpioExtensionNames = new ArrayList<String> ();
    private static ArrayList<GpioExtension> gpioExtensions = new ArrayList<GpioExtension> ();

    private static GpioExtension getExtension (String ExtensionName) { // throws Exception {
        int exIndex = -1;
        if (! gpioExtensionNames.isEmpty ()) {
            exIndex = gpioExtensionNames.indexOf (ExtensionName);
        }
        if (exIndex >= 0) {
            return gpioExtensions.get(exIndex);
        }
        try {
            Iterator<GpioExtension> extensions = loader.iterator();
            while (extensions.hasNext()) {
                GpioExtension ex = extensions.next ();
                if (ExtensionName.equals (ex.getExtensionName ())) {
                    gpioExtensionNames.add (ExtensionName);
                    gpioExtensions.add (ex);
                    return ex;
                }
            }
        } catch (ServiceConfigurationError serviceError) {
                serviceError.printStackTrace ();
        }
        return null;
    }
    
    private ExtensionService() {
        loader = ServiceLoader.load(GpioExtension.class);
    }
    
    public static synchronized ExtensionService getInstance() {
        if (service == null) {
            service = new ExtensionService();
        }
        return service;
    }
    
//    public String getExtensionName () {
//        return 
//    }
//    public String validateSystemNameFormat (String systemName);
//    public GpioPinDigitalInput provisionDigitalInputPin(GpioController gpio, String systemName);
//    public GpioPinDigitalOutput provisionDigitalOutputPin(GpioController gpio, String systemName);
//    public Sensor.PullResistance [] getPullResistances ();
    public static GpioExtension getExtensionFromSystemName (String systemName) { // throws Exception {
        String tokens[] = systemName.split (":");
        if (tokens.length < 2) {
            return null;
        }
        return (getExtension (tokens[1]));
    }

        /**
     * Validate the format of a system name
     * 
     * @param SystemName is the name to be validated
     * @return The validated system name or null if the name could not be validated.
     */
    public static String validateSystemNameFormat (String systemName) {
        GpioExtension ex = getExtensionFromSystemName (systemName);
        if (ex != null) {
            return ex.validateSystemNameFormat(systemName);
        }
        return null;
    }

}
