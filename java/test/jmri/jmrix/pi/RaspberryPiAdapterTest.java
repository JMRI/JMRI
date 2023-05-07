package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for RaspberryPiAdapter
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiAdapterTest {

   @Test
   public void testCtor(){
       RaspberryPiAdapter a = new RaspberryPiAdapter();
       assertThat(a).isNotNull();
   }

   private GpioProvider myProvider = null;

    @BeforeEach
    public void setUp() {
       JUnitUtil.setUp();
       myProvider = new PiGpioProviderScaffold();
       GpioFactory.setDefaultProvider(myProvider);
       jmri.util.JUnitUtil.resetInstanceManager();
    }

    @AfterEach
    public void tearDown() {
        // shutdown() will forcefully shutdown all GPIO monitoring threads and scheduled tasks, includes unexport.pin
        Assertions.assertNotNull(myProvider);
        myProvider.shutdown();
        JUnitUtil.tearDown();
    }

}
