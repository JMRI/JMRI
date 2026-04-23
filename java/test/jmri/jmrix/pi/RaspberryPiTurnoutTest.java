package jmri.jmrix.pi;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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

    private PiGpioProviderScaffold myProvider = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        myProvider = new PiGpioProviderScaffold();

        t = new RaspberryPiTurnout("PT2"){
            @Override
            protected void forwardCommandChangeToLayout(int s){}
        };
    }

    @Override
    @AfterEach
    public void tearDown() {
        if (t != null) {
            t.dispose();
        }
        Assertions.assertNotNull(myProvider);
        myProvider.shutdown();

        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RaspberryPiTurnoutTest.class);

}
