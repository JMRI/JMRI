package jmri.jmrit.beantable.turnout;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import java.util.EventObject;
import java.util.Hashtable;

import javax.annotation.Nonnull;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.table.*;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;

import jmri.swing.NamedBeanComboBox;
import jmri.util.swing.JComboBoxUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JTable to display a TurnoutTableDataModel.
 * Code originally within TurnoutTableAction.
 * 
 * @author Bob Jacobsen Copyright (C) 2003, 2004, 2007
 * @author Egbert Broerse Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021
 */
public class TurnoutTableJTable extends JTable {
    
    final TurnoutTableDataModel model;
    
    final Hashtable<Turnout, TableCellRenderer> rendererMapSensor1 = new Hashtable<>();
    final Hashtable<Turnout, TableCellEditor> editorMapSensor1 = new Hashtable<>();

    final Hashtable<Turnout, TableCellRenderer> rendererMapSensor2 = new Hashtable<>();
    final Hashtable<Turnout, TableCellEditor> editorMapSensor2 = new Hashtable<>();
    
    public TurnoutTableJTable(TurnoutTableDataModel mdl){
        super(mdl);
        model = mdl;
    }
    
    @Override
    public String getToolTipText(@Nonnull MouseEvent e) {
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);
        int realRowIndex = convertRowIndexToModel(rowIndex);
        int realColumnIndex = convertColumnIndexToModel(colIndex);
        return model.getCellToolTip(this, realRowIndex, realColumnIndex);
    }
    
    /**
     * Disable Windows Key or Mac Meta Keys being pressed acting
     * as a trigger for editing the focused cell.
     * Causes unexpected behaviour, i.e. button presses.
     * {@inheritDoc}
     */
    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        if (e instanceof KeyEvent) {
            if ( ((KeyEvent) e).getKeyCode() == KeyEvent.VK_WINDOWS
                || ( (KeyEvent) e).getKeyCode() == KeyEvent.VK_META ) {
                return false;
            }
        }
        return super.editCellAt(row, column, e);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        // Convert the displayed index to the model index, rather than the displayed index
        int modelColumn = this.convertColumnIndexToModel(column);
        if (modelColumn == TurnoutTableDataModel.SENSOR1COL || modelColumn == TurnoutTableDataModel.SENSOR2COL) {
            return getRenderer(row, modelColumn);
        } else {
            return super.getCellRenderer(row, column);
        }
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        //Convert the displayed index to the model index, rather than the displayed index
        int modelColumn = this.convertColumnIndexToModel(column);
        if (modelColumn == TurnoutTableDataModel.SENSOR1COL || modelColumn == TurnoutTableDataModel.SENSOR2COL) {
            return getEditor(row, modelColumn);
        } else {
            return super.getCellEditor(row, column);
        }
    }

    TableCellRenderer getRenderer(int row, int column) {
        TableCellRenderer retval;
        Turnout t = (Turnout) getModel().getValueAt(row, TurnoutTableDataModel.SYSNAMECOL);
        java.util.Objects.requireNonNull(t, "SYSNAMECOL column content must be nonnull");
        switch (column) {
            case TurnoutTableDataModel.SENSOR1COL:
                retval = rendererMapSensor1.get(t);
                break;
            case TurnoutTableDataModel.SENSOR2COL:
                retval = rendererMapSensor2.get(t);
                break;
            default:
                return null;
        }

        if (retval == null) {
            if (column == TurnoutTableDataModel.SENSOR1COL) {
                loadRenderEditMaps(rendererMapSensor1, editorMapSensor1, t, t.getFirstSensor());
            } else {
                loadRenderEditMaps(rendererMapSensor2, editorMapSensor2, t, t.getSecondSensor());
            }
            retval = rendererMapSensor1.get(t);
        }
        log.debug("fetched for Turnout \"{}\" renderer {}", t, retval);
        return retval;
    }

    TableCellEditor getEditor(int row, int column) {
        TableCellEditor retval;
        Turnout t = (Turnout) getModel().getValueAt(row, TurnoutTableDataModel.SYSNAMECOL);
        java.util.Objects.requireNonNull(t, "SYSNAMECOL column content must be nonnull");
        switch (column) {
            case TurnoutTableDataModel.SENSOR1COL:
                retval = editorMapSensor1.get(t);
                break;
            case TurnoutTableDataModel.SENSOR2COL:
                retval = editorMapSensor2.get(t);
                break;
            default:
                return null;
        }
        if (retval == null) {
            if (column == TurnoutTableDataModel.SENSOR1COL) {
                loadRenderEditMaps(rendererMapSensor1, editorMapSensor1, t, t.getFirstSensor());
                retval = editorMapSensor1.get(t);
            } else { //Must be two
                loadRenderEditMaps(rendererMapSensor2, editorMapSensor2, t, t.getSecondSensor());
                retval = editorMapSensor2.get(t);
            }
        }
        log.debug("fetched for Turnout \"{}\" editor {}", t, retval);
        return retval;
    }

    protected void loadRenderEditMaps(Hashtable<Turnout, TableCellRenderer> r, Hashtable<Turnout, TableCellEditor> e,
            Turnout t, Sensor s) {
        NamedBeanComboBox<Sensor> c = new NamedBeanComboBox<>(InstanceManager.getDefault(SensorManager.class), s, NamedBean.DisplayOptions.DISPLAYNAME);
        c.setAllowNull(true);
        JComboBoxUtil.setupComboBoxMaxRows(c);

        BeanBoxRenderer renderer = new BeanBoxRenderer();
        renderer.setSelectedItem(s);
        r.put(t, renderer);

        TableCellEditor editor = new BeanComboBoxEditor(c);
        e.put(t, editor);
        log.debug("initialize for Turnout \"{}\" Sensor \"{}\"", t, s);
    }
    
    static class BeanBoxRenderer extends NamedBeanComboBox<Sensor> implements TableCellRenderer {

        public BeanBoxRenderer() {
            super(InstanceManager.getDefault(SensorManager.class));
            setAllowNull(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            if (value instanceof Sensor) {
                setSelectedItem(value);
            } else {
                setSelectedItem(null);
            }
            return this;
        }
    }
    
    static class BeanComboBoxEditor extends DefaultCellEditor {

        public BeanComboBoxEditor(NamedBeanComboBox<Sensor> beanBox) {
            super(beanBox);
        }
    }
    
     
private final static Logger log = LoggerFactory.getLogger(TurnoutTableJTable.class);

}
