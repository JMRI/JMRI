package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Ms2LocoRecordTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    void decodesProtocolRangesWithoutAccessoryStyleOneBasedAdjustment() {
        String[][] cases = {
            {"0xc04e", "DCC", "78"},
            {"0x18", "MM2", "24"},
            {"0x200c", "MM2", "12"},
            {"0x4001", "MFX", "1"},
            {"0x7fff", "MFX", "16383"},
            {"0xc000", "DCC", "0"},
            {"65535", "DCC", "16383"}
        };
        for (String[] values : cases) {
            Ms2LocoRecord record = new Ms2LocoRecord("A", ".uid=" + values[0], "");
            assertEquals(values[1], record.getProtocol(), values[0]);
            assertEquals(values[2], record.getProtocolId(), values[0]);
        }
    }

    @Test
    void preservesUnsupportedAndMalformedIdsWithoutGuessingAProtocol() {
        for (String uid : new String[]{"", "0x3800", "0x8001", "0x10000", "65536", "-1", "invalid"}) {
            Ms2LocoRecord record = new Ms2LocoRecord("A", ".uid=" + uid, "");
            assertEquals(uid, record.getField(".uid"));
            assertEquals("", record.getProtocol());
            assertEquals("", record.getProtocolId());
        }
    }

    @Test
    void detailedUidTakesPrecedenceOverListEntry() {
        Ms2LocoRecord record = new Ms2LocoRecord("A", ".uid=0x18\n.mfxuid=0xffffffff",
                ".uid=0xc04e\n.typ=dcc\n.mfxuid=0x1");
        assertEquals("0xc04e", record.getField(".uid"));
        assertEquals("DCC", record.getProtocol());
        assertEquals("78", record.getProtocolId());
        assertEquals("0x1", record.getField(".mfxuid"));
    }
}
