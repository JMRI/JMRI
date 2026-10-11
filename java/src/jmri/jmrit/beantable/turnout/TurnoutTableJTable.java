package jmri.jmrit.beantable.turnout;

import java.awt.Component;

import java.util.HashMap;

import javax.swing.JTable;
import javax.swing.table.*;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.swing.NamedBeanComboBox;

/**
 * JTable to display a TurnoutTableDataModel.
 * Code originally within TurnoutTableAction.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2004, 2007
 * @author Egbert Broerse Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021, 2024
 */
public class TurnoutTableJTable extends jmri.jmrit.beantable.BeanTableJTable<Turnout> {

    final HashMap<Turnout, TableCellRenderer> rendererMapSensor1 = new HashMap<>();
    final HashMap<Turnout, TableCellEditor> editorMapSensor1 = new HashMap<>();

    final HashMap<Turnout, TableCellRenderer> rendererMapSensor2 = new HashMap<>();
    final HashMap<Turnout, TableCellEditor> editorMapSensor2 = new HashMap<>();

    public TurnoutTableJTable(TurnoutTableDataModel mdl){
        super(mdl);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        // Convert the displayed index to the model index, rather than the displayed index
        int modelColumn = convertColumnIndexToModel(column);
        if (modelColumn == TurnoutTableDataModel.SENSOR1COL || modelColumn == TurnoutTableDataModel.SENSOR2COL) {
            return getRenderer(convertRowIndexToModel(row), modelColumn);
        } else {
            return super.getCellRenderer(row, column);
        }
    }

    @Override
    public TableCellEditor getCellEditor(final int row, final int column) {
        //Convert the displayed index to the model index, rather than the displayed index
        int modelColumn = this.convertColumnIndexToModel(column);
        if (modelColumn == TurnoutTableDataModel.SENSOR1COL || modelColumn == TurnoutTableDataModel.SENSOR2COL) {
            return getEditor(convertRowIndexToModel(row), modelColumn);
        } else {
            return super.getCellEditor(row, column);
        }
    }

    private TableCellRenderer getRenderer(int modelRow, int modelColumn) {
        TableCellRenderer retval;
        Turnout t = (Turnout) getModel().getValueAt(modelRow, TurnoutTableDataModel.SYSNAMECOL);
        java.util.Objects.requireNonNull(t, "SYSNAMECOL column content must be nonnull");
        switch (modelColumn) {
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
            if (modelColumn == TurnoutTableDataModel.SENSOR1COL) {
                loadRenderEditMaps(rendererMapSensor1, editorMapSensor1, t, t.getFirstSensor());
            } else {
                loadRenderEditMaps(rendererMapSensor2, editorMapSensor2, t, t.getSecondSensor());
            }
            retval = rendererMapSensor1.get(t);
        }
        log.debug("fetched for Turnout \"{}\" renderer {}", t, retval);
        return retval;
    }

    private TableCellEditor getEditor(int modelRow, int modelColumn) {
        TableCellEditor retval;
        Turnout t = (Turnout) getModel().getValueAt(modelRow, TurnoutTableDataModel.SYSNAMECOL);
        java.util.Objects.requireNonNull(t, "SYSNAMECOL column content must be nonnull");
        switch (modelColumn) {
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
            if (modelColumn == TurnoutTableDataModel.SENSOR1COL) {
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

    private void loadRenderEditMaps(HashMap<Turnout, TableCellRenderer> r, HashMap<Turnout, TableCellEditor> e,
            Turnout t, Sensor s) {
        NamedBeanComboBox<Sensor> c = new NamedBeanComboBox<>(InstanceManager.getDefault(
            SensorManager.class), s, NamedBean.DisplayOptions.DISPLAYNAME);

        TurnoutTableSensorBoxRenderer renderer = new TurnoutTableSensorBoxRenderer();
        r.put(t, renderer);

        TableCellEditor editor = new jmri.util.table.JComboBoxEditor(c, null);
        e.put(t, editor);
        log.debug("initialize for Turnout \"{}\" Sensor \"{}\"", t, s);
    }

    private static class TurnoutTableSensorBoxRenderer extends jmri.util.table.NamedBeanBoxRenderer<Sensor> {

        TurnoutTableSensorBoxRenderer() {
            super(InstanceManager.getDefault(SensorManager.class));
            setAllowNull(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            int tableCol = table.convertColumnIndexToModel(column);
            int tableRow = table.convertRowIndexToModel(row);
            
            Turnout t = (Turnout)table.getModel().getValueAt(tableRow, TurnoutTableDataModel.SYSNAMECOL);
            if ( t == null ) {
                return this;
            }
            if (value instanceof Sensor) {
                if (( tableCol == TurnoutTableDataModel.SENSOR1COL ) && 
                    (t.getFeedbackMode() != Turnout.ONESENSOR && t.getFeedbackMode() != Turnout.TWOSENSOR )) {
                    setErrorBorder();
                } else if ( tableCol == TurnoutTableDataModel.SENSOR2COL && t.getFeedbackMode() != Turnout.TWOSENSOR ) {
                    setErrorBorder();
                } else {
                    setNormalBorder();
                }
            } else {
                if (( tableCol == TurnoutTableDataModel.SENSOR1COL ) && 
                        (t.getFeedbackMode() == Turnout.ONESENSOR || t.getFeedbackMode() == Turnout.TWOSENSOR )) {
                    setErrorBorder();
                } else if ( tableCol == TurnoutTableDataModel.SENSOR2COL && t.getFeedbackMode() == Turnout.TWOSENSOR ) {
                    setErrorBorder();
                } else {
                    setNormalBorder();
                }
            }
            return this;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TurnoutTableJTable.class);

}
