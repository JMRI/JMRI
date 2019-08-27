package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RaspberryPiTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase {

    @Override
    public int numListeners() { return 0; }

    @Override
    public void checkThrownMsgSent() throws InterruptedException {}

    @Override
    public void checkClosedMsgSent() throws InterruptedException {}

    private GpioProvider myProvider;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        myProvider = new PiGpioProviderScaffold();
        GpioFactory.setDefaultProvider(myProvider);

        t = new RaspberryPiTurnout("PT2"){
            @Override
            protected void forwardCommandChangeToLayout(int s){}
        };
    }

    @After
    public void tearDown() {
        if (t != null) {
            t.dispose(); // is supposed to unprovisionPin 2
        }
        // shutdown() will forcefully shutdown all GPIO monitoring threads and scheduled tasks, includes unexport.pin
        myProvider.shutdown();

        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RaspberryPiTurnoutTest.class);

}
