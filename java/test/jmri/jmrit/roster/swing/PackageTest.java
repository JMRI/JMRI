package jmri.jmrit.roster.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        RosterTableModelTest.class,
        jmri.jmrit.roster.swing.attributetable.PackageTest.class,
        jmri.jmrit.roster.swing.rostergroup.PackageTest.class,
        jmri.jmrit.roster.swing.speedprofile.PackageTest.class,
        jmri.jmrit.roster.swing.rostertree.PackageTest.class,
        RosterFrameTest.class,
        GlobalRosterEntryComboBoxTest.class,
        RosterEntryComboBoxTest.class,
        RosterEntryListCellRendererTest.class,
        RosterEntrySelectorPanelTest.class,
        RosterFrameActionTest.class,
        RosterFrameStartupActionFactoryTest.class,
        RosterGroupComboBoxTest.class,
        RosterGroupsPanelTest.class,
        RosterTableTest.class,
        CopyRosterGroupActionTest.class,
        CreateRosterGroupActionTest.class,
        DeleteRosterGroupActionTest.class,
        RenameRosterGroupActionTest.class,
        RemoveRosterEntryToGroupActionTest.class,
        RosterEntryToGroupActionTest.class,
        RosterMenuTest.class,
})

/**
 * Tests for the jmrit.roster.swing package
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2012
 */
public class PackageTest  {
}
