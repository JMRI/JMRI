package jmri.jmrix.pi;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author dmj
 */
public class ProvisionMCP23017Test {

    private GpioController gpio = null;

    private GpioProvider myProvider;

    @Test
    public void testBadNameFormat() {
        ProvisionMCP23017 Pinstance = new ProvisionMCP23017 ();
        String s = Pinstance.validateSystemNameFormat ("PT:MCP23017:1:38:17");
        Assert.assertNull(s);
    }

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
