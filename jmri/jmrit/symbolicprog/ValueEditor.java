/**
 * ValueEditor.java
 *
 * Description:		Represents an enum table cell
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version
 */

package jmri.jmrit.symbolicprog;

import java.awt.Component;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

public class ValueEditor extends JComboBox implements TableCellEditor {

	protected transient Vector listeners;
	protected transient int originalValue;

	public ValueEditor() {
		super();
		listeners = new Vector();
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
                                               boolean isSelected,
                                               int row,int column) {
		if (log.isDebugEnabled()) log.debug("getTableCellEditorComponent "+row+" "
											+column+" "+isSelected+" "
											+value);
		table.setRowSelectionInterval(row, row);
		table.setColumnSelectionInterval(column, column);
			if (value instanceof Component)
				return (Component) value;
			else if (value instanceof String)
				return new JLabel((String)value);
			else
				return new JLabel("Unknown value type!");
	}

	// CellEditor methods
	public void cancelCellEditing() {
		if (log.isDebugEnabled()) log.debug("cancelCellEditing");
		fireEditingCanceled();
	}

	public Object getCellEditorValue() {
		if (log.isDebugEnabled()) log.debug("getCellEditorValue");
		return new Integer(0);
	}

	public boolean isCellEditable(EventObject eo) {return true;}

	public boolean shouldSelectCell(EventObject eo) {
		return true;
	}

	public boolean stopCellEditing() {
		if (log.isDebugEnabled()) log.debug("stopCellEditing");
		fireEditingStopped();
		return true;
	}

	public void addCellEditorListener(CellEditorListener cel) {
		listeners.addElement(cel);
	}

	public void removeCellEditorListener(CellEditorListener cel) {
		listeners.removeElement(cel);
	}

	protected void fireEditingCanceled() {
		if (log.isDebugEnabled()) log.debug("fireEditingCancelled");
		// setValue(originalValue);
		ChangeEvent ce = new ChangeEvent(this);
		for (int i = listeners.size(); i >= 0; i--) {
			((CellEditorListener)listeners.elementAt(i)).editingCanceled(ce);
		}
	}

	protected void fireEditingStopped() {
		if (log.isDebugEnabled()) log.debug("fireEditingStopped");
		ChangeEvent ce = new ChangeEvent(this);
		for (int i = listeners.size() - 1; i >= 0; i--) {
			((CellEditorListener)listeners.elementAt(i)).editingStopped(ce);
		}
	}

	// initialize logging
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ValueEditor.class.getName());
}
