/** 
 * CvTableModel.java
 *
 * Description:		Table data model for display of CvValues in symbolic programmer
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrit.symbolicprog;

import java.util.Vector;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import com.sun.java.util.collections.List;
import org.jdom.Element;

public class CvTableModel extends javax.swing.table.AbstractTableModel implements ActionListener, PropertyChangeListener {

	private int _numRows = 0;                // must be zero until Vectors are initialized
	private Vector _cvDisplayVector = new Vector();  // vector of CvValue objects, in display order
	private Vector _cvAllVector = new Vector(512);  // vector of all 512 possible CV objects
	public Vector allCvVector() { return _cvAllVector; }
	private Vector _writeButtons = new Vector();
	private Vector _readButtons = new Vector();
	
	// Defines the columns
	private static final int NUMCOLUMN   = 0;
	private static final int VALCOLUMN   = 1;
	private static final int STATECOLUMN = 2;
	private static final int READCOLUMN  = 3;
	private static final int WRITECOLUMN = 4;
	private static final int HIGHESTCOLUMN = WRITECOLUMN+1;
	
	private JLabel _status = null;
	
	public CvTableModel(JLabel status) {  
		super();
		
		// save a place for notification
		_status = status;
		// initialize the 512-length _cvAllVector;
		for (int i=0; i<512; i++) _cvAllVector.addElement(null);
		
		// define just address CV at start, pending some variables
		addCV("1");
		}
	
	// basic methods for AbstractTableModel implementation
	public int getRowCount() { return _numRows; }
	
	public int getColumnCount( ){ return HIGHESTCOLUMN;}

	public String getColumnName(int col) { 
		switch (col) {
			case NUMCOLUMN: return "Number";
			case VALCOLUMN: return "Value";
			case STATECOLUMN: return "State";
			case READCOLUMN: return "Read";
			case WRITECOLUMN: return "Write";
			default: return "unknown";
		}
	}
	
	public Class getColumnClass(int col) { 
		switch (col) {
			case NUMCOLUMN: return String.class;
			case VALCOLUMN: return JTextField.class;
			case STATECOLUMN: return String.class;
			case READCOLUMN: return JButton.class;
			case WRITECOLUMN: return JButton.class;
			default: return null;
		}
	}

	public boolean isCellEditable(int row, int col) {
		switch (col) {
			case NUMCOLUMN: return false;
			case VALCOLUMN: return true;
			case STATECOLUMN: return false;
			case READCOLUMN: return true;
			case WRITECOLUMN: return true;
			default: return false;
		}
	}
	
	public String getName(int row) {  // name is text number
		return ""+((CvValue)_cvDisplayVector.elementAt(row)).number();
	}
	
	public String getValString(int row) {
		return ""+((CvValue)_cvDisplayVector.elementAt(row)).getValue();
	}
	
	public CvValue getCvByRow(int row) { return ((CvValue)_cvDisplayVector.elementAt(row)); }
	public CvValue getCvByNumber(int row) { return ((CvValue)_cvAllVector.elementAt(row)); }
	
	public Object getValueAt(int row, int col) { 
		switch (col) {
			case NUMCOLUMN: 
				return ""+((CvValue)_cvDisplayVector.elementAt(row)).number();
			case VALCOLUMN:
				return ((CvValue)_cvDisplayVector.elementAt(row)).getTableEntry();
			case STATECOLUMN:
				int state = ((CvValue)_cvDisplayVector.elementAt(row)).getState();
				switch (state) {
					case CvValue.UNKNOWN:  		return "Unknown";
					case CvValue.READ:  		return "Read";
					case CvValue.EDITTED:  		return "Editted";
					case CvValue.STORED:  		return "Stored";
					case CvValue.FROMFILE:  	return "From file";
					default: return "inconsistent";
				}
			case READCOLUMN: 
				return _readButtons.elementAt(row);
			case WRITECOLUMN:
				return _writeButtons.elementAt(row);
			default: return "unknown";
		}
	}	

	public void setValueAt(Object value, int row, int col) { 
		switch (col) {
			case VALCOLUMN: // Object is actually an Integer
				((CvValue)_cvDisplayVector.elementAt(row)).setValue(((Integer)value).intValue() );
				break;
			default:
				break;
		}
	}	
	
	public void actionPerformed(ActionEvent e) {
		if (log.isDebugEnabled()) log.debug("action command: "+e.getActionCommand());
		char b = e.getActionCommand().charAt(0);
		int row = Integer.valueOf(e.getActionCommand().substring(1)).intValue();
		if (log.isDebugEnabled()) log.debug("event on "+b+" row "+row);
		if (b=='R') {
			// read command
			((CvValue)_cvDisplayVector.elementAt(row)).read(_status);
		} else {
			// write command
			((CvValue)_cvDisplayVector.elementAt(row)).write(_status);
		}
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		fireTableDataChanged();	
	}
	
	public void addCV(String s) {
		int num = Integer.valueOf(s).intValue();
		if (_cvAllVector.elementAt(num) == null) {
			JButton bw = new JButton("Write");
			bw.setActionCommand("W"+_numRows);
			bw.addActionListener(this);
			_writeButtons.addElement(bw);
			JButton br = new JButton("Read");
			br.setActionCommand("R"+_numRows);
			br.addActionListener(this);
			_readButtons.addElement(br);
			_numRows++;
			CvValue cv = new CvValue(num);
			_cvAllVector.setElementAt(cv, num);
			_cvDisplayVector.addElement(cv);
			// connect to this CV to ensure the table display updates
			cv.addPropertyChangeListener(this);
			fireTableDataChanged();
		}
	}
	
	public boolean decoderDirty() {
		int len = _cvDisplayVector.size();
		for (int i=0; i< len; i++) {
			if (((CvValue)(_cvDisplayVector.elementAt(i))).getState() == CvValue.EDITTED ) {
				if (log.isDebugEnabled()) 
					log.debug("CV decoder dirty due to "+((CvValue)(_cvDisplayVector.elementAt(i))).number());
				return true;
			}
		}
		return false;
	}

	public void dispose() {
		if (log.isDebugEnabled()) log.debug("dispose");
	
		// remove buttons
		for (int i = 0; i<_writeButtons.size(); i++) {
			((JButton)_writeButtons.elementAt(i)).removeActionListener(this);
		}
		for (int i = 0; i<_readButtons.size(); i++) {
			((JButton)_readButtons.elementAt(i)).removeActionListener(this);
		}

		// remove CV listeners
		for (int i = 0; i<_cvDisplayVector.size(); i++) {
			((CvValue)_cvDisplayVector.elementAt(i)).removePropertyChangeListener(this);
		}
		
		// null references, so that they can be gc'd even if this isn't.				
		_cvDisplayVector.removeAllElements();
		_cvDisplayVector = null;

		_cvAllVector.removeAllElements();
		_cvAllVector = null;
		
		_writeButtons.removeAllElements();
		_writeButtons = null;
		
		_readButtons.removeAllElements();
		_readButtons = null;

		_status = null;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CvTableModel.class.getName());
}
