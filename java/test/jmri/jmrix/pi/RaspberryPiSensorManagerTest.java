package jmri.jmrix.pi;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.mockito.Mockito;
import static org.mockito.Mockito.any;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioUtil;

import jmri.Sensor;


@MockPolicy(Slf4jMockPolicy.class)
@PrepareForTest({Gpio.class,GpioUtil.class,GpioFactory.class,GpioProvider.class,GpioController.class})
@RunWith(PowerMockRunner.class)

/**
 * <P>
 * Tests for RaspberryPiSensorManager
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiSensorManagerTest extends jmri.managers.AbstractSensorMgrTest {

    private GpioController mocked_gpioController = null;
    private GpioPinDigitalInput mypin = null;

    @Override
    public String getSystemName(int i) {
        return "PiS" + i;
    }

    @Test
    public void ConstructorTest(){
        Assert.assertNotNull(l);
    }

    @Test
    public void checkPrefix(){
        Assert.assertEquals("Prefix","Pi",l.getSystemPrefix());
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
       //PowerMockito.mockStatic(Gpio.class);
       //PowerMockito.when(Gpio.wiringPiSetup()).thenReturn(0);
       //PowerMockito.mockStatic(GpioUtil.class);
       //PowerMockito.when(GpioUtil.isPinSupported(Mockito.anyInt())).thenReturn(0);
       mocked_gpioController = PowerMockito.mock(GpioController.class);
       PowerMockito.mockStatic(GpioProvider.class);
       PowerMockito.mockStatic(GpioFactory.class);
       PowerMockito.when(GpioFactory.getInstance()).thenReturn(mocked_gpioController);
       mypin= PowerMockito.mock(GpioPinDigitalInput.class);
       Mockito.when(mocked_gpioController.provisionDigitalInputPin(Mockito.any(Pin.class),Mockito.any(PinPullResistance.class))).thenReturn(mypin);
       jmri.util.JUnitUtil.resetInstanceManager();
       l = new RaspberryPiSensorManager("Pi");
    }

    @After
    public void tearDown() {
       jmri.util.JUnitUtil.resetInstanceManager();
    }

}
