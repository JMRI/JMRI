package jmri.jmrit.beantable.sensor;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
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
 */
public class SensorTableDataModelTest extends jmri.jmrit.beantable.AbstractBeanTableDataModelBase<Sensor> {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }
    
    @Override
    public int getModelColumnCount(){
        return 13;
    }
    
    @Test
    public void testGetColumnNames() {
        assertEquals("Column5 - INVERTCOL",Bundle.getMessage("Inverted"),
            t.getColumnName(SensorTableDataModel.INVERTCOL));
        assertEquals("Column6 - EDITCOL","",
            t.getColumnName(SensorTableDataModel.EDITCOL));
        assertEquals("Column7 - USEGLOBALDELAY",Bundle.getMessage("SensorUseGlobalDebounce"),
            t.getColumnName(SensorTableDataModel.USEGLOBALDELAY));
        assertEquals("Column8 - ACTIVEDELAY",Bundle.getMessage("SensorActiveDebounce"),
            t.getColumnName(SensorTableDataModel.ACTIVEDELAY));
        assertEquals("Column9 - INACTIVEDELAY",Bundle.getMessage("SensorInActiveDebounce"),
            t.getColumnName(SensorTableDataModel.INACTIVEDELAY));
        assertEquals("Column10 - PULLUPCOL",Bundle.getMessage("SensorPullUp"),
            t.getColumnName(SensorTableDataModel.PULLUPCOL));
        assertEquals("Column11 - FORGETCOL",Bundle.getMessage("StateForgetHeader"),
            t.getColumnName(SensorTableDataModel.FORGETCOL));
        assertEquals("Column12 - QUERYCOL",Bundle.getMessage("StateQueryHeader"),
            t.getColumnName(SensorTableDataModel.QUERYCOL));
    }
    
    @Test
    public void testGetColumnClasses() {
        assertEquals("INVERTCOL ",Boolean.class,t.getColumnClass(SensorTableDataModel.INVERTCOL) );
        assertEquals("EDITCOL ",JButton.class,t.getColumnClass(SensorTableDataModel.EDITCOL) );
        assertEquals("USEGLOBALDELAY ",Boolean.class,t.getColumnClass(SensorTableDataModel.USEGLOBALDELAY) );
        assertEquals("ACTIVEDELAY Long not long",Long.class,t.getColumnClass(SensorTableDataModel.ACTIVEDELAY) );
        assertEquals("INACTIVEDELAY Long not long",Long.class,t.getColumnClass(SensorTableDataModel.INACTIVEDELAY) );
        assertEquals("PULLUPCOL ",JComboBox.class,t.getColumnClass(SensorTableDataModel.PULLUPCOL) );
        assertEquals("FORGETCOL ",JButton.class,t.getColumnClass(SensorTableDataModel.FORGETCOL) );
        assertEquals("QUERYCOL ",JButton.class,t.getColumnClass(SensorTableDataModel.QUERYCOL) );
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

    private SensorTableDataModel createGraphicStateModel() {
        InstanceManager.getDefault(GuiLafPreferencesManager.class).setGraphicTableState(true);
        // the model reads the preference in its constructor, so create a fresh one
        return new SensorTableDataModel();
    }

    @Test
    public void testGraphicRendererDoesNotResizeRows() throws JmriException {
        SensorManager mgr = InstanceManager.getDefault(SensorManager.class);
        for (int i = 1; i <= 10; i++) {
            Sensor s = mgr.provideSensor("IS" + i);
            s.setKnownState((i % 2 == 0) ? Sensor.ACTIVE : Sensor.INACTIVE);
        }
        SensorTableDataModel model = createGraphicStateModel();
        CountingJTable table = new CountingJTable(model);
        model.configValueColumn(table);
        TableCellRenderer r = table.getDefaultRenderer(JLabel.class);
        assertNotNull("graphic state renderer installed", r);
        table.rowHeightCalls = 0;
        for (int pass = 0; pass < 5; pass++) {
            for (int row = 0; row < table.getRowCount(); row++) {
                Component c = r.getTableCellRendererComponent(table,
                        table.getValueAt(row, SensorTableDataModel.VALUECOL),
                        false, false, row, SensorTableDataModel.VALUECOL);
                assertNotNull("renderer returns a component", c);
            }
        }
        assertEquals("rendering must not set row heights", 0, table.rowHeightCalls);
        model.dispose();
    }

    @Test
    public void testGraphicRendererReusesComponent() throws JmriException {
        SensorManager mgr = InstanceManager.getDefault(SensorManager.class);
        mgr.provideSensor("IS1").setKnownState(Sensor.ACTIVE);
        mgr.provideSensor("IS2").setKnownState(Sensor.INACTIVE);
        SensorTableDataModel model = createGraphicStateModel();
        JTable table = new JTable(model);
        model.configValueColumn(table);
        TableCellRenderer r = table.getDefaultRenderer(JLabel.class);
        Component first = r.getTableCellRendererComponent(table,
                table.getValueAt(0, SensorTableDataModel.VALUECOL),
                false, false, 0, SensorTableDataModel.VALUECOL);
        Component second = r.getTableCellRendererComponent(table,
                table.getValueAt(1, SensorTableDataModel.VALUECOL),
                false, false, 1, SensorTableDataModel.VALUECOL);
        assertSame("renderer reuses a single component", first, second);
        model.dispose();
    }

    @Test
    public void testGraphicRowHeightSetByConfigureTable() {
        InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        SensorTableDataModel model = createGraphicStateModel();
        CountingJTable table = new CountingJTable(model);
        table.setColumnModel(new XTableColumnModel());
        table.createDefaultColumnsFromModel();
        model.configureTable(table);
        assertTrue("icons read so a row height is available",
                SensorTableDataModel.ImageIconRenderer.getIconRowHeight() > 0);
        assertTrue("row height fits the state icons",
                table.getRowHeight() >= SensorTableDataModel.ImageIconRenderer.getIconRowHeight());
        model.dispose();
    }

    @Test
    public void testGraphicRendererForegroundReset() {
        SensorTableDataModel model = createGraphicStateModel();
        JTable table = new JTable(model);
        model.configValueColumn(table);
        TableCellRenderer r = table.getDefaultRenderer(JLabel.class);
        JLabel label = (JLabel) r.getTableCellRendererComponent(table,
                Bundle.getMessage("SensorStateActive"), false, false, 0, SensorTableDataModel.VALUECOL);
        Color defaultForeground = label.getForeground();
        label = (JLabel) r.getTableCellRendererComponent(table,
                Bundle.getMessage("BeanStateInconsistent"), false, false, 0, SensorTableDataModel.VALUECOL);
        assertEquals("inconsistent state shown in red", Color.red, label.getForeground());
        assertEquals("tool tip follows the state", Bundle.getMessage("BeanStateInconsistent"), label.getToolTipText());
        label = (JLabel) r.getTableCellRendererComponent(table,
                Bundle.getMessage("SensorStateActive"), false, false, 0, SensorTableDataModel.VALUECOL);
        assertEquals("foreground reset after an inconsistent cell", defaultForeground, label.getForeground());
        model.dispose();
    }

    @Test
    public void testGraphicEditorClickTogglesSensor() throws JmriException {
        Sensor s = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        s.setKnownState(Sensor.INACTIVE);
        SensorTableDataModel model = createGraphicStateModel();
        JTable table = new JTable(model);
        model.configValueColumn(table);
        assertTrue("cell goes into edit mode",
                table.editCellAt(0, SensorTableDataModel.VALUECOL));
        Component editor = table.getEditorComponent();
        assertNotNull("editor component in place", editor);
        MouseEvent evt = new MouseEvent(editor, MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(), 0, 1, 1, 1, false);
        for (MouseListener ml : editor.getMouseListeners()) {
            ml.mousePressed(evt);
        }
        assertEquals("sensor toggled by clicking the cell", Sensor.ACTIVE, s.getKnownState());
        model.dispose();
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        t = new SensorTableDataModel();
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (t!=null){
            t.dispose();
        }
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(SensorTableDataModelTest.class);

}
