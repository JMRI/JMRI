/** 
 * VariableTableModel.java
 *
 * Description:		Table data model for display of variables in symbolic programmer
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrit.symbolicprog;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import com.sun.java.util.collections.List;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

public class VariableTableModel extends AbstractTableModel implements ActionListener, PropertyChangeListener {

	private String headers[] = null;

	private Vector rowVector = new Vector();  // vector of Variable items
	private CvTableModel _cvModel = null;          // reference to external table model
	private Vector _writeButtons = new Vector();
	private Vector _readButtons = new Vector();
	private JLabel _status = null;
	
	/** Defines the columns; values understood are: 	
	 *  "Name", "Value", "Range", "Read", "Write", "Comment", "CV", "Mask", "State"
	 */
	public VariableTableModel(JLabel status, String h[], CvTableModel cvModel) { 
		super();
		_status = status;
		_cvModel = cvModel; 
		headers = h;
		}
	
	// basic methods for AbstractTableModel implementation
	public int getRowCount() { 
		return rowVector.size();
	}
	
	public int getColumnCount( ){ return headers.length;}

	public String getColumnName(int col) { 
		if (log.isDebugEnabled()) log.debug("getColumnName "+col);
		return headers[col];
	}
	
	public Class getColumnClass(int col) { 
		// if (log.isDebugEnabled()) log.debug("getColumnClass "+col);
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
		if (log.isDebugEnabled()) log.debug("isCellEditable "+col);
		if (headers[col].equals("Value"))
			return true;
		else if (headers[col].equals("Read"))
			return true;
		else if (headers[col].equals("Write") 
				&& !((VariableValue)(rowVector.elementAt(row))).getReadOnly())
			return true;
		else
			return false;
	}
			
	public VariableValue getVariable(int row) {
		return ((VariableValue)rowVector.elementAt(row));
	}
			
	public String getName(int row) {
		return ((VariableValue)rowVector.elementAt(row)).name();
	}
	
	public String getStdName(int row) {
		return ((VariableValue)rowVector.elementAt(row)).stdName();
	}
	
	public String getValString(int row) {
		return ((VariableValue)rowVector.elementAt(row)).getValueString();
	}

	public void setIntValue(int row, int val) {
		((VariableValue)rowVector.elementAt(row)).setIntValue(val);
	}
	public void setState(int row, int val) {
		if (log.isDebugEnabled()) log.debug("setState row: "+row+" val: "+val);
		((VariableValue)rowVector.elementAt(row)).setState(val);
	}
	public int getState(int row) {
		return ((VariableValue)rowVector.elementAt(row)).getState();
	}
	
	/*
	 * Request a "unique representation", e.g. something we can show
	 * for the row-th variable.
	 */
	public Object getRep(int row, String format) {
		VariableValue v = (VariableValue)rowVector.elementAt(row);
		return v.getRep(format);
	}
	
	public Object getValueAt(int row, int col) { 
		// if (log.isDebugEnabled()) log.debug("getValueAt "+row+" "+col);
		VariableValue v = (VariableValue)rowVector.elementAt(row);
		if (headers[col].equals("Value"))
			return v.getValue();
		else if (headers[col].equals("Read")) {
			return _readButtons.elementAt(row);
		} else if (headers[col].equals("Write")) {
			return _writeButtons.elementAt(row);
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
				case CvValue.FROMFILE:  return "From file";
				default: return "inconsistent";
			}
		}
		else if (headers[col].equals("Range")) 
			return v.rangeVal();
		else
			return "Later, dude";
	}
		
	public void setValueAt(Object value, int row, int col) { 
		if (log.isDebugEnabled()) log.debug("setvalueAt "+row+" "+col+" "+value);
		setFileDirty(true);
	}
	
	// for loading config:	
	// Read from an Element to configure the row
	public void setRow(int row, Element e, Namespace ns) {
		// get the values for the VariableValue ctor
		if (log.isDebugEnabled()) log.debug("Starting to setRow");
		String name = e.getAttribute("name").getValue();
		String stdname = ( e.getAttribute("stdName")!=null ?
						 e.getAttribute("stdName").getValue() :
						 null);
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
		if (e.getAttribute("readOnly") != null) {
			readOnly = e.getAttribute("readOnly").getValue().equals("yes") ? true : false;
			if (log.isDebugEnabled()) log.debug("found readOnly "+e.getAttribute("readOnly").getValue());
			if (readOnly) { // readOnly, config write, read buttons
				JButton bw = new JButton();
				_writeButtons.addElement(bw);
			} else { // not readOnly, config write, read buttons
				JButton bw = new JButton("Write");
				bw.setActionCommand("W"+row);
				bw.addActionListener(this);
				_writeButtons.addElement(bw);
			}
		} else {
			log.warn("Element missing readOnly attribute: "+name);
		}
		
		// config read button
		JButton br = new JButton("Read");
		br.setActionCommand("R"+row);
		br.addActionListener(this);
		_readButtons.addElement(br);

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
								CV, mask, minVal, maxVal, _cvModel.allCvVector(), _status, stdname);
								
		} else if ( (child = e.getChild("hexVal", ns)) != null) {
			Attribute a;
			if ( (a = child.getAttribute("min")) != null)
				minVal = Integer.valueOf(a.getValue(),16).intValue();
			if ( (a = child.getAttribute("max")) != null)
				maxVal = Integer.valueOf(a.getValue(),16).intValue();
			v = new HexVariableValue(name, comment, readOnly, 
								CV, mask, minVal, maxVal, _cvModel.allCvVector(), _status, stdname);
								
		} else if ( (child = e.getChild("enumVal", ns)) != null) {
			List l = child.getChildren("enumChoice", ns);
			EnumVariableValue v1 = new EnumVariableValue(name, comment, readOnly, 
								CV, mask, 0, l.size()-1, _cvModel.allCvVector(), _status, stdname);
			v = v1;
			for (int k=0; k< l.size(); k++)
				v1.addItem(((Element)l.get(k)).getAttribute("choice").getValue());

		} else if ( (child = e.getChild("speedTableVal", ns)) != null) {
			log.warn("Speed tables are still experimental!");
			// ensure all 28 CVs exist
			for (int i=0; i<28; i++) { _cvModel.addCV(""+(CV+i)); }
	
			v = new SpeedTableVarValue(name, comment, readOnly, 
								CV, mask, minVal, maxVal, _cvModel.allCvVector(), _status, stdname);

		} else if ( (child = e.getChild("longAddressVal", ns)) != null) {
			_cvModel.addCV(""+(CV+1));  // ensure 2nd CV exists
			v = new LongAddrVariableValue(name, comment, readOnly, 
								CV, mask, minVal, maxVal, _cvModel.allCvVector(), _status, stdname);
		} else {
			log.error("Did not find a valid variable type");
			return;
		}

		// back to general processing
		// record new variable, hook up listeners
		rowVector.addElement(v);
		v.addPropertyChangeListener(this);
	}
	
	public void newDecVariableValue(String name, int CV, String mask) {
		setFileDirty(true);
		String comment = "";
		boolean readOnly = false;
		int minVal = 0;
		int maxVal = 255;
		_cvModel.addCV(""+CV);

		int row = getRowCount();
		
		// config write button
		JButton bw = new JButton("Write");
		bw.setActionCommand("W"+row);
		bw.addActionListener(this);
		_writeButtons.addElement(bw);

		// config read button
		JButton br = new JButton("Read");
		br.setActionCommand("R"+row);
		br.addActionListener(this);
		_readButtons.addElement(br);

		VariableValue v = new DecVariableValue(name, comment, readOnly, 
								CV, mask, minVal, maxVal, _cvModel.allCvVector(), _status, null);
		rowVector.addElement(v);
		v.addPropertyChangeListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (log.isDebugEnabled()) log.debug("action performed,  command: "+e.getActionCommand());
		setFileDirty(true);
		char b = e.getActionCommand().charAt(0);
		int row = Integer.valueOf(e.getActionCommand().substring(1)).intValue();
		if (log.isDebugEnabled()) log.debug("event on "+b+" row "+row);
			if (b=='R') {
			// read command
			read(row);
		} else {
			// write command
			write(row);
		}
	}
	
	public void read(int i) {
		VariableValue v = (VariableValue)rowVector.elementAt(i);
		v.read();
	}

	public void write(int i) {
		VariableValue v = (VariableValue)rowVector.elementAt(i);
		v.write();
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		if (log.isDebugEnabled()) log.debug("prop changed "+e.getPropertyName()
													+" new value: "+e.getNewValue());
		setFileDirty(true);
		fireTableDataChanged();	
	}

	public void configDone() {
		fireTableDataChanged();	
	}

	// fileDirty represents any chance to values, etc, hence rewriting the
	// file is desirable
	public boolean fileDirty() {
		return _fileDirty;
	}
	public void setFileDirty(boolean b) {
		_fileDirty = b;
	}
	private boolean _fileDirty;
	
	public boolean decoderDirty() {
		int len = rowVector.size();
		for (int i=0; i< len; i++) {
			if (((VariableValue)(rowVector.elementAt(i))).getState() == CvValue.EDITTED ) return true;
		}
		return false;
	}

	public VariableValue findVar(String name) {
		for (int i=0; i<getRowCount(); i++) 
			if (name.equals(getStdName(i))) return getVariable(i);
		for (int i=0; i<getRowCount(); i++) 
			if (name.equals(getName(i))) return  getVariable(i);
		return null;
	}

	public int findVarIndex(String name) {
		for (int i=0; i<getRowCount(); i++) 
			if (name.equals(getStdName(i))) return i;
		for (int i=0; i<getRowCount(); i++) 
			if (name.equals(getName(i))) return i;
		return -1;
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VariableTableModel.class.getName());

}
