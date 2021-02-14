package jmri.jmrix.nce;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NceSensorTest extends jmri.implementation.AbstractSensorTestBase {
    
    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkActiveMsgSent() {}

    @Override
    public void checkInactiveMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}


    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        t = new NceSensor("NS1");
    }

    @Override
    @AfterEach
    public void tearDown() {
        t.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NceSensorTest.class);

}
