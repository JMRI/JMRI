/** 
 * ValueEditor.java
 *
 * Description:		Represents an enum table cell
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.symbolicprog;

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

  public Component getTableCellEditorComponent(JTable table,Object value,
                                               boolean isSelected,
                                               int row,int column) {
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
  public void cancelCellEditing() {fireEditingCanceled();}

  public Object getCellEditorValue() {return new Integer(0);}

  public boolean isCellEditable(EventObject eo) {return true;}
  
  public boolean shouldSelectCell(EventObject eo) {
    return true;
  }

  public boolean stopCellEditing() {
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
    // setValue(originalValue);
    ChangeEvent ce = new ChangeEvent(this);
    for (int i = listeners.size(); i >= 0; i--) {
      ((CellEditorListener)listeners.elementAt(i)).editingCanceled(ce);
    }
  }

  protected void fireEditingStopped() {
    ChangeEvent ce = new ChangeEvent(this);
    for (int i = listeners.size() - 1; i >= 0; i--) {
      ((CellEditorListener)listeners.elementAt(i)).editingStopped(ce);
    }
  }
}
