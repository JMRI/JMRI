package jmri.jmrix.loconet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Michael Richardson Copyright (C) 2021
 */
public class LnConstantsTest {

    // No Ctor test, class only supplies static methods.

    @Test
    public void testOpCodeConstants() {
        assertEquals( 0x81, LnConstants.OPC_GPBUSY, "check 0x81");
        assertEquals( 0x82, LnConstants.OPC_GPOFF, "check 0x82");
        assertEquals( 0x83, LnConstants.OPC_GPON, "check 0x83");
        assertEquals( 0x85, LnConstants.OPC_IDLE, "check 0x85");
        assertEquals( 0x8a, LnConstants.OPC_RE_LOCORESET_BUTTON, "check 0x8a");
        assertEquals( 0xa0, LnConstants.OPC_LOCO_SPD, "check 0xa0");
        assertEquals( 0xa1, LnConstants.OPC_LOCO_DIRF, "check 0xa1");
        assertEquals( 0xa2, LnConstants.OPC_LOCO_SND, "check 0xa2");
        assertEquals( 0xb0, LnConstants.OPC_SW_REQ, "check 0xb0");
        assertEquals( 0xb1, LnConstants.OPC_SW_REP, "check 0xb1");
        assertEquals( 0xb2, LnConstants.OPC_INPUT_REP, "check 0xb2");
        assertEquals( 0xb3, LnConstants.OPC_UNKNOWN, "check 0xb3");
        assertEquals( 0xb4, LnConstants.OPC_LONG_ACK, "check 0xb4");
        assertEquals( 0xb5, LnConstants.OPC_SLOT_STAT1, "check 0xb5");
        assertEquals( 0xb6, LnConstants.OPC_CONSIST_FUNC, "check 0xb6");
        assertEquals( 0xb8, LnConstants.OPC_UNLINK_SLOTS, "check 0xb8");
        assertEquals( 0xb9, LnConstants.OPC_LINK_SLOTS, "check 0xb9");
        assertEquals( 0xba, LnConstants.OPC_MOVE_SLOTS, "check 0xba");
        assertEquals( 0xbb, LnConstants.OPC_RQ_SL_DATA, "check 0xbb");
        assertEquals( 0xbc, LnConstants.OPC_SW_STATE, "check 0xbc");
        assertEquals( 0xbd, LnConstants.OPC_SW_ACK, "check 0xbd");
        assertEquals( 0xbf, LnConstants.OPC_LOCO_ADR, "check 0xbf");
        assertEquals( 0xd0, LnConstants.OPC_MULTI_SENSE, "check 0xd0");
        assertEquals( 0xd7, LnConstants.OPC_PANEL_RESPONSE, "check 0xd7");
        assertEquals( 0xdf, LnConstants.OPC_PANEL_QUERY, "check 0xdf");
        assertEquals( 0xe0, LnConstants.OPC_MULTI_SENSE_LONG, "check 0xe0");
        assertEquals( 0xe4, LnConstants.OPC_LISSY_UPDATE, "check 0xe4");
        assertEquals( 0xe5, LnConstants.OPC_PEER_XFER, "check 0xe5");
        assertEquals( 0xe6, LnConstants.OPC_ALM_READ, "check 0xe6");
        assertEquals( 0xe7, LnConstants.OPC_SL_RD_DATA, "check 0xe7");
        assertEquals( 0xed, LnConstants.OPC_IMM_PACKET, "check 0xed");
        assertEquals( 0xee, LnConstants.OPC_IMM_PACKET_2, "check 0xee");
        assertEquals( 0xef, LnConstants.OPC_WR_SL_DATA, "check 0xef");
        assertEquals( 0xee, LnConstants.OPC_ALM_WRITE, "check 0xee");
    }

    @Test
    public void testOPC_NAME() {
        assertEquals( "OPC_GPBUSY", LnConstants.OPC_NAME(0x81), "check OPC_NAME(0x81)");
        assertEquals( "OPC_GPOFF", LnConstants.OPC_NAME(0x82), "check OPC_NAME(0x82)");
        assertEquals( "OPC_GPON", LnConstants.OPC_NAME(0x83), "check OPC_NAME(0x83)");
        assertEquals( "OPC_IDLE", LnConstants.OPC_NAME(0x85), "check OPC_NAME(0x85)");
        assertEquals( "OPC_RE_LOCORESET_BUTTON", LnConstants.OPC_NAME(0x8a), "check OPC_NAME(0x8a)");
        assertEquals( "OPC_LOCO_SPD", LnConstants.OPC_NAME(0xa0), "check OPC_NAME(0xa0)");
        assertEquals( "OPC_LOCO_DIRF", LnConstants.OPC_NAME(0xa1), "check OPC_NAME(0xa1)");
        assertEquals( "OPC_LOCO_SND", LnConstants.OPC_NAME(0xa2), "check OPC_NAME(0xa2)");
        assertEquals( "OPC_SW_REQ", LnConstants.OPC_NAME(0xb0), "check OPC_NAME(0xb0)");
        assertEquals( "OPC_SW_REP", LnConstants.OPC_NAME(0xb1), "check OPC_NAME(0xb1)");
        assertEquals( "OPC_INPUT_REP", LnConstants.OPC_NAME(0xb2), "check OPC_NAME(0xb2)");
        assertEquals( "OPC_UNKNOWN", LnConstants.OPC_NAME(0xb3), "check OPC_NAME(0xb3)");
        assertEquals( "OPC_LONG_ACK", LnConstants.OPC_NAME(0xb4), "check OPC_NAME(0xb4)");
        assertEquals( "OPC_SLOT_STAT1", LnConstants.OPC_NAME(0xb5), "check OPC_NAME(0xb5)");
        assertEquals( "OPC_CONSIST_FUNC", LnConstants.OPC_NAME(0xb6), "check OPC_NAME(0xb6)");
        assertEquals( "OPC_UNLINK_SLOTS", LnConstants.OPC_NAME(0xb8), "check OPC_NAME(0xb8)");
        assertEquals( "OPC_LINK_SLOTS", LnConstants.OPC_NAME(0xb9), "check OPC_NAME(0xb9)");
        assertEquals( "OPC_MOVE_SLOTS", LnConstants.OPC_NAME(0xba), "check OPC_NAME(0xba)");
        assertEquals( "OPC_RQ_SL_DATA", LnConstants.OPC_NAME(0xbb), "check OPC_NAME(0xbb)");
        assertEquals( "OPC_SW_STATE", LnConstants.OPC_NAME(0xbc), "check OPC_NAME(0xbc)");
        assertEquals( "OPC_SW_ACK", LnConstants.OPC_NAME(0xbd), "check OPC_NAME(0xbd)");
        assertEquals( "OPC_LOCO_ADR", LnConstants.OPC_NAME(0xbf), "check OPC_NAME(0xbf)");
        assertEquals( "OPC_MULTI_SENSE", LnConstants.OPC_NAME(0xd0), "check OPC_NAME(0xd0)");
        assertEquals( "OPC_PANEL_RESPONSE", LnConstants.OPC_NAME(0xd7), "check OPC_NAME(0xd7)");
        assertEquals( "OPC_PANEL_QUERY", LnConstants.OPC_NAME(0xdf), "check OPC_NAME(0xdf)");
        assertEquals( "OPC_MULTI_SENSE_LONG", LnConstants.OPC_NAME(0xe0), "check OPC_NAME(0xe0)");
        assertEquals( "OPC_LISSY_UPDATE", LnConstants.OPC_NAME(0xe4), "check OPC_NAME(0xe4)");
        assertEquals( "OPC_PEER_XFER", LnConstants.OPC_NAME(0xe5), "check OPC_NAME(0xe5)");
        assertEquals( "OPC_ALM_READ", LnConstants.OPC_NAME(0xe6), "check OPC_NAME(0xe6)");
        assertEquals( "OPC_SL_RD_DATA", LnConstants.OPC_NAME(0xe7), "check OPC_NAME(0xe7)");
        assertEquals( "OPC_IMM_PACKET", LnConstants.OPC_NAME(0xed), "check OPC_NAME(0xed)");
        assertEquals( "OPC_IMM_PACKET_2", LnConstants.OPC_NAME(0xee), "check OPC_NAME(0xee)");
        assertEquals( "OPC_WR_SL_DATA", LnConstants.OPC_NAME(0xef), "check OPC_NAME(0xef)");
        assertEquals( "<unknown>", LnConstants.OPC_NAME(0x80), "check OPC_0x80");
    }

    @Test
    public void testDEC_MODE() { // encode decoder type as a string

        assertEquals( "28", LnConstants.DEC_MODE(0x00), "check decoder mode (0x00)");
        assertEquals( "28 (Motorola)", LnConstants.DEC_MODE(0x01), "check decoder mode (0x01)");
        assertEquals( "14", LnConstants.DEC_MODE(0x02), "check decoder mode (0x02)");
        assertEquals( "128", LnConstants.DEC_MODE(0x03), "check decoder mode (0x03)");
        assertEquals( "28 (Allow Adv. consisting)", LnConstants.DEC_MODE(0x04), "check decoder mode (0x04)");
        assertEquals( "28", LnConstants.DEC_MODE(0x05), "check decoder mode (0x05)");
        assertEquals( "28", LnConstants.DEC_MODE(0x06), "check decoder mode (0x06)");
        assertEquals( "128 (Allow Adv. consisting)", LnConstants.DEC_MODE(0x07), "check decoder mode (0x07)");
        assertEquals( "28", LnConstants.DEC_MODE(0xf0), "check decoder mode (0xf0)");
        assertEquals( "28 (Motorola)", LnConstants.DEC_MODE(0x09), "check decoder mode (0x09)");
        assertEquals( "14", LnConstants.DEC_MODE(0x12), "check decoder mode (0x12)");
        assertEquals( "128", LnConstants.DEC_MODE(0x23), "check decoder mode (0x23)");
        assertEquals( "28 (Allow Adv. consisting)", LnConstants.DEC_MODE(0x44), "check decoder mode (0x44)");
        assertEquals( "28", LnConstants.DEC_MODE(0x85), "check decoder mode (0x85)");
        assertEquals( "28", LnConstants.DEC_MODE(0xf6), "check decoder mode (0xF6)");
        assertEquals( "128 (Allow Adv. consisting)", LnConstants.DEC_MODE(0x57), "check decoder mode (0x57)");
    }

    @Test
    public void testCONSIST_STAT() {
        assertEquals( "Not Consisted", LnConstants.CONSIST_STAT(0x00), "check consist stat (0x00)");
        assertEquals( "Consist TOP", LnConstants.CONSIST_STAT(0x08), "check consist stat (0x08)");
        assertEquals( "Sub Consist", LnConstants.CONSIST_STAT(0x40), "check consist stat (0x40)");
        assertEquals( "Mid Consist", LnConstants.CONSIST_STAT(0x48), "check consist stat (0x48)");
    }

    @Test
    public void testLOCO_STAT() {
        assertEquals( "Free", LnConstants.LOCO_STAT(0x00), "check consist stat (0x00)");
        assertEquals( "Idle", LnConstants.LOCO_STAT(0x20), "check consist stat (0x20)");
        assertEquals( "In-Use", LnConstants.LOCO_STAT(0x30), "check consist stat (0x30)");
        assertEquals( "Common", LnConstants.LOCO_STAT(0x10), "check consist stat (0x10)");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnConstantsTest.class);

}
