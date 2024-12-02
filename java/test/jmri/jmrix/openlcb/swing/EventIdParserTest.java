package jmri.jmrix.openlcb.swing;

import org.openlcb.EventID;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.can.CanInterface;

/**
 *
 * @author Bob Jacobsen  Copyright (C) 2024
 */
public class EventIdParserTest {

    @Test
    public void testBoring() {
        var eid = new EventID("02.02.59.00.00.00.00.00");
        Assert.assertEquals("", EventIdParser.parse(eid));
    }

    @Test
    public void testDefault() {
        var eid = new EventID("00.00.00.00.00.00.02.00");
        Assert.assertEquals("Reserved 00.00.00.00.00.00.02.00", EventIdParser.parse(eid));
    }

    @Test
    public void testWellKnown() {
        var eid = new EventID("01.00.00.00.00.00.FF.FE");
        Assert.assertEquals("Clear Emergency Off", EventIdParser.parse(eid));
    }

    @Test
    public void testFastClock() {
        var eid = new EventID("01.01.00.00.01.01.09.02");
        Assert.assertEquals("Fast Clock 1 time 9:02", EventIdParser.parse(eid));
        eid = new EventID("01.01.00.00.01.01.09.32");
        Assert.assertEquals("Fast Clock 1 time 9:50", EventIdParser.parse(eid));
        eid = new EventID("01.01.00.00.01.01.89.32");
        Assert.assertEquals("Fast Clock 1 Set time 9:50", EventIdParser.parse(eid));
        eid = new EventID("01.01.00.00.01.01.F0.02");
        Assert.assertEquals("Fast Clock 1 Start", EventIdParser.parse(eid));
    }

    @Test
    public void testRangeSuffix() {
        var eid = new EventID("00.00.00.00.00.00.FF.FF");
        Assert.assertEquals(0xFFFF, EventIdParser.rangeSuffix(eid));
        eid = new EventID("00.00.00.00.00.FF.00.00");
        Assert.assertEquals(0xFFFF, EventIdParser.rangeSuffix(eid));

        eid = new EventID("00.00.00.00.00.FF.80.00");
        Assert.assertEquals(0x7FFF, EventIdParser.rangeSuffix(eid));

        eid = new EventID("00.00.00.00.00.00.00.03");
        Assert.assertEquals(0x03, EventIdParser.rangeSuffix(eid));
    }

    @BeforeEach
    public void setUp() {

    }

    @AfterEach
    public void tearDown() {

    }

    // private final static Logger log = LoggerFactory.getLogger(EventIdParserTest.class);

}
