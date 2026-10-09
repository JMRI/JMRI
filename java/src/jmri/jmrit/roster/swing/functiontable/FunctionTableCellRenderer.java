package jmri.jmrit.roster.swing.functiontable;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

/**
 * A TableCellRenderer that provides a way for the user to view details of functions of a decoder in the roster. 
 * It is used in the FunctionLabelPane.
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Lionel Jeanson 2009-2026
 */

public class FunctionTableCellRenderer implements TableCellRenderer {

    public static final int CELL_HEIGHT = 32;
    public static final int LABEL_WIDTH = 200;
    public static final int[] columnWidths = new int[FunctionTableModel.NBCOL];
    public static final String[] columnTooltips = new String[FunctionTableModel.NBCOL];

    public FunctionTableCellRenderer() {
        super();

        columnWidths[FunctionTableModel.COL_FN]=CELL_HEIGHT;
        columnWidths[FunctionTableModel.COL_DO]=CELL_HEIGHT;
        columnWidths[FunctionTableModel.COL_VI]=CELL_HEIGHT;
        columnWidths[FunctionTableModel.COL_LK]=CELL_HEIGHT;
        columnWidths[FunctionTableModel.COL_OF]=CELL_HEIGHT;
        columnWidths[FunctionTableModel.COL_ON]=CELL_HEIGHT;
        columnWidths[FunctionTableModel.COL_SH]=CELL_HEIGHT;
        columnWidths[FunctionTableModel.COL_LA]=LABEL_WIDTH;

        columnTooltips[FunctionTableModel.COL_FN]=null;
        columnTooltips[FunctionTableModel.COL_DO]=Bundle.getMessage("FunctionButtonDisplayOrderToolTip");        
        columnTooltips[FunctionTableModel.COL_VI]=Bundle.getMessage("FunctionButtonVisibleToolTip");
        columnTooltips[FunctionTableModel.COL_LK]=Bundle.getMessage("FunctionButtonLockableToolTip");
        columnTooltips[FunctionTableModel.COL_OF]=Bundle.getMessage("FunctionButtonRosterImageToolTip");
        columnTooltips[FunctionTableModel.COL_ON]=Bundle.getMessage("FunctionButtonPressedRosterImageToolTip");
        columnTooltips[FunctionTableModel.COL_SH]=Bundle.getMessage("ShuntButtonToolTip");
        columnTooltips[FunctionTableModel.COL_LA]=null;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JPanel retPanel = new JPanel();
        retPanel.setLayout(new BorderLayout());

        if (value == null) {
            return retPanel;
        }

        int fn = ((FunctionTableModel)table.getModel()).getFnForRow(row);
        switch (column) {
            case FunctionTableModel.COL_FN:
                retPanel.add( new JLabel("" + fn), BorderLayout.CENTER);
                break;
            case FunctionTableModel.COL_DO:
                retPanel.add(new JLabel( ""+ value), BorderLayout.CENTER);
                break;
            case FunctionTableModel.COL_VI:
                JCheckBox visibleCheckBox = new JCheckBox();
                visibleCheckBox.setSelected((Boolean) value);                
                retPanel.add(visibleCheckBox, BorderLayout.CENTER);
                break;
            case FunctionTableModel.COL_LK:
                JCheckBox lockableCheckBox = new JCheckBox();
                lockableCheckBox.setSelected((Boolean) value);
                retPanel.add(lockableCheckBox, BorderLayout.CENTER);
                break;
            case FunctionTableModel.COL_OF:
                retPanel.add( ((FunctionTableModel)table.getModel()).getFunctionImagePanel(fn), BorderLayout.CENTER);
                break;
            case FunctionTableModel.COL_ON:
                retPanel.add( ((FunctionTableModel)table.getModel()).getFunctionSelectedImagePanel(fn), BorderLayout.CENTER);
                break;
            case FunctionTableModel.COL_SH:
                JCheckBox shuntingCB = new JCheckBox();
                shuntingCB.setSelected((Boolean)value);
                retPanel.add(shuntingCB, BorderLayout.CENTER);
                break;
            case FunctionTableModel.COL_LA:
                retPanel.add(new JTextField((String) value), BorderLayout.CENTER);
                break;
            default:
                break;
        }
        return retPanel;
    }
}
