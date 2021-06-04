/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.pi.extendgpio;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.*;

import java.io.IOException;
import java.util.ArrayList;

import jmri.Sensor;
import jmri.jmrix.pi.extendgpio.spi.GpioExtension;

import org.openide.util.lookup.ServiceProvider;

/**
 * MCP23017GpioExtension - Provide an interface between JMRI and the Raspberry Pi
 *                         Pi4j/WiringPi support for the MCP23017 GPIO extender
 * 
 * @author Dave Jordan
 */

@ServiceProvider(service = GpioExtension.class)
public class MCP23017GpioExtension implements GpioExtension {
    
    /**
     * Parse and store an MCP23017 Pin address and Pin.
     */
    static class ParsedPin {
        int busNumber;          // I2C Bus Number
        int channelNumber;      // MCP23017 Device Channel on that bus (32-39)
        int pinNumber;          // Pin Number on that MCP23017 (0-15)
        Pin pin;                // Pin as provided by Pi4j

        ParsedPin (String systemName) throws Exception {
            try {
                String [] tokens = systemName.split (":");
                if (tokens.length != 5) {
                    throw new Exception ();
                }
                busNumber     = Integer.parseInt(tokens[2]);
                channelNumber = Integer.parseInt(tokens[3]);
                pinNumber     = Integer.parseInt(tokens[4]);
                if ((pinNumber >= 0) && (pinNumber <= 15)) {
                    pin = MCP23017Pin.ALL[pinNumber];
                }
            } catch (NumberFormatException ex) {
                throw new Exception ();
            }
        }
    }

    /*
    *  Information about specific MCP23017 providers based on I2C bus number
    *  and device channel number on that I2C bus
    */

    static class ProviderElement {
        int busNumber;
        int channelNumber;
        MCP23017GpioProvider provider;

        ProviderElement (int bus, int chan, MCP23017GpioProvider _provider) {
            busNumber = bus;
            channelNumber = chan;
            provider = _provider;
        }
    }

    /*
    *  Maintain a cache of Extension providers that have been requested and loaded.
    */
    static ArrayList<ProviderElement> elementArray = new ArrayList<ProviderElement>();

    /**
     * Look up a bus/channel combination in the cache.  If found, use the cached provider; if not,
     * create a new provider and add it to the cache.
     */
    private MCP23017GpioProvider getProvider (int bus, int chan) {
        MCP23017GpioProvider provider;
        for (int i = 0; i < elementArray.size(); i++) {
            if ((bus == elementArray.get(i).busNumber) && (chan == elementArray.get(i).channelNumber)) {
                return elementArray.get(i).provider;
            }
        }
        try {
            provider = new MCP23017GpioProvider (bus, chan);
        } catch (com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException | IOException ex) {
            return null;
        }
        elementArray.add (new ProviderElement (bus, chan, provider));
        return provider;
    }

    /*
    *  The MCP23017 chip only supports PULL_UP and OFF
    */
    static Sensor.PullResistance [] MCP23017PullValues = { Sensor.PullResistance.PULL_UP,
                                                           Sensor.PullResistance.PULL_OFF };

    /**
     * Return the name of this extension
     * 
     * @return The extension name ("MCP23017")
     */
    public String getExtensionName () {
        return "MCP23017";
    }

    /**
     * Validate a pin SystemName
     * 
     * @param systemName The name to be validated
     * @return The validated system name or null if it could not be validated
     */
    public String validateSystemNameFormat (String systemName) {
        try {
            ParsedPin pp = new ParsedPin (systemName);
            MCP23017GpioProvider provider = getProvider (pp.busNumber, pp.channelNumber);
            if ((provider != null) && (pp.pinNumber >= 0) && (pp.pinNumber <= 15)) {
                return systemName;
            }
        } catch (Exception ex) {
        }
        return null;
    }

    /**
     * Provision a digital input pin
     * 
     * @param gpio The active GPIO Controller 
     * @param systemName The name of the pin
     * @return The input pin or null if it could not be provisioned
     */
    public GpioPinDigitalInput provisionDigitalInputPin(GpioController gpio, String systemName) {
        try {
            ParsedPin pp = new ParsedPin (systemName);
            MCP23017GpioProvider provider = getProvider (pp.busNumber, pp.channelNumber);
            if (provider == null) {
                return null;
            }
            GpioPinDigitalInput pin = gpio.provisionDigitalInputPin(provider, pp.pin, systemName);
            return pin;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Provision a digital output pin
     * 
     * @param gpio The active GPIO Controller 
     * @param systemName The name of the pin
     * @return The output pin or null if it could not be provisioned
     */
    public GpioPinDigitalOutput provisionDigitalOutputPin(GpioController gpio, String systemName) {
        try {
            ParsedPin pp = new ParsedPin (systemName);
            MCP23017GpioProvider provider = getProvider (pp.busNumber, pp.channelNumber);
            if (provider == null) {
                return null;
            }
            GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(provider, pp.pin, systemName, PinState.LOW);
            return pin;
        } catch (Exception ex) {
        }
        return null;
    }

    /**
     * Get an array of possible input pin pull resistance values
     * 
     * @return The array
     * 
     * The MCP23017 limits Pull values to either Pull Up or Off, Pull Down is not supported
     */
    @Override
    public Sensor.PullResistance [] getAvailablePullValues () {
        return MCP23017PullValues;
    }
    
}
