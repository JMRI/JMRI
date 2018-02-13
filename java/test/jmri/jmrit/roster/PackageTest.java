package jmri.jmrit.roster;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        RosterEntryTest.class,
        RosterTest.class,
        jmri.jmrit.roster.configurexml.PackageTest.class,
        CopyRosterItemActionTest.class,
        RosterEntryPaneTest.class,
        FunctionLabelPaneTest.class,
        IdentifyLocoTest.class,
        jmri.jmrit.roster.swing.PackageTest.class,
        LocoFileTest.class,
        RecreateRosterActionTest.class,
        RosterConfigManagerTest.class,
        RosterConfigPaneTest.class,
        RosterIconFactoryTest.class,
        RosterMediaPaneTest.class,
        RosterRecorderTest.class,
        jmri.jmrit.roster.rostergroup.PackageTest.class,
        DeleteRosterItemActionTest.class,
        ExportRosterItemActionTest.class,
        FullBackupExportActionTest.class,
        FullBackupImportActionTest.class,
        ImportRosterItemActionTest.class,
        PrintRosterActionTest.class,
        PrintRosterEntryTest.class,
        UpdateDecoderDefinitionActionTest.class,
        RosterSpeedProfileTest.class,
})

/**
 * Tests for the jmrit.roster package
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2012
 */
public class PackageTest  {
}
