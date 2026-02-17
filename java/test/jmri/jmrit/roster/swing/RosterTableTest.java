package jmri.jmrit.roster.swing;

import javax.swing.JFrame;
import javax.swing.table.DefaultTableCellRenderer;

import jmri.InstanceManager;
import jmri.jmrit.roster.*;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.gui.GuiLafPreferencesManager;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RosterTableTest {

    @Test
    public void testCTor() {
        RosterTable t = new RosterTable();
        assertNotNull(t, "exists");
        t.dispose();
    }

    @Test
    @DisabledIfHeadless
    public void testDisplaysOk() {
        RosterTable t = new RosterTable();
        JFrame frame = new JFrame("RosterTableTest testDisplaysOk");
        frame.add(t);

        ThreadingUtil.runOnGUI(() -> {
            frame.pack();
            frame.setVisible(true);
        } );

        JFrameOperator jfo = new JFrameOperator(frame.getTitle());
        assertNotNull(jfo);

        JTableOperator to = new JTableOperator(jfo);
        assertNotNull(to);
        assertFalse(t.getEditable());

        to.getHeaderOperator().clickForPopup();
        JPopupMenuOperator jpo = new JPopupMenuOperator();
        assertNotNull(jpo);
        new JMenuItemOperator(jpo,"Protocol").doClick();

        // check cell values for column Key A
        to.waitCell("value 1", 0, 12); // key a column value 1
        to.waitCell("value 22", 1, 12); // key a column value 1
        to.waitCell("", 2, 11); // key a column value 1

        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();

    }
    
    @Test
    @DisabledIfHeadless
    public void testDateEditable(){

        Roster.getDefault().getEntry(0).deleteAttribute("KeyA");
        Roster.getDefault().getEntry(1).deleteAttribute("KeyA");
        Roster.getDefault().getEntry(0).putAttribute(RosterEntry.ATTRIBUTE_LAST_OPERATED, "2022-10-31T06:22:00.000+00:00");

        RosterTable t = new RosterTable(true); // editable true
        JFrame frame = new JFrame("RosterTableTest testDateEditable");
        frame.add(t);

        ThreadingUtil.runOnGUI(() -> {
            frame.pack();
            frame.setVisible(true);
        } );

        JFrameOperator jfo = new JFrameOperator(frame.getTitle());
        assertNotNull(jfo);

        JTableOperator to = new JTableOperator(jfo);
        assertNotNull(to);
        assertTrue(t.getEditable());

        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();

    }

    @Test
    @DisabledIfHeadless
    public void testOperatingDurationEditable(){

        Roster.getDefault().getEntry(0).deleteAttribute("KeyA");
        Roster.getDefault().getEntry(1).deleteAttribute("KeyA");
        Roster.getDefault().getEntry(0).putAttribute(RosterEntry.ATTRIBUTE_OPERATING_DURATION, "1");

        RosterTable t = new RosterTable(true);
        JFrame frame = new JFrame("RosterTableTest testDurationEditable");
        frame.add(t);

        ThreadingUtil.runOnGUI(() -> {
            frame.pack();
            frame.setVisible(true);
        } );

        JFrameOperator jfo = new JFrameOperator(frame.getTitle());
        assertNotNull(jfo);

        JTableOperator to = new JTableOperator(jfo);
        assertNotNull(to);

        int durationCol = 11;

        var c = (DefaultTableCellRenderer) to.prepareRenderer(to.getCellRenderer(0, durationCol), 0, durationCol);
        assertEquals(Bundle.getMessage("DurationViewTip"), c.getToolTipText());
        assertEquals("00:00:01", c.getText());

        to.setValueAt("123456", 0, durationCol);
        c = (DefaultTableCellRenderer) to.prepareRenderer(to.getCellRenderer(0, durationCol), 0, durationCol);
        assertEquals("1 10:17:36", c.getText());

        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initGuiLafPreferencesManager();
        InstanceManager.getDefault(GuiLafPreferencesManager.class).setFontSize(14);
        JUnitUtil.initRosterConfigManager();

        RosterEntry r = RosterEntryImplementations.id1();
        r.putAttribute("KeyA", "value 1");
        Roster.getDefault().addEntry(r);

        r = RosterEntryImplementations.id2();
        r.putAttribute("KeyA", "value 22");
        Roster.getDefault().addEntry(r);

        r = RosterEntryImplementations.id3();
        Roster.getDefault().addEntry(r);

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RosterTableTest.class);
}
