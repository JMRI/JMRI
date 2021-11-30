package jmri.jmrix.maple;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the jmri.jmrix.maple.SerialSensor class
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SerialSensorTest extends jmri.implementation.AbstractSensorTestBase {

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkActiveMsgSent() {}

    @Override
    public void checkInactiveMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        //new MapleSystemConnectionMemo("K", "Maple");
        t = new SerialSensor("KS1"); // does not need the _memo
    }

    @AfterEach
    @Override
    public void tearDown() {
        t.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialSensorTest.class);

}
