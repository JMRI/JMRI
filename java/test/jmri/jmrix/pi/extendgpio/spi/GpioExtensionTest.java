/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.pi.extendgpio.spi;

import com.pi4j.io.gpio.*;

import jmri.NamedBean.BadSystemNameException;
import jmri.Sensor;
import jmri.jmrix.pi.extendgpio.ExtensionService;
import jmri.util.JUnitUtil;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the Raspberry Pi GpioExtension class.
 *
 * @author   Dave Jordan
 */
public class GpioExtensionTest {

    private class GpioExtensionImpl implements GpioExtension {

        @Override
        public String getExtensionName () {
            return "GpioExtensionImpl";
        }

        @Override
        public String validateSystemNameFormat (String systemName) throws BadSystemNameException {
            return null;
        }
    
        @Override
        public GpioPinDigitalInput provisionDigitalInputPin(GpioController gpio, String systemName) throws BadSystemNameException {
            return null;
        }
    
        @Override
        public GpioPinDigitalOutput provisionDigitalOutputPin(GpioController gpio, String systemName) throws BadSystemNameException {
            return null;
        }
    
        @Override
        public Sensor.PullResistance [] getAvailablePullValues () {
            return GpioExtension.super.getAvailablePullValues ();
        }
    
    }

    @Test
    public void testExtensionName(){
        GpioExtension ex = ExtensionService.getExtensionFromSystemName ("PT:MCP23017:");
        String s = ex.getExtensionName();
        Assert.assertEquals ("MCP23017", s);
    }

    @Test
    public void testPullValues() {
        GpioExtension ex = new GpioExtensionImpl ();
        Sensor.PullResistance [] myPV = ex.getAvailablePullValues ();
        Sensor.PullResistance [] defPV = Sensor.PullResistance.values();
        Assert.assertArrayEquals (defPV, myPV);
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
