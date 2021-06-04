/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.pi.extendgpio.spi;

import jmri.spi.JmriServiceProviderInterface;
import jmri.Sensor;

import jmri.NamedBean.BadSystemNameException;

import com.pi4j.io.gpio.*;

/**
 * GpioExtension - Interface to be implemented by Raspberry Pi GPIO extensions
 *                 such as MCP23017GpioExtension
 * 
 * @author Dave Jordan
 */
public interface GpioExtension extends JmriServiceProviderInterface {

    /**
     * Get the name of this extension
     * 
     * @return The name
     */
    public String getExtensionName ();

    /**
     * Validate a pin SystemName
     * 
     * @param systemName The name to be validated
     * @return The validated system name or null if it could not be validated
     * @throws BadSystemNameException If unable to validate the system name
     */
    public String validateSystemNameFormat (String systemName) throws BadSystemNameException;
    
    /**
     * Provision a digital input pin
     * 
     * @param gpio The active GPIO Controller 
     * @param systemName The name of the pin
     * @return The input pin or null if it could not be provisioned
     * @throws BadSystemNameException If unable to validate the system name
     */
    public GpioPinDigitalInput provisionDigitalInputPin(GpioController gpio, String systemName) throws BadSystemNameException;
    
    /**
     * Provision a digital output pin
     * 
     * @param gpio The active GPIO Controller 
     * @param systemName The name of the pin
     * @return The output pin or null if it could not be provisioned
     * @throws BadSystemNameException If unable to validate the system name
     */
    public GpioPinDigitalOutput provisionDigitalOutputPin(GpioController gpio, String systemName) throws BadSystemNameException;
    
    /**
     * Get an array of possible input pin pull resistance values
     * 
     * @return The array
     * 
     * The default implementation returns all values from Sensor.PullResistnace
     */
    default public Sensor.PullResistance [] getAvailablePullValues () {
        return Sensor.PullResistance.values();
    }
}
