package jmri.jmrix.dccpp;

import jmri.MeterGroup;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DCCppMeterGroupTest extends jmri.implementation.AbstractMeterGroupTestBase {

    @Test
    public void testCurrentReply() {
        ((DCCppMeterGroup) mm).message(DCCppReply.parseDCCppReply("a10")); // a syntactically valid current reply
        Assert.assertEquals("current level percentage 100.0 - 0.0", (10.0 / DCCppConstants.MAX_CURRENT) * 100, getCurrent(), 0.05);
    }

    public double getCurrent() {
        return mm.getMeterByName(MeterGroup.CurrentMeter).getMeter().getKnownAnalogValue();
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(tc);
        mm = new DCCppMeterGroup(memo);
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false, false);
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DCCppMeterGroupTest.class);
}
