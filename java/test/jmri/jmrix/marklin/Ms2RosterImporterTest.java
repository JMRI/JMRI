package jmri.jmrix.marklin;

import java.io.File;
import java.io.IOException;
import jmri.LocoAddress;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class Ms2RosterImporterTest {
    private static final String MS2 = "0x4D533272";

    private Roster roster;
    private Ms2RosterImporter importer;

    @BeforeEach
    void setUp(@TempDir File folder) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        JUnitUtil.initRosterConfigManager();
        roster = Roster.getDefault();
        importer = new Ms2RosterImporter(roster);
    }

    @AfterEach
    void tearDown() {
        JUnitUtil.tearDown();
    }

    private static Ms2LocoRecord loco(String name, String uid, String typ) {
        return new Ms2LocoRecord(name, ".name=" + name + "\n.uid=" + uid,
                "lok\n.uid=" + uid + "\n.name=" + name + "\n.typ=" + typ + "\n.mfxuid=0xffffffff");
    }

    private RosterEntry existing(String id, String address, LocoAddress.Protocol protocol) {
        RosterEntry entry = new RosterEntry();
        entry.setId(id);
        entry.setDccAddress(address);
        entry.setProtocol(protocol);
        entry.setRoadName("Sodor");
        entry.ensureFilenameExists();
        roster.addEntry(entry);
        entry.writeFile(null, null);
        return entry;
    }

    @Test
    void createsMotorolaEntryWithOriginAttributes() throws Exception {
        Ms2RosterImporter.Result result = importer.importLoco(loco("THOMAS", "0x4e", "mm2_prg"), MS2, "M");

        assertEquals(Ms2RosterImporter.Outcome.CREATED, result.getOutcome());
        RosterEntry entry = roster.getEntryForId("THOMAS");
        assertSame(entry, result.getEntry());
        assertEquals("78", entry.getDccAddress());
        assertEquals(LocoAddress.Protocol.MOTOROLA, entry.getProtocol());
        assertEquals("M", entry.getAttribute(Ms2RosterImporter.ATTRIBUTE_CONNECTION));
        assertEquals(MS2, entry.getAttribute(Ms2RosterImporter.ATTRIBUTE_MS2_UID));
        assertEquals("THOMAS", entry.getAttribute(Ms2RosterImporter.ATTRIBUTE_MS2_NAME));
        assertTrue(new File(roster.getRosterFilesLocation(), entry.getFileName()).exists());
    }

    @Test
    void updatesEntryMatchedByMotorolaAddressKeepingIdAndData() throws Exception {
        RosterEntry entry = existing("Thomas the Tank Engine No. 1", "78", LocoAddress.Protocol.MOTOROLA);
        existing("DCC 78", "78", LocoAddress.Protocol.DCC_LONG);

        Ms2RosterImporter.Result result = importer.importLoco(loco("THOMAS", "0x4e", "mm2_prg"), MS2, "M");

        assertEquals(Ms2RosterImporter.Outcome.UPDATED, result.getOutcome());
        assertSame(entry, result.getEntry());
        assertEquals("Thomas the Tank Engine No. 1", entry.getId());
        assertEquals("Sodor", entry.getRoadName());
        assertEquals("THOMAS", entry.getAttribute(Ms2RosterImporter.ATTRIBUTE_MS2_NAME));
        assertEquals(MS2, entry.getAttribute(Ms2RosterImporter.ATTRIBUTE_MS2_UID));
        assertNull(roster.getEntryForId("THOMAS"));
        assertEquals(2, roster.numEntries());
    }

    @Test
    void reimportRefreshesRenamedMs2Name() throws Exception {
        importer.importLoco(loco("THOMAS", "0x4e", "mm2_prg"), MS2, "M");
        Ms2RosterImporter.Result result = importer.importLoco(loco("TANK 1", "0x4e", "mm2_prg"), MS2, "M");

        assertEquals(Ms2RosterImporter.Outcome.UPDATED, result.getOutcome());
        assertEquals("THOMAS", result.getEntry().getId());
        assertEquals("TANK 1", result.getEntry().getAttribute(Ms2RosterImporter.ATTRIBUTE_MS2_NAME));
        assertEquals(1, roster.numEntries());
    }

    @Test
    void dccMatchesAnyDccProtocolAndCreatesShortOrLong() throws Exception {
        RosterEntry shortEntry = existing("Short 3", "3", LocoAddress.Protocol.DCC_SHORT);
        assertSame(shortEntry, importer.importLoco(loco("D3", "0xc003", "dcc"), MS2, "M").getEntry());

        RosterEntry created = importer.importLoco(loco("DCC-178", "0xc0b2", "dcc"), MS2, "M").getEntry();
        assertEquals("178", created.getDccAddress());
        assertEquals(LocoAddress.Protocol.DCC_LONG, created.getProtocol());
    }

    @Test
    void ambiguousMatchesAreResolvedOnlyByOriginAttributes() throws Exception {
        existing("A", "24", LocoAddress.Protocol.MOTOROLA);
        RosterEntry b = existing("B", "24", LocoAddress.Protocol.MOTOROLA);
        Ms2LocoRecord record = loco("MM2-24", "0x18", "mm2_prg");

        Ms2RosterImporter.ImportException ex = assertThrows(Ms2RosterImporter.ImportException.class,
                () -> importer.importLoco(record, MS2, "M"));
        assertTrue(ex.getMessage().contains("A, B"), ex.getMessage());

        b.putAttribute(Ms2RosterImporter.ATTRIBUTE_CONNECTION, "M");
        b.putAttribute(Ms2RosterImporter.ATTRIBUTE_MS2_UID, MS2);
        assertSame(b, importer.importLoco(record, MS2, "M").getEntry());
    }

    @Test
    void mfxAndUnknownIdsAreNotImportable() {
        Ms2LocoRecord mfx = loco("BR 218", "0x4007", "mfx");
        Ms2LocoRecord unknown = loco("X", "invalid", "mm2_prg");
        assertFalse(Ms2RosterImporter.isImportable(mfx));
        assertFalse(Ms2RosterImporter.isImportable(unknown));
        assertTrue(Ms2RosterImporter.isImportable(loco("THOMAS", "0x4e", "mm2_prg")));
        assertThrows(Ms2RosterImporter.ImportException.class, () -> importer.importLoco(mfx, MS2, "M"));
        assertEquals(0, roster.numEntries());
    }
}
