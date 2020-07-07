package jmri.jmrix.maple;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the jmri.jmrix.maple.SerialLight class
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SerialLightTest {

//    private SerialTrafficControlScaffold tcis = null;
    private MapleSystemConnectionMemo _memo = null;

    @Test
    public void testCTor() {
        SerialLight l = new SerialLight("KL1", _memo);
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testCTor2() {
        SerialLight l2 = new SerialLight("KL2", "light2", _memo);
        Assert.assertNotNull("exists", l2);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
//        tcis = new SerialTrafficControlScaffold();
        _memo = new MapleSystemConnectionMemo("K", "Maple");
    }

    @AfterEach
    public void tearDown() {
        _memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(SerialLightTest.class);

}
