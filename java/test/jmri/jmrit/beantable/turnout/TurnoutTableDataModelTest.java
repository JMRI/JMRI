package jmri.jmrit.beantable.turnout;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;
import jmri.util.gui.GuiLafPreferencesManager;
import jmri.util.swing.XTableColumnModel;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young (C) 2021
 */
public class TurnoutTableDataModelTest extends jmri.jmrit.beantable.AbstractBeanTableDataModelBase<Turnout> {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }
    
    @Override
    public int getModelColumnCount(){
        return 20;
    }
    
    @Test
    public void testGetColumnNames() {
        assertEquals("Column5 - INVERTCOL",Bundle.getMessage("Inverted"), 
            t.getColumnName(TurnoutTableDataModel.INVERTCOL));
        assertEquals("Column6 - LOCKCOL",Bundle.getMessage("Locked"),
            t.getColumnName(TurnoutTableDataModel.LOCKCOL));
        assertEquals("Column7 - EDITCOL","",
            t.getColumnName(TurnoutTableDataModel.EDITCOL));
        assertEquals("Column8 - KNOWNCOL",Bundle.getMessage("Feedback"),
            t.getColumnName(TurnoutTableDataModel.KNOWNCOL));
        assertEquals("Column9 - MODECOL",Bundle.getMessage("ModeLabel"),
            t.getColumnName(TurnoutTableDataModel.MODECOL));
        assertEquals("Column10 - SENSOR1COL",Bundle.getMessage("BlockSensor") + " 1",
            t.getColumnName(TurnoutTableDataModel.SENSOR1COL));
        assertEquals("Column11 - SENSOR2COL",Bundle.getMessage("BlockSensor") + " 2",
            t.getColumnName(TurnoutTableDataModel.SENSOR2COL));
        assertEquals("Column12 - OPSONOFFCOL",Bundle.getMessage("TurnoutAutomationMenu"),
            t.getColumnName(TurnoutTableDataModel.OPSONOFFCOL));
        assertEquals("Column13 - OPSEDITCOL","",
            t.getColumnName(TurnoutTableDataModel.OPSEDITCOL));
        assertEquals("Column14 - LOCKOPRCOL",Bundle.getMessage("LockMode"),
            t.getColumnName(TurnoutTableDataModel.LOCKOPRCOL));
        assertEquals("Column15 - LOCKDECCOL",Bundle.getMessage("Decoder"),
            t.getColumnName(TurnoutTableDataModel.LOCKDECCOL));
        assertEquals("Column16 - STRAIGHTCOL",Bundle.getMessage("ClosedSpeed"),
            t.getColumnName(TurnoutTableDataModel.STRAIGHTCOL));
        assertEquals("Column17 - DIVERGCOL",Bundle.getMessage("ThrownSpeed"),
            t.getColumnName(TurnoutTableDataModel.DIVERGCOL));
        assertEquals("Column18 - FORGETCOL",Bundle.getMessage("StateForgetHeader"),
            t.getColumnName(TurnoutTableDataModel.FORGETCOL));
        assertEquals("Column19 - QUERYCOL",Bundle.getMessage("StateQueryHeader"),
            t.getColumnName(TurnoutTableDataModel.QUERYCOL));

    }

    /**
     * JTable which counts calls to the per-row setRowHeight. The graphic state
     * renderer must never call it: setRowHeight(int, int) unconditionally
     * schedules a repaint of the whole table, so calling it while rendering a
     * cell keeps the table repainting itself forever.
     */
    private static class CountingJTable extends JTable {

        int rowHeightCalls = 0;

        CountingJTable(TableModel m) {
            super(m);
        }

        @Override
        public void setRowHeight(int row, int rowHeight) {
            rowHeightCalls++;
            super.setRowHeight(row, rowHeight);
        }
    }

    private TurnoutTableDataModel createGraphicStateModel() {
        InstanceManager.getDefault(GuiLafPreferencesManager.class).setGraphicTableState(true);
        // the model reads the preference in its constructor, so create a fresh one
        return new TurnoutTableDataModel();
    }

    @Test
    public void testGraphicRendererDoesNotResizeRows() {
        TurnoutManager mgr = InstanceManager.getDefault(TurnoutManager.class);
        for (int i = 1; i <= 10; i++) {
            Turnout to = mgr.provideTurnout("IT" + i);
            to.setCommandedState((i % 2 == 0) ? Turnout.THROWN : Turnout.CLOSED);
        }
        TurnoutTableDataModel model = createGraphicStateModel();
        CountingJTable table = new CountingJTable(model);
        model.configValueColumn(table);
        TableCellRenderer r = table.getDefaultRenderer(JLabel.class);
        assertNotNull("graphic state renderer installed", r);
        table.rowHeightCalls = 0;
        for (int pass = 0; pass < 5; pass++) {
            for (int row = 0; row < table.getRowCount(); row++) {
                Component c = r.getTableCellRendererComponent(table,
                        table.getValueAt(row, TurnoutTableDataModel.VALUECOL),
                        false, false, row, TurnoutTableDataModel.VALUECOL);
                assertNotNull("renderer returns a component", c);
            }
        }
        assertEquals("rendering must not set row heights", 0, table.rowHeightCalls);
        model.dispose();
    }

    @Test
    public void testGraphicRendererReusesComponent() {
        TurnoutManager mgr = InstanceManager.getDefault(TurnoutManager.class);
        mgr.provideTurnout("IT1").setCommandedState(Turnout.CLOSED);
        mgr.provideTurnout("IT2").setCommandedState(Turnout.THROWN);
        TurnoutTableDataModel model = createGraphicStateModel();
        JTable table = new JTable(model);
        model.configValueColumn(table);
        TableCellRenderer r = table.getDefaultRenderer(JLabel.class);
        Component first = r.getTableCellRendererComponent(table,
                table.getValueAt(0, TurnoutTableDataModel.VALUECOL),
                false, false, 0, TurnoutTableDataModel.VALUECOL);
        Component second = r.getTableCellRendererComponent(table,
                table.getValueAt(1, TurnoutTableDataModel.VALUECOL),
                false, false, 1, TurnoutTableDataModel.VALUECOL);
        assertSame("renderer reuses a single component", first, second);
        model.dispose();
    }

    @Test
    public void testGraphicRowHeightSetByConfigureTable() {
        InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        TurnoutTableDataModel model = createGraphicStateModel();
        CountingJTable table = new CountingJTable(model);
        table.setColumnModel(new XTableColumnModel());
        table.createDefaultColumnsFromModel();
        model.configureTable(table);
        assertTrue("icons read so a row height is available",
                TurnoutTableDataModel.ImageIconRenderer.getIconRowHeight() > 0);
        assertTrue("row height fits the state icons",
                table.getRowHeight() >= TurnoutTableDataModel.ImageIconRenderer.getIconRowHeight());
        model.dispose();
    }

    @Test
    public void testGraphicRendererForegroundReset() {
        TurnoutTableDataModel model = createGraphicStateModel();
        JTable table = new JTable(model);
        model.configValueColumn(table);
        TableCellRenderer r = table.getDefaultRenderer(JLabel.class);
        JLabel label = (JLabel) r.getTableCellRendererComponent(table,
                InstanceManager.getDefault(TurnoutManager.class).getClosedText(),
                false, false, 0, TurnoutTableDataModel.VALUECOL);
        Color defaultForeground = label.getForeground();
        label = (JLabel) r.getTableCellRendererComponent(table,
                Bundle.getMessage("BeanStateInconsistent"), false, false, 0, TurnoutTableDataModel.VALUECOL);
        assertEquals("inconsistent state shown in red", Color.red, label.getForeground());
        assertEquals("tool tip follows the state", Bundle.getMessage("BeanStateInconsistent"), label.getToolTipText());
        label = (JLabel) r.getTableCellRendererComponent(table,
                InstanceManager.getDefault(TurnoutManager.class).getClosedText(),
                false, false, 0, TurnoutTableDataModel.VALUECOL);
        assertEquals("foreground reset after an inconsistent cell", defaultForeground, label.getForeground());
        model.dispose();
    }

    @Test
    public void testGraphicEditorClickTogglesTurnout() {
        Turnout to = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        to.setCommandedState(Turnout.CLOSED);
        TurnoutTableDataModel model = createGraphicStateModel();
        JTable table = new JTable(model);
        model.configValueColumn(table);
        assertTrue("cell goes into edit mode",
                table.editCellAt(0, TurnoutTableDataModel.VALUECOL));
        Component editor = table.getEditorComponent();
        assertNotNull("editor component in place", editor);
        MouseEvent evt = new MouseEvent(editor, MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(), 0, 1, 1, 1, false);
        for (MouseListener ml : editor.getMouseListeners()) {
            ml.mousePressed(evt);
        }
        assertEquals("turnout toggled by clicking the cell", Turnout.THROWN, to.getCommandedState());
        model.dispose();
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        t = new TurnoutTableDataModel();
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (t!=null){
            t.dispose();
        }
        t = null;
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(TurnoutTableDataModelTest.class);

}
