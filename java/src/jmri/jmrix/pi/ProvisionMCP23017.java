package jmri.jmrix.pi;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.*;

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
class parsedPin {
    int BusNumber;          // I2C Bus Number
    int ChannelNumber;      // MCP23017 Device Channel on that bus (32-39)
    int PinNumber;          // Pin Number on that MCP23017 (0-15)
    Pin Pin;                // Pin as provided by Pi4j
    
    parsedPin (String SystemName) {
        String [] tokens = SystemName.split (":");
        BusNumber     = Integer.parseInt(tokens[2]);
        ChannelNumber = Integer.parseInt(tokens[3]);
        PinNumber     = Integer.parseInt(tokens[4]);
        Pin           = MCP23017Pin.ALL[PinNumber];
    }
}

/**
 * Element of a static cache of GPIO Providers for MCP23017 devices.
 * The Pi4j provider manages the GPIO pins on the device as a group and only
 * works when a single instance of the class manages a given (bus/channel) device.
 * 
 * @author dmj
 */
class providerElement {
    int BusNumber;
    int ChannelNumber;
    MCP23017GpioProvider Provider;
    
    providerElement (int bus, int chan, MCP23017GpioProvider provider) {
        BusNumber = bus;
        ChannelNumber = chan;
        Provider = provider;
    }
}

public class ProvisionMCP23017 {
    static ArrayList<providerElement> elementArray = new ArrayList();       // Provider cache
    
    /**
     * Look up a bus/channel combination in the cache.  If found, use the cached provider; if not,
     * create a new provider and add it to the cache.
     */
    private MCP23017GpioProvider getProvider (int bus, int chan) {
        MCP23017GpioProvider provider;
        for (int i = 0; i < elementArray.size(); i++) {
            if ((bus == elementArray.get(i).BusNumber) && (chan == elementArray.get(i).ChannelNumber)) {
                return elementArray.get(i).Provider;
            }
        }
        try {
            provider = new MCP23017GpioProvider (bus, chan);
        } catch (Exception ex) {
            return null;
        }
        elementArray.add (new providerElement (bus, chan, provider));
        return provider;
    }

    /**
     * Get an output pin.
     */
    public GpioPinDigitalOutput provisionDigitalOutputPin (GpioController gpio, String SystemName) {
        try {
            parsedPin pp = new parsedPin (SystemName);
            MCP23017GpioProvider provider = getProvider (pp.BusNumber, pp.ChannelNumber);
            GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(provider, pp.Pin, SystemName, PinState.LOW);
            return pin;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Get an input pin.
     */
    public GpioPinDigitalInput provisionDigitalInputPin (GpioController gpio, String SystemName) {
        try {
            parsedPin pp = new parsedPin (SystemName);
            MCP23017GpioProvider provider = getProvider (pp.BusNumber, pp.ChannelNumber);
            GpioPinDigitalInput pin = gpio.provisionDigitalInputPin(provider, pp.Pin, SystemName);
            return pin;
        } catch (Exception ex) {
            return null;
        }
    }
    
    public String validateSystemName (GpioController gpio, String SystemName) {
        try {
            parsedPin pp = new parsedPin (SystemName);
            MCP23017GpioProvider provider = getProvider (pp.BusNumber, pp.ChannelNumber);
            if ((pp.PinNumber >= 0) && (pp.PinNumber <= 15)) {
                return SystemName;
            }
        } catch (Exception ex) {
        }
        return null;
    }

}