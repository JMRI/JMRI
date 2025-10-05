package jmri.jmrit.roster.swing;

import jmri.InstanceManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterEntryImplementations;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.gui.GuiLafPreferencesManager;
import jmri.util.junit.annotations.DisabledIfHeadless;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.ArrayList;
import javax.swing.JFrame;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RosterEntryComboBoxTest {

    @Test
    public void testCTor() {
        RosterEntryComboBox t = new RosterEntryComboBox();
        Assert.assertNotNull("exists",t);
    }

    @Test
    @DisabledIfHeadless
    public void testComboLoads() {
        RosterEntryComboBox rebx = new RosterEntryComboBox();
        JFrame frame = new JFrame("Roster Combo Entry");
        frame.add(rebx);

        ThreadingUtil.runOnGUI(() -> {
            frame.pack();
            frame.setVisible(true);
        } );

        assertEquals(4,rebx.getItemCount()," Should be 3 entries plus 1 for Select Loco");

        ArrayList<RosterEntry> myExcludes = new ArrayList<RosterEntry>();
        myExcludes.add(Roster.getDefault().getEntryForId("id 2"));
        rebx.setExcludeItems(myExcludes);
        assertEquals(3,rebx.getItemCount()," Should be 2 entries plus 1 for Select Loco");

        boolean inList = false;
        for (int ix=0;ix<rebx.getItemCount();ix++) {
            if (rebx.getItemAt(ix) == Roster.getDefault().getEntryForId("id 2")) {
                inList = true;
                break;
            }
        }
        assertFalse(inList,  "Should Not be there");

        myExcludes = new ArrayList<RosterEntry>();
        rebx.setExcludeItems(myExcludes);
        assertEquals(4,rebx.getItemCount()," Should be Back at 3 entries plus 1 for Select Loco");

        myExcludes.add(RosterEntryImplementations.id4());
        rebx.setExcludeItems(myExcludes);
        assertEquals(4,rebx.getItemCount()," Id4 not in roster still 3 entries plus 1 for Select Loco");

        JUnitUtil.dispose(frame);
     }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initGuiLafPreferencesManager();
        InstanceManager.getDefault(GuiLafPreferencesManager.class).setFontSize(14);
        JUnitUtil.initRosterConfigManager();

        RosterEntry r = RosterEntryImplementations.id1();
        Roster.getDefault().addEntry(r);

        r = RosterEntryImplementations.id2();
        Roster.getDefault().addEntry(r);

        r = RosterEntryImplementations.id3();
        Roster.getDefault().addEntry(r);

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RosterEntryComboBoxTest.class);

}
