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
public class SerialSensorTest {

    private GrapevineSystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold tcis = null;

    @Test
    public void testCTor() {
        SerialSensor t = new SerialSensor("GS1", memo);
        Assert.assertNotNull("exists", t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new GrapevineSystemConnectionMemo();
        tcis = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tcis);
    }

    // reset objects
    @After
    public void tearDown() {
        tcis.terminateThreads();
        tcis = null;
        memo = null;
        //t.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialSensorTest.class);

}
