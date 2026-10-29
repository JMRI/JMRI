package jmri.jmrix.pi;

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

   private PiGpioProviderScaffold myProvider = null;

    @BeforeEach
    public void setUp() {
       JUnitUtil.setUp();
       myProvider = new PiGpioProviderScaffold();
       jmri.util.JUnitUtil.resetInstanceManager();
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(myProvider);
        myProvider.shutdown();
        JUnitUtil.tearDown();
    }

}
