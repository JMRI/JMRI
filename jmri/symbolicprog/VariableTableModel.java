/** 
 * VariableTableModel.java
 *
 * Description:		Table data model for display of variables in symbolic programmer
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.symbolicprog;

import javax.swing.JButton;
import java.util.Vector;
import com.sun.java.util.collections.List;
import org.jdom.Element;
import org.jdom.Namespace;


public class VariableTableModel extends javax.swing.table.AbstractTableModel {

	String headers[] = null;

	// values understood are: 	
	//  "Name", "Value", "Read", "Write", "Comment", "CV", "Mask"
	
	int numRows = 0;                // must be zero until Vectors are initialized
	Vector rowVector = new Vector();  // vector of row vectors
	
	public VariableTableModel(String h[]) {  
		headers = h;
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

		for (int j=0; j<getColumnCount(); j++) {
			// j is column; headers[j] is desired variable
			// would prefer switch, but have to switch on int
			String var = headers[j];
			if (var == "Name") {
				row.setElementAt(e.getAttribute("name").getValue(), j);
			} else if (var == "Value") {
				// handle value (temporary, until know how to add objects)
				if (e.getChild("binaryVal", ns) != null )  {
					row.setElementAt("binary: "+e.getChild("binaryVal", ns).getAttribute("phrasing").getValue(), j);
				} else if (e.getChild("decVal", ns) != null )  {
					row.setElementAt("decimal: "+e.getChild("decVal", ns).getAttribute("min").getValue()
									 +"-"+e.getChild("decVal", ns).getAttribute("max").getValue(), j);
				} else if (e.getChild("hexVal", ns) != null )  {
					row.setElementAt("hex", j);
				} else if (e.getChild("enumVal", ns) != null )  {
					String temp = "enum: ";
					List l = e.getChild("enumVal", ns).getChildren("enumEntry", ns);
					for (int k=0; k< l.size(); k++)
						temp += ((Element)l.get(k)).getAttribute("name");
					row.setElementAt(temp, j);
				} else if (e.getChild("speedTableVal", ns) != null )  {
					row.setElementAt("speed table", j);
				} else if (e.getChild("longAddressVal", ns) != null )  {
					row.setElementAt("long address", j);
				} else if (e.getChild("readOnlyVal", ns) != null )  {
					row.setElementAt("read only value", j);
				} else
				row.setElementAt("unrecognized data type!!!", j);
			} else if (var == "Read") {
			} else if (var == "Write") {
			} else if (var == "Comment") {
				if (e.getAttribute("comment") != null)
					row.setElementAt(e.getAttribute("comment").getValue(), j); 
			} else if (var == "CV") {
				row.setElementAt(e.getAttribute("CV").getValue(), j);
			} else if (var == "Mask") {
				row.setElementAt(e.getAttribute("mask").getValue(), j);
			} else { // no match is an error
				row.setElementAt("Unrecognized column name", j);
			}
			
		}
	}
	
	public void configDone() {
		fireTableDataChanged();	
	}
	
	
}
