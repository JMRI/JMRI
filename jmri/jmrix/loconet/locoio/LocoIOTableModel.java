// LocoIOTableModel.java

package jmri.jmrix.loconet.locoio;

import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

/** 
 * Configurer for LocoIO hardware.
 *<P>
 * The basic logic here is described in Chapter 7 of "Core Swing Advanced Programming".
 * 
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: LocoIOTableModel.java,v 1.1 2002-03-01 00:01:09 jacobsen Exp $
 */
public class LocoIOTableModel extends javax.swing.table.AbstractTableModel{

	private int _numRows = 16;
	
	// Defines the columns
	public static final int PINCOLUMN   	= 0;  // pin number
	public static final int ONMODECOLUMN   = 1;  // what makes this happen?
	public static final int ADDRCOLUMN 	= 2;  // what address is involved?
	public static final int CAPTURECOLUMN 	= 3;  // "capture" button
	public static final int READCOLUMN  	= 4;  // "read" button
	public static final int WRITECOLUMN 	= 5;  // "write" button
	public static final int HIGHESTCOLUMN 	= WRITECOLUMN+1;

	// not currently active, i.e. reserved for the future
	public static final int DOMODECOLUMN   = -1;  // what does it do?
	public static final int SETCOLUMN  	= -2;  // open/closed, on/off
	
	// store the modes
	Object[] addr = new Object[16];
	Object[] set  = new Object[16];
	Object[] onMode = {"<none>", "<none>", "<none>", "<none>", "<none>", "<none>", "<none>", "<none>", 
					 "<none>", "<none>", "<none>", "<none>", "<none>", "<none>", "<none>", "<none>"};
	Object[] doMode = {"<none>", "<none>", "<none>", "<none>", "<none>", "<none>", "<none>", "<none>", 
					 "<none>", "<none>", "<none>", "<none>", "<none>", "<none>", "<none>", "<none>"};
	
	public LocoIOTableModel() {  
		super();
	}
	
	// basic methods for AbstractTableModel implementation
	public int getRowCount() { return _numRows; }
	
	public int getColumnCount( ){ return HIGHESTCOLUMN;}

	public String getColumnName(int col) { 
		switch (col) {
			case PINCOLUMN: return "Pin";
			case ONMODECOLUMN: return "Action";
			case DOMODECOLUMN: return "Then do";
			case ADDRCOLUMN: return "Address";
			case SETCOLUMN: return "Set to";
			case CAPTURECOLUMN: return "";
			case READCOLUMN: return "";
			case WRITECOLUMN: return "";
			default: return "unknown";
		}
	}
	
	public Class getColumnClass(int col) { 
		switch (col) {
			case PINCOLUMN: return String.class;
			case ONMODECOLUMN: return String.class;	
			case DOMODECOLUMN: return String.class;
			case ADDRCOLUMN: return String.class;
			case SETCOLUMN: return String.class;
			case CAPTURECOLUMN: return JButton.class;
			case READCOLUMN: return JButton.class;
			case WRITECOLUMN: return JButton.class;	
			default: return null;
		}
	}

	public boolean isCellEditable(int row, int col) {
		switch (col) {
			case PINCOLUMN: return false;
			case DOMODECOLUMN: return true;
			case ONMODECOLUMN: return true;
			case SETCOLUMN: return true;
			case ADDRCOLUMN: return true;
			case CAPTURECOLUMN: return true;
			case READCOLUMN: return true;
			case WRITECOLUMN: return true;
			default: return false;
		}
	}
			
	public Object getValueAt(int row, int col) { 
		switch (col) {
			case PINCOLUMN: 
				return Integer.toString(row*2+1);  // 1 through 33 by 2
			case ONMODECOLUMN:
				return onMode[row];
			case DOMODECOLUMN:
				return doMode[row];
			case ADDRCOLUMN:
				return addr[row];
			case SETCOLUMN:
				return set[row];
			case CAPTURECOLUMN: 
				return "Capture";
			case READCOLUMN: 
				return "Read";
			case WRITECOLUMN:
				return "Write";
			default: return "unknown";
		}
	}	

	public int getPreferredWidth(int col) { 
		switch (col) {
			case PINCOLUMN: 
				return  new JLabel(" 31 ").getPreferredSize().width;
			case ONMODECOLUMN:
				return  new JLabel("Turnout closed status message").getPreferredSize().width;
			case DOMODECOLUMN:
				return  new JLabel("Send throw turnout command").getPreferredSize().width;
			case ADDRCOLUMN:
				return  new JLabel(" Address ").getPreferredSize().width;
			case SETCOLUMN:
				return  new JLabel(" <unknown> ").getPreferredSize().width;
			case CAPTURECOLUMN: 
			case READCOLUMN: 
			case WRITECOLUMN:
				return new JButton("Capture").getPreferredSize().width;
			default: return new JLabel(" <unknown> ").getPreferredSize().width;
		}
	}	

	public void setValueAt(Object value, int row, int col) { 
		if (col == ONMODECOLUMN) {
			if (isValidOnValue(value)) {
				onMode[row] = value;
				fireTableRowsUpdated(row,row);
			}
		} else if (col == DOMODECOLUMN) {
			if (isValidDoValue(value)) {
				doMode[row] = value;
				fireTableRowsUpdated(row,row);
			}
		} else if (col == ADDRCOLUMN) {
			addr[row] = value;
		} else if (col == SETCOLUMN) {
			set[row] = value;
		} else if (col == CAPTURECOLUMN) {
			System.out.println("capture from "+row+","+col);
		} else if (col == READCOLUMN) {
			System.out.println("read from "+row+","+col);
		} else if (col == WRITECOLUMN) {
			System.out.println("write from "+row+","+col);
		}
	}	
	
	protected boolean isValidOnValue(Object value) {
		if (value instanceof String) {
			String sValue = (String) value;
			for (int i=0; i<validOnModes.length; i++) {
				if (sValue.equals(validOnModes[i])) return true;
			}
		}
		return false;
	}
	
	public static String[] getValidOnModes() { return validOnModes; }

	static String[] validOnModes = {"Toggle switch", 
							"Pushbutton active low", "Pushbutton active high", 
							"Turnout close command", "Turnout thrown command", 
							"Status message"};

	protected boolean isValidDoValue(Object value) {
		if (value instanceof String) {
			String sValue = (String) value;
			for (int i=0; i<validDoModes.length; i++) {
				if (sValue.equals(validDoModes[i])) return true;
			}
		}
		return false;
	}
	
	public static String[] getValidDoModes() { return validDoModes; }
	
	static String[] validDoModes = {"Output lead",
									"Send close turnout command",
									"Send throw turnout command" };
	
	public void dispose() {
		if (log.isDebugEnabled()) log.debug("dispose");
			
		// null references, so that they can be gc'd even if this isn't.
		addr = null;
		set = null;
		onMode = null;				
		doMode = null;				
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoIOTableModel.class.getName());
}
