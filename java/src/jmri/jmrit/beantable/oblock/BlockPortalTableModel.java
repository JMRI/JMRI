
package jmri.jmrit.beantable.oblock;

/**
 * GUI to define OBlocks 
 *<P> 
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author	Pete Cressman (C) 2010
 * @version     $Revision$
 */

import org.apache.log4j.Logger;
import java.util.List;
import java.util.ResourceBundle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import jmri.NamedBean;

import jmri.jmrit.logix.OBlock;

class BlockPortalTableModel extends AbstractTableModel implements PropertyChangeListener {

    public static final int BLOCK_NAME_COLUMN = 0;
    public static final int PORTAL_NAME_COLUMN = 1;
    public static final int NUMCOLS = 2;

    static final ResourceBundle rbo = ResourceBundle.getBundle("jmri.jmrit.beantable.OBlockTableBundle");

    OBlockTableModel _oBlockModel;

    public BlockPortalTableModel(OBlockTableModel oBlockModel) {
        super();
        _oBlockModel = oBlockModel;
    }

    public int getColumnCount () {
        return NUMCOLS;
    }

    public int getRowCount() {
        int count = 0;
        List <NamedBean> list = _oBlockModel.getBeanList();
        for (int i=0; i<list.size(); i++) {
            count += ((OBlock)list.get(i)).getPortals().size();
        }
        return count;
    }

    public String getColumnName(int col) {
        switch (col) {
            case BLOCK_NAME_COLUMN: return rbo.getString("BlockName");
            case PORTAL_NAME_COLUMN: return rbo.getString("PortalName");
        }
        return "";
    }

    public Object getValueAt(int row, int col) {
        List <NamedBean> list = _oBlockModel.getBeanList();
        if (list.size() > 0) {
            int count = 0;
            int idx = 0;
            OBlock block = null;
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
        }
        return null;
    }


    public void setValueAt(Object value, int row, int col) {
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public Class<?> getColumnClass(int col) {
        return String.class;
    }

    public int getPreferredWidth(int col) {
        return new JTextField(15).getPreferredSize().width;
    }

    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();
        if (property.equals("length")|| property.equals("UserName")) {
            fireTableDataChanged();
        }
    }

    static Logger log = Logger.getLogger(BlockPortalTableModel.class.getName());
}
