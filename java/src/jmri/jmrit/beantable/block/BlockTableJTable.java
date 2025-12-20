package jmri.jmrit.beantable.block;

import java.awt.Component;
import java.text.DecimalFormat;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jmri.*;
import jmri.util.table.NamedBeanBoxRenderer;
import jmri.util.table.JComboBoxEditor;

/**
 * JTable for displaying Block BeanTable.
 * @author Steve Young Copyright (C) 2024
 */
public class BlockTableJTable extends jmri.jmrit.beantable.BeanTableJTable<Block> implements Disposable {

    private final HashMap<Block, NamedBeanBoxRenderer<Sensor>> sensorComboMap = new HashMap<>();
    private final HashMap<Block, NamedBeanBoxRenderer<Reporter>> reporterComboMap = new HashMap<>();

    public BlockTableJTable( BlockTableDataModel blockTableDataModel) {
        super(blockTableDataModel);
        setDefaultRenderer(Float.class, new LengthRenderer());
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        // Convert the displayed index to the model index, rather than the displayed index
        switch (convertColumnIndexToModel(column)) {
            case BlockTableDataModel.SENSORCOL:
                return getSensorRenderer(convertRowIndexToModel(row));
            case BlockTableDataModel.REPORTERCOL:
                return getReporterRenderer(convertRowIndexToModel(row));
            default:
                return super.getCellRenderer(row, column);
        }
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        //Convert the displayed index to the model index, rather than the displayed index
        switch (convertColumnIndexToModel(column)) {
            case BlockTableDataModel.SENSORCOL:
                Block t = (Block) getModel().getValueAt(convertRowIndexToModel(row), BlockTableDataModel.SYSNAMECOL);
                return new JComboBoxEditor(sensorComboMap.get(t),null );
            case BlockTableDataModel.REPORTERCOL:
                Block tt = (Block) getModel().getValueAt(convertRowIndexToModel(row), BlockTableDataModel.SYSNAMECOL);
                return new JComboBoxEditor(reporterComboMap.get(tt), null);
            default:
                return super.getCellEditor(row, column);
        }
    }

    private TableCellRenderer getSensorRenderer(int modelRow) {
        Block t = (Block) getModel().getValueAt(modelRow, BlockTableDataModel.SYSNAMECOL);
        java.util.Objects.requireNonNull(t, "SYSNAMECOL column content must be nonnull");
        return sensorComboMap.computeIfAbsent(t, k ->
            new NamedBeanBoxRenderer<Sensor>(InstanceManager.getDefault(SensorManager.class)));
    }

    private TableCellRenderer getReporterRenderer(int modelRow) {
        Block t = (Block) getModel().getValueAt(modelRow, BlockTableDataModel.SYSNAMECOL);
        java.util.Objects.requireNonNull(t, "SYSNAMECOL column content must be nonnull");
        return reporterComboMap.computeIfAbsent(t, k ->
            new NamedBeanBoxRenderer<Reporter>(InstanceManager.getDefault(ReporterManager.class)));
    }

    private static class LengthRenderer extends javax.swing.table.DefaultTableCellRenderer{

        private static final DecimalFormat twoDigit = new DecimalFormat("0.00");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if ( value instanceof Float && comp instanceof JLabel ) {
                ((JLabel)comp).setText(twoDigit.format(value));
            }
            return comp;
        }

    }

    @Override
    public void dispose(){
        sensorComboMap.forEach( (b,s) -> s.dispose() );
        sensorComboMap.clear();
        reporterComboMap.forEach( (b,r) -> r.dispose() );
        reporterComboMap.clear();
    }

}
