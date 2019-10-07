package jmri.jmrix.loconet;

import static jmri.jmrix.loconet.LnConstants.DEC_MODE_128;
import static jmri.jmrix.loconet.LnConstants.DEC_MODE_128A;
import static jmri.jmrix.loconet.LnConstants.DEC_MODE_14;
import static jmri.jmrix.loconet.LnConstants.DEC_MODE_28A;
import static jmri.jmrix.loconet.LnConstants.DEC_MODE_28TRI;
import static jmri.jmrix.loconet.LnConstants.DEC_MODE_MASK;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LnConstantsTest {

    @Test
    public void testCTor() {
        LnConstants t = new LnConstants();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testOpCodeConstants() {
        Assert.assertEquals("check 0x81", 0x81, LnConstants.OPC_GPBUSY);
        Assert.assertEquals("check 0x82", 0x82, LnConstants.OPC_GPOFF);
        Assert.assertEquals("check 0x83", 0x83, LnConstants.OPC_GPON);
        Assert.assertEquals("check 0x85", 0x85, LnConstants.OPC_IDLE);
        Assert.assertEquals("check 0x8a", 0x8a, LnConstants.OPC_RE_LOCORESET_BUTTON);
        Assert.assertEquals("check 0xa0", 0xa0, LnConstants.OPC_LOCO_SPD);
        Assert.assertEquals("check 0xa1", 0xa1, LnConstants.OPC_LOCO_DIRF);
        Assert.assertEquals("check 0xa2", 0xa2, LnConstants.OPC_LOCO_SND);
        Assert.assertEquals("check 0xb0", 0xb0, LnConstants.OPC_SW_REQ);
        Assert.assertEquals("check 0xb1", 0xb1, LnConstants.OPC_SW_REP);
        Assert.assertEquals("check 0xb2", 0xb2, LnConstants.OPC_INPUT_REP);
        Assert.assertEquals("check 0xb3", 0xb3, LnConstants.OPC_UNKNOWN);
        Assert.assertEquals("check 0xb4", 0xb4, LnConstants.OPC_LONG_ACK);
        Assert.assertEquals("check 0xb5", 0xb5, LnConstants.OPC_SLOT_STAT1);
        Assert.assertEquals("check 0xb6", 0xb6, LnConstants.OPC_CONSIST_FUNC);
        Assert.assertEquals("check 0xb8", 0xb8, LnConstants.OPC_UNLINK_SLOTS);
        Assert.assertEquals("check 0xb9", 0xb9, LnConstants.OPC_LINK_SLOTS);
        Assert.assertEquals("check 0xba", 0xba, LnConstants.OPC_MOVE_SLOTS);
        Assert.assertEquals("check 0xbb", 0xbb, LnConstants.OPC_RQ_SL_DATA);
        Assert.assertEquals("check 0xbc", 0xbc, LnConstants.OPC_SW_STATE);
        Assert.assertEquals("check 0xbd", 0xbd, LnConstants.OPC_SW_ACK);
        Assert.assertEquals("check 0xbf", 0xbf, LnConstants.OPC_LOCO_ADR);
        Assert.assertEquals("check 0xd0", 0xd0, LnConstants.OPC_MULTI_SENSE);
        Assert.assertEquals("check 0xd7", 0xd7, LnConstants.OPC_PANEL_RESPONSE);
        Assert.assertEquals("check 0xdf", 0xdf, LnConstants.OPC_PANEL_QUERY);
        Assert.assertEquals("check 0xe4", 0xe4, LnConstants.OPC_LISSY_UPDATE);
        Assert.assertEquals("check 0xe5", 0xe5, LnConstants.OPC_PEER_XFER);
        Assert.assertEquals("check 0xe6", 0xe6, LnConstants.OPC_ALM_READ);
        Assert.assertEquals("check 0xe7", 0xe7, LnConstants.OPC_SL_RD_DATA);
        Assert.assertEquals("check 0xed", 0xed, LnConstants.OPC_IMM_PACKET);
        Assert.assertEquals("check 0xee", 0xee, LnConstants.OPC_IMM_PACKET_2);
        Assert.assertEquals("check 0xef", 0xef, LnConstants.OPC_WR_SL_DATA);
        Assert.assertEquals("check 0xee", 0xee, LnConstants.OPC_ALM_WRITE);
    }

    @Test
    public void testOPC_NAME() {
        Assert.assertEquals("check OPC_NAME(0x81)", "OPC_GPBUSY", LnConstants.OPC_NAME(0x81) );
        Assert.assertEquals("check OPC_NAME(0x82)", "OPC_GPOFF", LnConstants.OPC_NAME(0x82));
        Assert.assertEquals("check OPC_NAME(0x83)", "OPC_GPON", LnConstants.OPC_NAME(0x83));
        Assert.assertEquals("check OPC_NAME(0x85)", "OPC_IDLE", LnConstants.OPC_NAME(0x85));
        Assert.assertEquals("check OPC_NAME(0x8a)", "OPC_RE_LOCORESET_BUTTON", LnConstants.OPC_NAME(0x8a));
        Assert.assertEquals("check OPC_NAME(0xa0)", "OPC_LOCO_SPD", LnConstants.OPC_NAME(0xa0));
        Assert.assertEquals("check OPC_NAME(0xa1)", "OPC_LOCO_DIRF", LnConstants.OPC_NAME(0xa1));
        Assert.assertEquals("check OPC_NAME(0xa2)", "OPC_LOCO_SND", LnConstants.OPC_NAME(0xa2));
        Assert.assertEquals("check OPC_NAME(0xb0)", "OPC_SW_REQ", LnConstants.OPC_NAME(0xb0));
        Assert.assertEquals("check OPC_NAME(0xb1)", "OPC_SW_REP", LnConstants.OPC_NAME(0xb1));
        Assert.assertEquals("check OPC_NAME(0xb2)", "OPC_INPUT_REP", LnConstants.OPC_NAME(0xb2));
        Assert.assertEquals("check OPC_NAME(0xb3)", "OPC_UNKNOWN", LnConstants.OPC_NAME(0xb3));
        Assert.assertEquals("check OPC_NAME(0xb4)", "OPC_LONG_ACK", LnConstants.OPC_NAME(0xb4));
        Assert.assertEquals("check OPC_NAME(0xb5)", "OPC_SLOT_STAT1", LnConstants.OPC_NAME(0xb5));
        Assert.assertEquals("check OPC_NAME(0xb6)", "OPC_CONSIST_FUNC", LnConstants.OPC_NAME(0xb6));
        Assert.assertEquals("check OPC_NAME(0xb8)", "OPC_UNLINK_SLOTS", LnConstants.OPC_NAME(0xb8));
        Assert.assertEquals("check OPC_NAME(0xb9)", "OPC_LINK_SLOTS", LnConstants.OPC_NAME(0xb9));
        Assert.assertEquals("check OPC_NAME(0xba)", "OPC_MOVE_SLOTS", LnConstants.OPC_NAME(0xba));
        Assert.assertEquals("check OPC_NAME(0xbb)", "OPC_RQ_SL_DATA", LnConstants.OPC_NAME(0xbb));
        Assert.assertEquals("check OPC_NAME(0xbc)", "OPC_SW_STATE", LnConstants.OPC_NAME(0xbc));
        Assert.assertEquals("check OPC_NAME(0xbd)", "OPC_SW_ACK", LnConstants.OPC_NAME(0xbd));
        Assert.assertEquals("check OPC_NAME(0xbf)", "OPC_LOCO_ADR", LnConstants.OPC_NAME(0xbf));
        Assert.assertEquals("check OPC_NAME(0xd0)", "OPC_MULTI_SENSE", LnConstants.OPC_NAME(0xd0));
        Assert.assertEquals("check OPC_NAME(0xd7)", "OPC_PANEL_RESPONSE", LnConstants.OPC_NAME(0xd7));
        Assert.assertEquals("check OPC_NAME(0xdf)", "OPC_PANEL_QUERY", LnConstants.OPC_NAME(0xdf));
        Assert.assertEquals("check OPC_NAME(0xe4)", "OPC_LISSY_UPDATE", LnConstants.OPC_NAME(0xe4));
        Assert.assertEquals("check OPC_NAME(0xe5)", "OPC_PEER_XFER", LnConstants.OPC_NAME(0xe5));
        Assert.assertEquals("check OPC_NAME(0xe6)", "OPC_ALM_READ", LnConstants.OPC_NAME(0xe6));
        Assert.assertEquals("check OPC_NAME(0xe7)", "OPC_SL_RD_DATA", LnConstants.OPC_NAME(0xe7));
        Assert.assertEquals("check OPC_NAME(0xed)", "OPC_IMM_PACKET", LnConstants.OPC_NAME(0xed));
        Assert.assertEquals("check OPC_NAME(0xee)", "OPC_IMM_PACKET_2", LnConstants.OPC_NAME(0xee));
        Assert.assertEquals("check OPC_NAME(0xef)", "OPC_WR_SL_DATA", LnConstants.OPC_NAME(0xef));
        Assert.assertEquals("check OPC_0x80", "<unknown>", LnConstants.OPC_NAME(0x80));
    }

    @Test
    public void testDEC_MODE() { // encode decoder type as a string

        Assert.assertEquals("check decoder mode (0x00)", "28", LnConstants.DEC_MODE(0x00));
        Assert.assertEquals("check decoder mode (0x01)", "28 (Motorola)", LnConstants.DEC_MODE(0x01));
        Assert.assertEquals("check decoder mode (0x02)", "14", LnConstants.DEC_MODE(0x02));
        Assert.assertEquals("check decoder mode (0x03)", "128", LnConstants.DEC_MODE(0x03));
        Assert.assertEquals("check decoder mode (0x04)", "28 (Allow Adv. consisting)", LnConstants.DEC_MODE(0x04));
        Assert.assertEquals("check decoder mode (0x05)", "28", LnConstants.DEC_MODE(0x05));
        Assert.assertEquals("check decoder mode (0x06)", "28", LnConstants.DEC_MODE(0x06));
        Assert.assertEquals("check decoder mode (0x07)", "128 (Allow Adv. consisting)", LnConstants.DEC_MODE(0x07));
        Assert.assertEquals("check decoder mode (0xf0)", "28", LnConstants.DEC_MODE(0xf0));
        Assert.assertEquals("check decoder mode (0x09)", "28 (Motorola)", LnConstants.DEC_MODE(0x09));
        Assert.assertEquals("check decoder mode (0x12)", "14", LnConstants.DEC_MODE(0x12));
        Assert.assertEquals("check decoder mode (0x23)", "128", LnConstants.DEC_MODE(0x23));
        Assert.assertEquals("check decoder mode (0x44)", "28 (Allow Adv. consisting)", LnConstants.DEC_MODE(0x44));
        Assert.assertEquals("check decoder mode (0x85)", "28", LnConstants.DEC_MODE(0x85));
        Assert.assertEquals("check decoder mode (0xF6)", "28", LnConstants.DEC_MODE(0xf6));
        Assert.assertEquals("check decoder mode (0x57)", "128 (Allow Adv. consisting)", LnConstants.DEC_MODE(0x57));
    }

    @Test
    public void testCONSIST_STAT() {
        Assert.assertEquals("check consist stat (0x00)", "Not Consisted", LnConstants.CONSIST_STAT(0x00));
        Assert.assertEquals("check consist stat (0x08)", "Consist TOP", LnConstants.CONSIST_STAT(0x08));
        Assert.assertEquals("check consist stat (0x40)", "Sub Consist", LnConstants.CONSIST_STAT(0x40));
        Assert.assertEquals("check consist stat (0x48)", "Mid Consist", LnConstants.CONSIST_STAT(0x48));
    }

    @Test
    public void testLOCO_STAT() {
        Assert.assertEquals("check consist stat (0x00)", "Free", LnConstants.LOCO_STAT(0x00));
        Assert.assertEquals("check consist stat (0x20)", "Idle", LnConstants.LOCO_STAT(0x20));
        Assert.assertEquals("check consist stat (0x30)", "In-Use", LnConstants.LOCO_STAT(0x30));
        Assert.assertEquals("check consist stat (0x10)", "Common", LnConstants.LOCO_STAT(0x10));
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnConstantsTest.class);

}
