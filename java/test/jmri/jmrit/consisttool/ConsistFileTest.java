package jmri.jmrit.consisttool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import jmri.Consist;
import jmri.ConsistManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.roster.Roster;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test simple functioning of ConsistFile
 *
 * @author Paul Bender Copyright (C) 2015
 */
public class ConsistFileTest {

    @Test
    public void testCtor() {
        ConsistFile file = new ConsistFile();
        assertNotNull(file, "exists");
    }

    @Test
    public void testDefaultFileLocation() {
        String fileName = Roster.getDefault().getRosterLocation()
                + "roster" + File.separator
                + "consist" + File.separator;
        assertEquals(fileName, ConsistFile.getFileLocation(),
            "Consist File Location");
    }

    @Test
    public void testReadFile() throws IOException, org.jdom2.JDOMException {
        ConsistFile file = new ConsistFile();
        InstanceManager.getDefault(ConsistManager.class);
        file.readFile("java/test/jmri/jmrit/consisttool/consist.xml");
    }

    @Test
    public void testWriteFile(@TempDir File folder) throws IOException {
        ConsistFile file = new ConsistFile();
        ConsistManager cm = InstanceManager.getDefault(ConsistManager.class);
        DccLocoAddress addr = new DccLocoAddress(5, false);
        Consist c = cm.getConsist(addr);
        c.add(new DccLocoAddress(10, false), true);
        c.add(new DccLocoAddress(1000, true), false);
        String fileName = folder.getPath() + File.separator + "consist.xml";
        file.writeFile(cm.getConsistList(), fileName);
        assertTrue(new File(fileName).exists(), "file created");
    }

    @Test
    public void testWriteDefaultFile() throws IOException {
        ConsistFile file = new ConsistFile();
        ConsistManager cm = InstanceManager.getDefault(ConsistManager.class);
        DccLocoAddress addr = new DccLocoAddress(5, false);
        Consist c = cm.getConsist(addr);
        c.add(new DccLocoAddress(10, false), true);
        c.add(new DccLocoAddress(1000, true), false);
        file.writeFile(cm.getConsistList());
        String fileName = ConsistFile.defaultConsistFilename();
        assertTrue(new File(fileName).exists(), "file created");
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        JUnitUtil.setUp();
        jmri.profile.Profile profile = new jmri.profile.NullProfile(folder);
        JUnitUtil.resetProfileManager(profile);
        JUnitUtil.initRosterConfigManager();
        Roster.getDefault().setRosterLocation("");
        InstanceManager.setDefault(ConsistPreferencesManager.class, new ConsistPreferencesManager());
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
