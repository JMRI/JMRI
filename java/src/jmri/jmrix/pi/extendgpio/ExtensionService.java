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
 * ExtensionService - Handle the loading of Raspberry Pi GPIO extensions
 *                    such as MCP23017GpioExtension
 * 
 * @author Dave Jordan
 */
public class ExtensionService {
    private static ExtensionService service;
    private static ServiceLoader<GpioExtension> loader = ServiceLoader.load(GpioExtension.class);
    
    private static ArrayList<String> gpioExtensionNames = new ArrayList<String> ();
    private static ArrayList<GpioExtension> gpioExtensions = new ArrayList<GpioExtension> ();

    /**
     * Find an extension based on the extension name
     * 
     * @param extensionName is the name of the extension to be located
     * @return The extension or null if an extension could not be found
     */
     private static GpioExtension getExtension (String extensionName) { // throws Exception {
        int exIndex = -1;
        if (! gpioExtensionNames.isEmpty ()) {
            exIndex = gpioExtensionNames.indexOf (extensionName);
        }
        if (exIndex >= 0) {
            return gpioExtensions.get(exIndex);
        }
        try {
            Iterator<GpioExtension> extensions = loader.iterator();
            while (extensions.hasNext()) {
                GpioExtension ex = extensions.next ();
                if (extensionName.equals (ex.getExtensionName ())) {
                    gpioExtensionNames.add (extensionName);
                    gpioExtensions.add (ex);
                    return ex;
                }
            }
        } catch (ServiceConfigurationError serviceError) {
                serviceError.printStackTrace ();
        }
        return null;
    }
        
    /**
     * Find an extension based on the pin's system name
     * 
     * @param systemName is the name to be validated
     * @return The extension or null if an extension could not be found
     */
    public static GpioExtension getExtensionFromSystemName (String systemName) { // throws Exception {
        String tokens[] = systemName.split (":");
        if (tokens.length < 2) {
            return null;
        }
        return (getExtension (tokens[1]));
    }

    /**
     * Validate the format of a system name.  This mostly checks that an appropriate
     * extension can be found.
     * 
     * @param systemName is the name to be validated
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
