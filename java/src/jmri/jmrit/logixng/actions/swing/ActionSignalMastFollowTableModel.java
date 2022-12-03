package jmri.jmrit.logixng.actions.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import jmri.SignalMast;
import jmri.util.swing.JComboBoxUtil;

/**
 * Table model for ActionSignalMastFollowSwing
 * @author Daniel Bergqvist Copyright 2022
 */
public class ActionSignalMastFollowTableModel extends AbstractTableModel {

    public static final int COLUMN_SOURCE_ASPECT = 0;
    public static final int COLUMN_DEST_ASPECT = 1;

    private SignalMast _primaryMast;
    private SignalMast _secondaryMast;
    private final List<AspectMapping> _aspectMapping = new ArrayList<>();


    public ActionSignalMastFollowTableModel(
            SignalMast primaryMast,
            SignalMast secondaryMast,
            Map<String, String> aspectMap) {

        _primaryMast = primaryMast;
        _secondaryMast = secondaryMast;

        updateMap(aspectMap);
    }

    public void setPrimaryMast(SignalMast primaryMast) {
        _primaryMast = primaryMast;
        updateMap(null);
    }

    public void setSecondaryMast(SignalMast secondaryMast) {
        _secondaryMast = secondaryMast;
        updateMap(null);
    }

    private void updateMap(Map<String, String> aspectMap) {
        Map<String, String> oldAspectMap = getAspectMapping();

        if (aspectMap != null) {
            oldAspectMap.putAll(aspectMap);
        }

        _aspectMapping.clear();

        if (_primaryMast != null) {
            String defaultDestAspect = "";
            Set<String> validDestAspectsSet = new HashSet<>();
            if (_secondaryMast != null) {
                validDestAspectsSet.addAll(_secondaryMast.getValidAspects());
                defaultDestAspect = _secondaryMast.getValidAspects().firstElement();
            }

            for (String sourceAspect : _primaryMast.getValidAspects()) {
                String destAspect = oldAspectMap.get(sourceAspect);
                if (destAspect != null && validDestAspectsSet.contains(destAspect)) {
                    _aspectMapping.add(new AspectMapping(sourceAspect, destAspect));
                } else {
                    _aspectMapping.add(new AspectMapping(sourceAspect, defaultDestAspect));
                }
            }
        }
        fireTableStructureChanged();
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return _aspectMapping.size();
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return 2;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case COLUMN_SOURCE_ASPECT:
                return Bundle.getMessage("ActionSignalMastFollowTableModel_ColumnSourceAspect");
            case COLUMN_DEST_ASPECT:
                return Bundle.getMessage("ActionSignalMastFollowTableModel_ColumnDestAspect");
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case COLUMN_SOURCE_ASPECT:
            case COLUMN_DEST_ASPECT:
                return String.class;
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCellEditable(int row, int col) {
        return col == COLUMN_DEST_ASPECT;
    }

    /** {@inheritDoc} */
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case COLUMN_DEST_ASPECT:
                if (value != null) {
                    _aspectMapping.get(rowIndex)._destAspect = value.toString();
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= _aspectMapping.size()) throw new IllegalArgumentException("Invalid row");

        switch (columnIndex) {
            case COLUMN_SOURCE_ASPECT:
                return _aspectMapping.get(rowIndex)._sourceAspect;
            case COLUMN_DEST_ASPECT:
                return _aspectMapping.get(rowIndex)._destAspect;
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }

    public void setColumnsForComboBoxes(JTable table) {
        JComboBox<String> destAspectComboBox = new JComboBox<>();
        table.setRowHeight(destAspectComboBox.getPreferredSize().height);
        table.getColumnModel().getColumn(COLUMN_DEST_ASPECT)
                .setPreferredWidth((destAspectComboBox.getPreferredSize().width) + 4);
    }

    public Map<String, String> getAspectMapping() {
        Map<String, String> aspectMapping = new HashMap<>();
        for (AspectMapping mapping : _aspectMapping) {
            aspectMapping.put(mapping._sourceAspect, mapping._destAspect);
        }
        return aspectMapping;
    }


    public static class DestAspectCellEditor extends AbstractCellEditor
            implements TableCellEditor, ActionListener {

        private final ActionSignalMastFollowTableModel _aspectMappingTableModel;
        private String _destAspect;

        public DestAspectCellEditor(ActionSignalMastFollowTableModel aspectMappingTableModel) {
            _aspectMappingTableModel = aspectMappingTableModel;
        }

        @Override
        public Object getCellEditorValue() {
            return this._destAspect;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {

            if (value == null) value = "";

            if (! (value instanceof String)) {
                throw new IllegalArgumentException("value is not a String: " + value.getClass().getName());
            }

            JComboBox<String> destAspectComboBox = new JComboBox<>();

            if (_aspectMappingTableModel._secondaryMast != null) {
                for (String aspect : _aspectMappingTableModel._secondaryMast.getValidAspects()) {
                    destAspectComboBox.addItem(aspect);
                }
            }
            JComboBoxUtil.setupComboBoxMaxRows(destAspectComboBox);

            destAspectComboBox.setSelectedItem(value);
            destAspectComboBox.addActionListener(this);

            return destAspectComboBox;
        }

        @Override
        @SuppressWarnings("unchecked")  // Not possible to check that event.getSource() is instanceof JComboBox<InitialValueType>
        public void actionPerformed(ActionEvent event) {
            if (! (event.getSource() instanceof JComboBox)) {
                throw new IllegalArgumentException("value is not an JComboBox: " + event.getSource().getClass().getName());
            }
            JComboBox<String> destAspectComboBox = (JComboBox<String>) event.getSource();
            _destAspect = destAspectComboBox.getItemAt(destAspectComboBox.getSelectedIndex());

        }

    }


    private static class AspectMapping {

        String _sourceAspect;
        String _destAspect;

        public AspectMapping(String sourceAspect, String destAspect) {
            _sourceAspect = sourceAspect;
            _destAspect = destAspect;
        }

    }

}
