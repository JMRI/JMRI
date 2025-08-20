package jmri.jmrit.operations;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.trains.trainbuilder.TrainCommon;

/**
 * Common table model methods for operations.
 * 
 * @author Daniel Boudreau Copyright (C) 2023, 2025
 */
public abstract class OperationsTableModel extends javax.swing.table.AbstractTableModel {
    
    protected JTable _table;
    
    public void initTable(JTable table) {
        _table = table;
        table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
        table.setDefaultRenderer(Object.class, new MyTableCellRenderer());
        table.setDefaultRenderer(Integer.class, new MyTableCellRenderer());
    }

    protected Color getForegroundColor(int row) {
        return _table.getForeground();
    }
    
    /**
     * Search list for rolling stock by road number
     * 
     * @param roadNumber The string road number to search for.
     * @param rsList     The list to search
     * @return -1 if not found, table row number if found
     */
    public int findRollingStockByRoadNumber(String roadNumber, List<?> rsList) {
        if (rsList != null) {
            if (!roadNumber.equals(_roadNumber)) {
                return getIndex(0, roadNumber, rsList);
            }
            int index = getIndex(_index, roadNumber, rsList);
            if (index > 0) {
                return index;
            }
            return getIndex(0, roadNumber, rsList);
        }
        return -1;
    }

    protected String _roadNumber = "";
    protected int _index = 0;

    protected int getIndex(int start, String roadNumber, List<?> rsList) {
        for (int index = start; index < rsList.size(); index++) {
            RollingStock rs = (RollingStock) rsList.get(index);
            if (rs != null) {
                _roadNumber = roadNumber;
                _index = index + 1;
                String[] number = rs.getNumber().split(TrainCommon.HYPHEN);
                // check for wild card '*'
                if (roadNumber.startsWith("*") && roadNumber.endsWith("*") && roadNumber.length() > 1) {
                    String rN = roadNumber.substring(1, roadNumber.length() - 1);
                    if (rs.getNumber().contains(rN)) {
                        return index;
                    }
                } else if (roadNumber.startsWith("*")) {
                    String rN = roadNumber.substring(1);
                    if (rs.getNumber().endsWith(rN) || number[0].endsWith(rN)) {
                        return index;
                    }
                } else if (roadNumber.endsWith("*")) {
                    String rN = roadNumber.substring(0, roadNumber.length() - 1);
                    if (rs.getNumber().startsWith(rN)) {
                        return index;
                    }
                } else if (roadNumber.contains("*")) {
                    String rn = roadNumber.replace("*", ":");
                    String[] rN = rn.split(":");
                    if (rs.getNumber().startsWith(rN[0]) && number[0].endsWith(rN[rN.length - 1])) {
                        return index;
                    }
                } else if (rs.getNumber().equals(roadNumber) || number[0].equals(roadNumber)) {
                    return index;
                }
            }
        }
        _roadNumber = "";
        return -1;
    }

    public boolean showAll = true; // when true show all rolling stock
    public String locationName = null; // only show rolling stock at this location
    public String trackName = null; // only show rolling stock using this track

    protected void filterList(List<?> list) {
        if (showAll) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            RollingStock rs = (RollingStock) list.get(i);
            if (rs.getLocation() == null) {
                list.remove(i--);
                continue;
            }
            // filter out cars that don't have a location name that matches
            if (locationName != null) {
                if (!rs.getLocationName().equals(locationName)) {
                    list.remove(i--);
                    continue;
                }
                if (trackName != null) {
                    if (!rs.getTrackName().equals(trackName)) {
                        list.remove(i--);
                    }
                }
            }
        }
    }

    public class MyTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                int modelRow = table.convertRowIndexToModel(row);
                component.setForeground(getForegroundColor(modelRow));
            }
            return component;
        }
    }
}
