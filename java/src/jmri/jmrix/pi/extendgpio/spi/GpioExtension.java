/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.pi.extendgpio.spi;

import com.pi4j.io.gpio.*;

import jmri.spi.JmriServiceProviderInterface;

import jmri.Sensor;

/**
 *
 * @author dmj
 */
public interface GpioExtension extends JmriServiceProviderInterface {
    public String getExtensionName ();
    public String validateSystemNameFormat (String systemName);
    public GpioPinDigitalInput provisionDigitalInputPin(GpioController gpio, String systemName);
    public GpioPinDigitalOutput provisionDigitalOutputPin(GpioController gpio, String systemName);
    public Sensor.PullResistance [] getAvailablePullValues ();
}
