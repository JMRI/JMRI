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

	
	int numRows = 0;                // must be zero until Vectors are initialized
	Vector rowVector = new Vector();  // vector of row vectors
	
	/** Defines the columns; values understood are: 	
	 *  "Name", "Value", "Range", "Read", "Write", "Comment", "CV", "Mask"
	 */
	public VariableTableModel(String h[]) {  
		headers = h;
		}
	
	// basic methods for AbstractTableModel implementation
	public int getRowCount() { 
		return numRows;
	}
	
	public int getColumnCount( ){ return headers.length;}

	public String getColumnName(int col) { return headers[col];}
	
	public Class getColumnClass(int col) { 
		if (headers[col] == "Value")
			return JComboBox.class;
		else
			return String.class;
	}

	public boolean isCellEditable(int row, int col) {
		if (headers[col] == "Value")
			return true;
		else
			return false;
	}
			
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
				// handle value entry
				if (e.getChild("binaryVal", ns) != null )  {
					String phrasing = e.getChild("binaryVal", ns).getAttribute("phrasing").getValue();
					if (phrasing.equals("OffOn"))
						row.setElementAt(new JCheckBox("On"), j);
					else if (phrasing.equals("OnOff"))
						row.setElementAt(new JCheckBox("Off"), j);
					else if (phrasing.equals("NoYes"))
						row.setElementAt(new JCheckBox("Yes"), j);
					else if (phrasing.equals("YesNo"))
						row.setElementAt(new JCheckBox("No"), j);
					else row.setElementAt(new JCheckBox("Unknown phrasing "+phrasing), j);
				} else if (e.getChild("decVal", ns) != null )  {
					row.setElementAt(new JTextField(""), j);
				} else if (e.getChild("hexVal", ns) != null )  {
					row.setElementAt(new JTextField(""), j);
				} else if (e.getChild("enumVal", ns) != null )  {
					List l = e.getChild("enumVal", ns).getChildren("enumChoice", ns);
					JComboBox c = new JComboBox();
					System.out.println("length "+l.size());
					for (int k=0; k< l.size(); k++)
						c.addItem(((Element)l.get(k)).getAttribute("choice").getValue());
					row.setElementAt(c, j);
				} else if (e.getChild("speedTableVal", ns) != null )  {
					row.setElementAt(new JLabel("speed table"), j);
				} else if (e.getChild("longAddressVal", ns) != null )  {
					row.setElementAt(new JLabel("long address"), j);
				} else if (e.getChild("readOnlyVal", ns) != null )  {
					row.setElementAt(new JLabel("read only value"), j);
				} else
				row.setElementAt("unrecognized data type!!!", j);
			} else if (var == "Range") {
				// list range for value
				if (e.getChild("binaryVal", ns) != null )  {
					row.setElementAt("binary: "+e.getChild("binaryVal", ns).getAttribute("phrasing").getValue(), j);
				} else if (e.getChild("decVal", ns) != null )  {
					row.setElementAt("decimal: "+e.getChild("decVal", ns).getAttribute("min").getValue()
									 +"-"+e.getChild("decVal", ns).getAttribute("max").getValue(), j);
				} else if (e.getChild("hexVal", ns) != null )  {
					row.setElementAt("hex:", j);
				} else if (e.getChild("enumVal", ns) != null )  {
					row.setElementAt("pick one", j);
				} else if (e.getChild("speedTableVal", ns) != null )  {
					row.setElementAt("Speed table", j);
				} else if (e.getChild("longAddressVal", ns) != null )  {
					row.setElementAt("Long address", j);
				} else if (e.getChild("readOnlyVal", ns) != null )  {
					row.setElementAt("Read only value", j);
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
