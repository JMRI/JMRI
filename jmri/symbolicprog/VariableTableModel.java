/** 
 * VariableTableModel.java
 *
 * Description:		Table data model for display of variables in symbolic programmer
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.symbolicprog;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import javax.swing.JTextField;
import javax.swing.JButton;
import com.sun.java.util.collections.List;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;


public class VariableTableModel extends AbstractTableModel implements ActionListener, PropertyChangeListener {

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
			br.addActionListener(this);
			return br;
		} else if (headers[col].equals("Write")) {
			JButton bw = new JButton("Write");
			bw.setActionCommand("W"+row);
			bw.addActionListener(this);
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
		}
		else if (headers[col].equals("Range")) 
			return v.rangeVal();
		else
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
		
		int minVal = 0;
		int maxVal = 255;
		
		boolean readOnly = false;
		
		if (_cvModel == null) {
			log.error("CvModel reference is null; cannot add variables");
			return;
		}
		_cvModel.addCV(""+CV);
		
		// have to handle various value types, see "snippet"
		Element child;
		VariableValue v = null;
		if ( (child = e.getChild("decVal", ns)) != null) {
			Attribute a;
			if ( (a = child.getAttribute("min")) != null)
				minVal = Integer.valueOf(a.getValue()).intValue();
			if ( (a = child.getAttribute("max")) != null)
				maxVal = Integer.valueOf(a.getValue()).intValue();
			v = new DecVariableValue(name, comment, readOnly, 
								CV, mask, minVal, maxVal, _cvModel.allCvVector());
								
		} else if ( (child = e.getChild("hexVal", ns)) != null) {
			Attribute a;
			if ( (a = child.getAttribute("min")) != null)
				minVal = Integer.valueOf(a.getValue(),16).intValue();
			if ( (a = child.getAttribute("max")) != null)
				maxVal = Integer.valueOf(a.getValue(),16).intValue();
			v = new HexVariableValue(name, comment, readOnly, 
								CV, mask, minVal, maxVal, _cvModel.allCvVector());
								
		} else if ( (child = e.getChild("enumVal", ns)) != null) {
			List l = child.getChildren("enumChoice", ns);
			EnumVariableValue v1 = new EnumVariableValue(name, comment, readOnly, 
								CV, mask, 0, l.size()-1, _cvModel.allCvVector());
			v = v1;
			for (int k=0; k< l.size(); k++)
				v1.addItem(((Element)l.get(k)).getAttribute("choice").getValue());

		} else if ( (child = e.getChild("speedTableVal", ns)) != null) {
			log.warn("Not yet able to handle speedTableVal");
			return;
		} else if ( (child = e.getChild("longAddressVal", ns)) != null) {
			_cvModel.addCV(""+(CV+1));  // ensure 2nd CV exists
			v = new LongAddrVariableValue(name, comment, readOnly, 
								CV, mask, minVal, maxVal, _cvModel.allCvVector());
		} else {
			log.error("Did not find a valid variable type");
			return;
		}

		// back to general processing
		rowVector.addElement(v);
		v.addPropertyChangeListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		System.out.println("action command: "+e.getActionCommand());
		char b = e.getActionCommand().charAt(0);
		int row = Integer.valueOf(e.getActionCommand().substring(1)).intValue();
		System.out.println("event "+b+" row "+row);
		VariableValue v = (VariableValue)rowVector.elementAt(row);
		if (b=='R') {
			// read command
			v.read();
		} else {
			// write command
			v.write();
		}
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		fireTableDataChanged();	
	}

	public void configDone() {
		fireTableDataChanged();	
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VariableTableModel.class.getName());

}
