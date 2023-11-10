package jmri.jmrix.ecos.utilities;

import jmri.jmrix.ecos.EcosInterfaceScaffold;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.jmrix.ecos.EcosTrafficController;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AddRosterEntryToEcosTest {

    @Test
    public void testCTor() {
        EcosTrafficController tc = new EcosInterfaceScaffold();
        EcosSystemConnectionMemo memo = new EcosSystemConnectionMemo(tc);
        AddRosterEntryToEcos t = new AddRosterEntryToEcos("Add Roster Entry Test",memo);
        Assertions.assertNotNull(t, "exists");
        tc.terminateThreads();
        memo.dispose();
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testAddRosterEntryToEcosActionPerformed(){
        
        EcosTrafficController tc = new EcosInterfaceScaffold();
        EcosSystemConnectionMemo memo = new EcosSystemConnectionMemo(tc);
        AddRosterEntryToEcos t = new AddRosterEntryToEcos("Add Roster Entry Test",memo);
        Assertions.assertNotNull(t);
        Thread tr = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("AddToEcosTitle"), Bundle.getMessage("ButtonCancel"));
        t.actionPerformed(null);
        JUnitUtil.waitFor(() -> !tr.isAlive(), "AddToEcos dialog found and clicked Cancel");
        tc.terminateThreads();
        memo.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
