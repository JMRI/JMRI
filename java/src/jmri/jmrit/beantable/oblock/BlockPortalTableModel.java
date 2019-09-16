package jmri.jmrit.beantable.oblock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import jmri.jmrit.logix.OBlock;
import jmri.util.NamedBeanComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to define OBlocks
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
 * @author Pete Cressman (C) 2010
 */
public class BlockPortalTableModel extends AbstractTableModel implements PropertyChangeListener {

    public static final int BLOCK_NAME_COLUMN = 0;
    public static final int PORTAL_NAME_COLUMN = 1;
    public static final int NUMCOLS = 2;

    OBlockTableModel _oBlockModel;

    public BlockPortalTableModel(OBlockTableModel oBlockModel) {
        super();
        _oBlockModel = oBlockModel;
    }

    @Override
    public int getColumnCount() {
        return NUMCOLS;
    }

    @Override
    public int getRowCount() {
        int count = 0;
        List<OBlock> list = _oBlockModel.getBeanList();
        for (int i = 0; i < list.size(); i++) {
            count += list.get(i).getPortals().size();
        }
        return count;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case BLOCK_NAME_COLUMN:
                return Bundle.getMessage("BlockName");
            case PORTAL_NAME_COLUMN:
                return Bundle.getMessage("PortalName");
            default:
                log.warn("Unhandled column name: {}", col);
                break;
        }
        return "";
    }

    @Override
    public Object getValueAt(int row, int col) {
        List<OBlock> list = _oBlockModel.getBeanList();
        if (list.size() > 0) {
            int count = 0;
            int idx = 0;  //accumulated row count
            OBlock block = null;
            OBlock[] array = new OBlock[list.size()];
            array = list.toArray(array);
            Arrays.sort(array, new NamedBeanComparator<>());
            while (count <= row) {
                count += array[idx++].getPortals().size();
            }
            block = array[--idx];
            idx = row - (count - block.getPortals().size());
            if (col == BLOCK_NAME_COLUMN) {
                if (idx == 0) {
                    return block.getDisplayName();
                }
                return "";
            }
            return block.getPortals().get(idx).getName();
            /*
             while (count <= row)  {
             count += ((OBlock)list.get(idx++)).getPortals().size();
             }
             block = (OBlock)list.get(--idx);
             idx = row - (count - block.getPortals().size());
             if (col==BLOCK_NAME_COLUMN) {
             if (idx==0) {
             return block.getDisplayName();
             }
             return "";
             }
             return block.getPortals().get(idx).getName();
             */
        }
        return null;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return String.class;
    }

    public int getPreferredWidth(int col) {
        return new JTextField(15).getPreferredSize().width;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();
        if (property.equals("length") || property.equals("UserName")) {
            fireTableDataChanged();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(BlockPortalTableModel.class);
}
