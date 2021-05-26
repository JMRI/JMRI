package jmri.jmrix.pi;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Interface to create MCP23017 pins based on a System Name in the form:
 *      :MCP23017:<bus#>:<channel#>:<pin#>
 * Pins are managed by the Pi4j MCP23017GpioProvider and MCP23017Pin classes.
 * 
 * @author dmj
 */

/**
 * Parse and store an MCP23017 Pin address and Pin.
 */
class ParsedPin {
    int busNumber;          // I2C Bus Number
    int channelNumber;      // MCP23017 Device Channel on that bus (32-39)
    int pinNumber;          // Pin Number on that MCP23017 (0-15)
    Pin pin;                // Pin as provided by Pi4j
    
    ParsedPin (String SystemName) {
        String [] tokens = SystemName.split (":");
        busNumber     = Integer.parseInt(tokens[2]);
        channelNumber = Integer.parseInt(tokens[3]);
        pinNumber     = Integer.parseInt(tokens[4]);
        if ((pinNumber >= 0) && (pinNumber <= 15)) {
            pin = MCP23017Pin.ALL[pinNumber];
        }
    }
}

/**
 * Element of a static cache of GPIO Providers for MCP23017 devices.
 * The Pi4j provider manages the GPIO pins on the device as a group and only
 * works when a single instance of the class manages a given (bus/channel) device.
 * 
 * @author dmj
 */
class ProviderElement {
    int busNumber;
    int channelNumber;
    MCP23017GpioProvider provider;
    
    ProviderElement (int bus, int chan, MCP23017GpioProvider _provider) {
        busNumber = bus;
        channelNumber = chan;
        provider = _provider;
    }
}

public class ProvisionMCP23017 {
    static ArrayList<ProviderElement> elementArray = new ArrayList<ProviderElement>();       // Provider cache
    
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

    /**
     * Get an output pin.
     */
    public GpioPinDigitalOutput provisionDigitalOutputPin (GpioController gpio, String SystemName) {
        ParsedPin pp = new ParsedPin (SystemName);
        MCP23017GpioProvider provider = getProvider (pp.busNumber, pp.channelNumber);
        if (provider == null) {
            return null;
        }
        GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(provider, pp.pin, SystemName, PinState.LOW);
        return pin;
    }

    /**
     * Get an input pin.
     */
    public GpioPinDigitalInput provisionDigitalInputPin (GpioController gpio, String SystemName) {
        ParsedPin pp = new ParsedPin (SystemName);
        MCP23017GpioProvider provider = getProvider (pp.busNumber, pp.channelNumber);
        if (provider == null) {
            return null;
        }
        GpioPinDigitalInput pin = gpio.provisionDigitalInputPin(provider, pp.pin, SystemName);
        return pin;
    }
    
    public String validateSystemNameFormat (String SystemName) {
        try {
            ParsedPin pp = new ParsedPin (SystemName);
            MCP23017GpioProvider provider = getProvider (pp.busNumber, pp.channelNumber);
            if ((provider != null) && (pp.pinNumber >= 0) && (pp.pinNumber <= 15)) {
                return SystemName;
            }
        } catch (Exception ex) {
        }
        return null;
    }

}
