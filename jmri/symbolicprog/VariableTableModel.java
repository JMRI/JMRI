/** 
 * VariableTableModel.java
 *
 * Description:		Table data model for display of variables in symbolic programmer
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.symbolicprog;

import javax.swing.*;
import java.util.Vector;
import com.sun.java.util.collections.List;
import org.jdom.Element;
import org.jdom.Namespace;


public class VariableTableModel extends javax.swing.table.AbstractTableModel {

	String headers[] = null;

	Vector rowVector = new Vector();  // vector of Variable items
	CvTableModel _cvModel = null;          // reference to external table model
	
	/** Defines the columns; values understood are: 	
	 *  "Name", "Value", "Range", "Read", "Write", "Comment", "CV", "Mask", "State"
	 */
	public VariableTableModel(String h[], CvTableModel cvModel) { 
		_cvModel = cvModel; 
		headers = h;
		}
	
	// basic methods for AbstractTableModel implementation
	public int getRowCount() { 
		return rowVector.size();
	}
	
	public int getColumnCount( ){ return headers.length;}

	public String getColumnName(int col) { return headers[col];}
	
	public Class getColumnClass(int col) { 
		if (headers[col].equals("Value"))
			return JTextField.class;
		else if (headers[col].equals("Read"))
			return JButton.class;
		else if (headers[col].equals("Write"))
			return JButton.class;
		else
			return String.class;
	}

	public boolean isCellEditable(int row, int col) {
		if (headers[col].equals("Value"))
			return true;
		else if (headers[col].equals("Read"))
			return true;
		else if (headers[col].equals("Write"))
			return true;
		else
			return false;
	}
			
	public Object getValueAt(int row, int col) { 
		VariableValue v = (VariableValue)rowVector.elementAt(row);
		if (headers[col].equals("Value"))
			return v.getValue();
		else if (headers[col].equals("Read")) {
			JButton br = new JButton("Read");
			br.setActionCommand("R"+row);
			// br.addActionListener(this);
			return br;
		} else if (headers[col].equals("Write")) {
			JButton bw = new JButton("Write");
			bw.setActionCommand("W"+row);
			// bw.addActionListener(this);
			return bw;
		} else if (headers[col].equals("CV"))
			return ""+v.getCvNum();
		else if (headers[col].equals("Name"))
			return ""+v.name();
		else if (headers[col].equals("Comment"))
			return v.getComment();
		else if (headers[col].equals("Mask"))
			return v.getMask();
		else if (headers[col].equals("State")) {
			int state = v.getState();
			switch (state) {
				case CvValue.UNKNOWN:  	return "Unknown";
				case CvValue.READ:  	return "Read";
				case CvValue.EDITTED:  	return "Editted";
				case CvValue.STORED:  	return "Stored";
				default: return "inconsistent";
			}
		} else
			return "Later, dude";
	}
		
	public void setValueAt(Object value, int row, int col) { 
	}
	
	// for loading config:	
	// Read from an Element to configure the row
	public void setRow(int i, Element e, Namespace ns) {
		// get the values for the VariableValue ctor

		String name = e.getAttribute("name").getValue();
		String comment = null;
		if (e.getAttribute("comment") != null)
			comment = e.getAttribute("comment").getValue();
		int CV = Integer.valueOf(e.getAttribute("CV").getValue()).intValue();
		String mask = null;
		if (e.getAttribute("mask") != null) 
			mask = e.getAttribute("mask").getValue();
		else {
			log.warn("Element missing mask attribute: "+name);
			mask ="VVVVVVVV";
		}

		boolean readOnly = false;
		
		// have to handle various value types, see "snippet"

		if (_cvModel == null) log.error("CvModel reference is null; cannot add variables");
		_cvModel.addCV(""+CV);

		// assume decimal type for now, and continue
		VariableValue v = new DecVariableValue(name, comment, readOnly, 
								CV, mask, _cvModel.allCvVector());
		rowVector.addElement(v);
	}
	
	public void configDone() {
		fireTableDataChanged();	
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VariableTableModel.class.getName());

}
