package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link RosterCvs}, and for {@link SimulatedLoco} built from a
 * roster entry, on a roster file written for the test.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class RosterCvsTest {

    private RosterEntry entry(String fileName, String values) throws IOException {
        File dir = new File(Roster.getDefault().getRosterFilesLocation());
        Assertions.assertTrue(dir.isDirectory() || dir.mkdirs(), "roster directory");
        if (values != null) {
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<locomotive-config>\n"
                    + "  <locomotive id=\"Sim\" fileName=\"" + fileName + "\">\n    <values>\n" + values
                    + "    </values>\n  </locomotive>\n</locomotive-config>\n";
            Files.write(new File(dir, fileName).toPath(), xml.getBytes(StandardCharsets.UTF_8));
        }
        RosterEntry e = new RosterEntry(fileName);
        e.setId("Sim");
        e.setDccAddress("3");
        return e;
    }

    @Test
    public void testReadsCvValues() throws IOException {
        RosterEntry e = entry("Sim.xml", "      <CVvalue name=\"3\" value=\"20\"/>\n"
                + "      <CVvalue name=\"4\" value=\"12\"/>\n"
                + "      <CVvalue name=\"19\" value=\"5\"/>\n"
                + "      <CVvalue name=\"23\" value=\"10\"/>\n"
                + "      <CVvalue name=\"24\" value=\"133\"/>\n"
                + "      <CVvalue name=\"29\" value=\"not a number\"/>\n");
        Map<String, Integer> cvs = RosterCvs.read(e);
        assertEquals(20, cvs.get("3"));
        assertEquals(133, cvs.get("24"));
        assertFalse(cvs.containsKey("29"), "a value that is not a number is left out");

        SimulatedLoco loco = SimulatedLoco.builder(e).length(100).build();
        assertEquals("Sim", loco.getRosterId());
        assertEquals(3, loco.getAddress().getNumber());
        assertEquals(20, loco.getCv3());
        assertEquals(12, loco.getCv4());
        assertEquals(5, loco.getCv19());
        assertEquals(10, loco.getCv23());
        assertEquals(133, loco.getCv24());
        assertTrue(loco.isInConsist());
        assertEquals(5, loco.getRespondingAddress().getNumber(), "obeys its consist address");
        assertFalse(loco.hasMeasuredSpeeds(), "no speed profile");
        // in a consist: CV4 12 minus CV24's 5 (bit 7 set)
        assertEquals(7, DecoderModel.of(loco, false).getDecelerationCv());
    }

    @Test
    public void testNoFile() throws IOException {
        assertTrue(RosterCvs.read(entry("Missing.xml", null)).isEmpty());
        SimulatedLoco loco = SimulatedLoco.builder(entry("Missing.xml", null)).build();
        assertEquals(0, loco.getCv3());
        assertFalse(loco.isInConsist());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
