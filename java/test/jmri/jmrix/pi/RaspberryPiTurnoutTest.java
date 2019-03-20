package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        GpioProvider myprovider = new PiGpioProviderScaffold();
        GpioFactory.setDefaultProvider(myprovider);
        jmri.util.JUnitUtil.resetInstanceManager();
        t = new RaspberryPiTurnout("PiT2"){
            @Override
            protected void forwardCommandChangeToLayout(int s){}
        };
    }

    @After
    public void tearDown() {
	t.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RaspberryPiTurnoutTest.class);

}
