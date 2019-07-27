package jmri.jmrix.grapevine;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.grapevine.SerialSensor class.
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SerialSensorTest extends jmri.implementation.AbstractSensorTestBase {

    private GrapevineSystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold tcis = null;

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkOnMsgSent() {}

    @Override
    public void checkOffMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new GrapevineSystemConnectionMemo();
        tcis = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tcis);
        t = new SerialSensor("GS1", memo);
    }

    // reset objects
    @After
    @Override
    public void tearDown() {
        t.dispose();
        tcis.terminateThreads();
        tcis = null;
        memo = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialSensorTest.class);

}
