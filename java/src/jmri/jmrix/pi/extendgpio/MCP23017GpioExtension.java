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
 *
 * @author dmj
 */


@ServiceProvider(service = GpioExtension.class)
public class MCP23017GpioExtension implements GpioExtension {
    
    /**
     * Parse and store an MCP23017 Pin address and Pin.
     */
    class ParsedPin {
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

    static Sensor.PullResistance [] MCP23017PullValues = { Sensor.PullResistance.PULL_UP,
                                                                Sensor.PullResistance.PULL_OFF };

    public String  getExtensionName () {
        return "MCP23017";
    }

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
    
    public Sensor.PullResistance [] getAvailablePullValues () {
        return MCP23017PullValues;
    }
    
}
