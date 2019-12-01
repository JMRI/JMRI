package jmri.jmrix.secsi;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SerialSensorTest extends jmri.implementation.AbstractSensorTestBase {

    private SerialTrafficControlScaffold tcis = null;
    private SecsiSystemConnectionMemo memo = null;

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
        memo = new SecsiSystemConnectionMemo();
        tcis = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tcis);
        t = new SerialSensor("VS1", memo);
    }

    @After
    @Override
    public void tearDown() {
    	t.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialSensorTest.class);

}
