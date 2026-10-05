package jmri.jmrit.roster.swing.functiontable;

import javax.swing.JPanel;
import javax.swing.JTable;

import jmri.util.swing.EditableResizableImagePanel;
import jmri.util.swing.JmriMouseEvent;
import jmri.util.swing.JmriMouseListener;

/**
 * A JmriMouseListener that listen for mouse events in the FunctionTable. 
 * It can handle mouse events in the table but also mouse events on the EditableResizableImagePanel in some of the table cells.
 * 
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

public class FunctionTableMouseListener implements JmriMouseListener {
    JTable functionsTable;
    
    public FunctionTableMouseListener(JTable table) {
        this.functionsTable = table;
    }

    private EditableResizableImagePanel getEditableResizableImagePanelAt(int x, int y) {
        return getEditableResizableImagePanelAt(functionsTable, x, y);
    }

    @Override
    public void mouseClicked(JmriMouseEvent e) {
        // Handle mouse click event
        EditableResizableImagePanel cmp = getEditableResizableImagePanelAt(e.getX(), e.getY());
        if (cmp != null) {
            cmp.getMyMouseAdapter().mouseClicked(e);
        }
    }

    @Override
    public void mousePressed(JmriMouseEvent e) {
        // Handle mouse press event
        EditableResizableImagePanel cmp = getEditableResizableImagePanelAt(e.getX(), e.getY());
        if (cmp != null) {
            cmp.getMyMouseAdapter().mousePressed(e);
        }
    }

    @Override
    public void mouseReleased(JmriMouseEvent e) {
        // Handle mouse release event
        EditableResizableImagePanel cmp = getEditableResizableImagePanelAt(e.getX(), e.getY());
        if (cmp != null) {
            cmp.getMyMouseAdapter().mouseReleased(e);
            ((FunctionTableModel) functionsTable.getModel()).fireTableDataChanged();
        }        
    }

    @Override
    public void mouseEntered(JmriMouseEvent e) {
        // Handle mouse enter event
        EditableResizableImagePanel cmp = getEditableResizableImagePanelAt(e.getX(), e.getY());
        if (cmp != null) {
            cmp.getMyMouseAdapter().mouseEntered(e);
        }
    }

    @Override
    public void mouseExited(JmriMouseEvent e) {
        // Handle mouse exit event
        EditableResizableImagePanel cmp = getEditableResizableImagePanelAt(e.getX(), e.getY());
        if (cmp != null) {
            cmp.getMyMouseAdapter().mouseExited(e);
        }   
    }

    public static EditableResizableImagePanel getEditableResizableImagePanelAt(JTable t, int x, int y) {
        int row = t.rowAtPoint(new java.awt.Point(x, y));
        int column = t.columnAtPoint(new java.awt.Point(x, y));
        if (row == -1 || column == -1) {
            return null;
        }

        Object cmp = t.prepareRenderer(t.getCellRenderer(row, column), row, column);        
        if (cmp != null && cmp instanceof JPanel && ((JPanel) cmp).getComponentCount() > 0 && ((JPanel) cmp).getComponent(0) instanceof EditableResizableImagePanel) {
            return (EditableResizableImagePanel) ((JPanel) cmp).getComponent(0) ;
        }
        return null;
    }
     
}
