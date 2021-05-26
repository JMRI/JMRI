package jmri.jmrix.pi;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

public class RaspberryPiGpioExFactoryTest {
    
    private GpioController gpio = null;
    
    @Test
    public void testBadOutputPin() {
        GpioPinDigitalOutput p = RaspberryPiGpioExFactory.provisionOutputPinByName (gpio, "");
        Assert.assertNull(p);
    }
    
    @Test
    public void testBadInputPin() {
        GpioPinDigitalInput p = RaspberryPiGpioExFactory.provisionInputPinByName (gpio, "");
        Assert.assertNull(p);
    }

    private GpioProvider myProvider;

    @BeforeEach
    public void setUp() {
       JUnitUtil.setUp();
       myProvider = new PiGpioProviderScaffold();
       GpioFactory.setDefaultProvider(myProvider);
       gpio = GpioFactory.getInstance ();
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    
}
