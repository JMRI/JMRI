/** 
 * VariableTableModel.java
 *
 * Description:		Table data model for display of variables in symbolic programmer
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package symbolicprog;

import ErrLoggerJ.ErrLog;

import javax.swing.JButton;
import java.util.Vector;
import org.jdom.Element;
import org.jdom.Namespace;


public class VariableTableModel extends javax.swing.table.AbstractTableModel {

	String headers[] = new String[]  {
		"Name", "Value", "Read", "Write", "Comment", "CV", "mask" };
	
	int numRows = 0;                // must be zero until Vectors are initialized
	Vector rowVector = new Vector();  // vector of row vectors
	
	VariableTableModel() {  
		}
	
	// basic methods for AbstractTableModel implementation
	public int getRowCount() { 
		return numRows;
	}
	
	public int getColumnCount( ){ return headers.length;}

	public String getColumnName(int col) { return headers[col];}
		
	public Object getValueAt(int row, int col) { 
		return ((Vector)(rowVector.elementAt(row))).elementAt(col);	
	}
		
	// for loading config:
	public void setNumRows(int n) { 
		rowVector = new Vector(n); 
		for (int i=0; i<n; i++) rowVector.addElement(new Vector());
		numRows = n;
	}
	
	// Read from an Element to configure the row
	public void setRow(int i, Element e, Namespace ns) {
		Vector row = (Vector)(rowVector.elementAt(i));
		// initialize the row to be sure its got right number of elements
		row.removeAllElements();
		for (int j=0; j<getColumnCount(); j++) row.addElement(null);

		// start setting configured values in positions defined by headers
		row.setElementAt(e.getAttribute("name").getValue(), 0);
		if (e.getAttribute("comment") != null)
			row.setElementAt(e.getAttribute("comment").getValue(), 4); 
		row.setElementAt(e.getAttribute("CV").getValue(), 5);
		row.setElementAt(e.getAttribute("mask").getValue(), 6);
		
		// add controls
		
		// handle value (temporary, until know how to add objects)
		if (e.getChild("binaryVal", ns) != null )  {
			row.setElementAt("binary", 1);
		} else if (e.getChild("decVal", ns) != null )  {
			row.setElementAt("decimal", 1);
		} else if (e.getChild("hexVal", ns) != null )  {
			row.setElementAt("hex", 1);
		} else if (e.getChild("enumVal", ns) != null )  {
			row.setElementAt("enum", 1);
		}

	}
	
	public void configDone() {
		fireTableDataChanged();	
	}
	
	
}
