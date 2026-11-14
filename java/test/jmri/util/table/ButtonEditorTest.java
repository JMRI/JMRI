package jmri.util.table;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import jmri.util.JmriJFrame;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ButtonEditorTest {

    @Test
    public void testCTor() {
        ButtonEditor t = new ButtonEditor(new JButton("Test"));
        Assertions.assertNotNull( t, "exists");
    }

    private JmriJFrame opened = null;
    private JmriJFrame tableFrame = null;

    /*
     * A window with a table whose one cell is an "Edit" button; clicking it opens a second window, as the
     * Edit buttons of JMRI's tables do.
     */
    private JTable showTableWithEditButton(List<String> tableFocusLost) {
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            DefaultTableModel model = new DefaultTableModel(new Object[][]{{"Edit"}}, new Object[]{"Edit"}) {
                @Override
                public void setValueAt(Object value, int row, int col) {
                    // as the tables do: open the window after the click
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        opened = new JmriJFrame("ButtonEditorTest opened", false, false);
                        opened.setSize(300, 200);
                        opened.setLocation(400, 100);
                        opened.setVisible(true);
                    });
                }
            };
            JTable table = new JTable(model);
            table.getColumnModel().getColumn(0).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(0).setCellEditor(new ButtonEditor(new JButton()));
            table.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    // the button may already have left the table when the event is handled
                    java.awt.Window w = e.getOppositeComponent() == null ? null
                            : javax.swing.SwingUtilities.getWindowAncestor(e.getOppositeComponent());
                    if (e.getOppositeComponent() != null && !(e.getOppositeComponent() instanceof java.awt.Window)
                            && (w == null || w == tableFrame)) {
                        tableFocusLost.add(e.getOppositeComponent().getClass().getSimpleName());
                    }
                }
            });
            tableFrame = new JmriJFrame("ButtonEditorTest table", false, false);
            tableFrame.add(new JScrollPane(table));
            tableFrame.setSize(300, 200);
            tableFrame.setLocation(50, 100);
            tableFrame.setVisible(true);
            return table;
        });
    }

    /**
     * Clicking a table's button does not take the focus from the table within its window: if it did, a
     * window the button opens could lose the focus again to the table's window.
     */
    @Test
    @DisabledIfHeadless
    public void testClickLeavesFocusWithTable() {
        List<String> tableFocusLost = new ArrayList<>(); // written and read on the GUI thread
        JTable table = showTableWithEditButton(tableFocusLost);
        JemmyUtil.waitFor(tableFrame);
        ThreadingUtil.runOnGUI(table::requestFocusInWindow);
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(table::isFocusOwner), "table has the focus");

        new JTableOperator(new JFrameOperator(tableFrame)).clickOnCell(0, 0);
        JUnitUtil.waitFor(() -> opened != null, "button opened a window");
        // the click has been handled: the button's edit is over and the window it opened is showing
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(() -> !table.isEditing() && opened.isShowing()),
            "click handled");
        JemmyUtil.waitFor(opened);
        Assertions.assertEquals(List.of(), ThreadingUtil.runOnGUIwithReturn(() -> new ArrayList<>(tableFocusLost)),
            "the focus did not move from the table to another component of its window");
    }

    /**
     * The window a table's button opens becomes the active window.
     */
    @Test
    @DisabledIfHeadless
    public void testOpenedWindowBecomesActive() {
        List<String> tableFocusLost = new ArrayList<>(); // written and read on the GUI thread
        showTableWithEditButton(tableFocusLost);
        JemmyUtil.waitFor(tableFrame);
        new JTableOperator(new JFrameOperator(tableFrame)).clickOnCell(0, 0);
        JUnitUtil.waitFor(() -> opened != null, "button opened a window");
        JemmyUtil.waitFor(opened);
    }

    @AfterEach
    public void closeWindows() {
        if (opened != null) {
            JUnitUtil.dispose(opened);
        }
        if (tableFrame != null) {
            JUnitUtil.dispose(tableFrame);
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(ButtonEditorTest.class);

}
