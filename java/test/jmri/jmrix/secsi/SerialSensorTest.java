package jmri.jmrix.secsi;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SerialSensorTest extends jmri.implementation.AbstractSensorTestBase {

    private SerialTrafficControlScaffold tcis = null;
    private SecsiSystemConnectionMemo memo = null;

    @Override
    public int numListeners() {
        return 0;
    }

    @Override
    public void checkActiveMsgSent() {
    }

    @Override
    public void checkInactiveMsgSent() {
    }

    @Override
    public void checkStatusRequestMsgSent() {
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new SecsiSystemConnectionMemo();
        tcis = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tcis);
        t = new SerialSensor("VS1", memo);
    }

    @AfterEach
    @Override
    public void tearDown() {
        t.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialSensorTest.class);
}
